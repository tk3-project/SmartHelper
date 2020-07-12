package com.g15.smarthelper.ScenarioHandler;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContentResolver;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.g15.smarthelper.R;
import com.g15.smarthelper.MainActivity;


public class HomeAction extends ContextWrapper {

    private static final String LOG_TAG = "ScenarioHome";
    private static final String CHANNEL_ID = "channel_03";
    private static int notificationId = 1;

    public HomeAction(Context base) {
        super(base);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.v(LOG_TAG, "Creating notification channel for home scenario action.");
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

        try {
            nightMode(getBaseContext());
        } catch (Exception e) {
            Log.e(LOG_TAG, "Could not automatically switch to night mode.", e);
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setColor(getResources().getColor(R.color.colorPrimary))
                .setContentTitle(getString(R.string.home_notification_title))
                .setContentText(getString(R.string.home_notification_text))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(notificationId++, notification);
        Log.i(LOG_TAG, "Home action notification sent.");
    }

    public void nightMode(Context context) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(context)) {
                Log.i(LOG_TAG, "Switching to night mode.");
                setAutoBrightness(context, false);

                int currentBrightness = getScreenBrightness(context);
                int newBrightness = currentBrightness > 60 ? 12 : (int) (currentBrightness * 0.2);

                setBrightness(context, newBrightness);
            } else {
                Log.w(LOG_TAG, "Access on settings not granted.");
            }
        }
    }

    public void dayMode(Context context) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(context)) {
                Log.i(LOG_TAG, "Switching to day mode.");
                ContentResolver cResolver = context.getContentResolver();
                int maxBrightness = 255;
                setAutoBrightness(context, true);
                setBrightness(context, maxBrightness);
            } else {
                Log.w(LOG_TAG, "Access on settings not granted.");
            }
        }
    }

    private static int getScreenBrightness(Context context) {
        try {
            return Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(LOG_TAG, "Could not get screen brightness settings.", e);
        }
        return 0;
    }

    public static void setBrightness(Context context, int brightness) {
        if (brightness < 0 || brightness > 255) {
            Log.e(LOG_TAG, "Could not set brightness to invalid value " + brightness + ". " +
                    "Only values in range [0,255] are permitted.");
            return;
        }
        ContentResolver cResolver = context.getContentResolver();
        Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
    }

    public static void setAutoBrightness(Context context, boolean autoBrightness) {
        ContentResolver contentResolver = context.getContentResolver();
        int brightnessMode = autoBrightness
                ? Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
                : Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
        Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, brightnessMode);
    }
}
