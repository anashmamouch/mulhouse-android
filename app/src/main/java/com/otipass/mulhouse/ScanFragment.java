/**
 ================================================================================

 OTIPASS
 reims main package

 @author ED ($Author: ede $)

 @version $Rev: 6452 $
 $Id: ScanFragment.java 6452 2016-07-07 13:59:44Z ede $

 ================================================================================
 */
package com.otipass.mulhouse;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.otipass.tools.ExceptionHandler;
import com.otipass.tools.StoppableRunnable;

import models.Constants;


public class ScanFragment extends android.app.Fragment {
    private static final String TAG = Constants.SCAN_TAG;
    private View rootView;
    private TextView tvHeader, tvService;
    private Handler mPollingHandler = new Handler();
    private StoppableRunnable mTask;
    private final int cPollingFrequency = 60 * 5 * 1000; // 5mn
    private boolean firstPoll = true;

    public ScanFragment() {
        // Required empty public constructor
    }


    @Override
    public void onResume()
    {
        super.onResume();
        ImageView animationTarget1 = (ImageView) rootView.findViewById(R.id.img_wave);
        animationTarget1.setImageBitmap(null);
        animationTarget1.setBackgroundResource(R.drawable.anim_scan);
        AnimationDrawable animation1 = (AnimationDrawable) animationTarget1.getBackground();
        animation1.start();
        ImageView animationTarget2 = (ImageView) rootView.findViewById(R.id.img_pass);
        Animation animation2 = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_card);
        animationTarget2.startAnimation(animation2);

    }
    @Override
    public void onPause()
    {
        super.onPause();
        if (mTask != null) {
            stopPolling() ;
        }
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
        rootView = inflater.inflate(R.layout.fragment_scan, container, false);
        String serviceName = getArguments().getString("name");
        tvHeader = (TextView) rootView.findViewById(R.id.header_text);
        tvHeader.setText(getString(R.string.entry_control));
        tvService = (TextView) rootView.findViewById(R.id.header_service);
        tvService.setText(serviceName);
        rootView.post(new Runnable() {
            @Override
            public void run() {
                onFragmentReady();
            }
        });
        return rootView;
    }

    private void stopPolling() {
        if (mTask != null) {
            mTask.stop();
            if (mPollingHandler != null) {
                mPollingHandler.removeCallbacks(mTask);
            }
        }
    }
    private void startPolling(int frequency) {
        if (firstPoll) {
            firstPoll = false;
        } else {
            Footer footer = Footer.getInstance();
            if (footer.isOnline()) {
                footer.silentSynchronize();
            }

        }
        mPollingHandler.postDelayed(mTask, frequency);
    }
    /*
     * Does some initialization when the fragment is loaded
     */
    private void onFragmentReady() {
        mTask = new StoppableRunnable() {
            public void stoppableRun() {
                startPolling(cPollingFrequency);
            }
        };
        startPolling(cPollingFrequency);
    }
}
