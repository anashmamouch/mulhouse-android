/**
 ================================================================================

 OTIPASS
 swdownload package

 @author ED ($Author: ede $)

 @version $Rev: 6352 $
 $Id: SwDownload.java 6352 2016-06-10 15:53:06Z ede $

 ================================================================================
 */
package com.otipass.swdownload;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import models.Constants;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import com.otipass.sql.DbAdapter;
import com.otipass.sql.Param;

import javax.net.ssl.HttpsURLConnection;

public class SwDownload {
	public static final int cPending = 1;
	public static final int cOK = 2;
	public static final int cError = 3;
	private int statusDwnld;
	private String apkPath;
	private Context context;
	long fileSize, lngReceived;
	int progress;
	HttpsURLConnection conn;

	public SwDownload(Context context) {
		this.context = context;
	}
	
	public String getApkPath() {
		return apkPath;
	}
	
	public int getProgress() {
		return progress;
	}
	
	private void download(final String urlToDownload) {
		statusDwnld = cPending;
		new Thread() {
			@Override
			public void run() {
				try {
					progress = 0;
					lngReceived = 0;
					URL url = new URL(urlToDownload);
					URLConnection connection = url.openConnection();
					connection.setRequestProperty("Accept-Encoding", "identity");
					final int fileSize = connection.getContentLength();
					connection.connect();
					// download the file
					if (fileSize > 0) {
						InputStream input = new BufferedInputStream(url.openStream());
						OutputStream output = new FileOutputStream(apkPath);

						byte data[] = new byte[4096];
						int count;
						while ((count = input.read(data)) != -1) {
							output.write(data, 0, count);
							lngReceived += count;
							progress = (int)(lngReceived * 100 / fileSize) ;
						}
						output.flush();
						output.close();
						input.close();
						File file = new File(apkPath);
						if (file.exists()) {
							statusDwnld = cOK;
						} else {
							statusDwnld = cError;
						}
					} else {
						statusDwnld = cError;
						Log.e(Constants.TAG, "Filesize:"+fileSize);
					}
				} catch (Exception e) {
					statusDwnld = cError;
					Log.e(Constants.TAG, e.getMessage());
				}
			}
		}.start();

	}

    public int getDownloadStatus() {
    	return statusDwnld;
    }
    
    public void downloadSW(String newVersionName) {
		String v = newVersionName.replace(".", "");
		String file = Constants.PROJECT + "_v"+ v + ".apk";
		apkPath = Environment.getExternalStorageDirectory().toString() + "/download/" + file;
		String url = "https://" + Constants.plateform + ".otipass.net/mobile/" + Constants.PROJECT + "/apk/" + file;
		Log.d(Constants.TAG, "url:"+url);
		download(url);
    }

    public static boolean detectNewSoftwareVersion(Context context, DbAdapter dbAdapter) {
    	boolean newVersion = false;
		try {
			String currentVersionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
			Param param = dbAdapter.getParam(1L);
			String newVersionName = param.getSoftwareVersion();
			float f1 = Float.valueOf(currentVersionName);
			float f2 = Float.valueOf(newVersionName);
			newVersion = f2 > f1;
		} catch (Exception e) {
			Log.e(Constants.TAG, "SwDownload.detectSoftwareDownload() -" + e.getMessage());
		}
    	return newVersion;
    }
}
