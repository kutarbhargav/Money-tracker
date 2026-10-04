package com.kutarbhargav.moneytracker;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
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
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import java.io.IOException;
import java.io.InputStream;
import android.widget.Toast;

public class MainActivity extends Activity {
    private WebView webView;
    private static final int NOTIFICATION_REQ = 701;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createChannel();

        webView = new WebView(this);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);

        webView.setWebViewClient(new WebViewClient() {
            @Override public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return loadAsset(request.getUrl().getPath());
            }
            @Override public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                try { return loadAsset(Uri.parse(url).getPath()); } catch (Exception e) { return null; }
            }
            @Override public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(MainActivity.this, "Unable to load Money Tracker", Toast.LENGTH_LONG).show();
            }
        });
        webView.addJavascriptInterface(new AndroidBridge(this), "Android");
        setContentView(webView);
        webView.loadUrl("https://moneytracker.local/index.html");
    }

    private WebResourceResponse loadAsset(String path) {
        if (path == null || path.isEmpty()) return null;
        String assetPath = path.startsWith("/") ? path.substring(1) : path;
        if (assetPath.isEmpty()) assetPath = "index.html";
        try {
            InputStream in = getAssets().open(assetPath);
            String mime = "text/plain";
            String lower = assetPath.toLowerCase();
            if (lower.endsWith(".html")) mime = "text/html";
            else if (lower.endsWith(".css")) mime = "text/css";
            else if (lower.endsWith(".js")) mime = "application/javascript";
            else if (lower.endsWith(".svg")) mime = "image/svg+xml";
            else if (lower.endsWith(".json") || lower.endsWith(".webmanifest")) mime = "application/json";
            return new WebResourceResponse(mime, "UTF-8", in);
        } catch (IOException e) { return null; }
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
        getSharedPreferences("reminder", MODE_PRIVATE).edit()
                .putBoolean("enabled", true).putInt("hour", hour).putInt("minute", minute).apply();
        requestNotifications();
        if (Build.VERSION.SDK_INT >= 31) {
            AlarmManager am = getSystemService(AlarmManager.class);
            if (!am.canScheduleExactAlarms()) {
                try { startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                        Uri.parse("package:" + getPackageName()))); } catch (Exception ignored) {}
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
            Toast.makeText(this, "Allow notifications, then tap Test again.", Toast.LENGTH_LONG).show();
            return;
        }
        NotificationManager nm = getSystemService(NotificationManager.class);
        Intent open = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 19024, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder b = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(this, ReminderReceiver.CHANNEL_ID)
                : new Notification.Builder(this);
        b.setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Money Tracker")
                .setContentText("Test notification — your reminders are working.")
                .setAutoCancel(true).setContentIntent(pi);
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
