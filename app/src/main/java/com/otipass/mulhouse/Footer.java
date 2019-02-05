/**
================================================================================

    PASS MUSEUM project

    Package com.otipass.passmuseum

    @copyright Otipass 2013. All rights reserved.
    @author ED ($Author: ede $)

    @version $Rev: 6414 $
    $Id: Footer.java 6414 2016-06-23 14:36:10Z ede $

================================================================================
 */
package com.otipass.mulhouse;

import java.text.SimpleDateFormat;
import java.util.Date;

import models.Constants;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.otipass.mulhouse.R;
import com.otipass.sql.DbAdapter;
import com.otipass.synchronization.SynchroAlarm;
import com.otipass.synchronization.SynchronizationService;
import com.otipass.tools.ExceptionHandler;
import com.otipass.tools.tools;


@SuppressLint("NewApi")
public class Footer extends Fragment {
	public static final String ARG_ACTION = "footer_action";

	private static Footer mInstance = null;

	// dialog types uses by handler
	public static final int cTypeDialog = 1;
	public static final int cTypeLabel = 2;
	public static final int cTypeWait = 3;

	private ImageView imgConnect;
	private TextView connectionStatus;
	public static int displayType;
	private ProgressDialog progressDialog;
	private static String communicationStatusText;
	private boolean syncInProgress;
	private int synchronizationType;
	private int initSequenceStatus;
	private int communicationSequenceStatus;
	private int progressValue;
	private int progressStep;
	private int nbWLCards = 0;
	private int nbCards = 0;
	private int nbWLSteps = 0;
	private int lastProgress;
	private int WLProgress;
	private boolean synchroInProgress = false;
	private boolean mustRetry = false;
	private boolean toogle;
	private Bundle bundle;
	private String footer_text;
	private int img_footer;
	private SynchronizationService synchro;
	private AlertDialog waitDialog;

	// use this function to call an instance 
	public static Footer getInstance() {          
		if (mInstance == null) {      
			mInstance = new Footer();    
		}
		return mInstance;  
	}

	public Footer() {
		// Empty constructor required for fragment subclasses
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Handle uncaught exception : there will be redirected to BugMsgActivity
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(getActivity()));

		View rootView = inflater.inflate(R.layout.footer, container, false);
		imgConnect = (ImageView) rootView.findViewById(R.id.img_connection_status);
		connectionStatus = (TextView) rootView.findViewById(R.id.connection_status);

		if (isOnline()) {
			connectionStatus.setText(getString(R.string.Aucun_appel_serveur));
		}

