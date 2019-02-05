/**
 ================================================================================

 OTIPASS
 reims main package

 @author ED ($Author: ede $)

 @version $Rev: 6455 $
 $Id: CancelFragment.java 6455 2016-07-08 11:18:45Z ede $

 ================================================================================
 */
package com.otipass.mulhouse;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.otipass.sql.Otipass;
import com.otipass.tools.ExceptionHandler;
import com.otipass.tools.LastEntry;
import com.otipass.tools.tools;

import java.util.Calendar;

import models.Constants;


public class CancelFragment extends android.app.Fragment {
    private static final String TAG = Constants.CANCEL_TAG;
    private View rootView;
    private Context context;
    private TextView tvMessage;
    private Button btnCancel;
    private LinearLayout messageLayout;
    private LastEntry lastEntry;
    private long idEntry;

    public CancelFragment() {
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
        rootView = inflater.inflate(R.layout.fragment_cancel, container, false);
        btnCancel = (Button) rootView.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnCancel.setVisibility(Button.GONE);
                if (lastEntry.cancelLastEntry() == LastEntry.cOK) {
                    messageLayout.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.container_ok, null));
                    tvMessage.setText(getString(R.string.cancel_success));
                    // upload cancellation to server
                    Footer footer = Footer.getInstance();
                    footer.silentSynchronize();
                } else {
                    messageLayout.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.container_error, null));
                    tvMessage.setText(getString(R.string.cancel_fail));
                }
            }
        });
        tvMessage = (TextView) rootView.findViewById(R.id.cancel_text);
        messageLayout = (LinearLayout) rootView.findViewById(R.id.layout_cancel);
        detectEntryCancellation();
        return rootView;
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    private void detectEntryCancellation() {
        try {
            String serial;
            lastEntry = LastEntry.getInstance(getActivity());
            Calendar cal = Calendar.getInstance();
            if ((idEntry = lastEntry.getLastEntry(cal)) > 0) {
                String date = tools.formatDateA(lastEntry.getDate());
                if (Constants.SHOW_OTIPASS_NUM) {
                    serial = String.valueOf(lastEntry.getOtipass().getNumOtipass());
                } else {
                    serial = lastEntry.getSerial2Show();
                }
                tvMessage.setText(getString(R.string.entry_2_cancel, serial, date));
            } else {
                tvMessage.setText(getString(R.string.no_entry_2_cancel));
                btnCancel.setVisibility(Button.GONE);
            }
        } catch (Exception ex) {
            Log.e(Constants.TAG, CancelFragment.class.getName() + " - detectEntryCancellation - " + ex.getMessage());
        }
    }
}
