package com.n1kdo.lotwlook;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;

import com.n1kdo.adif.AdifRecord;
import com.n1kdo.lotwlook.data.LoTWLookDAO;

import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class Util {
    private static final String TAG = Util.class.getSimpleName();
    private static final String NOTIFICATION_CHANNEL_ID = "LOTWLOOK_NTF_CHN_ID";

    static boolean isItMe(String username) {
        String[] parts = Util.class.getName().split(Pattern.quote("."));
        return parts.length > 2 && parts[1].equals(username);
    }

    static boolean isOnline(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connMgr == null) {
            return false;
        }
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    static void alert(Activity activity, CharSequence message) {
        AlertDialog.Builder adb = new AlertDialog.Builder(activity);
        adb.setMessage(message);
        adb.setNeutralButton("OK", null);
        adb.setIcon(android.R.drawable.ic_dialog_alert);
        adb.create().show();
    }

    private static final int[] bkgResList = {
            R.drawable.el,
            R.drawable.ol,
            R.drawable.bel,
            R.drawable.bol,
            R.drawable.er,
            R.drawable.or,
            R.drawable.ber,
            R.drawable.bor};

    static TextView textViewForTable(final Context context, CharSequence s, int displayMode, int color) {
        TextView textView = new TextView(context);
        textView.setGravity(Gravity.NO_GRAVITY);
        textView.setPadding(2, 2, 2, 2);

        displayMode = displayMode & 0x07;
        textView.setBackgroundResource(bkgResList[displayMode]);

        textView.setText(s);
        textView.setTextColor(color);
        return textView;
    }

    static void createNotification(Context context, CharSequence title, CharSequence message) {
        Log.d(TAG, "createNotification(\"" + title + "\", \"" + message + "\")");

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID);
            if (notificationChannel == null) {
                String appName = context.getResources().getString(R.string.app_name);
                notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, appName, NotificationManager.IMPORTANCE_LOW);
                notificationChannel.setDescription("LoTWLook new QSL");
                notificationChannel.enableLights(false);
                notificationChannel.enableVibration(false);
                notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setContentTitle(title).setContentText(message);
        builder.setOnlyAlertOnce(true);
        builder.setPriority(NotificationCompat.PRIORITY_LOW);
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(context, MainActivity.class);

        // The stack builder object will contain an artificial back stack for
        // the started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);

        builder.setAutoCancel(true);
        int notificationId = 0; // ID for this app.  there is only one.
        notificationManager.notify(notificationId, builder.build());
    } // createNotification()

    public static int updateDatabase(ContextWrapper context, Date lastQslDate, List<AdifRecord> adifRecordsList, int maxDatabaseEntries) {
        int newQsls;
        LoTWLookDAO dao = new LoTWLookDAO(context);
        dao.open();

        List<AdifRecord> newAdifRecords = dao.updateDatabase(adifRecordsList);

        newQsls = newAdifRecords.size();
        if (newQsls != 0) {
            AdifRecord newestAdifRecord = newAdifRecords.get(newQsls - 1);
            long oldestRecordId = newestAdifRecord.getId() - maxDatabaseEntries;
            if (oldestRecordId > 0) {
                dao.deleteOldAdifRecords(oldestRecordId);
            }
        }
        dao.close();

        PreferencesActivity.updateLastQslDate(context, lastQslDate.getTime());

        return newQsls;
    } // updateDatabase()

}
