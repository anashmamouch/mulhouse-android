package com.otipass.synchronization;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import models.Constants;

public class AlarmReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Log.i(Constants.TAG, "OnReceive AlarmReceiver");
            Bundle extras = intent.getExtras();
            String alarmType = extras.getString(Constants.WAKE_UP_STR);
            if (alarmType != null) {
                SynchronizationService synchro = new SynchronizationService(context);
                if (alarmType.equals(Constants.ALARM_DOWNLOAD_STR)) {
                    synchro.start(context, Constants.DO_NIGHT_DOWNLOAD);
                } else if (alarmType.equals(Constants.ALARM_UPLOAD_STR)) {
                    synchro.start(context, Constants.DO_NIGHT_UPLOAD);
                }
                Log.i(Constants.TAG, "Call:" + alarmType);
            }
        } catch (Exception ex) {Log.e(Constants.TAG, AlarmReceiver.class.getName() + " - onReceive -" + ex.getMessage());}
    }
}