package com.kutarbhargav.moneytracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        android.content.SharedPreferences p = context.getSharedPreferences("reminder", Context.MODE_PRIVATE);
        if (p.getBoolean("enabled", false)) ReminderScheduler.schedule(context, p.getInt("hour", 21), p.getInt("minute", 0));
    }
}
