/**
 ================================================================================

 OTIPASS
 synchronization package

 @author ED ($Author: ede $)

 @version $Rev: 6452 $
 $Id: SynchroAlarm.java 6452 2016-07-07 13:59:44Z ede $

 ================================================================================
 */
package com.otipass.synchronization;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.otipass.tools.tools;

import models.Constants;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.widget.Toast;

public class SynchroAlarm {

	private static String[] splitTime(String time) {
		String sTime[] = null;
		try {
			sTime = time.split(":");
		} catch(Exception e) {}
		return sTime;
	}

	private static int convertTime(String time) {
		int convert = 0;
		try {
			String[] sTime = time.split(":");
			convert = (Integer.valueOf(sTime[0]) * 3600) + (Integer.valueOf(sTime[1]) * 60) + Integer.valueOf(sTime[2]); 
		} catch(Exception e) {

		}
		return convert;
	}

	public static void setAlarm(Context context, String callingTime) {
        Calendar firstTime = Calendar.getInstance();
        Calendar now = Calendar.getInstance();
		String sTime[];
		try {
			sTime = splitTime(callingTime);
			firstTime.set(Calendar.HOUR_OF_DAY, Integer.valueOf(sTime[0]));
			firstTime.set(Calendar.MINUTE, Integer.valueOf(sTime[1]));
			firstTime.set(Calendar.SECOND, Integer.valueOf(sTime[2]));
			firstTime.set(Calendar.MILLISECOND, 0);
            if (firstTime.before(now)) {
                firstTime.add(Calendar.HOUR, 24);
            }

            AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
            Intent intent1 = new Intent(context, AlarmReceiver.class);
            intent1.putExtra(Constants.WAKE_UP_STR, Constants.ALARM_UPLOAD_STR);
            PendingIntent p1 = PendingIntent.getBroadcast(context, 1, intent1, PendingIntent.FLAG_CANCEL_CURRENT);
			// 1st alarm is to upload entries and updates
            //am.setRepeating(AlarmManager.RTC_WAKEUP, firstTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY, p1);
            am.cancel(p1);
            am.setExact(AlarmManager.RTC_WAKEUP, firstTime.getTimeInMillis(), p1);
            Log.i(Constants.TAG, "Start 1st alarm: " + tools.formatSQLDate(firstTime));
			Intent intent2 = new Intent(context, AlarmReceiver.class);
			intent2.putExtra(Constants.WAKE_UP_STR, Constants.ALARM_DOWNLOAD_STR);
			PendingIntent p2 = PendingIntent.getBroadcast(context, 2, intent2, PendingIntent.FLAG_CANCEL_CURRENT);
			// 2nd alarm is to get the WL, user, param
			// it is set 3 h later than the 1st one
			long delay = (long)(3 * 3600 * 1000);
            am.cancel(p2);
            am.setExact(AlarmManager.RTC_WAKEUP, firstTime.getTimeInMillis() + delay, p2);
//			am.setRepeating(AlarmManager.RTC_WAKEUP, firstTime.getTimeInMillis() + delay, AlarmManager.INTERVAL_DAY, p2);
			Calendar newCal = Calendar.getInstance();
			newCal.setTimeInMillis(firstTime.getTimeInMillis() + delay);
			Log.i(Constants.TAG, "Start 2nd alarm: " + tools.formatSQLDate(newCal));
		} catch (Exception ex) {
			Log.e(Constants.TAG, "SynchroAlarm.setAlarm() " + ex.getMessage());
		}
	}
	

}
