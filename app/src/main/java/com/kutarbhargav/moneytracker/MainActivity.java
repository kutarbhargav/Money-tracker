package com.kutarbhargav.moneytracker;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.webkit.WebViewAssetLoader;

public class MainActivity extends Activity {
    private WebView webView;
    private static final int NOTIFICATION_REQ = 701;
    private WebViewAssetLoader assetLoader;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createChannel();

        assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .build();

        webView = new WebView(this);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        webView.setWebViewClient(new WebViewClient() {
            @Override public android.webkit.WebResourceResponse shouldInterceptRequest(WebView view, android.webkit.WebResourceRequest request) {
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }
        });
        webView.addJavascriptInterface(new AndroidBridge(this), "Android");
        setContentView(webView);
        webView.loadUrl("https://appassets.androidplatform.net/assets/index.html");
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager nm = getSystemService(NotificationManager.class);
            nm.createNotificationChannel(new NotificationChannel(
                    ReminderReceiver.CHANNEL_ID,
                    getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT));
        }
    }

    public void requestNotifications() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_REQ);
        }
    }

    public void schedule(int hour, int minute) {
        getSharedPreferences("reminder", MODE_PRIVATE).edit().putBoolean("enabled", true).putInt("hour", hour).putInt("minute", minute).apply();
        requestNotifications();
        if (Build.VERSION.SDK_INT >= 31) {
            AlarmManager am = getSystemService(AlarmManager.class);
            if (!am.canScheduleExactAlarms()) {
                try { startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:" + getPackageName()))); } catch (Exception ignored) {}
            }
        }
        ReminderScheduler.schedule(this, hour, minute);
    }

    public void cancel() {
        getSharedPreferences("reminder", MODE_PRIVATE).edit().putBoolean("enabled", false).apply();
        ReminderScheduler.cancel(this);
    }

    public void testNotification() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestNotifications();
            return;
        }
        NotificationManager nm = getSystemService(NotificationManager.class);
        Intent open = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 19024, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder b = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(this, ReminderReceiver.CHANNEL_ID) : new Notification.Builder(this);
        b.setSmallIcon(R.drawable.ic_notification).setContentTitle("Money Tracker").setContentText("Test notification — your reminders are working.").setAutoCancel(true).setContentIntent(pi);
        nm.notify(19025, b.build());
    }

    public class AndroidBridge {
        private final MainActivity a;
        AndroidBridge(MainActivity a){this.a=a;}
        @JavascriptInterface public void enableReminder(int hour, int minute){a.runOnUiThread(() -> a.schedule(hour, minute));}
        @JavascriptInterface public void disableReminder(){a.runOnUiThread(a::cancel);}
        @JavascriptInterface public void updateReminder(int hour, int minute){a.runOnUiThread(() -> a.schedule(hour, minute));}
        @JavascriptInterface public void testNotification(){a.runOnUiThread(a::testNotification);}
    }

    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }
}
