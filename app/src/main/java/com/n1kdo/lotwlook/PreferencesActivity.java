package com.n1kdo.lotwlook;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import java.util.List;

public class PreferencesActivity extends AppCompatActivity implements OnSharedPreferenceChangeListener {
    private static final String TAG = PreferencesActivity.class.getSimpleName();

    public static final String PREFERENCES_KEY = "com.n1kdo.lotwlook.preferences";

    public static final String UPDATE_INTERVAL_KEY_NAME = "updateInterval";
    public static final String MAX_DATABASE_ENTRIES_KEY = "maxDatabaseEntries";
    public static final String LAST_SEEN_QSL_KEY = "lastSeenQslRecordId";
    public static final String LAST_QSL_DATE_KEY = "lastQslDate";
    public static final String USERNAME_KEY = "username";
    public static final String OWNCALL_KEY = "owncall";
    public static final String PASSWORD_KEY = "password";

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar preferences_toolbar = findViewById(R.id.preferences_toolbar);
        setSupportActionBar(preferences_toolbar);
        preferences_toolbar.setNavigationIcon(R.drawable.ic_action_back);
        preferences_toolbar.setNavigationOnClickListener(v -> onBackPressed());

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_layout, new PreferencesSettingsFragment())
                .commit();
    }

    @Override
    public final void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "key=" + key);
        if (UPDATE_INTERVAL_KEY_NAME.equals(key)) {
            Context context = this.getApplication();

            int updateIntervalHours = Integer.parseInt(sharedPreferences.getString("updateInterval", "0"));
            Log.d(TAG, "update interval set to " + updateIntervalHours + " hours.");

            JobScheduler jobScheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
            if (jobScheduler != null) {
                // cancel any existing job.
                List<JobInfo> scheduledJobs = jobScheduler.getAllPendingJobs();
                for (JobInfo job : scheduledJobs) {
                    if (job.getId() == MainActivity.LOTW_UPDATE_JOB_ID) {
                        jobScheduler.cancel(MainActivity.LOTW_UPDATE_JOB_ID);
                        break;
                    }
                }
                if (updateIntervalHours != 0) {
                    // start a new job.
                    Log.w(TAG, "Configuring LoTW update job.");
                    ComponentName serviceComponent = new ComponentName(context, LotwAdifJobService.class);
                    JobInfo.Builder builder = new JobInfo.Builder(MainActivity.LOTW_UPDATE_JOB_ID, serviceComponent);
                    long updateMillis = updateIntervalHours * 3600000L;
                    builder.setPeriodic(updateMillis, 1800000L);  // set flex to 30 minutes
                    builder.setPersisted(true);
                    builder.setRequiresDeviceIdle(true);
                    builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
                    jobScheduler.schedule(builder.build());
                }
            }
        } // if UPDATE_INTERVAL_KEY_NAME
    } // onSharedPreferenceChange

    static void updateLastQslDate(Context context, long datetimevalue) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        context.getSharedPreferences(PreferencesActivity.PREFERENCES_KEY, Context.MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();
        editor.putLong(PreferencesActivity.LAST_QSL_DATE_KEY, datetimevalue);
        editor.apply();
    }

    public static void updateLastQslSeen(Context context, long newvalue) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        context.getSharedPreferences(PreferencesActivity.PREFERENCES_KEY, Context.MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();
        editor.putLong(PreferencesActivity.LAST_SEEN_QSL_KEY, newvalue);
        editor.apply();
    }

}