		return rootView;
	}

	@Override
	public void onResume()
	{
		super.onResume();
	}

	public int getInitSequenceStatus() {
		return initSequenceStatus;
	}

	public int getCommunicationSequenceStatus() {
		return communicationSequenceStatus;
	}

	public boolean getSynchroInProgress() {
		if (synchroInProgress) {
			// a synchronization is running, a new one is requested, but not possible
			// it will be launched automatically when this one is finished
			mustRetry = true;
		}
		return synchroInProgress;
	}

	public String getSynchroProgress() {
		String s = "";
		if (nbWLSteps > 0) {
			s = String.valueOf((int)(nbWLCards * 100 / nbWLSteps)) + "%";
		}
		return s;
	}

	public void startInitSequence(int providerId, int deviceType, int communicationType) {
		displayType = cTypeDialog;
		synchronizationType = SynchronizationService.cInitSynchronization;
		initSequenceStatus = SynchronizationService.cComPending;
		progressStep = 1; // 1% per operation for the progress Bar
		WLProgress = 90; // 90% for the white list import 
		progressDialog = new ProgressDialog(getActivity());
		progressDialog.setMessage(getString(R.string.Communication_en_cours));
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setProgress(0);
		progressDialog.setCancelable(false);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.show();
		try {
			synchro = new SynchronizationService(getActivity());
			synchro.setProviderId(providerId);
			synchro.setDeviceType(deviceType);
			synchro.setCommunicationType(communicationType);
			synchro.setWLType(SynchronizationService.cGetTotalWL);
			synchro.start(Constants.DO_INIT, communicationHandler);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public int synchronise(int type){
		displayType = cTypeDialog;
		synchroInProgress = false;
		int status = SynchronizationService.cComClientMethodFailure;
		synchronizationType = SynchronizationService.cUserSynchronization;
		if (isOnline()) {
			if (type == SynchronizationService.cGetTotalWL) {
				progressStep = 1; // 1% per operation for the progress Bar
				WLProgress = 90; // 90% for the white list import
			} else {
				progressStep = 2; // 2% per operation for the progress Bar
				WLProgress = 80; // 80% for the white list import
			}
			communicationSequenceStatus = SynchronizationService.cComPending;
			progressDialog = new ProgressDialog(getActivity());
			progressDialog.setMessage(getString(R.string.Communication_en_cours));
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setProgress(0);
			progressDialog.setCancelable(false);
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.show();
			try {
				synchro = new SynchronizationService(getActivity());
				synchro.setWLType(type);
				if (type == SynchronizationService.cGetTotalWL) {
					synchro.start(Constants.DO_TOTAL_SYNCHRO, communicationHandler);
				}else{
					synchro.start(Constants.DO_PARTIAL_SYNCHRO, communicationHandler);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			tools.showAlert(getActivity(), getString(R.string.aucune_connexion_disponible), tools.cError);
		}
		return status;
	}

	public void silentSynchronize(){
		displayType = cTypeLabel;
		if (isOnline()) {
			try {
				synchro = new SynchronizationService(getActivity());
				synchro.start(Constants.SEND_UPDATES, communicationHandler);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	public void getMessages(){
		displayType = cTypeWait;
		if (isOnline()) {
			waitDialog = tools.showWait(getActivity());
			try {
				synchro = new SynchronizationService(getActivity());
				synchro.start(Constants.GET_MESSAGES, communicationHandler);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public  boolean isOnline() {
		boolean connected = false;

		try {
			ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
			connected = networkInfo != null && networkInfo.isAvailable() &&  networkInfo.isConnected();

			if (connected) {
				imgConnect.setImageResource(R.drawable.connection_idle);
				toogle = true;
			} else {
				imgConnect.setImageResource(R.drawable.no_connection);
				communicationStatusText = getString(R.string.aucune_connexion_disponible);
				setCommunicationStatusText();
			}
		} catch (Exception e) {
			Log.e(Constants.TAG, SynchronizationService.class.getName() + " - isOnline -" + e.getMessage());
		}
		return connected;
		//return false;
	}

	
	/*
	 * modifies the communication status bar 
	 * 
	 * @return void
	 */
	private void setCommunicationStatusText() {
		connectionStatus.setText(communicationStatusText);
	}


	/*
	 * displays a communication progress message to the user
	 * 
	 * @param int step: the communication step involved
	 * @param int param: additionnal parameter
	 * 
	 * @return void
	 */


	private void displayCommunicationProgress(int step, int param) {
		int progress;
		if (displayType == cTypeDialog) {
			switch (step) {
			case SynchronizationService.cInitStep:
				progressDialog.setMessage(getString(R.string.Init));
				progressValue = 0;

				break;
			case SynchronizationService.cUploadStep:
				progressDialog.setMessage(getString(R.string.Upload_step));
				progressDialog.incrementProgressBy(progressStep);
				progressValue = progressStep;

				break;
			case SynchronizationService.cEntryStep:
				progressDialog.setMessage(getString(R.string.Entry_step));
				progressDialog.incrementProgressBy(progressStep);
				progressValue += progressStep;

				break;
			case SynchronizationService.cgetWLStep:
				progressDialog.setMessage(getString(R.string.WL_step));
				progressDialog.incrementProgressBy(WLProgress);
				progressValue += WLProgress;

				break;
			case SynchronizationService.cInitPartialWLStep1:
				progressDialog.setMessage(getString(R.string.WL_step_n));

				break;
			case SynchronizationService.cInitPartialWLStep2:
				nbWLSteps = param;
				if (nbWLSteps > 0) {
					nbWLCards = nbWLSteps;
					nbCards = 1;
					progressDialog.setMessage(getString(R.string.WL_step_n) + " 0/" + String.valueOf(nbWLCards) );
					progressDialog.incrementProgressBy(progressStep);
					progressValue += progressStep;
					lastProgress = 1;
				} else {
					progressDialog.incrementProgressBy(WLProgress);
				}

				break;
			case SynchronizationService.cgetPartialWLStep:
				progressDialog.setMessage(getString(R.string.WL_step_n) + " " + String.valueOf(param * nbCards) + "/" + String.valueOf(nbWLCards) );
				if (param == 0) {
					param = 1;
				}
				if (nbWLSteps == 0) {
					nbWLSteps = 1;
				}
				progress = (int)(WLProgress * param / nbWLSteps);
				if (progress > lastProgress) {
					progressDialog.incrementProgressBy(progress - lastProgress);
					progressValue += (progress - lastProgress);
					lastProgress = progress;
				}

				break;
			case SynchronizationService.cInitWLStep0:
				break;
			case SynchronizationService.cInitWLStep1:
				nbWLCards = param;
				break;
			case SynchronizationService.cInitWLStep2:
				nbWLSteps = param;
				nbCards = (int)(nbWLCards / nbWLSteps);
				progressDialog.setMessage(getString(R.string.WL_step_n) + " 0/" + String.valueOf(nbWLCards) );
				progressDialog.incrementProgressBy(progressStep);
				progressValue += progressStep;
				lastProgress = 1;

				break;
			case SynchronizationService.cgetTotalWLStep:
				progressDialog.setMessage(getString(R.string.WL_step_n) + " " + String.valueOf(param * nbCards) + "/" + String.valueOf(nbWLCards) );
				if (param == 0) {
					param = 1;
				}
				if (nbWLSteps == 0) {
					nbWLSteps = 1;
				}
				progress = (int)(WLProgress * param / nbWLSteps);
				if (progress > lastProgress) {
					progressDialog.incrementProgressBy(progress - lastProgress);
					progressValue += (progress - lastProgress);
					lastProgress = progress;
				}
				break;
			case SynchronizationService.cInsertWLStep:
				progressDialog.setMessage(getString(R.string.WL_Insert_step));
				progressDialog.incrementProgressBy(progressStep);
				progressValue += progressStep;

				break;
			case SynchronizationService.cgetUserStep:
				progressDialog.setMessage(getString(R.string.User_step));
				progressDialog.incrementProgressBy(progressStep);
				progressValue += progressStep;

				break;
			case SynchronizationService.cInsertUserStep:
				progressDialog.setMessage(getString(R.string.User_Insert_step));
				progressDialog.incrementProgressBy(progressStep);
				progressValue += progressStep;
				break;
			case SynchronizationService.cgetParamStep:
				progressDialog.setMessage(getString(R.string.Param_step));
				progressDialog.incrementProgressBy(progressStep);
				progressValue += progressStep;
				break;
			
			case SynchronizationService.cgetParamStep1:
				progressDialog.setMessage(getString(R.string.Param_step1));
				break;


			case SynchronizationService.cInsertParamStep:
				progressDialog.setMessage(getString(R.string.Param_step));
				progressDialog.incrementProgressBy(progressStep);
				progressValue += progressStep;
				break;

			case SynchronizationService.cUploadLogCatStep:
				progressDialog.setMessage(getString(R.string.UploadLogCat_step));
				progressValue += (progressStep / 2);
				break;

			case SynchronizationService.cUploadDBStep:
				progressDialog.setMessage(getString(R.string.UploadDB_step));
				progressValue += (progressStep / 2);
				break;

			case SynchronizationService.cStockStep:
				progressDialog.setMessage(getString(R.string.Stock_step));
				progressDialog.incrementProgressBy(progressStep);
				progressValue += progressStep;
				break;

			case SynchronizationService.cBugStep:
				progressDialog.setMessage(getString(R.string.Bug_step));
				progressDialog.incrementProgressBy(progressStep);
				progressValue += progressStep;
				break;

			case SynchronizationService.cMessageStep:
				progressDialog.setMessage(getString(R.string.Message_step));
				progressDialog.incrementProgressBy(progressStep);
				if (progressValue < 100) {
					progressValue += (100 - progressValue);
				}
				break;

			default:
				break;
			}
		} else {
			switch (step) {
			case SynchronizationService.cUploadStep:
				communicationStatusText = getString(R.string.Upload_step_1);
				break;
			case SynchronizationService.cgetWLStep:
				communicationStatusText = getString(R.string.Upload_step_2);
				break;
			case SynchronizationService.cInsertWLStep:
				communicationStatusText = getString(R.string.Upload_step_3);
				break;
			case SynchronizationService.cInitPartialWLStep1:
				communicationStatusText = getString(R.string.Upload_step_2);
				break;
			case SynchronizationService.cInitPartialWLStep2:
				communicationStatusText = getString(R.string.Upload_step_2);
				nbWLSteps = param;
				break;
			case SynchronizationService.cgetPartialWLStep:
				nbWLCards = param;
				break;
			default:
				break;
			}
			setCommunicationStatusText();
			if (toogle) {
				imgConnect.setImageResource(R.drawable.connection);
			} else {
				imgConnect.setImageResource(R.drawable.connection_idle);
			}
			toogle = !toogle;

		}
	}

	/*
	 * displays a communication end message to the user
	 * 
	 * @param int status : the communication status
	 * @param int step: the communication step involved
	 * 
	 * @return void
	 */


	private void displayCommunicationEnd(int status, int step) {
		String message = "", title = null;
		Date date = new Date();
		final int commStatus = status;
		int idIcon = R.mipmap.ic_launcher;
		title = getString(R.string.Global_information);


		if (displayType == cTypeDialog) {
			switch (status) {
			case SynchronizationService.cComOK:
				// end of communication with server
				progressDialog.dismiss();
				message = getString(R.string.Fin_communication_ok);
				communicationStatusText = getString(R.string.Upload) + ' ' + getString(R.string.Upload_end_OK) + ' ' + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(date);
				setCommunicationStatusText();

				break;
			default:
				// error detected during communication with server
				idIcon = R.drawable.ic_ko;
				title = getString(R.string.error_message_title);

				progressDialog.dismiss();
				communicationStatusText = getString(R.string.Upload) + ' ' + getString(R.string.Upload_end_err) + ' ' + status + ' ' + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(date);
				setCommunicationStatusText();
				message = getString(R.string.Fin_communication_erreur) + status + "\n";

				switch (step) {
				case SynchronizationService.cConnectionStep:
					message += getString(R.string.Connexion_erreur);

					break;
				case SynchronizationService.cInitStep:
					message += getString(R.string.Init_erreur);

					break;
				case SynchronizationService.cUploadStep:
					progressDialog.setMessage(getString(R.string.Upload_erreur));

					break;
				case SynchronizationService.cEntryStep:
					progressDialog.setMessage(getString(R.string.Entry_erreur));

					break;
				case SynchronizationService.cgetWLStep:
					message += getString(R.string.WL_erreur);

					break;
				case SynchronizationService.cgetParamStep:
					message += getString(R.string.Param_erreur);

					break;
				case SynchronizationService.cgetUserStep:
					message += getString(R.string.User_erreur);

					break;
				case SynchronizationService.cInsertWLStep:
					message += getString(R.string.WL_Rec_erreur);

					break;
				case SynchronizationService.cInsertUserStep:
					message += getString(R.string.User_Rec_erreur);

					break;
				case SynchronizationService.cInsertParamStep:
					message += getString(R.string.Param_Rec_erreur);

					break;
				default:
					message += getString(R.string.Autre_erreur);

					break;
				}
				break;
			}
			new AlertDialog.Builder(getActivity())
					.setMessage(message)
					.setCancelable(false)
					.setIcon(idIcon)
					.setTitle(title)
					.setPositiveButton(getString(R.string.Global_ok),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
									switch (synchronizationType) {
										case SynchronizationService.cInitSynchronization:
											initSequenceStatus = commStatus;
											break;
										default:
											communicationSequenceStatus = commStatus;
											break;
									}
								}
							}).show();
			switch (synchronizationType) {
				case SynchronizationService.cGetPartialWL:
				case SynchronizationService.cGetTotalWL:
					// copy database
					DbAdapter dbAdapter = new DbAdapter(getActivity());
					dbAdapter.open();
					dbAdapter.databaseBackupSDCard();
					break;
				default:
					break;
			}


		} else if (displayType == cTypeLabel){
			communicationSequenceStatus = status;
			switch (status) {
				case SynchronizationService.cComOK:
					// end of communication with server
					communicationStatusText = getString(R.string.Upload) + ' ' + getString(R.string.Upload_end_OK) + ' ' + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(date);

					break;
				default:
					// error detected during communication with server
					communicationStatusText = getString(R.string.Upload) + ' ' + getString(R.string.Upload_end_err) + ' ' + status + ' ' + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(date);

					break;
			}
			setCommunicationStatusText();
			if (mustRetry) {
				// a new synchronization has been cancelled because this one was not finished, allow new synchronization
				mustRetry = false;
			} else {
				synchroInProgress = false;
			}
			imgConnect.setImageResource(R.drawable.connection_idle);
		} else {
			if (waitDialog != null) {
				waitDialog.dismiss();
			}
			communicationSequenceStatus = status;
			switch (status) {
				case SynchronizationService.cComOK:
					// end of communication with server
					communicationStatusText = getString(R.string.Upload) + ' ' + getString(R.string.Upload_end_OK) + ' ' + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(date);

					break;
				default:
					// error detected during communication with server
					communicationStatusText = getString(R.string.Upload) + ' ' + getString(R.string.Upload_end_err) + ' ' + status + ' ' + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(date);

					break;
			}
			setCommunicationStatusText();
			imgConnect.setImageResource(R.drawable.connection_idle);
		}
	}


	/*
	 * Communication Handler
	 * Displays messages upon communication thread events 
	 * 
	 */


	public Handler communicationHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SynchronizationService.cProgressMsg:
				// progress messages
				displayCommunicationProgress(msg.arg1, msg.arg2);
				break;

			default:
				// end messages
				syncInProgress = false;
				displayCommunicationEnd(msg.arg1, msg.arg2);
				if (msg.arg1 == SynchronizationService.cComOK) {
				}
				break;

			}
		}
	};



}
