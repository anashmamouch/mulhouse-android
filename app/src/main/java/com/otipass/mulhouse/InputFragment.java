/**
 ================================================================================

 OTIPASS
 reims main package

 @author ED ($Author: ede $)

 @version $Rev: 6367 $
 $Id: InputFragment.java 6367 2016-06-14 13:37:20Z ede $

 ================================================================================
 */
package com.otipass.mulhouse;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.otipass.sql.DbAdapter;
import com.otipass.sql.Otipass;
import com.otipass.tools.Callback;
import com.otipass.tools.ExceptionHandler;
import com.otipass.tools.tools;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import models.Constants;

public class InputFragment extends android.app.Fragment {
    protected Context context;
    private View rootView;
    private Callback.OnInputListener mReturnListener;
    private AutoCompleteTextView tvSerial;
    private DbAdapter dbAdapter;

    public InputFragment() {
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
        rootView = inflater.inflate(R.layout.fragment_input, container, false);
        tvSerial = (AutoCompleteTextView) rootView.findViewById(R.id.enter_serial);
        if (Constants.SHOW_OTIPASS_NUM || Constants.SERIAL_CIPHERED) {
            // decimal value only
            tvSerial.setInputType(InputType.TYPE_CLASS_NUMBER);
        }

        Button btnValid = (Button) rootView.findViewById(R.id.btn_Valid);
        btnValid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s;
                boolean checkNumotipass = false;
                if (Constants.SHOW_OTIPASS_NUM) {
                    s = tvSerial.getText().toString();
                    checkNumotipass = true;
                } else if (Constants.SERIAL_CIPHERED) {
                    s = tools.formatCipherSerial(tvSerial.getText().toString());
                } else {
                    s = tvSerial.getText().toString();
                }

                if (checkSerial(s, checkNumotipass)) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
                    if (Constants.SHOW_OTIPASS_NUM) {
                        mReturnListener.onInput(s);
                    } else if (Constants.SERIAL_CIPHERED) {
                        BigDecimal num = new BigDecimal(tvSerial.getText().toString());
                        mReturnListener.onInput(String.format("%X", num.toBigInteger()));
                    } else {
                        mReturnListener.onInput(s);
                    }
                }
            }
        });
        dbAdapter = new DbAdapter(getActivity());
        dbAdapter.open();

        if (!Constants.SERIAL_CIPHERED) {
            initAutocompletion();
        }
        return rootView;

    }

    private boolean checkSerial(String serial, boolean checkNumotipass) {
        boolean ok = true;
        if (Constants.SHOW_OTIPASS_NUM) {
            if (dbAdapter.getOtipass(Long.valueOf(serial)) == null) {
                ok = false;
            }
        } else if (!Constants.SERIAL_CIPHERED) {
            if (dbAdapter.getOtipassBySerial(serial) == null) {
                ok = false;
            }
        }
        if (!ok) {
            tools.showAlert(getActivity(), getString(R.string.must_enter_valid_pass), tools.cWarning);
        }
        return ok;
    }

    private void initAutocompletion() {
        List<String> serialList = new ArrayList<String>();
        if (Constants.SHOW_OTIPASS_NUM) {
            serialList = dbAdapter.getNumotipassList();
        } else {
            serialList = dbAdapter.getOtipassSerialList();
        }
        final ArrayAdapter<String> adapter;
        adapter = new ArrayAdapter<String>(getActivity(), R.layout.autocomplete_item, serialList);
        tvSerial.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                s = tvSerial.getText();
                if (s.length() > 3) {
                    tvSerial.setAdapter(adapter);
                }
            }
        });

    }

    // attach callbacks to the calling activity
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mReturnListener = (Callback.OnInputListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnInputListener");
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


}
