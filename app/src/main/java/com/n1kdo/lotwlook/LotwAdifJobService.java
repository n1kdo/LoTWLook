package com.n1kdo.lotwlook;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.Log;

import com.n1kdo.adif.AdifRecord;
import com.n1kdo.adif.AdifResult;
import com.n1kdo.adif.AdifResultException;
import com.n1kdo.lotw.QueryLoTW;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by Jeff on 2/4/2018.
 * Oreo fixes for power management.  no longer starting a new intent to get QSLs in background.
 */

public class LotwAdifJobService extends JobService {
    private static final String TAG = LotwAdifJobService.class.getSimpleName();

    @Override
    public final boolean onStartJob(JobParameters jobParameters) {
        Log.d(TAG, "onStartJob()");
        Context context = getApplicationContext();
        if (Util.isOnline(context)) {
            LoTWQueryThread queryThread = new LoTWQueryThread(this, jobParameters);
            Log.d(TAG, "starting queryThread");
            queryThread.start();
        } else {
            jobFinished(jobParameters, false); // otherwise this is called in the queryThread.
        }
        return true;
    }

    private static class LoTWQueryThread extends Thread {
        private final JobParameters jobParameters;
        private final LotwAdifJobService service;

        private LoTWQueryThread(LotwAdifJobService service, JobParameters jobParameters) {
            this.service = service;
            this.jobParameters = jobParameters;
        }

        public final void run() {
            Log.d(TAG, "calling checkLoTW()");
            service.checkLoTW();
            Log.d(TAG, "returned from checkLoTW()");
            service.jobFinished(jobParameters, false);
        }
    }

    @Override
    public final boolean onStopJob(JobParameters params) {
        return true;
    }

    private void checkLoTW() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        getSharedPreferences(PreferencesActivity.PREFERENCES_KEY, MODE_PRIVATE);

        String username = sharedPreferences.getString(PreferencesActivity.USERNAME_KEY, "");
        String owncall = sharedPreferences.getString(PreferencesActivity.OWNCALL_KEY, "");
        String password = sharedPreferences.getString(PreferencesActivity.PASSWORD_KEY, "");

        if (username.length() == 0 || password.length() == 0) {
            // no credentials.
            Log.i(TAG, "Missing credentials!");
            Util.createNotification(this, "Update problem...", "LoTW credentials missing");
            return;
        }

        long lastQslSeen = sharedPreferences.getLong(PreferencesActivity.LAST_SEEN_QSL_KEY, 0L);
        int maxDatabaseEntries = Integer
                .valueOf(sharedPreferences.getString(PreferencesActivity.MAX_DATABASE_ENTRIES_KEY, "100"));

        long lastQslDateLong = sharedPreferences.getLong(PreferencesActivity.LAST_QSL_DATE_KEY, 0L);

        if (lastQslDateLong == 0) { // really don't want to search from the beginning of time
            lastQslDateLong = System.currentTimeMillis() - (30L * 86400000L); // 30 days.
        }

        Date lastQslDate = (lastQslDateLong == 0) ? null : new Date(lastQslDateLong);
        AdifResult adifResult;
        try {
            adifResult = QueryLoTW.callLotw(username, password, owncall, null, true, lastQslDate, null, null,
                    null, null, 0);

            List<AdifRecord> adifRecords = adifResult.getRecords();
            Log.d(TAG, "lotw query returned " + adifRecords.size() + " qsl records...");

            if (adifRecords.size() != 0) {
                Date newLastQslDate = adifResult.getHeader().getLotw_lastqsl();
                if (newLastQslDate == null) {
                    Log.e(TAG, "adif parse error: lastQslDate is null");
                    throw new AdifResultException(AdifResultException.INVALID_ADIF_RESULT);
                }
                Log.d(TAG, "Last qsl date is " + newLastQslDate.toString());

                Collections.reverse(adifRecords);

                int newQsls = Util.updateDatabase(this, newLastQslDate, adifRecords, maxDatabaseEntries);

                if (newQsls > 0) {
                    AdifRecord lastQslRecord = adifRecords.get(adifRecords.size() - 1);
                    int unseenQsls = (int) (lastQslRecord.getId() - lastQslSeen);
                    if (unseenQsls < 1) {
                        unseenQsls = newQsls;
                    }
                    Resources res = getResources();
                    String title = res.getQuantityString(R.plurals.new_qsls, unseenQsls, unseenQsls);
                    String message = title + " " + getString(R.string.including) + " " + lastQslRecord.getCall();
                    Util.createNotification(this, title, message);
                }
            }
        } catch (AdifResultException e) {
            if (e.getExceptionType() != AdifResultException.IO_EXCEPTION) {
                Util.createNotification(this, "Update problem...", e.getMessage());
            }
            Log.e(TAG, "onHandleIntent got exception", e);
            //e.printStackTrace();
        }
    }

}
