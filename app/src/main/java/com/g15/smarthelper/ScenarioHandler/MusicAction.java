package com.g15.smarthelper.ScenarioHandler;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.net.Uri;

import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.g15.smarthelper.R;


public class MusicAction extends ContextWrapper {

    private static final String LOG_TAG = "ScenarioMusic";
    private static final String CHANNEL_ID = "channel_02";
    private static int notificationId = 1;

    public MusicAction(Context base) {
        super(base);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            String description = getString(R.string.description);

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(description);
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.createNotificationChannel(channel);
        }
    }

    public void sendNotification() {
        createNotificationChannel();
        Intent intent = openMusicApp();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setColor(getResources().getColor(R.color.colorPrimary))
                .setContentTitle(getString(R.string.music_notification_title))
                .setContentText(getString(R.string.music_notification_text))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(notificationId++, notification);
        Log.i(LOG_TAG, "Notification is sent.");
    }

    public Intent openMusicApp() {
        Intent launchIntent;

        try {
            getPackageManager().getPackageInfo("com.spotify.music", 0);
            launchIntent = new Intent(Intent.ACTION_VIEW);
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            launchIntent.setData(Uri.parse("spotify:playlist:4cgeOaRCHDkVDQPaDrRQFR:play"));
            launchIntent.putExtra(Intent.EXTRA_REFERRER,
                    Uri.parse("android-app://" + this.getPackageName()));
            Log.i(LOG_TAG, "Spotify opened.");

        } catch (PackageManager.NameNotFoundException e) {
            launchIntent = new Intent("android.intent.action.MUSIC_PLAYER");
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Log.i(LOG_TAG, "Please open other music players.");
        }
        startActivity(launchIntent);
        return launchIntent;
    }
}
