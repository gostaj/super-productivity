
package com.superproductivity.superproductivity;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class KeepAliveNotificationService extends Service {
    private static final int NOTIFY_ID = 1;
    private static final String NOTIFY_CHANNEL_ID = "SUP_KeepAlive";
    private NotificationManager notificationManager;
    private final NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
            NOTIFY_CHANNEL_ID);
    public static final String UPDATE_PERMANENT_NOTIFICATION = "com.superproductivity.superproductivity.UPDATE_PERMANENT_NOTIFICATION";

    BroadcastReceiver receiver;


    // use this as an inner class like here or as a top-level class
    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.w("TW", "KeepAliveService: onReceive");
            String action = intent.getAction();
            if (action.equals(UPDATE_PERMANENT_NOTIFICATION)) {
                //action for sms received
                String title = intent.getStringExtra("title");
                String message = intent.getStringExtra("message");
                int progress = intent.getIntExtra("progress", -1);
                boolean isNotify = intent.getBooleanExtra("notify", false);
                KeepAliveNotificationService.this.updateNotification(title, message, progress);
                Log.w("TW", "KeepAliveService: onReceive: " + title + "||" + message);
            }
        }
    }

    @Override
    public void onCreate() {
        // get an instance of the receiver in your service
        IntentFilter filter = new IntentFilter();
        filter.addAction(UPDATE_PERMANENT_NOTIFICATION);
        receiver = new MyReceiver();
        registerReceiver(receiver, filter);
    }

    @Override
    public void onDestroy() {
        Log.w("TW", "KeepAliveService: onDestroy");
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.w("TW", "KeepAliveService: Start");

        startForeground();
        return super.onStartCommand(intent, flags, startId);
    }


    public void updateNotification(String title, String message, int progress) {
        if (progress == 999) {
            builder.setSmallIcon(R.drawable.ic_stat_sync);
            builder.setProgress(100, progress, true);
        } else if (progress == 333) {
            builder.setSmallIcon(R.drawable.ic_stat_play);
            builder.setProgress(0, 0, false);
        } else if (progress > -1) {
            builder.setSmallIcon(R.drawable.ic_stat_play);
            builder.setProgress(100, progress, false);
        } else {
            builder.setSmallIcon(R.drawable.ic_stat_sp);
            builder.setProgress(0, 0, false);
        }
        builder.setContentText(message)
                .setContentTitle(title);
        if (title.equals("")) {
            builder.setContentTitle(null);
        }
        if (message.equals("")) {
            builder.setContentText(null);
        }

        notificationManager.notify(NOTIFY_ID, builder.build());
    }

    private void startForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // create notification channel
            String description = "Here to keep the app alive for syncing";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(NOTIFY_CHANNEL_ID, NOTIFY_CHANNEL_ID, importance);
            channel.setDescription(description);
            notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            // create notification
            Intent notificationIntent = new Intent(this, FullscreenActivity.class);

            PendingIntent pendingIntent = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                pendingIntent = PendingIntent.getActivity(this, 0,
                        notificationIntent, PendingIntent.FLAG_MUTABLE);
            } else {
                pendingIntent = PendingIntent.getActivity(this, 0,
                        notificationIntent, 0);
            }


            Notification notification = builder
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .setSmallIcon(R.drawable.ic_stat_sp)
                    .setContentText("Service is running background")
                    .setContentIntent(pendingIntent)
                    .build();

            startForeground(NOTIFY_ID, notification);
        }
    }
}
