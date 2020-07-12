package com.g15.smarthelper.ScenarioHandler;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.UiModeManager;
import android.content.Context;
import android.content.ContentResolver;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;
import android.net.Uri;
import android.provider.Settings;
import android.view.WindowManager;

import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.g15.smarthelper.Constants;
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

    public void NightMode(Context context, Activity activity){
        int newBrightness = -1;
        if (isAutoBrightness(context)){
            autoBrightness(context, false);
        }
        int currentBrightness = getScreenBrightness(context);

        if (currentBrightness > 60) {
            newBrightness = 5;
        } else {
            newBrightness = (int) (currentBrightness * 0.08);
        }
        setBrightness(activity, newBrightness);
        saveBrightness(context, newBrightness);
        Log.i(LOG_TAG, "Switched to Night Mode.");
    }

    public void DayMode(Context context){
        if (!isAutoBrightness(context)){
            autoBrightness(context, true);
        }
        Log.i(LOG_TAG, "Switched to Day Mode.");
    }

    /**
     * check if brightness mode is automatic.
     * @param context
     * @return true if turned on.
     */

    private static boolean isAutoBrightness(Context context) {
        boolean autoBrightness = false;
        try {
            autoBrightness = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return autoBrightness;
    }

    /**
     * get current system brightness
     * @param context
     * @return the current system brightness
     */

    private static int getScreenBrightness(Context context) {
        int currentBrightness = 0;
        try {
            currentBrightness = Settings.System.
                    getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return currentBrightness;
    }

    public static void setBrightness(Activity activity, int brightness) {
        WindowManager.LayoutParams layoutParams = activity.getWindow().getAttributes();
        float newBrightness = Float.valueOf(brightness) * (1f / 255f);
        Log.i(LOG_TAG, "New Brightness: " + newBrightness);
        layoutParams.screenBrightness = newBrightness;
        activity.getWindow().setAttributes(layoutParams);
    }

    /**
     * turn on or turn off the automatic mode
     * @param context
     * @param flag
     * @return true if turn on; false if turn off.
     */

    public static boolean autoBrightness(Context context, boolean flag){
        int value = 0;
        if (flag) {
            value = Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        } else {
            value = Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;
        }
        return Settings.System.putInt(context.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE, value);
    }

    public static void saveBrightness(Context context, int brightness) {
        ContentResolver resolver = context.getContentResolver();
        Uri uri = android.provider.Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS);
        android.provider.Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
        resolver.notifyChange(uri, null);
    }
}
