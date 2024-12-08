package com.tdtu.edu.vn.mygallery.Notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d("BootCompletedReceiver", "Boot completed detected, rescheduling notifications");
            NotificationScheduler.scheduleRepeatingNotification(context);
        }
    }
}
