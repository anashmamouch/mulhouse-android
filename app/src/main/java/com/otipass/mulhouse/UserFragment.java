/**
 ================================================================================

 OTIPASS
 reims main package

 @author ED ($Author: ede $)

 @version $Rev: 6450 $
 $Id: UserFragment.java 6450 2016-07-06 09:43:10Z ede $

 ================================================================================
 */
package com.otipass.mulhouse;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.otipass.sql.User;
import com.otipass.tools.Callback;
import com.otipass.tools.Callback.OnUserIdentificationListener;
import com.otipass.sql.DbAdapter;
import com.otipass.tools.ExceptionHandler;
import com.otipass.tools.tools;

import models.Constants;


public class UserFragment extends android.app.Fragment {
    private Context context;
    private static final String TAG = Constants.USER_TAG;
    private OnUserIdentificationListener mReturnListener;
    private TextView tvError;
    private User user;
    private int idUser;

    public UserFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Handle uncaught exception : there will be redirected to BugMsgActivity
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(getActivity()));
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user, container, false);
        final EditText pwdField = (EditText) rootView.findViewById(R.id.pwd);
        final EditText loginField = (EditText) rootView.findViewById(R.id.login);
        tvError = (TextView) rootView.findViewById(R.id.error_text);
        tvError.setVisibility(TextView.GONE);
        Button connexion = (Button) rootView.findViewById(R.id.btn_Valid);
        connexion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pwd, login;
                pwd = pwdField.getText().toString().trim();
                login = loginField.getText().toString().trim();
                // check admin credentials
                if (checkUser(login, pwd)) {
                    View view = getActivity().getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        mReturnListener.onUserIdentificationReturn(idUser);
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
            mReturnListener = (OnUserIdentificationListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnUserIdentificationListener");
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


    private boolean checkUser(String login, String clearPwd) {
        boolean checked = false;
        String cipheredPwd, salt;
        DbAdapter dbAdapter = new DbAdapter(getActivity());
        dbAdapter.open();

        user = dbAdapter.getUserByLogin(login);
        if (user != null) {
            cipheredPwd = user.getPassword();
            salt = user.getSalt();
            checked = tools.checkMD5(clearPwd, salt, cipheredPwd);
            idUser = (int)user.getId();
        }
        return checked;
    }

}
