package com.n1kdo.lotwlook;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableRow;

import com.n1kdo.adif.AdifBand;
import com.n1kdo.adif.AdifCountry;
import com.n1kdo.adif.AdifMode;
import com.n1kdo.adif.AdifRecord;
import com.n1kdo.util.Utilities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends Activity {
    private static final String TAG = SearchActivity.class.getSimpleName();
    static final String L5 = "vBnyJTYwVh9bqgboB21QxvnoLatDiT6RRPkXOo84";
    private static final int LOTW_ADIF_REQUEST_CODE = 0;
    private static final long DAY_IN_MILLISECONDS = 86400000L;
    private static final long ALMOST_A_DAY_IN_MILLISECONDS = DAY_IN_MILLISECONDS - 1000;

    private ProgressDialog progressDialog = null;

    private Date startDate = null;
    private Date endDate = null;

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

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
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
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

            Date currentDate = new Date();
            int y = currentDate.getYear();
            int m = currentDate.getMonth();
            int d = currentDate.getDate();

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
                startDate = new Date(startYear, startMonth, d);
            }
            if (endDate == null) {
                endDate = new Date(y, m, d);
            }

            startDateButton.setText(Utilities.SHORT_DATE_FORMAT.format(startDate));
            endDateButton.setText(Utilities.SHORT_DATE_FORMAT.format(endDate));
            visibility = View.VISIBLE;
        } else {
            visibility = View.GONE;
        }

        tableRow1.setVisibility(visibility);
        tableRow2.setVisibility(visibility);
    }

    @SuppressWarnings({"deprecation", "unused"})
    public final void startDateButtonClicked(View view) {
        new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        startDate = new Date(year - 1900, month, day);
                        Button startDateButton = findViewById(R.id.startDateButton);
                        startDateButton.setText(Utilities.SHORT_DATE_FORMAT.format(startDate));
                    }
                },
                startDate.getYear() + 1900, startDate.getMonth(), startDate.getDate()).show();
    }

    @SuppressWarnings({"deprecation", "unused"})
    public final void endDateButtonClicked(View view) {
        new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        endDate = new Date(year - 1900, month, day);
                        Button endDateButton = findViewById(R.id.endDateButton);
                        endDateButton.setText(Utilities.SHORT_DATE_FORMAT.format(endDate));
                    }
                },
                endDate.getYear() + 1900, endDate.getMonth(), endDate.getDate()).show();
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
        if (theirCall.length() > 0) {
            lotwAdifServiceIntent.putExtra(LotwAdifIntentService.CALLSIGN, theirCall);
            paramCount++;
        }

        if (searchDateRangeCbx.isChecked()) {
            if (startDate != null) {
                lotwAdifServiceIntent.putExtra(LotwAdifIntentService.QSO_START_DATE, startDate.getTime());
                paramCount++;
            }

            if (endDate != null) {
                // make end of day - 23:59:59
                endDate = new Date(endDate.getTime() + ALMOST_A_DAY_IN_MILLISECONDS);
                lotwAdifServiceIntent.putExtra(LotwAdifIntentService.QSO_END_DATE, endDate.getTime());
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
            Util.alert(this, getString(R.string.missingSearchCriteria));
            return;
        }

        lotwAdifServiceIntent.putExtra(LotwAdifIntentService.DETAIL, true);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(R.string.accessingLoTW);
        progressDialog.setMessage(getString(R.string.pleaseWait));
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.show();

        startService(lotwAdifServiceIntent);
    } // searchButtonClick()

    @Override
    protected final void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOTW_ADIF_REQUEST_CODE) {
            switch (resultCode) {
                case LotwAdifIntentService.ERROR_CODE:
                    Log.w(TAG, "onActivityResult result is ERROR_CODE");
                    String errorMessage = data.getStringExtra(LotwAdifIntentService.ERROR_MESSAGE);
                    Util.alert(this, errorMessage);
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
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    } // onActivityResult()

}
