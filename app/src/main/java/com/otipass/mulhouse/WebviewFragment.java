/**
 ================================================================================

 OTIPASS
 reims main package

 @author ED ($Author: ede $)

 @version $Rev: 6371 $
 $Id: WebviewFragment.java 6371 2016-06-14 15:04:03Z ede $

 ================================================================================
 */
package com.otipass.mulhouse;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.otipass.sql.DbAdapter;
import com.otipass.sql.User;
import com.otipass.tools.Callback;
import com.otipass.tools.ExceptionHandler;
import com.otipass.tools.tools;

import org.apache.http.util.EncodingUtils;

import java.net.URL;

import models.Constants;


public class WebviewFragment extends android.app.Fragment {
    private static final String TAG = Constants.WEBVIEW_TAG;
    private int idUser = 0;
    public WebviewFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_webview, container, false);
        final RelativeLayout l1 = (RelativeLayout) rootView.findViewById(R.id.loadingPanel);
        String webAction="", action="", controller="", login="", pwd="", postData="";
        boolean sendDeviceId = false;


        // webView
        WebView wv = (WebView) rootView.findViewById(R.id.webView);
        WebSettings webSettings = wv.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        wv.setWebChromeClient(new WebChromeClient());
        wv.setWebViewClient(new WebViewClient());

        wv.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                // do your stuff here
                l1.setVisibility(RelativeLayout.GONE);
            }
        });
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            wv.setWebContentsDebuggingEnabled(true);
        }
        wv.setWebChromeClient(new WebChromeClient() {
            public boolean onConsoleMessage(ConsoleMessage cm) {
                Log.d(Constants.WEBVIEW_TAG, cm.message() + " -- From line "
                        + cm.lineNumber() + " of "
                        + cm.sourceId() );
                return true;
            }
        });
        Bundle bundle = getArguments();
        if (bundle != null) {
            idUser = bundle.getInt("user");
            webAction = bundle.getString("action");
        }
        if ((webAction != null) && (webAction.length() > 0)) {
            if (webAction.equals(Constants.STATS_ENTRY_WV)) {
                controller = "entry";
                action = "list";
                sendDeviceId = true;
            } else if (webAction.equals(Constants.STATS_SALE_WV)) {
                controller = "orderpos";
                action = "stat";
            } else if (webAction.equals(Constants.STOCK_WV)) {
                controller = "stockcommand";
                action = "add";
            } else if (webAction.equals(Constants.REQUEST_WV)) {
                controller = "request";
                action = "list";
            }
            DbAdapter dbAdapter = new DbAdapter(getActivity());
            dbAdapter.open();

            User user = dbAdapter.getUser((long)idUser);
            if (user != null) {
                pwd = user.getPassword();
                login = user.getUserid();
                postData = "userid=" + login + "&password_=" + pwd + "&controller_=" + controller + "&action_=" + action;
                if (sendDeviceId) {
                    postData += "&device_id="+ tools.getDeviceId(getActivity());
                }
                wv.postUrl("https://" + Constants.plateform + Constants.URL_WEB_ACCESS, postData.getBytes());
            }

        }
        return rootView;

    }

}
