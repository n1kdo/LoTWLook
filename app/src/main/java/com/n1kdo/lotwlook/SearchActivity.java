package com.n1kdo.lotwlook;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableRow;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.n1kdo.adif.AdifBand;
import com.n1kdo.adif.AdifCountry;
import com.n1kdo.adif.AdifMode;
import com.n1kdo.adif.AdifRecord;
import com.n1kdo.util.Utilities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class SearchActivity extends AppCompatActivity {
    private static final String TAG = SearchActivity.class.getSimpleName();
    private static final int LOTW_ADIF_REQUEST_CODE = 0;
    private static final long DAY_IN_MILLISECONDS = 86400000L;
    private static final long ALMOST_A_DAY_IN_MILLISECONDS = DAY_IN_MILLISECONDS - 1000;

    private ProgressBar searchProgressBar = null;
    private Button searchButton = null; // disabled while a query is in flight (the modal dialog used to block it)

    private Date startDate = null;
    private Date endDate = null;

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar search_toolbar = findViewById(R.id.search_toolbar);
        setSupportActionBar(search_toolbar);
        searchProgressBar = findViewById(R.id.searchProgressBar);
        searchButton = findViewById(R.id.searchButton);
        search_toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        CheckBox searchDateRangeCbx = findViewById(R.id.searchDateRangeCbx);
        TableRow tableRow1 = findViewById(R.id.tableRow1);
        TableRow tableRow2 = findViewById(R.id.tableRow2);
        int visibility = searchDateRangeCbx.isChecked() ? View.VISIBLE : View.GONE;
        tableRow1.setVisibility(visibility);
        tableRow2.setVisibility(visibility);

        Spinner modeSpinner = findViewById(R.id.modeSpinner);
        List<String> modeNames = new ArrayList<>(AdifMode.getAdifModeNames());
        modeNames.add(0, getString(R.string.all));
        ArrayAdapter<String> modesSpinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                modeNames);
        modesSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modeSpinner.setAdapter(modesSpinnerArrayAdapter);

        Spinner bandSpinner = findViewById(R.id.bandSpinner);
        List<String> bandNames = new ArrayList<>(AdifBand.getAdifBandNames());
        bandNames.add(0, getString(R.string.all));
        ArrayAdapter<String> bandsSpinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                bandNames);
        bandsSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bandSpinner.setAdapter(bandsSpinnerArrayAdapter);

        Spinner dxccSpinner = findViewById(R.id.dxccSpinner);
        List<String> dxccNames = new ArrayList<>(AdifCountry.getAdifCountryNames());
        dxccNames.add(0, getString(R.string.all));
        ArrayAdapter<String> dxccSpinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                dxccNames);
        dxccSpinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dxccSpinner.setAdapter(dxccSpinnerArrayAdapter);
    } // onCreate()

    @Override
    public final boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return (super.onOptionsItemSelected(item));
    }

    @SuppressWarnings("deprecation")
    public final void dateRangeCheckboxClick(View view) {
        Log.d(TAG, "dateRangeCheckboxClick");
        CheckBox searchDateRangeCbx = findViewById(R.id.searchDateRangeCbx);
        TableRow tableRow1 = findViewById(R.id.tableRow1);
        TableRow tableRow2 = findViewById(R.id.tableRow2);

        int visibility;

        if (searchDateRangeCbx.isChecked()) {
            Button startDateButton = findViewById(R.id.startDateButton);
            Button endDateButton = findViewById(R.id.endDateButton);

            // All dates in this app are UTC; interpret the current date in UTC.
            Calendar now = utcCalendar(new Date());
            int y = now.get(Calendar.YEAR);
            int m = now.get(Calendar.MONTH);
            int d = now.get(Calendar.DAY_OF_MONTH);

            if (startDate == null) {
                int startYear;
                int startMonth;
                if (m == 0) { // january
                    startMonth = 11;
                    startYear = y - 1;
                } else {
                    startMonth = m - 1;
                    startYear = y;
                }
                startDate = utcDate(startYear, startMonth, d);
            }
            if (endDate == null) {
                endDate = utcDate(y, m, d);
            }

            startDateButton.setText(Utilities.formatShortDate(startDate));
            endDateButton.setText(Utilities.formatShortDate(endDate));
            visibility = View.VISIBLE;
        } else {
            visibility = View.GONE;
        }

        tableRow1.setVisibility(visibility);
        tableRow2.setVisibility(visibility);
    }

    @SuppressWarnings("unused")
    public final void startDateButtonClicked(View view) {
        Calendar calendar = utcCalendar(startDate);
        new DatePickerDialog(this,
                (view1, year, month, day) -> {
                    startDate = utcDate(year, month, day);
                    Button startDateButton = findViewById(R.id.startDateButton);
                    startDateButton.setText(Utilities.formatShortDate(startDate));
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    @SuppressWarnings("unused")
    public final void endDateButtonClicked(View view) {
        Calendar calendar = utcCalendar(endDate);
        new DatePickerDialog(this,
                (view1, year, month, day) -> {
                    endDate = utcDate(year, month, day);
                    Button endDateButton = findViewById(R.id.endDateButton);
                    endDateButton.setText(Utilities.formatShortDate(endDate));
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    // The picker's year/month/day values are UTC calendar values, not device-local ones.
    private static Date utcDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.clear();
        calendar.set(year, month, day);
        return calendar.getTime();
    }

    private static Calendar utcCalendar(Date date) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.setTime(date);
        return calendar;
    }

    @SuppressLint("DefaultLocale")
    public final void searchButtonClick(View view) {
        Log.d(TAG, "searchButtonClick");

        EditText theirCallEditText = findViewById(R.id.theirCallEditText);
        CheckBox searchDateRangeCbx = findViewById(R.id.searchDateRangeCbx);

        Spinner modeSpinner = findViewById(R.id.modeSpinner);
        Spinner bandSpinner = findViewById(R.id.bandSpinner);
        Spinner dxccSpinner = findViewById(R.id.dxccSpinner);

        int paramCount = 0;

        Intent lotwAdifServiceIntent = new Intent(getApplicationContext(), LotwAdifIntentService.class);
        PendingIntent pendingResult = createPendingResult(LOTW_ADIF_REQUEST_CODE, new Intent(), 0);
        lotwAdifServiceIntent.putExtra(LotwAdifIntentService.PENDING_RESULT, pendingResult);
        lotwAdifServiceIntent.putExtra(LotwAdifIntentService.UPDATE_DATABASE, false);

        String theirCall = theirCallEditText.getText().toString();
        if (!theirCall.isEmpty()) {
            lotwAdifServiceIntent.putExtra(LotwAdifIntentService.CALLSIGN, theirCall);
            paramCount++;
        }

        if (searchDateRangeCbx.isChecked()) {
            if (startDate != null) {
                lotwAdifServiceIntent.putExtra(LotwAdifIntentService.QSO_START_DATE, startDate.getTime());
                paramCount++;
            }

            if (endDate != null) {
                // make end of day - 23:59:59. Use a local variable: mutating the field
                // would shift the stored end date forward by a day on every search.
                Date adjustedEndDate = new Date(endDate.getTime() + ALMOST_A_DAY_IN_MILLISECONDS);
                lotwAdifServiceIntent.putExtra(LotwAdifIntentService.QSO_END_DATE, adjustedEndDate.getTime());
                paramCount++;
            }
        }

        if (modeSpinner.getSelectedItemPosition() > 0) {
            String modeName = modeSpinner.getSelectedItem().toString();
            String mode = AdifMode.getAdifModeByName(modeName).toString();
            lotwAdifServiceIntent.putExtra(LotwAdifIntentService.QSO_MODE, mode);
            paramCount++;
        }

        if (bandSpinner.getSelectedItemPosition() > 0) {
            String bandName = bandSpinner.getSelectedItem().toString();
            lotwAdifServiceIntent.putExtra(LotwAdifIntentService.QSO_BAND, bandName.toUpperCase(Locale.US));
            paramCount++;
        }

        if (dxccSpinner.getSelectedItemPosition() > 0) {
            String countryName = dxccSpinner.getSelectedItem().toString();
            AdifCountry country = AdifCountry.getAdifCountryByName(countryName);
            int dxcc = country.getNumber();
            lotwAdifServiceIntent.putExtra(LotwAdifIntentService.QSO_DXCC, dxcc);
            paramCount++;
        }

        if (paramCount < 1) {
            LotwLookUtils.alert(this, getString(R.string.missingSearchCriteria));
            return;
        }

        lotwAdifServiceIntent.putExtra(LotwAdifIntentService.DETAIL, true);

        searchProgressBar.setVisibility(View.VISIBLE);
        searchButton.setEnabled(false);

        startService(lotwAdifServiceIntent);
    } // searchButtonClick()

    @Override
    protected final void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOTW_ADIF_REQUEST_CODE) {
            switch (resultCode) {
                case LotwAdifIntentService.ERROR_CODE:
                    Log.w(TAG, "onActivityResult result is ERROR_CODE");
                    String errorMessage = data.getStringExtra(LotwAdifIntentService.ERROR_MESSAGE);
                    LotwLookUtils.alert(this, errorMessage);
                    break;
                case LotwAdifIntentService.RESULT_CODE:
                    ArrayList<AdifRecord> adifRecordsList = data.getParcelableArrayListExtra(LotwAdifIntentService.ADIF_RECORDS_LIST);
                    String truncated = data.getStringExtra(LotwAdifIntentService.TRUNCATED_MESSAGE);
                    Intent intent = new Intent(this, SearchResultsActivity.class);
                    intent.putParcelableArrayListExtra(LotwAdifIntentService.ADIF_RECORDS_LIST, adifRecordsList);
                    if (truncated != null) {
                        intent.putExtra(LotwAdifIntentService.TRUNCATED_MESSAGE, truncated);
                    }

                    startActivity(intent);
                    break;
            }
            searchProgressBar.setVisibility(View.GONE);
            searchButton.setEnabled(true);
        }
        super.onActivityResult(requestCode, resultCode, data);
    } // onActivityResult()

}
