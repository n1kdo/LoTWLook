package com.n1kdo.lotwlook;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.n1kdo.adif.AdifCountry;
import com.n1kdo.adif.AdifRecord;
import com.n1kdo.util.Utilities;

public class ShowQslDetailsActivity extends Activity {
    private static final String TAG = ShowQslDetailsActivity.class.getSimpleName();
    static final String L3 = "e3gqejiisUw2CzbYTHw1Hu1zF9wgQUIkULJZI2cp";

    public static final String ADIF_RECORD = "adifRecord";

    private TableRow makeNewTableRow(CharSequence heading, CharSequence data) {
        TableRow tableRow = new TableRow(this);
        tableRow.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        TextView headingView = new TextView(this);
        headingView.setGravity(Gravity.END);
        headingView.setPadding(2, 2, 2, 2);
        headingView.setTypeface(null, Typeface.BOLD);
        headingView.setText(heading);
        tableRow.addView(headingView);

        TextView dataView = new TextView(this);
        dataView.setPadding(2, 2, 2, 2);
        dataView.setGravity(Gravity.NO_GRAVITY);
        dataView.setText(data);
        tableRow.addView(dataView);

        return tableRow;
    }

    private void addNonEmptyRow(TableLayout tableLayout, CharSequence heading, CharSequence s) {
        if (heading != null && s != null && s.length() != 0) {
            tableLayout.addView(makeNewTableRow(heading, s));
        }
    }

    private void addNonZeroRow(TableLayout tableLayout, CharSequence heading, int i) {
        if (heading != null && i != 0) {
            tableLayout.addView(makeNewTableRow(heading, Integer.toString(i)));
        }
    }

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        setContentView(R.layout.activity_show_qsl_details);
        Intent intent = getIntent();
        AdifRecord adifRecord = intent.getParcelableExtra(ADIF_RECORD);
        TableLayout tableLayout = findViewById(R.id.detailsTableLayout);
        if (adifRecord != null && tableLayout != null) {
            addNonEmptyRow(tableLayout, getString(R.string.callsignTitle), adifRecord.getLotwOwnCall());
            addNonEmptyRow(tableLayout, getString(R.string.workedTitle), adifRecord.getCall());
            if (adifRecord.getDxcc() != 0) {
                addNonEmptyRow(tableLayout, getString(R.string.dxccTitle),
                        AdifCountry.getAdifCountryByNumber(adifRecord.getDxcc()).getName());
            }
            addNonZeroRow(tableLayout, getString(R.string.cqZoneTitle), adifRecord.getCqZone());
            addNonZeroRow(tableLayout, getString(R.string.ituZoneTitle), adifRecord.getItuZone());
            addNonEmptyRow(tableLayout, getString(R.string.gridTitle), adifRecord.getGridSquare());
            addNonEmptyRow(tableLayout, getString(R.string.stateTitle), adifRecord.getState());
            addNonEmptyRow(tableLayout, getString(R.string.countyTitle), adifRecord.getCounty());
            if (adifRecord.getQsoDateTime() != null && adifRecord.getQsoDateTime().getTime() != 0) {
                addNonEmptyRow(tableLayout, getString(R.string.dateTimeTitle), Utilities.formatDate(adifRecord.getQsoDateTime()));
            }
            addNonEmptyRow(tableLayout, getString(R.string.modeTitle), adifRecord.getMode());
            addNonEmptyRow(tableLayout, getString(R.string.bandTitle), adifRecord.getBand());
            addNonEmptyRow(tableLayout, getString(R.string.frequencyTitle), adifRecord.getFreq());
            addNonEmptyRow(tableLayout, getString(R.string.rxFrequencyTitle), adifRecord.getFreqRx());
            addNonEmptyRow(tableLayout, getString(R.string.iotaTitle), adifRecord.getIota());
            addNonEmptyRow(tableLayout, getString(R.string.creditGrantedTitle), adifRecord.getCreditGrantedString());
            if (adifRecord.getQslRDate().getTime() != 0)
            {
                addNonEmptyRow(tableLayout, getString(R.string.qslDateTitle), Utilities.formatDate(adifRecord.getQslRDate()));
            }
        }
    } // onCreate()

    @Override
    public final boolean onOptionsItemSelected(MenuItem item) {
        if (item != null) {
            switch (item.getItemId()) {
                case android.R.id.home:
                    onBackPressed();
                    return true;
            }
            return (super.onOptionsItemSelected(item));
        } else {
            return false;
        }
    }
}
