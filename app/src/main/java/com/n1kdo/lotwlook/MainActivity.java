package com.n1kdo.lotwlook;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.n1kdo.adif.AdifRecord;
import com.n1kdo.lotwlook.data.LoTWLookDAO;
import com.n1kdo.util.Utilities;

import java.util.Comparator;
import java.util.List;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    public static final int LOTW_ADIF_REQUEST_CODE = 0;

    public static final int LOTW_UPDATE_JOB_ID = 0x73;

    private ProgressDialog progressDialog = null;

    private long lastQslSeen; // is the record id from the saved preferences...
    private long highestQslIdShownOnScreen = 0L;
    private long lastQslDateTime = 0L;

    private List<AdifRecord> adifRecordsList = null;

    private enum SortColumn {NATURAL, MYCALL, CALLSIGN, QSO_DATE, BAND, MODE, COUNTRY}

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");

        ////  DEBUG...
        if (false) {
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder(StrictMode.getVmPolicy())
                    .detectAll()
                    .penaltyLog()
                    .build());
        }

        super.onCreate(savedInstanceState);

        Log.d(TAG, "reading shared preferences...");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        getSharedPreferences(PreferencesActivity.PREFERENCES_KEY, MODE_PRIVATE);

        String username = sharedPreferences.getString(PreferencesActivity.USERNAME_KEY, "");
        String password = sharedPreferences.getString(PreferencesActivity.PASSWORD_KEY, "");
        lastQslSeen = sharedPreferences.getLong(PreferencesActivity.LAST_SEEN_QSL_KEY, 0L);
        lastQslDateTime = sharedPreferences.getLong(PreferencesActivity.LAST_QSL_DATE_KEY, 0L);
        int updateIntervalHours = Integer.valueOf(sharedPreferences.getString(PreferencesActivity.UPDATE_INTERVAL_KEY_NAME,
                "0"));

        if (username.length() == 0 || password.length() == 0) {
            alertCredentials();
        }

        setContentView(R.layout.activity_main);
        Log.d(TAG, "reading QSL data from database...");
        new ReadDatabaseAsyncTask().execute();

        Log.d(TAG, "validating update job is scheduled...");
        if (updateIntervalHours != 0) { // make sure update job is configured.
            JobScheduler jobScheduler = (JobScheduler) getApplicationContext().getSystemService(JOB_SCHEDULER_SERVICE);
            if (jobScheduler != null) {
                boolean foundUpdateJob = false;
                List<JobInfo> scheduledJobs = jobScheduler.getAllPendingJobs();
                for (JobInfo job : scheduledJobs) {
                    if (job.getId() == LOTW_UPDATE_JOB_ID) {
                        foundUpdateJob = true;
                        break;
                    }
                }
                if (!foundUpdateJob) { // configure update job
                    Log.w(TAG, "Configuring LoTW update job.");
                    ComponentName serviceComponent = new ComponentName(getApplicationContext(), LotwAdifJobService.class);
                    JobInfo.Builder builder = new JobInfo.Builder(LOTW_UPDATE_JOB_ID, serviceComponent);
                    long updateMillis = updateIntervalHours * 3600000L;
                    builder.setPeriodic(updateMillis, 1800000L);  // set flex to 30 minutes
                    builder.setPersisted(true);
                    builder.setRequiresDeviceIdle(true);
                    builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
                    jobScheduler.schedule(builder.build());
                } else {
                    Log.w(TAG, "LoTW update job was found to be configured.");
                }
            }
        } // if updateIntervalHours > 0
    } // onCreate()

    @Override
    public final void onDestroy() {
        super.onDestroy();
    }

    @Override
    public final boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @SuppressLint("StaticFieldLeak")
    @SuppressWarnings("NonStaticInnerClassInSecureContext")
    private class ReadDatabaseAsyncTask extends AsyncTask<Void, Void, List<AdifRecord>> {
        protected final List<AdifRecord> doInBackground(Void ... arg) {
            Log.d(TAG, "reading from database in background");
            List<AdifRecord> result;
            LoTWLookDAO dao = new LoTWLookDAO(MainActivity.this);
            dao.open();
            result = dao.getAllAdifRecords();
            dao.close();
            Log.d(TAG, "done reading from database in background");
            return result;
        }

        protected final void onPostExecute(List<AdifRecord> result) {
            Log.d(TAG, "read QSL data from database");
            adifRecordsList = result;
            showTable(SortColumn.NATURAL, true);
        }
    }

    private void showTable(final SortColumn sortColumn, final boolean descending) {
        Log.d(TAG, "showTable()");
        Comparator<AdifRecord> comparator = AdifRecord.NATURAL_COMPARE;
        String descendingIndicator = descending ? "\u25bc" : "\u25b2";

        TableLayout tableLayout = findViewById(R.id.qslTableLayout);

        // delete any old data rows
        tableLayout.removeViews(2, tableLayout.getChildCount() - 2);

        // set up sorts
        TextView tableHeader = findViewById(R.id.tableHeader);
        tableHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTable(SortColumn.NATURAL, true);
            }
        });

        TextView myCallHeader = findViewById(R.id.mycallHeader);
        myCallHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTable(SortColumn.MYCALL, sortColumn == SortColumn.MYCALL && !descending);
            }
        });

        TextView callsignHeader = findViewById(R.id.callsignHeader);
        callsignHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTable(SortColumn.CALLSIGN, sortColumn == SortColumn.CALLSIGN && !descending);
            }
        });

        TextView qsoDateHeader = findViewById(R.id.qsoDateHeader);
        qsoDateHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTable(SortColumn.QSO_DATE, sortColumn == SortColumn.QSO_DATE && !descending);
            }
        });
        TextView bandHeader = findViewById(R.id.bandHeader);
        bandHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTable(SortColumn.BAND, sortColumn == SortColumn.BAND && !descending);
            }
        });
        TextView modeHeader = findViewById(R.id.modeHeader);
        modeHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTable(SortColumn.MODE, sortColumn == SortColumn.MODE && !descending);
            }
        });
        TextView countryHeader = findViewById(R.id.countryHeader);
        countryHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTable(SortColumn.COUNTRY, sortColumn == SortColumn.COUNTRY && !descending);
            }
        });

        if (sortColumn == SortColumn.MYCALL) {
            myCallHeader.setText(getString(R.string.callsignColumnHeader, descendingIndicator));
            comparator = AdifRecord.MYCALL_COMPARE;
        } else {
            myCallHeader.setText(getString(R.string.callsignColumnHeader, ""));
        }

        if (sortColumn == SortColumn.CALLSIGN) {
            callsignHeader.setText(getString(R.string.workedColumnHeader, descendingIndicator));
            comparator = AdifRecord.CALLSIGN_COMPARE;
        } else {
            callsignHeader.setText(getString(R.string.workedColumnHeader, ""));
        }

        if (sortColumn == SortColumn.QSO_DATE) {
            qsoDateHeader.setText(getString(R.string.qsoDateColumnHeader, descendingIndicator));
            comparator = AdifRecord.QSO_DATE_COMPARE;
        } else {
            qsoDateHeader.setText(getString(R.string.qsoDateColumnHeader, ""));
        }

        if (sortColumn == SortColumn.BAND) {
            bandHeader.setText(getString(R.string.bandColumnHeader, descendingIndicator));
            comparator = AdifRecord.BAND_COMPARE;
        } else {
            bandHeader.setText(getString(R.string.bandColumnHeader, ""));
        }

        if (sortColumn == SortColumn.MODE) {
            modeHeader.setText(getString(R.string.modeColumnHeader, descendingIndicator));
            comparator = AdifRecord.MODE_COMPARE;
        } else {
            modeHeader.setText(getString(R.string.modeColumnHeader, ""));
        }

        if (sortColumn == SortColumn.COUNTRY) {
            countryHeader.setText(getString(R.string.countryColumnHeader, descendingIndicator));
            comparator = AdifRecord.COUNTRY_COMPARE;
        } else {
            countryHeader.setText(getString(R.string.countryColumnHeader, ""));
        }

        AdifRecord.sort(adifRecordsList, comparator, descending);

        TableRow tableRow;
        int color;
        int displayMode;
        int nRec = adifRecordsList.size();
        for (int i = 0; i < nRec; i++) {
            final AdifRecord adifRecord = adifRecordsList.get(i);
            displayMode = 0;
            if ((i & 0x0001) == 1) { // odd row
                displayMode += 1;
            }
            if (i == nRec - 1) { // last row
                displayMode += 2;
            }

            tableRow = new TableRow(this);
            tableRow.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

            color = (adifRecord.getId() > lastQslSeen) ? Color.RED : Color.BLACK;

            if (adifRecord.getId() > highestQslIdShownOnScreen) {
                highestQslIdShownOnScreen = adifRecord.getId();
            }

            // add the table fields
            tableRow.addView(Util.textViewForTable(this, adifRecord.getLotwOwnCall(), displayMode, color));

            // special processing to make the QSL's call into a clickable "link"
            SpannableString spannableString = new SpannableString(adifRecord.getCall());
            spannableString.setSpan(new UnderlineSpan(), 0, spannableString.length(), 0);
            TextView linkView = Util.textViewForTable(this, spannableString, displayMode, Color.BLUE);
            linkView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent detailsIntent = new Intent(getApplicationContext(), ShowQslDetailsActivity.class);
                    detailsIntent.putExtra(ShowQslDetailsActivity.ADIF_RECORD, adifRecord);
                    startActivity(detailsIntent);
                }
            });
            tableRow.addView(linkView);

            tableRow.addView(Util.textViewForTable(this, Utilities.formatDate(adifRecord.getQsoDateTime()), displayMode, color));
            tableRow.addView(Util.textViewForTable(this, adifRecord.getBand(), displayMode, color));
            tableRow.addView(Util.textViewForTable(this, adifRecord.getMode(), displayMode, color));
            tableRow.addView(Util.textViewForTable(this, adifRecord.getCountry(), displayMode + 4, color));

            tableLayout.addView(tableRow);
        }

        if (highestQslIdShownOnScreen > lastQslSeen) {
            PreferencesActivity.updateLastQslSeen(this, highestQslIdShownOnScreen);
            lastQslSeen = highestQslIdShownOnScreen;
        }

        Log.d(TAG, "showTable() done");
    }

    @Override
    public final boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                updateFromLoTW();
                return true;
            case R.id.action_search:
                if (checkIsOnline()) {
                    startActivity(new Intent(this, SearchActivity.class));
                }
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, PreferencesActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        } // switch
    } // opOptionsItemSelected()

    private boolean isConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connMgr != null) {
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            return (networkInfo != null && networkInfo.isConnected());
        }
        return false;
    }

    private boolean checkIsOnline() {
        if (!isConnected()) {
            Toast.makeText(this, R.string.network_unavailable, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void updateFromLoTW() {
        if (checkIsOnline()) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle(getString(R.string.accessingLoTW));
            progressDialog.setMessage(getString(R.string.pleaseWait));
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(true);
            progressDialog.show();

            if (lastQslDateTime == 0) { // really don't want to search from the beginning of time
                lastQslDateTime = System.currentTimeMillis() - (30L * 86400000L); // 30 days.  
            }

            PendingIntent pendingResult = createPendingResult(LOTW_ADIF_REQUEST_CODE, new Intent(), 0);
            Intent intent = new Intent(this, LotwAdifIntentService.class);
            intent.putExtra(LotwAdifIntentService.PENDING_RESULT, pendingResult);
            intent.putExtra(LotwAdifIntentService.QSL_SINCE_DATE, lastQslDateTime);
            intent.putExtra(LotwAdifIntentService.UPDATE_DATABASE, true);
            startService(intent);
        }
    }

    @SuppressLint("DefaultLocale")
    @Override
    protected final void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult(" + requestCode + ", " + resultCode + ", Intent data)");
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
        Resources res = getResources();

        if (requestCode == LOTW_ADIF_REQUEST_CODE) {
            switch (resultCode) {
                case LotwAdifIntentService.ERROR_CODE:
                    Log.w(TAG, "onActivityResult result is ERROR_CODE");
                    //Toast.makeText(this, R.string.cannot_connect_to_lotw, Toast.LENGTH_LONG).show();
                    String errorMessage = data.getStringExtra(LotwAdifIntentService.ERROR_MESSAGE);
                    Util.alert(this, errorMessage);
                    break;
                case LotwAdifIntentService.NO_CONNECTIVITY_CODE:
                    Log.w(TAG, "onActivityResult result is NO_CONNECTIVITY_CODE");
                    Toast.makeText(this, R.string.network_unavailable, Toast.LENGTH_LONG).show();
                    break;
                case LotwAdifIntentService.NO_CREDENTIALS_CODE:
                    Log.w(TAG, "onActivityResult result is NO_CREDENTIALS_CODE");
                    alertCredentials();
                    break;
                case LotwAdifIntentService.BAD_CREDENTIALS_CODE:
                    Log.w(TAG, "onActivityResult result is BAD_CREDENTIALS_CODE");
                    alertCredentials();
                    break;
                case LotwAdifIntentService.LOGIN_FAILURE_CODE:
                    Log.w(TAG, "onActivityResult result is LOGIN_FAILURE_CODE");
                    Util.alert(this, res.getString(R.string.loginFailed));
                    break;
                case LotwAdifIntentService.RESULT_CODE:
                    adifRecordsList = data.getParcelableArrayListExtra(LotwAdifIntentService.ADIF_RECORDS_LIST);
                    boolean updateDatabase = data.getBooleanExtra(LotwAdifIntentService.UPDATE_DATABASE, false);
                    int newQsls = data.getIntExtra(LotwAdifIntentService.NEW_QSL_COUNT, 0);
                    Log.d(TAG, "onActivityResult got back " + adifRecordsList.size() + " qsls");

                    if (updateDatabase) {
                        new ReadDatabaseAsyncTask().execute();
                    } else {
                        showTable(SortColumn.NATURAL, true);
                    }
                    if (newQsls > 0) {
                        String text = res.getQuantityString(R.plurals.new_qsls, newQsls, newQsls);
                        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, R.string.no_new_qsls, Toast.LENGTH_LONG).show();
                    }
                    break;
            } // switch
        } // if requestCode == LOTW_ADIF_REQUEST_CODE
        super.onActivityResult(requestCode, resultCode, data);
    } // onActivityResult()

    private void alertCredentials() {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle(R.string.invalid_missing_credentials);
        adb.setMessage(R.string.you_need_to_set_credentials);
        adb.setPositiveButton(R.string.set_credentials, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(getApplicationContext(), PreferencesActivity.class));
            }
        });

        adb.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                onBackPressed(); // get out of the app.
            }
        });
        adb.setIcon(android.R.drawable.ic_dialog_alert);
        adb.show();
    } // alertCredentials()
}
