package com.dermu.coinassignment;

import android.content.Context;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ListView;

public class CreditCardListActivity extends ActionBarActivity {
    private static final String TAG = "CoinCCList";
    private static final String PREF_LAST_REFRESH = "last_refresh";
    private static final String PREF_NAME = "CoinPrefs";
    private static final String CREDIT_CARD_SERVER = "https://s3.amazonaws.com/mobile.coin.vc/ios/assignment/data.json";

    private CreditCardAdapter ccAdapter;

    // ####################### Menu ###################################################
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_credit_card_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // ####################### Life Cycle ###################################################
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_LEFT_ICON); //FIXME

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_credit_card_list);
        setTitle("");
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
//            actionBar.setIcon(R.drawable.ic_launcher);
//            actionBar.setLogo(R.drawable.ic_launcher);
//            actionBar.setDisplayUseLogoEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
//            actionBar.setHomeButtonEnabled(false);
//            actionBar.setDisplayShowTitleEnabled(true);
        }

        //Hide the TSB (BACK, HOME, RECENT)
        HideNavbar();

        ccAdapter = new CreditCardAdapter(getApplicationContext(), getContentResolver().query(
                CreditCardProvider.CONTENT_URI,
                new String[]{
                        CreditCardProvider._ID,
                        CreditCardProvider.CARD_NUMBER,
                        CreditCardProvider.FIRST_NAME,
                        CreditCardProvider.LAST_NAME,
                        CreditCardProvider.EXPIRATION,
                        CreditCardProvider.BG_IMAGE
                },
                null,
                null,
                CreditCardProvider.CREATION_DATE + " DESC"));

        ListView cardList = (ListView) findViewById(R.id.cardlist);
        if (cardList != null) {
            cardList.setAdapter(ccAdapter);
//            cardList.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN); //FIXME
        }

        //TODO: place the logo in the actionBar properly
        //TODO: add the custom bg with gradient
        //TODO: change expiration date from "03/2016" to "03 / 16"
        //TODO: add more spaces in the card number too !
    }

    public void HideNavbar() {
        if(Build.VERSION.SDK_INT < 19) { //19 or above api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else {
            //for lower api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshIfNeeded();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ccAdapter.getCursor().close();
    }

    // ####################### methods ###################################################

    /**
     * Call this method to check if it's been long enough since the last refresh and if it is
     * it will trigger another downloadString of the data.
     */
    private void refreshIfNeeded() {
        showProgress();
        AsyncTask <Object, Object, Object> bgdownloader = new AsyncTask<Object, Object, Object>() {
            @Override
            protected Object doInBackground(Object... params) {
                int delay = Utils.getIntegerResourceSafely(
                        getResources(),
                        R.integer.credit_card_list_refresh_delay,
                        300000); // 5 minutes by default

                SharedPreferences sp = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
                if (sp != null) {
                    long lastRefresh = sp.getLong(PREF_LAST_REFRESH, 0);

                    if (System.currentTimeMillis() - lastRefresh > delay) {
                        downloadAndStoreCreditCards();
                    }
                } else {
                    Log.e(TAG, "Unable to get Shared Preferences for " + PREF_NAME);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                hideProgress();
                refreshContentIfNew();
            }
        };
        bgdownloader.execute();
    }

    /**
     * Shows the UI element to let the user know that the list of credit cards is currently
     * being updated in the background but without impeding their action.
     */
    private void showProgress() {
        setTitle(R.string.refreshing);
    }

    /**
     * Hide the UI element that lets the user know we are refreshing the list of cards in the bg.
     */
    private void hideProgress() {
        setTitle("");
    }

    /**
     * downloads the list of cards and saves it.
     */
    private void downloadAndStoreCreditCards() {
        Context applicationContext = getApplicationContext();
        String responseFile = Downloader.downloadString(CREDIT_CARD_SERVER, applicationContext);
        //Dump all rows in the db before adding new ones.
        getContentResolver().delete(CreditCardProvider.CONTENT_URI, "_id is not null", null);
        JsonUtils.parseAndSaveJson(responseFile, applicationContext);
        refreshContentIfNew();
    }

    private void refreshContentIfNew() {
        //This actually doesn't need to do anything because the adapter has a listener on dataset change

        //TODO: Handle empty lists !
        //TODO: Add a special mechanism to cache the credit card bg.
        // always downloadString them in case they changed but only downloadString them once per sync.
        // if the next card bg is the same don't downloadString again.
    }
}
