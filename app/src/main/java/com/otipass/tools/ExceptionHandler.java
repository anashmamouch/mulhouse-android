/**
 ================================================================================

 OTIPASS
 tools package

 @author ED ($Author: ede $)

 @version $Rev: 6358 $
 $Id: DbAdapter.java 6358 2016-06-13 09:54:21Z ede $

 ================================================================================
 */
package com.otipass.tools;

import java.io.PrintWriter;
import java.io.StringWriter;

import models.Constants;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;

import com.otipass.mulhouse.BugMsgActivity;
import com.otipass.sql.Bug;
import com.otipass.sql.DbAdapter;
import com.otipass.sql.Param;

public class ExceptionHandler implements java.lang.Thread.UncaughtExceptionHandler {
	private final Activity context;
	private final String LINE_SEPARATOR = "\n";
	private DbAdapter dbAdapter;

	public ExceptionHandler(Activity context) {
		this.context = context;
	}

	public void uncaughtException(Thread thread, Throwable exception) {
		
		dbAdapter = new DbAdapter(context);
		dbAdapter.open();
		
		Param param = dbAdapter.getParam(1L);
		
		StringWriter stackTrace = new StringWriter();
		exception.printStackTrace(new PrintWriter(stackTrace));
		StringBuilder errorReport = new StringBuilder();
		errorReport.append("************ CAUSE OF ERROR ************\n\n");
		errorReport.append(stackTrace.toString());

		errorReport.append("\n************ DEVICE INFORMATION ***********\n");

		errorReport.append("Model: ");
		errorReport.append(Build.MODEL);
		errorReport.append(LINE_SEPARATOR);
		errorReport.append("Serial: ");
		errorReport.append(tools.getDeviceUID(context));
		errorReport.append(LINE_SEPARATOR);
		errorReport.append("Provider: ");
		errorReport.append(param.getName());
		errorReport.append(LINE_SEPARATOR);
		String now = tools.formatNow(Constants.SQL_FULL_DATE_FORMAT);
		dbAdapter.insertBug(new Bug(errorReport.toString(), now));

		Intent intent = new Intent(context, BugMsgActivity.class);
		intent.putExtra("error", errorReport.toString());
		context.startActivity(intent);
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(10);
	}

}