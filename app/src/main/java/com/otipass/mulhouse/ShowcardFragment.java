/**
 ================================================================================

 OTIPASS
 reims main package

 @author ED ($Author: ede $)

 @version $Rev: 6455 $
 $Id: ShowcardFragment.java 6455 2016-07-08 11:18:45Z ede $

 ================================================================================
 */
package com.otipass.mulhouse;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
//import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.otipass.sql.Coupon;
import com.otipass.sql.DbAdapter;
import com.otipass.sql.Entry;
import com.otipass.sql.Otipass;
import com.otipass.sql.PackageObject;
import com.otipass.sql.Service;
import com.otipass.synchronization.SynchronizationService;
import com.otipass.tools.Callback;
import com.otipass.tools.ExceptionHandler;
import com.otipass.tools.LastEntry;
import com.otipass.tools.OtipassCard;
import com.otipass.tools.Services;
import com.otipass.tools.tools;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import models.Constants;
public class ShowcardFragment extends android.app.Fragment {
    private static final String TAG = Constants.SHOWCARD_TAG;
    private final int cGreen = 0;
    private final int cRed = 1;
    private final int cOrange = 2;
    private String serial = "";
    private String serialDecimal = "";
    private int numotipass = 0;
    private View rootView;
    private ImageView animationTarget;
    private int checkCardStatus = 0;
    private DbAdapter dbAdapter;
    private Otipass otipass, oldOtipass;
    private int nbServices = 0;
    private String statusMessage = "";
    private String passNumber = "";
    private String serviceStatus = "";
    private Footer footer;
    private boolean checkedAtServer;
    private boolean checkServerNeeded;
    private boolean checkPending = false;
    private SynchronizationService synchroService;
    private boolean forcedEntry = false;
    private boolean checkDone = false;
    private boolean animDone = false;
    private Animation animation;
    private Context context;
    private Services providerServices;
    private String serviceName;
    private int counterService = 0;
    private int currentService = 0;
    private TextView tvHeader, tvService;
    private HashMap<Integer, String> servicesList;
    private boolean mustSelectService = false;
    private int nbss;

