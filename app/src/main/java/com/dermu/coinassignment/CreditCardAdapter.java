package com.dermu.coinassignment;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Francois on 8/31/2015.
 */
public class CreditCardAdapter extends CursorAdapter {
    private static final String TAG = "CoinCCAdapter";
    private int cardNumColIndex;
    private int firstNameColIndex;
    private int lastNameColIndex;
    private int expiDateColIndex;
    private int bgImageColIndex;

    private final HashMap<String, Bitmap> cardBgs = new HashMap<>();

    private Bitmap defaultBg;

    public CreditCardAdapter(Context context, Cursor c) {
        super(context, c, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        if (c == null) {
            return;
        }
        cardNumColIndex   = c.getColumnIndex(CreditCardProvider.CARD_NUMBER);
        firstNameColIndex = c.getColumnIndex(CreditCardProvider.FIRST_NAME);
        lastNameColIndex  = c.getColumnIndex(CreditCardProvider.LAST_NAME);
        expiDateColIndex  = c.getColumnIndex(CreditCardProvider.EXPIRATION);
        bgImageColIndex  = c.getColumnIndex(CreditCardProvider.BG_IMAGE);

        defaultBg = BitmapFactory.decodeResource(
                context.getResources(),
                R.drawable.card_background);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.credit_card, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if (cursor == null) {
            return;
        }

        String cardNum = cursor.getString(cardNumColIndex);
        String firstName = cursor.getString(firstNameColIndex);
        String lastName = cursor.getString(lastNameColIndex);
        String expirationDate = cursor.getString(expiDateColIndex);
        String bgImage = cursor.getString(bgImageColIndex);

        TextView cardNumberTV = (TextView) view.findViewById(R.id.cardNumber);
        TextView nameTV = (TextView) view.findViewById(R.id.name);
        TextView expirationDateTV = (TextView) view.findViewById(R.id.expirationDate);
        ImageView bgIV = (ImageView) view.findViewById(R.id.cardBg);

        if (cardNumberTV != null) {
            cardNumberTV.setText(cardNum);
        }
        if (nameTV != null) {
            nameTV.setText(firstName + " " + lastName);
        }
        if (expirationDateTV != null) {
            expirationDateTV.setText(expirationDate);
        }
        if (bgIV != null) {
            bgIV.setImageBitmap(getBitmapFor(bgImage, view));
        }
    }

    @Override
    protected void onContentChanged() {
        super.onContentChanged();
        this.notifyDataSetChanged();
    }

    private Bitmap getBitmapFor(String bgImageUrl, View view) {
        Bitmap image;
        synchronized (cardBgs) {
            image = cardBgs.get(bgImageUrl);
        }
        if (image == null) {
            Log.d(TAG, "Did not find " + bgImageUrl + " in the cache. ");
            Log.d(TAG, "cardBgs has " + cardBgs.size() + " images in it.");
            image = defaultBg;
            synchronized (cardBgs) {
                cardBgs.put(bgImageUrl, defaultBg);
            }

                AsyncTask <ImageToRefresh, ImageToRefresh, ImageToRefresh> imageDownloader
                        = new AsyncTask<ImageToRefresh, ImageToRefresh, ImageToRefresh>() {

                    @Override
                    protected ImageToRefresh doInBackground(ImageToRefresh... params) {
                        ImageToRefresh itr = params[0];
                        itr.bitmap = Downloader.downloadBitmap(itr.imageUrl);
                        synchronized (cardBgs) {
                            cardBgs.put(itr.imageUrl, itr.bitmap);
                            Log.d(TAG, "Adding a bitmap for " + itr.imageUrl + " in the cache. ");
                            Log.d(TAG, "cardBgs has " + cardBgs.size() + " images in it.");
                        }
                        return itr;
                    }

                    @Override
                    protected void onPostExecute(ImageToRefresh image) {
                        super.onPostExecute(image);

                        ImageView bgIV = (ImageView) image.view.findViewById(R.id.cardBg);
                        if (bgIV != null) {
                            Bitmap bitmap;
                            synchronized (cardBgs) {
                                bitmap = cardBgs.get(image.imageUrl);
                            }
                            if (bitmap == null) {
                                bitmap = defaultBg;
                            }
                            bgIV.setImageBitmap(bitmap);
                            onContentChanged();
                        }
                    }
                }.execute(new ImageToRefresh(bgImageUrl, view));
//            }
        }
        return image;
    }

    private class ImageToRefresh {
        public String imageUrl;
        public View view;
        public Bitmap bitmap;

        public ImageToRefresh(String imageUrl, View view) {
            this.imageUrl = imageUrl;
            this.view = view;
        }
    }
}
