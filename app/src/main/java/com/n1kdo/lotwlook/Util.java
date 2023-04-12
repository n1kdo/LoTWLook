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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import com.n1kdo.adif.AdifRecord;
import com.n1kdo.lotwlook.data.LoTWLookDAO;

import java.util.Date;
import java.util.List;

public class Util {
    private static final String TAG = Util.class.getSimpleName();
    private static final String NOTIFICATION_CHANNEL_ID = "LOTWLOOK_NTF_CHN_ID";

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
        // see https://developer.android.com/develop/ui/views/notifications/build-notification

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String name = context.getResources().getString(R.string.app_name);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription("LoTWLook new QSL");
            notificationManager.createNotificationChannel(channel);
        }
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
