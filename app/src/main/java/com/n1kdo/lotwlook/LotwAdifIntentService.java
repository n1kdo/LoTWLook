package com.n1kdo.lotwlook;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.n1kdo.adif.AdifRecord;
import com.n1kdo.adif.AdifResult;
import com.n1kdo.adif.AdifResultException;
import com.n1kdo.lotw.QueryLoTW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class LotwAdifIntentService extends IntentService {
    private static final String TAG = LotwAdifIntentService.class.getSimpleName();
    static final String L1 = "AQEAt5xE6YAM0muVX3OOLyE0kwi6lW3mlNm1/LdA";

    public static final String PENDING_RESULT = "pendingResult"; // object
    public static final String CALLSIGN = "callsign"; // string
    public static final String DETAIL = "detail"; // boolean
    public static final String QSL_SINCE_DATE = "sinceDate"; // long
    public static final String QSO_START_DATE = "startDate"; // long
    public static final String QSO_END_DATE = "endDate"; // long
    public static final String QSO_MODE = "mode"; // string
    public static final String QSO_BAND = "band"; // string
    public static final String QSO_DXCC = "dxcc"; // long
    public static final String UPDATE_DATABASE = "updateDatabase"; // boolean

    public static final String ADIF_RECORDS_LIST = "adifRecordsList";
    public static final String NEW_QSL_COUNT = "newQslCount";
    public static final String ERROR_MESSAGE = "errorMessage";
    public static final String TRUNCATED_MESSAGE = "truncatedMessage";

    // response codes
    public static final int RESULT_CODE = 0;
    public static final int ERROR_CODE = 1;
    public static final int NO_CONNECTIVITY_CODE = 2;
    public static final int NO_CREDENTIALS_CODE = 3;
    public static final int BAD_CREDENTIALS_CODE = 4;
    public static final int LOGIN_FAILURE_CODE = 5;

    public LotwAdifIntentService() {
        super(TAG);
    }

    @SuppressLint("DefaultLocale")
    @Override
    protected final void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent()");
        PendingIntent reply = intent.getParcelableExtra(PENDING_RESULT);

        if (reply == null) {
            // if reply is null, we cannot do a damned thing, so just bail here now.
            Log.wtf(TAG, "Could not get the pending intent value!");
            return;
        }

        try {
            if (!Util.isOnline(this)) {
                reply.send(NO_CONNECTIVITY_CODE);
                return;
            }

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            getSharedPreferences(PreferencesActivity.PREFERENCES_KEY, MODE_PRIVATE);

            String username = sharedPreferences.getString(PreferencesActivity.USERNAME_KEY, "");
            String owncall = sharedPreferences.getString(PreferencesActivity.OWNCALL_KEY, "");
            String password = sharedPreferences.getString(PreferencesActivity.PASSWORD_KEY, "");

            if (username.length() == 0 || password.length() == 0) {
                // no credentials.
                reply.send(NO_CREDENTIALS_CODE);
                return;
            }

            int maxDatabaseEntries = Integer.valueOf(sharedPreferences.getString(PreferencesActivity.MAX_DATABASE_ENTRIES_KEY, "100"));

            String callsign = intent.getStringExtra(CALLSIGN);
            boolean detail = intent.getBooleanExtra(DETAIL, true);
            Date sinceDate = dateFromIntentExtra(intent, QSL_SINCE_DATE);
            Date qsoStartDate = dateFromIntentExtra(intent, QSO_START_DATE);
            Date qsoEndDate = dateFromIntentExtra(intent, QSO_END_DATE);
            String mode = intent.getStringExtra(QSO_MODE);
            String band = intent.getStringExtra(QSO_BAND);
            int dxcc = intent.getIntExtra(QSO_DXCC, 0);
            boolean updateDatabase = intent.getBooleanExtra(UPDATE_DATABASE, false);

            Intent result = new Intent();

            try {
                AdifResult adifResult = QueryLoTW.callLotw(username, password, owncall, callsign, detail, sinceDate, qsoStartDate,
                        qsoEndDate, mode, band, dxcc);

                ArrayList<AdifRecord> adifRecords = adifResult.getRecords();
                Log.d(TAG, "got " + adifRecords.size() + " new qsl records...");
                int newQsls = 0;

                if (adifRecords.size() != 0) { // limit the size of the list to the maxDatabaseEntries count.
                    if (adifRecords.size() > maxDatabaseEntries) { // shorten the list.
                        Log.d(TAG, "shortening returned list.");
                        adifRecords = new ArrayList<>(adifRecords.subList(0, maxDatabaseEntries));
                        result.putExtra(TRUNCATED_MESSAGE, "results truncated to " + maxDatabaseEntries);
                    }

                    Collections.reverse(adifRecords);

                    Date lastQslDate = adifResult.getHeader().getLotw_lastqsl();
                    if (lastQslDate == null) {
                        Log.e(TAG, "adif parse error: lastQslDate is null");
                        throw new AdifResultException(AdifResultException.INVALID_ADIF_RESULT);
                    }
                    Log.d(TAG, "Last qsl date is " + lastQslDate.toString());

                    if (updateDatabase) {
                        newQsls = Util.updateDatabase(this, lastQslDate, adifRecords, maxDatabaseEntries);
                    }
                }
                result.putExtra(NEW_QSL_COUNT, newQsls);
                result.putParcelableArrayListExtra(ADIF_RECORDS_LIST, adifRecords);
                result.putExtra(UPDATE_DATABASE, updateDatabase);

                reply.send(this, RESULT_CODE, result);
            } catch (AdifResultException e) {
                Log.e(TAG, "onHandleIntent got exception", e);
                e.printStackTrace();
                switch (e.getExceptionType()) {
                    case AdifResultException.INVALID_CREDENTIALS_EXCEPTION:
                        reply.send(BAD_CREDENTIALS_CODE);
                        break;
                    case AdifResultException.LOGIN_FAILED_EXCEPTION:
                        reply.send(LOGIN_FAILURE_CODE);
                        break;
                    default:
                        result.putExtra(ERROR_MESSAGE, e.getMessage());
                        reply.send(this, ERROR_CODE, result);
                } // switch 
            } catch (Exception e) {
                result.putExtra(ERROR_MESSAGE, e.getMessage());
                reply.send(this, ERROR_CODE, result);
            }
        } catch (PendingIntent.CanceledException ce) {
            Log.i(TAG, "canceledException", ce);
        }
    } // onHandleIntent()

    public static Date dateFromIntentExtra(Intent intent, String item) {
        long dateLong = intent.getLongExtra(item, 0L);
        if (dateLong != 0)
            return new Date(dateLong);
        else
            return null;
    }
} // class
