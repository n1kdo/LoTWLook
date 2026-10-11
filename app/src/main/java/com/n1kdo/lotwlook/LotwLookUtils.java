package com.n1kdo.lotwlook;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;

import com.n1kdo.adif.AdifRecord;
import com.n1kdo.lotwlook.data.LoTWLookDAO;

import java.util.Date;
import java.util.List;

public class LotwLookUtils {
    private static final String TAG = LotwLookUtils.class.getSimpleName();
    private static final String NOTIFICATION_CHANNEL_ID = "LOTWLOOK_NTF_CHN_ID";

    static boolean isOnline(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connMgr == null) {
            return false;
        }
        Network network = connMgr.getActiveNetwork();
        if (network == null) {
            return false;
        }
        NetworkCapabilities capabilities = connMgr.getNetworkCapabilities(network);
        return (capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET));
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
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setSilent(true)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

        // minSdk is 28, so the NotificationChannel API (added in API 26) is always available.
        String name = context.getResources().getString(R.string.app_name);
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
        channel.setDescription("LoTWLook new QSL");
        notificationManager.createNotificationChannel(channel);
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
            // callers pass the records newest-first, so the last element is the OLDEST of the
            // batch; that's the record we use to compute how far back to keep.
            AdifRecord oldestNewRecord = newAdifRecords.get(newQsls - 1);
            long oldestRecordId = oldestNewRecord.getId() - maxDatabaseEntries;
            if (oldestRecordId > 0) {
                dao.deleteOldAdifRecords(oldestRecordId);
            }
        }
        dao.close();

        // Deliberately only advance the stored "since" date when at least one NEW record was
        // inserted. If every returned record was a duplicate, re-querying the same window next
        // time is cheap and idempotent, whereas advancing past records we didn't keep could
        // cause confirmations to be missed. (Callers already skip this method entirely when
        // the returned list is empty.)
        if (newQsls != 0) {
            PreferencesActivity.updateLastQslDate(context, lastQslDate.getTime());
        }

        return newQsls;
    } // updateDatabase()

}
