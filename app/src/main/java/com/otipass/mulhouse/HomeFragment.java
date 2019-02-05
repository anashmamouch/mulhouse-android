/**
 ================================================================================

 OTIPASS
 reims main package

 @author ED ($Author: ede $)

 @version $Rev: 6452 $
 $Id: HomeFragment.java 6452 2016-07-07 13:59:44Z ede $

 ================================================================================
 */
package com.otipass.mulhouse;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.otipass.adapters.ViewPagerAdapter;
import com.otipass.sql.DbAdapter;
import com.otipass.sql.Param;
import com.otipass.sql.Support;
import com.otipass.sql.User;
import com.otipass.swdownload.SwDownload;
import com.otipass.synchronization.SynchronizationService;
import com.otipass.tools.Callback;
import com.otipass.tools.ExceptionHandler;
import com.otipass.tools.Messages;
import com.otipass.sql.Msg;
import com.otipass.tools.StoppableRunnable;
import com.otipass.tools.tools;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import models.Constants;


public class HomeFragment extends android.app.Fragment  {
    private static final int cDownload = 1;
    private static final int cSynchro = 2;
    private static final int cSpeedAnim = 5000;

    private int idUser;
    private TextView tvMessages, tvIndex;
    private DbAdapter dbAdapter;
    private int userProfile;
    private Messages messages;
    private List<Msg> displayedMessages;
    private int indexMsg, nbMessages;
    private ViewPager intro_images;
    private LinearLayout pagerIndicator;
    private int dotsCount;
    private ImageView[] dots;
    private ImageView imgPass;
    private int[] imgIds;
    private ViewPagerAdapter mAdapter;
    private View rootView;
    private ViewPager viewPager;
    private int imgIndex, pageIndex;
    private Button btnMessage;
    private static int currentPagerPosition;
    private Callback.OnReturnListener mReturnListener;
    private Callback.OnScanRequestListener mScanRequestListener;
    private ProgressDialog progressDialog;
    private SwDownload swDownload;
    private Handler mAnimHandler = new Handler();
    private StoppableRunnable mTask;
    private boolean pageResumed = false;
    private Context context;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)   {
        super.onCreate(savedInstanceState);
        // Handle uncaught exception : there will be redirected to BugMsgActivity
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(getActivity()));
        context = getActivity();
    }

    public void onResume() {
        super.onResume();
        pageResumed = true;
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mTask != null) {
            stopAnimMessages() ;
        }
    }

    public void stopAnimMessages() {
        if (mTask != null) {
            mTask.stop();
            if (mAnimHandler != null) {
                mAnimHandler.removeCallbacks(mTask);
            }
        }
    }

    public void startAnimMessages(long delayMillis) {
        try {
            if (pageResumed) {
                // not to start immediately
                pageResumed = false;
            } else {
                if (currentPagerPosition == (mAdapter.getCount() - 1)) {
                    currentPagerPosition = 0;
                } else {
                    currentPagerPosition++;
                }
                viewPager.setCurrentItem(currentPagerPosition, true);
            }
            mAnimHandler.postDelayed(mTask, delayMillis);
        } catch(Exception e) {
            Log.e(Constants.HOME_TAG, "startAnimMessages" + e.getMessage());
        }
    }

    /*
     * attach a callback activity action to the fragment
     *  this is needed when synchronization is done, to go back to home page (= refresh this fragment)
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mReturnListener = (Callback.OnReturnListener) activity;
            mScanRequestListener = (Callback.OnScanRequestListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnReturnListener and OnScanRequestListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        boolean usePager = true;
        rootView = inflater.inflate(R.layout.fragment_home, container, false);
        LinearLayout passLayout = (LinearLayout) rootView.findViewById(R.id.layout_pass);
        // the message layout needs some initialization which needs to be done when the view is set
        RelativeLayout messageLayout = (RelativeLayout) rootView.findViewById(R.id.layout_message);
        imgPass = (ImageView) rootView.findViewById(R.id.img_pass);
        // set a listener directly go to scan when clicking image
        if (imgPass != null) {
            imgPass.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mScanRequestListener.onScanRequest();
                }
            });
        }
        // get the user and his profile
        idUser = getArguments().getInt("user");
        dbAdapter = new DbAdapter(getActivity());
        dbAdapter.open();
        User user = dbAdapter.getUser(idUser);
        if (user != null) {
            userProfile = user.getProfile();
        } else {
            userProfile = Constants.USR_CONTROLLER;
        }
        // get the messages to display according to user profile
        messages = Messages.getInstance();
        messages.checkMessages(getActivity(), dbAdapter, userProfile);
        nbMessages = getMessages();
        usePager = setMessagesTitle();
        if (usePager) {
            setViewPager();
            passLayout.setVisibility(LinearLayout.GONE);
            messageLayout.setVisibility(LinearLayout.VISIBLE);
            messageLayout.post(new Runnable() {
                @Override
                public void run() {
                    if (mAdapter != null) {
                        onFragmentReady();
                    }
                }
            });
        } else {
            passLayout.setVisibility(LinearLayout.VISIBLE);
            messageLayout.setVisibility(LinearLayout.GONE);
        }

        return rootView;
    }


    /*
     * Initialize viewPager
     *
     */
    private void setViewPager() {
        // view pager management to display messages
        viewPager = (ViewPager) rootView.findViewById(R.id.pager_container);
        pagerIndicator = (LinearLayout) rootView.findViewById(R.id.viewPagerCountDots);
        // prepare pager adapter
        Spanned[] txtViews = new Spanned[nbMessages];
        String[] txtButtons = new String[nbMessages];
        int[] msgTypes = new int[nbMessages];
        int i =0;
        for (Msg msg : displayedMessages) {
            txtViews[i] = msg.getSpanned();
            txtButtons[i] = msg.getBtnMsg();
            msgTypes[i] = msg.getType();
            i++;
        }
        mAdapter = new ViewPagerAdapter(getActivity(), txtViews, txtButtons, msgTypes);
        viewPager.setAdapter(mAdapter);

        currentPagerPosition = 0;
        // this listener to catch viewPager changes
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                pageSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                    stopAnimMessages();
                }
            }
        });
        // this sets the bottom buttons to switch form one page to another
        setUiPageViewController();
    }

    /*
     * Initialize messages title "vous avez un nouveau message" ...
     *  Returns if the viewPager is needed
     */
    private boolean setMessagesTitle() {
        boolean usePager = true;
        TextView tvTitle;
        tvTitle = (TextView ) rootView.findViewById(R.id.title_message_text);
        switch (nbMessages) {
            case 0:
                usePager = false;
                tvTitle.setText(getActivity().getString(R.string.no_messages));
                break;
            case 1:
                tvTitle.setText(getActivity().getString(R.string.nouveau_message));
                break;
            default:
                tvTitle.setText(getActivity().getString(R.string.nouveaux_messages, nbMessages));
                break;
        }
        return usePager;
    }

    /*
     * Does some initialization when the fragment is loaded
     */
    private void onFragmentReady() {
        for (pageIndex = 0; pageIndex < mAdapter.getCount(); pageIndex++) {
            btnMessage = mAdapter.getButon(pageIndex);
            if (btnMessage != null) {
                // set a listener for each viewPager button
                btnMessage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Msg msg = displayedMessages.get(currentPagerPosition);
                        processAction(msg);
                    }
                });

            }
        }
        viewPager.setCurrentItem(currentPagerPosition, true);

        mTask = new StoppableRunnable() {
            public void stoppableRun() {
                startAnimMessages(cSpeedAnim);
            }
        };
       startAnimMessages(cSpeedAnim);

    }

    /*
     * Set up the viewPager control buttons (bottom bar)
     */
    private void setUiPageViewController() {
        dotsCount = mAdapter.getCount();
        dots = new ImageView[dotsCount];
        if (dotsCount > 1) {
            for (imgIndex = 0; imgIndex < dotsCount; imgIndex++) {
                dots[imgIndex] = new ImageView(getActivity());
                dots[imgIndex].setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.nonselecteditem_dot, null));
                dots[imgIndex].setId(imgIndex);
                dots[imgIndex].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            stopAnimMessages();
                            viewPager.setCurrentItem(v.getId(), true);
                        } catch (Exception e) {
                        }
                    }
                });

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(4, 0, 4, 0);
                pagerIndicator.addView(dots[imgIndex], params);
            }
            dots[0].setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.selecteditem_dot, null));
        }
    }

    /*
     * Change viewPager control buttons when a new page is selected
     */
    private void pageSelected(int position) {
        currentPagerPosition = position;
        for (int i = 0; i < dotsCount; i++) {
            dots[i].setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.nonselecteditem_dot, null));

        }
        dots[position].setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.selecteditem_dot, null));
    }

    /*
     * process action launched by message button click
     */
    private void processAction(Msg msg) {
        Footer footer = Footer.getInstance();
        switch (msg.getType()) {
            case Messages.cMsgDB:
                // DB is not in a correct state, , should to the full data base initialization
                if (footer.isOnline()) {
                    footer.synchronise(SynchronizationService.cGetTotalWL);
                    Synchro synchro = new Synchro(cSynchro);
                    synchro.execute();
                } else {
                    tools.showAlert(getActivity(), getString(R.string.msg_pas_connecte), tools.cWarning);
                }
                break;
            case Messages.cMsgSWversion:
                // New software version available, launch Download Manager
                if (footer.isOnline()) {
                    Param param = dbAdapter.getParam(1L);
                    String newVersionName = param.getSoftwareVersion();
                    swDownload = new SwDownload(getActivity());
                    progressDialog = new ProgressDialog(getActivity());
                    progressDialog.setMessage(getString(R.string.Communication_en_cours));
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progressDialog.setProgress(0);
                    progressDialog.setMax(100);
                    progressDialog.show();
                    swDownload.downloadSW(newVersionName);
                    Synchro synchro = new Synchro(cDownload);
                    synchro.execute();

                } else {
                    tools.showAlert(getActivity(), getString(R.string.msg_pas_connecte), tools.cWarning);
                }
                break;
            case Messages.cMsgSynchro:
                // No synchronization since the last day, should synchronize
                if (footer.isOnline()) {
                    footer.synchronise(SynchronizationService.cGetPartialWL);
                    Synchro synchro = new Synchro(cSynchro);
                    synchro.execute();
                } else {
                    tools.showAlert(getActivity(), getString(R.string.msg_pas_connecte), tools.cWarning);
                }
                break;

            case Messages.cMsgGeneral:
                // hide the selected message
                msg.setHidden(1);
                dbAdapter.updateMessage(msg);
                mReturnListener.onReturn(Constants.ACTION_IDLE);
                break;

            case Messages.cMsgSupport:
                // access to SAV ticket management clears the selected support message
                dbAdapter.updateSupportfHidden(msg.getId(), 1);
                mReturnListener.onReturn(Constants.ACTION_SAV);
                break;
        }
    }

    /*
     * get Messages to display according to the user profile
     */
    private int getMessages() {
        int nbMsgs = 0;
        try {
            List<Msg> msgs = messages.getMessages(userProfile);
            List<Msg> msgsDwnld = new ArrayList<Msg>();
            List<Msg> msgsStock = new ArrayList<Msg>();
            List<Msg> msgsGeneral = new ArrayList<Msg>();
            List<Msg> msgsSynchro = new ArrayList<Msg>();
            List<Msg> msgsSupport = new ArrayList<Msg>();
            displayedMessages = new ArrayList<Msg>();

            if (msgs.size() > 0) {
                for (Msg msg : msgs) {
                    if (msg.getType() == Messages.cMsgSupport) {
                        msgsSupport.add(msg);
                    } else if (msg.getType() == Messages.cMsgStock) {
                        msgsStock.add(msg);
                    } else if (msg.getType() == Messages.cMsgSWversion) {
                        msgsDwnld.add(msg);
                    } else if (msg.getType() == Messages.cMsgSynchro) {
                        msgsSynchro.add(msg);
                    } else {
                        msgsGeneral.add(msg);
                    }
                }
                // start with support messages
                for (Msg msg : msgsSupport) {
                    nbMsgs++;
                    displayedMessages.add(msg);
                }
                // then with synchro messages
                for (Msg msg : msgsSynchro) {
                     nbMsgs++;
                     displayedMessages.add(msg);
                }
                // then download messages
                for (Msg msg : msgsDwnld) {
                     nbMsgs++;
                     displayedMessages.add(msg);
                }
                // then stock messages
                for (Msg msg : msgsStock) {
                    if (userProfile >= msg.getMinProfile()) {
                        nbMsgs++;
                        displayedMessages.add(msg);
                    }
                }
                // finally general messages
                for (Msg msg : msgsGeneral) {
                    if (userProfile >= msg.getMinProfile()) {
                        nbMsgs++;
                        displayedMessages.add(msg);
                    }
                }
            }

        } catch (Exception ex) {
            // onStopMessages();
        }
        return nbMsgs;
    }

    // ----------------------------- UI synchronization process ------------------------------------
    /*
     * this class is intended to wait for the end of the software download or synchronisation
     */
    private class Synchro extends AsyncTask<Void, Integer, Void>
    {
        private int type;

        public Synchro(int type) {
            this.type = type;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Integer... values){
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            int status;
            int lastProgress, progress, delta;
            switch(type) {
                case cDownload:
                    status = SwDownload.cPending;
                    progress = lastProgress = delta = 0;
                    do {
                        status = swDownload.getDownloadStatus();
                        progress = swDownload.getProgress();
                        if (progress > lastProgress) {
                            delta = progress - lastProgress;
                            lastProgress = progress;
                        }
                        if (delta > 0) {
                            progressDialog.incrementProgressBy(delta);
                            delta = 0;
                        }
                    } while (status == SwDownload.cPending);
                    break;
                case cSynchro:
                    boolean end = false;
                    do {
                        end = (tools.getServiceState(context) != tools.cCommmunicationPending);
                    } while (!end);
                    break;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            switch(type) {
                case cDownload:
                    progressDialog.dismiss();
                    if (swDownload.getDownloadStatus() == SwDownload.cOK) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        Uri uri = Uri.fromFile(new File(swDownload.getApkPath()));
                        intent.setDataAndType(uri, "application/vnd.android.package-archive");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } else {
                        tools.showAlert(getActivity(), getString(R.string.download_error), tools.cError);
                    }
                    break;
                case cSynchro:
                    // return to home activity to refresh messages
                    mReturnListener.onReturn(Constants.ACTION_IDLE);
                    break;
            }
        }
    }

}
