package com.kutarbhargav.moneytracker;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

public class ReminderReceiver extends BroadcastReceiver {
    public static final String CHANNEL_ID = "money_tracker_daily";
    @Override public void onReceive(Context context, Intent intent) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel(CHANNEL_ID, context.getString(com.kutarbhargav.moneytracker.R.string.notification_channel_name), NotificationManager.IMPORTANCE_DEFAULT);
            ch.setDescription(context.getString(com.kutarbhargav.moneytracker.R.string.notification_channel_description));
            nm.createNotificationChannel(ch);
        }
        if (Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return;

        Intent open = new Intent(context, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi = PendingIntent.getActivity(context, 19022, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        android.app.Notification.Builder b = Build.VERSION.SDK_INT >= 26
                ? new android.app.Notification.Builder(context, CHANNEL_ID)
                : new android.app.Notification.Builder(context);
        b.setSmallIcon(R.drawable.ic_notification)
         .setContentTitle("Money Tracker")
         .setContentText("Don't forget to add today's expenses.")
         .setAutoCancel(true)
         .setContentIntent(pi)
         .setCategory(android.app.Notification.CATEGORY_REMINDER)
         .setPriority(android.app.Notification.PRIORITY_DEFAULT);
        nm.notify(19023, b.build());

        android.content.SharedPreferences p = context.getSharedPreferences("reminder", Context.MODE_PRIVATE);
        if (p.getBoolean("enabled", false)) {
            ReminderScheduler.schedule(context, p.getInt("hour", 21), p.getInt("minute", 0));
        }
    }
}
