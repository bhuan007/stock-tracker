package com.example.stocktracker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import java.util.Calendar;

public class AlarmHelper {
    private static final String TAG = "AlarmHelper";



    public static void initAlarm(Context context, int id, boolean isExist, long triggermillis, long intervalMillis) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlertReceiver.class);
        intent.putExtra("id", id);
        intent.putExtra("isExist", isExist);
        PendingIntent pendingIntent;
        if (isExist) {
            pendingIntent = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        else {
            pendingIntent = PendingIntent.getBroadcast(context, id, intent, 0);
        }


        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggermillis , intervalMillis, pendingIntent);
//        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggermillis, pendingIntent);
        Log.d(TAG, "initAlarm: Alarm set for " + triggermillis);
    }

    public static void cancelAlarm(Context context, int id ) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, intent, 0);

        alarmManager.cancel(pendingIntent);
        Log.d(TAG, "Alarm cancelled for id " + id);
    }

}
