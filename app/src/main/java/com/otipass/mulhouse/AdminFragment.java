/**
 ================================================================================

 OTIPASS
 reims main package

 @author ED ($Author: ede $)

 @version $Rev: 6414 $
 $Id: AdminFragment.java 6414 2016-06-23 14:36:10Z ede $

 ================================================================================
 */
package com.otipass.mulhouse;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Button;
import android.widget.EditText;

import com.otipass.tools.*;
import com.otipass.tools.Callback.OnReturnListener;
import com.otipass.synchronization.SynchronizationService;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import models.Constants;


public class AdminFragment extends android.app.Fragment {
    private static final String TAG = Constants.ADMIN_TAG;
    private OnReturnListener mReturnListener;
    private Context context;
    private static final int cWifi = 1;
    private static final int c3G = 2;
    private static final int cEthernet = 3;
    private static final int cTablet = 1;
    private static final int cSmartphone = 2;
    int providerId = 0;
    int comStatus;
    private TextView tvError;


    public AdminFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Handle uncaught exception : there will be redirected to BugMsgActivity
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(getActivity()));
        context = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_admin, container, false);
        final EditText pwdField = (EditText) rootView.findViewById(R.id.pwd);
        final EditText providerField = (EditText) rootView.findViewById(R.id.provider_id);
        tvError = (TextView) rootView.findViewById(R.id.error_text);
        tvError.setVisibility(TextView.GONE);
        Button connexion = (Button) rootView.findViewById(R.id.btn_Valid);
        connexion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pwd, sId;
                pwd = pwdField.getText().toString().trim();
                sId = providerField.getText().toString().trim();
                // check admin credentials
                if (checkUser(pwd)) {
                    try {
                        providerId = Integer.valueOf(sId);
                    } catch (Exception e) {

                    }
                    if (providerId > 0) {
                        tvError.setVisibility(TextView.GONE);
                        init();
                    } else {
                        tvError.setVisibility(TextView.VISIBLE);
                        tvError.setText(getString(R.string.Provider_id_incorrect));
                    }
                } else {
                    tvError.setVisibility(TextView.VISIBLE);
                    tvError.setText(getString(R.string.Identifiants_incorrects));
                }
            }
        });
        return rootView;
    }


    // attach callbacks to the calling activity
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mReturnListener = (OnReturnListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnReturnListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mReturnListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private boolean checkUser(String clearPwd) {
        boolean checked = false;
        String cipheredPwd = admin.getPwd();
        String salt = admin.getSalt();
        checked = tools.checkMD5(clearPwd, salt, cipheredPwd);
        return checked;
    }

    private void init() {
        boolean connected;
        int connectionType = 0;
        int deviceType = 0;
        comStatus = SynchronizationService.cComClientMethodFailure;

        try {
            View view = getActivity().getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            // detection WiFi network
            if (mWifi != null && mWifi.isAvailable()) {
                connectionType = cWifi;
            } else {
                // detection 3G network
                NetworkInfo mMobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                if (mMobile != null && mMobile.isAvailable()) {
                    connectionType = c3G;
                } else {
                    //detection Ethernet ?? needs to be checked
                    connectionType = cEthernet;
                }
            }
            // detection tablet or smartphone
            boolean isTablet = tools.isTablet();
            if (isTablet) {
                deviceType = cTablet;
            } else {
                deviceType = cSmartphone;
            }

            // connection detection
            Footer footer = Footer.getInstance();
            if (footer.isOnline()) {
                footer.startInitSequence(providerId, deviceType, connectionType);
                Synchro sync = new Synchro();
                sync.execute();
            } else {
                tools.showAlert(context, getString(R.string.aucune_connexion_disponible), tools.cError);
            }
        } catch (Exception e) {
            Log.e(TAG, SynchronizationService.class.getName() + " - init -" + e.getMessage());
        }
    }

    // This tasks waits until initialization completion, then returns ti MainActivity
    private class Synchro extends AsyncTask<Void, Integer, Void>
    {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Integer... values){
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            int status;
            do {
            } while (tools.getServiceState(context) != tools.cIdle);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mReturnListener.onReturn(Constants.ACTION_IDLE);
        }
    }

}
