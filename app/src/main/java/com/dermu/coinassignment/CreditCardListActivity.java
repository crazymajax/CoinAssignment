package com.dermu.coinassignment;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

public class CreditCardListActivity extends ActionBarActivity {
    private static final String TAG = "CoinCCList";
    private static final String PREF_LAST_REFRESH = "last_refresh";
    private static final String PREF_NAME = "CoinPrefs";
    private static final String CREDIT_CARD_SERVER
            = "https://s3.amazonaws.com/mobile.coin.vc/ios/assignment/data.json";
            // change this to invalid URL to test failed downloads

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
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_credit_card_list);

        //Initialize the actionBar with the right icon and text.
        setTitle("");
        ActionBar actionBar = getSupportActionBar();
        Drawable icon = getResources().getDrawable(R.drawable.ic_launcher);
        if (actionBar != null && icon != null) {
            icon = resize(icon);
            actionBar.setIcon(icon);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        //Hide the TSB (BACK, HOME, RECENT)
        HideNavbar();

        Cursor cursor = null;
        try {
            cursor = getContentResolver().query( // this cursor will get closed in onDestroy()
                    CreditCardProvider.CONTENT_URI,
                    new String[]{
                            CreditCardProvider._ID,
                            CreditCardProvider.CARD_NUMBER,
                            CreditCardProvider.FIRST_NAME,
                            CreditCardProvider.LAST_NAME,
                            CreditCardProvider.EXPIRATION,
                            CreditCardProvider.BG_IMAGE
                    },
                    null, //CreditCardProvider._ID + " is null", // to test with nothing in the DB
                    null,
                    CreditCardProvider.CREATION_DATE + " DESC");
        } catch (Exception e) {
            Log.e(TAG, "Unable to create DB cursor !");
        }
        ccAdapter = new CreditCardAdapter(getApplicationContext(), cursor);

        ListView cardList = (ListView) findViewById(R.id.cardlist);
        if (cardList != null) {
            cardList.setAdapter(ccAdapter);
            cardList.setEmptyView(findViewById(R.id.emptyListText));
        }

        //TODO: add unit tests
    }

    private Drawable resize(Drawable image) {
        Bitmap b = ((BitmapDrawable)image).getBitmap();
        int px = dpToPx(45); // 45dp into pixel value
        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, px, px, false);
        return new BitmapDrawable(getResources(), bitmapResized);
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public void HideNavbar() {
        if(Build.VERSION.SDK_INT < 19) { //19 or above api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else {
            //for lower api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                          | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
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
        if ( ccAdapter != null) {
            Cursor cursor = ccAdapter.getCursor();
            if (cursor != null) {
                cursor.close();
            }
        }
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
    }
}
