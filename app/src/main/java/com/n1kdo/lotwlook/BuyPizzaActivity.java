package com.n1kdo.lotwlook;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class BuyPizzaActivity extends Activity {
    private static final String TAG = BuyPizzaActivity.class.getSimpleName();
    public static final String PIZZA_PRICE_STRING = "pizzaPriceString";
    static final String AFTER_OCHO = "TSgHJzlcTiqO63PdW3y+hRuLewIDAQAB";

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_pizza);
        Intent intent = getIntent();

        String pizzaPriceString = intent.getStringExtra(PIZZA_PRICE_STRING);
        TextView priceTextView = findViewById(R.id.pizzaPriceText);
        if (pizzaPriceString != null) {
            priceTextView.setText(pizzaPriceString);
        }

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

    public final void buyPizzaButtonClick(View view) {
        Log.d(TAG, "buyPizzaButtonClick");
        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);
        finish();

    } // buyPizzaButtonClick()

}
