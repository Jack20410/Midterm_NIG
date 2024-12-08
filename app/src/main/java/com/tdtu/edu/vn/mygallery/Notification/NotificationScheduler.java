package com.tdtu.edu.vn.mygallery.Notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class NotificationScheduler {

    public static void scheduleRepeatingNotification(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Schedule the notification to repeat every minute (for demo purposes)
        long interval = AlarmManager.INTERVAL_FIFTEEN_MINUTES / 15; // 1 minute for demo
        long startTime = Calendar.getInstance().getTimeInMillis() + 1000; // Starts in 1 second

        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, startTime, interval, pendingIntent);
        }
    }
}