    public ShowcardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Handle uncaught exception : there will be redirected to BugMsgActivity
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(getActivity()));
        context = getActivity();
        dbAdapter = new DbAdapter(context);
        dbAdapter.open();
        footer = Footer.getInstance();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // serial or numotipass may be passed as arguments, according to NDEF reading
        serial = getArguments().getString("serial");
        numotipass = getArguments().getInt("numotipass");
        currentService = getArguments().getInt("idservice");
        mustSelectService = getArguments().getBoolean("selectService");
        servicesList = (HashMap) getArguments().getSerializable("servicesList");
        serviceName = "";
        if (!mustSelectService) {
            try {
                serviceName = dbAdapter.getServiceNameById(currentService);
            } catch (Exception ex) {
                Log.e(Constants.TAG, "ShowCardFragment onCreateView()" + ex.getMessage());
            }
        }
        rootView = inflater.inflate(R.layout.fragment_showcard, container, false);
        tvHeader = (TextView) rootView.findViewById(R.id.header_text);
        tvHeader.setText(getString(R.string.entry_control));
        tvService = (TextView) rootView.findViewById(R.id.header_service);
        tvService.setText(serviceName);
        animationTarget = (ImageView) rootView.findViewById(R.id.img_status);
        animation = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_showcard);

        checkDone = false;
        RelativeLayout showLayout = (RelativeLayout) rootView.findViewById(R.id.show_layout);
        showLayout.post(new Runnable() {
            @Override
            public void run() {
                checkedAtServer = checkServerNeeded = getArguments().getBoolean("checkDone");
                processScannedCard();
            }
        });
        animationTarget.post(new Runnable() {
            public void run() {
                if (animation != null) {
                    animDone = false;
                    animationTarget.startAnimation(animation);
                }
            }
        });
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animDone = true;
                if (checkDone) {
                    displayStatus(checkCardStatus);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        return rootView;
    }
    @Override
    public void onResume()
    {
        super.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }
    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }


    /*
     * Check that there are services associated to the current provider
     */
    private int checkProviderServices() {
        int status = cRed;
        if ((nbServices = dbAdapter.providerHasServices()) > 0) {
            status = cGreen;
        } else {
            statusMessage = getString(R.string.no_service_4_provider);
        }
        return status;
    }


    /*
     * get Otipass from serial or numotipass, depending on NFC NDEF reading enabled
     */
    private Otipass selectOtipass(String serial, int numotipass) {
        otipass = null;
        try {
            if (numotipass > 0) {
                otipass = dbAdapter.getOtipass(numotipass);
            } else if (!serial.isEmpty()) {
                if (Constants.SERIAL_CIPHERED) {
                    // this version assumes that serial is ciphered from the decimal value
                    BigInteger num = new BigInteger(serial, 16);
                    serialDecimal = String.valueOf(num);
                    serial = tools.formatCipherSerial(serialDecimal);
                }
                otipass = dbAdapter.getOtipassBySerial(serial);
            }
        } catch (Exception ex) {
            Log.e(TAG, ShowcardFragment.class.getName() + " - selectOtipass -" + ex.getMessage());
        }
        return otipass;
    }

    /*
     * Compute expiry date
     */
    private Calendar computeExpiry(int idPackage) {
        Calendar expiryDate = Calendar.getInstance();
        PackageObject p = dbAdapter.getPackageById(idPackage);
        // caution, duration is the period !
        expiryDate.add(Calendar.DAY_OF_YEAR, p.getPeriod());
        return expiryDate;
    }

    /*
     * Search for occurences of provider services in otipass services string
     */
    private int getNbActiveServices(Services srvsPackage) {
        int cptSrv = 0;
        if (mustSelectService) {
            // provider has several services, check remaining services
            List<Integer> srvsids = srvsPackage.getActiveServices(providerServices);
            cptSrv = srvsids.size();
            if (cptSrv == 1) {
                mustSelectService = false;
                currentService = srvsids.get(0);
                serviceName = dbAdapter.getServiceNameById(currentService);
            }
        } else {
            cptSrv = srvsPackage.getCounter(currentService);
        }
        return cptSrv;
    }

    /*
     * Main card ckeck process
     * This process may be run twice:
     * 1st time - checkedAtServer = false, check at server requested
     * 2nd time - checkedAtServer = true, after the server check
     */
    private int checkCard(String serial, int numotipass) {
        int status = cGreen;
        Calendar expiryDate = null, nowDate = null;
        try {
            checkServerNeeded = false;
            otipass = selectOtipass(serial, numotipass);
            if (otipass != null) {
                this.numotipass = (int) otipass.getNumOtipass();
                this.serial = otipass.getSerial();
                // to eventually cancel the entry
                oldOtipass = (Otipass)otipass.clone();
                // hold the otipass services
                Services srvsPackage = new Services(otipass.getService());
                // hold the provider services
                String service_package = dbAdapter.getServicebyPackageId(otipass.getPid());
                providerServices = new Services(service_package);
                if (otipass.getStatus() == Constants.PASS_INVALID) {
                    // card is rejected on INVALID state
                    status = cRed;
                    statusMessage = getString(R.string.err_invalid_pass);
                } else if (otipass.getStatus() == Constants.PASS_UNDEFINED) {
                    // this status is the initial card status (not delivered to point of sales)
                    if (footer.isOnline()) {
                        if (checkedAtServer) {
                            // after the server check, card in same state
                            status = cRed;
                            statusMessage = getString(R.string.pass_not_ok);
                        } else {
                            // request a server check
                            checkServerNeeded = true;
                        }
                    } else {
                        // no possible control, card is rejected
                        status = cRed;
                        statusMessage = getString(R.string.pass_not_ok_off_line);
                    }
                } else if (otipass.getStatus() == Constants.PASS_CREATED) {
                    // card is checked on CREATED state
                    if (footer.isOnline()) {
                        if (checkedAtServer) {
                            // accept the card as forced entry after server check
                            status = cOrange;
                            statusMessage = getString(R.string.created_state);
                            // compute expiry date, because the tablet won't get update from server
                            expiryDate = computeExpiry(otipass.getPid());
                            otipass.setExpiry(tools.formatSQLDate(expiryDate));
                            forcedEntry = true;
                        } else {
                            // request a server check
                            checkServerNeeded = true;
                        }
                    } else {
                        if (getNbActiveServices(srvsPackage) > 0) {
                            // no control possible, accept the card as forced entry
                            status = cOrange;
                            statusMessage = getString(R.string.created_state_off_line);
                            // compute expiry date, because the tablet won't get update from server
                            expiryDate = computeExpiry(otipass.getPid());
                            otipass.setExpiry(tools.formatSQLDate(expiryDate));
                            forcedEntry = true;
                        } else {
                            // no service for this pass
                            status = cRed;
                            statusMessage = getString(R.string.no_service);
                        }
                    }
                } else {
                    // the card is EXPIRED, INACTIVE or ACTIVE
                    if ((otipass.getStatus() != Constants.PASS_INACTIVE)) {
                        expiryDate = null;
                        if (otipass.getExpiry().length() > 10) {
                            expiryDate = tools.setCalendar(otipass.getExpiry());
                        }
                    }
                    if ((otipass.getStatus() == Constants.PASS_ACTIVE)) {
                        // check expiry date
                        nowDate = Calendar.getInstance();
                        if (expiryDate != null) {
                            if (nowDate.after(expiryDate)) {
                                otipass.setStatus((short) Constants.PASS_EXPIRED);
                                dbAdapter.updateOtipass(otipass);
                            }
                        } else {
                            if (footer.isOnline()) {
                                if (checkedAtServer) {
                                    status = cRed;
                                    statusMessage = getString(R.string.no_expiry);
                                } else {
                                    // request a server check
                                    checkServerNeeded = true;
                                }
                            } else {
                                // no expiry date, something wrong :(
                                status = cRed;
                                statusMessage = getString(R.string.no_expiry_off_line);
                            }
                        }
                    }
                    if ((status == cGreen) && (!checkServerNeeded)) {
                        if (otipass.getStatus() == Constants.PASS_INACTIVE) {
                            // compute expiry
                            expiryDate = computeExpiry(otipass.getPid());
                            otipass.setExpiry(tools.formatSQLDate(expiryDate));
                        }
                        String sExpiry = tools.formatDate(expiryDate);
                        if (otipass.getStatus() == Constants.PASS_EXPIRED) {
                            status = cRed;
                            statusMessage = getString(R.string.err_expired_pass, sExpiry);
                        } else {
                            // the card is  INACTIVE or ACTIVE
                            if (getNbActiveServices(srvsPackage) > 0) {
                                status = cGreen;
                                if (otipass.getStatus() == Constants.PASS_INACTIVE) {
                                    statusMessage = getString(R.string.first_use) + " ";
                                }
                                statusMessage += getString(R.string.valid_pass_ok, sExpiry);
                            } else {
                                // no service for this pass
                                status = cRed;
                                statusMessage = getString(R.string.no_service);
                            }
                        }
                    }
                }
            } else {
                // otipass is null
                if (footer.isOnline()) {
                    if (checkedAtServer) {
                        // card is rejected
                        status = cRed;
                        statusMessage = getString(R.string.err_pas_city_pass);
                    } else {
                        // request a server check
                        checkServerNeeded = true;
                    }
                } else {
                    // device not online and otipass not found, card is rejected
                    status = cRed;
                    statusMessage = getString(R.string.device_off_line);
                }
            }
        } catch (Exception ex) {
            status = cRed;
            Log.e(TAG, ShowcardFragment.class.getName() + " - checkCard -" + ex.getMessage());
        }
        return status;
    }

    private void displayStatus(int status) {
        if (animDone) {
            int bcks[] = {R.mipmap.st_green, R.mipmap.st_red, R.mipmap.st_orange};
            status = status > 2 ? 1 : status;
            TextView tvPassNumber = (TextView) rootView.findViewById(R.id.pass_number);
            String s = Constants.SHOW_OTIPASS_NUM ? String.valueOf(numotipass) : Constants.SERIAL_CIPHERED ? tools.formatSerial(serialDecimal): serial;
            tvPassNumber.setText(getString(R.string.pass_no, s));
            TextView tvPassMessage = (TextView) rootView.findViewById(R.id.pass_message);
            tvPassMessage.setText(statusMessage);
            TextView tvServiceStatus = (TextView) rootView.findViewById(R.id.service_status);
            switch (status) {
                case cRed:
                    tvServiceStatus.setText(getString(R.string.entry_denied));
                    tvPassNumber.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.red, null));
                    tvPassMessage.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.red, null));
                    tvServiceStatus.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.red, null));
                    animationTarget.setImageDrawable(ResourcesCompat.getDrawable(getResources(), bcks[status], null));
                    tools.setRFIDLock(getActivity(), false);
                    break;
                case cOrange:
                    if (!mustSelectService) {
                        if (counterService > 0) {
                            tvServiceStatus.setText(getString(R.string.entry_consumed_remaining, serviceName, counterService));
                        } else {
                            tvServiceStatus.setText(getString(R.string.one_entry_consumed, serviceName));
                        }
                        animationTarget.setImageDrawable(ResourcesCompat.getDrawable(getResources(), bcks[status], null));
                    }
                    tvPassNumber.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.orange, null));
                    tvPassMessage.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.orange, null));
                    tvServiceStatus.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.orange, null));
                    break;
                default:
                    if (!mustSelectService) {
                        if (counterService > 0) {
                            tvServiceStatus.setText(getString(R.string.entry_consumed_remaining, serviceName, counterService));
                        } else {
                            tvServiceStatus.setText(getString(R.string.one_entry_consumed, serviceName));
                        }
                        animationTarget.setImageDrawable(ResourcesCompat.getDrawable(getResources(), bcks[status], null));
                    }
                    tvPassNumber.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.green, null));
                    tvPassMessage.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.green, null));
                    tvServiceStatus.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.green, null));
                    break;
            }
            if ((status != cRed) && mustSelectService) {
                // get remaining services
                Services srvs = new Services(otipass.getService());
                HashMap<Integer, String> pList = srvs.getServices2Select(servicesList);
                // call service selection fragment
                FragmentManager fragmentManager = getFragmentManager();
                Bundle args = new Bundle();
                args.putSerializable("servicesList", pList);
                ServiceSelection dialog = new ServiceSelection();
                dialog.setCancelable(false);
                dialog.setArguments(args);
                dialog.show(fragmentManager, "");
            } else {
                tools.setRFIDLock(getActivity(), false);
            }
        }
    }

    private void processScannedCard() {
        checkDone = false;

        try {
            if (mustSelectService || (currentService > 0)) {
                if (!serial.isEmpty() || (numotipass > 0)) {
                    if (serial.equals(Constants.UNKNOWN_PASS)) {
                        // this may be returned by the external reader
                        checkDone = true;
                        statusMessage = getString(R.string.err_unknown_pass);
                        checkCardStatus = cRed;
                        displayStatus(cRed);
                    } else {
                        checkCardStatus = checkCard(serial, numotipass);
                        if (checkServerNeeded) {
                            // start the check to the server
                            synchroService = new SynchronizationService(getActivity());
                            if (numotipass > 0) {
                                synchroService.start(Constants.CHECK_OTIPASS, numotipass);
                            } else {
                                String s = Constants.SERIAL_CIPHERED ? tools.formatCipherSerial(serialDecimal) : serial;
                                synchroService.start(Constants.CHECK_SERIAL, s);
                            }
                            CheckSynchro synchro = new CheckSynchro();
                            synchro.execute();
                        } else {
                            if (!mustSelectService && (checkCardStatus != cRed)) {
                                consumeService(otipass, currentService);
                            }
                            checkDone = true;
                            displayStatus(checkCardStatus);
                        }
                    }
                } else {
                    statusMessage = getString(R.string.err_unknown_pass);
                    checkCardStatus = cRed;
                    displayStatus(checkCardStatus);
                }
            } else {
                checkDone = true;
                statusMessage = getString(R.string.no_service_4_provider);
                checkCardStatus = cRed;
                displayStatus(checkCardStatus);
            }
        } catch (Exception ex) {
            Log.e(Constants.TAG, ShowcardFragment.class.getName() + " - processScannedCard - " + ex.getMessage());
            checkDone = true;
        }
        finally {
        }
    }


    public int consumeService(Otipass otipass, int idservice) {
        int st = 0;
        int status = otipass.getStatus(); // initial status of the card
        Entry entry = null;
        try {
            dbAdapter.StartTransaction();
            Service service = dbAdapter.getService(idservice);
            if (service.getType() == Constants.COUNTER_SERVICE) {
                Log.i(Constants.TAG, "dec srv:"+idservice);
                Services srvs = new Services(otipass.getService());
                otipass.setService(srvs.decService(idservice));
                counterService = srvs.getCounter(idservice);
            } else {
                counterService = 0;
            }
            if ((status == Constants.PASS_INACTIVE) || (status == Constants.PASS_CREATED)) {
                otipass.setStatus((short)Constants.PASS_ACTIVE);
            }
            int nb = dbAdapter.updateOtipass(otipass);
            if (nb != 1) {
                st = 1;
            } else {
                // save the entry
                short event = forcedEntry?Entry.cForcedEntry:Entry.cNormalEntry;
                Calendar cal = Calendar.getInstance();
                entry = dbAdapter.insertEntry(tools.formatSQLDate(cal), (int) otipass.getNumOtipass(), (short) 1, event, false, idservice);
                // save the lastEntry
                if (entry != null) {
                    LastEntry lastEntry = LastEntry.getInstance(getActivity());
                    // it saves the last otipass state, to eventually restore it
                    String s = Constants.SERIAL_CIPHERED ? tools.formatSerial(serialDecimal):serial;
                    lastEntry.recordLastEntry(entry.getId(), oldOtipass, cal, s);
                }
            }
        } catch (Exception ex) {
            Log.e(Constants.TAG, ShowcardFragment.class.getName() + " - consumeService - " + ex.getMessage());
            st = 1;
        }
        finally {
            boolean commit = (st == 0);
            dbAdapter.endTransaction(commit);
        }
        if (entry != null) {
            // send entry to the server
            Footer footer = Footer.getInstance();
            footer.silentSynchronize();
        }

        return st;
    }

    // this class is intended to wait for the check of the card at the server
    private class CheckSynchro extends AsyncTask<Void, Integer, Void>
    {
        private AlertDialog alert;
        @Override
        protected void onPreExecute() {

            Drawable icon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_attention, null);
            String title = getString(R.string.Global_information);
            alert = new AlertDialog.Builder(getActivity()).create();
            alert.setTitle(title);
            alert.setIcon(icon);
            alert.setMessage(getString(R.string.check_card_state_running) + getString(R.string.Global_veuillez_patienter));
            alert.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values){
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            boolean end = false;
            do {
                end = (tools.getServiceState(context) != tools.cCommmunicationPending);
            } while (!end);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            int status;
            Otipass otipassDB;
            alert.dismiss();
            // this will avoid another check at the server on the same Pass
            checkedAtServer = true;

            // info sent back by the server concerning Otipass
            otipass = synchroService.getCheckedCard(); // this one is from server DB
            if (otipass != null) {
                status = otipass.getStatus();
                numotipass = (int)otipass.getNumOtipass();
                try {
                    // otipassDB is the one in the tablet inner DB
                    if ((otipassDB = dbAdapter.getOtipass(otipass.getNumOtipass())) != null) {
                        // when offline, services might have been consumed
                        Services srvsOtipass = new Services(otipassDB.getService());
                        Services srvsUpdate = new Services(otipass.getService());
                        // set services according to consumed services
                        otipass.setService(srvsOtipass.updateService(srvsUpdate, currentService));
                        dbAdapter.updateOtipass(otipass);
                    } else {
                        dbAdapter.insertOtipassObject(otipass);
                    }
                } catch (Exception e) {
                    Log.e(Constants.TAG, ShowcardFragment.class.getName() + " - onPostExecute - " + e.getMessage());
                }
            }
            checkDone = true;
            processScannedCard();
        }
    }

}
