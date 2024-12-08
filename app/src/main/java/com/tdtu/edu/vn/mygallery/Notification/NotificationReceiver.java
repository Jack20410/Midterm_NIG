package com.tdtu.edu.vn.mygallery.Notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.tdtu.edu.vn.mygallery.MainActivity;
import com.tdtu.edu.vn.mygallery.R;

public class NotificationReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "gallery_reminder_channel";
    private static final String CHANNEL_NAME = "Gallery Reminder";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Create the notification channel
        createNotificationChannel(context);

        // Intent to open MainActivity when the notification is clicked
        Intent openIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_gallery) // Default icon for gallery apps
                .setContentTitle("GALLERY")
                .setContentText("Time to check on your nice pictures!!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Show the notification
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(1, builder.build());
        }
    }

    private void createNotificationChannel(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Reminds users to check their gallery.");
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
