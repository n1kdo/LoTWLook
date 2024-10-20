package com.n1kdo.lotwlook;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.n1kdo.adif.AdifRecord;
import com.n1kdo.util.Utilities;

import java.util.Comparator;
import java.util.List;

public class SearchResultsActivity extends AppCompatActivity {
    private static final String TAG = SearchResultsActivity.class.getSimpleName();

    public enum SortColumn {NATURAL, MYCALL, CALLSIGN, QSO_DATE, BAND, MODE, COUNTRY}

    private List<AdifRecord> adifRecordsList = null;

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar search_toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(search_toolbar);
        search_toolbar.setTitle(R.string.searchResults);
        search_toolbar.setNavigationIcon(R.drawable.ic_action_back);
        search_toolbar.setNavigationOnClickListener(v -> onBackPressed());

        Intent intent = getIntent();
        this.adifRecordsList = intent.getParcelableArrayListExtra(LotwAdifIntentService.ADIF_RECORDS_LIST);
        String truncated = intent.getStringExtra(LotwAdifIntentService.TRUNCATED_MESSAGE);
        showTable(SortColumn.NATURAL, true);
        if (truncated != null) {
            Toast.makeText(this, truncated, Toast.LENGTH_LONG).show();
        }
    } // onCreate()

    @Override
    public final boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return (super.onOptionsItemSelected(item));
    }

    public final void showTable(final SortColumn sortColumn, final boolean descending) {
        Comparator<AdifRecord> comparator = AdifRecord.NATURAL_COMPARE;
        String descendingIndicator = descending ? "▼" : "▲";
        TableLayout tableLayout = findViewById(R.id.qslTableLayout);

        // delete any old data rows
        tableLayout.removeViews(2, tableLayout.getChildCount() - 2);

        // set up sorts
        TextView myCallHeader = findViewById(R.id.mycallHeader);
        myCallHeader.setOnClickListener(v -> showTable(SortColumn.MYCALL, sortColumn == SortColumn.MYCALL && !descending));

        TextView callsignHeader = findViewById(R.id.callsignHeader);
        callsignHeader.setOnClickListener(v -> showTable(SortColumn.CALLSIGN, sortColumn == SortColumn.CALLSIGN && !descending));

        TextView qsoDateHeader = findViewById(R.id.qsoDateHeader);
        qsoDateHeader.setOnClickListener(v -> showTable(SortColumn.QSO_DATE, sortColumn == SortColumn.QSO_DATE && !descending));
        TextView bandHeader = findViewById(R.id.bandHeader);
        bandHeader.setOnClickListener(v -> showTable(SortColumn.BAND, sortColumn == SortColumn.BAND && !descending));
        TextView modeHeader = findViewById(R.id.modeHeader);
        modeHeader.setOnClickListener(v -> showTable(SortColumn.MODE, sortColumn == SortColumn.MODE && !descending));
        TextView countryHeader = findViewById(R.id.countryHeader);
        countryHeader.setOnClickListener(v -> showTable(SortColumn.COUNTRY, sortColumn == SortColumn.COUNTRY && !descending));

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
        int color = Color.BLACK;
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
            tableRow.addView(LotwLookUtils.textViewForTable(this, adifRecord.getLotwOwnCall(), displayMode, color));

            // special processing to make the QSL's call into a clickable "link"
            SpannableString spannableString = new SpannableString(adifRecord.getCall());
            spannableString.setSpan(new UnderlineSpan(), 0, spannableString.length(), 0);
            TextView linkView = LotwLookUtils.textViewForTable(this, spannableString, displayMode, Color.BLUE);
            linkView.setOnClickListener(v -> {
                Intent detailsIntent = new Intent(getApplicationContext(), ShowQslDetailsActivity.class);
                detailsIntent.putExtra(ShowQslDetailsActivity.ADIF_RECORD, adifRecord);
                startActivity(detailsIntent);
            });
            tableRow.addView(linkView);

            tableRow.addView(LotwLookUtils.textViewForTable(this, Utilities.formatDate(adifRecord.getQsoDateTime()), displayMode, color));
            tableRow.addView(LotwLookUtils.textViewForTable(this, adifRecord.getBand(), displayMode, color));
            tableRow.addView(LotwLookUtils.textViewForTable(this, adifRecord.getMode(), displayMode, color));
            tableRow.addView(LotwLookUtils.textViewForTable(this, adifRecord.getCountry(), displayMode + 4, color));

            tableLayout.addView(tableRow);
        } // for
    } // showTable()

}
