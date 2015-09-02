package com.dermu.coinassignment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A Utility class for making requests to a URL and get the response.
 * Created by Francois on 8/31/2015.
 */
public class Downloader {

    private static final String TAG = "CoinDownloader";

    public static final String STORAGE_FILE_NAME = "credit_cards.json";

    /**
     * @param url the url of the request you want to make
     * @param ctx the appContext
     * @return the string returned by the server for the given request URL
     */
    public static String downloadString(String url, Context ctx) {
//        String responseString = "";

        if (TextUtils.isEmpty(url)) {
            Log.e(TAG, "Unable to downloadString from an invalid url: " + url);
//            return responseString;
            return null;
        }

        HttpClient client = new DefaultHttpClient();
        HttpResponse response = null;
        try {
            response = client.execute(new HttpGet(url));
        } catch (IOException e) {
            Log.e(TAG, "Unable to downloadString " + url);
            e.printStackTrace();
        }

        if (response != null) {
            StatusLine status = response.getStatusLine();

            HttpEntity entity = response.getEntity();
            if (entity != null) {
                try {
                    if (status.getStatusCode() == HttpStatus.SC_OK) {
                        //TODO: Check if this is necessary:
                        File dir = ctx.getFilesDir();
                        File file = new File(dir, STORAGE_FILE_NAME);
                        if (file.exists()) {
                            file.delete();
                        }
                        //Or if this will erase and overwrite everything.
                        FileOutputStream out = ctx.openFileOutput(STORAGE_FILE_NAME, Context.MODE_PRIVATE);
//                        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
                        try {
//                            entity.writeTo(out2);
//                            Log.v(TAG, "Response from the cloud for downloadString was: " + out2.toString());
                            entity.writeTo(out);
//                            //At scale this would need to go in a file but how many credit
//                            // cards can a user have ? Probably not that many...
//                            responseString = out.toString();
                        } catch (IOException e) {
                            Log.e(TAG, "Unable to write downloaded data to output: " + out);
                            e.printStackTrace();
                        } finally {
                            try {
                                out.close();
                            } catch (IOException e) {
                                Log.e(TAG, "Unable to close the output: " + out);
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "Unable to get storage file: " + STORAGE_FILE_NAME);
                    e.printStackTrace();
//                } finally {
//                    try {
//                        InputStream content = entity.getContent();
//                        content.close();
//                    } catch (IOException e) {
//                        Log.e(TAG, "Unable to get content from entity.");
//                        e.printStackTrace();
//                    }
                }
            } else {
                Log.e(TAG, "Unable to get a valid entity from the response.");
            }
        }
        return STORAGE_FILE_NAME;
    }



    /**
     * @param url the url of the request you want to make
     * @return the bitmap returned by the server for the given request URL
     */
    public static Bitmap downloadBitmap(String url) {
        Bitmap bitmap = null;

        if (TextUtils.isEmpty(url)) {
            Log.e(TAG, "Unable to download from an invalid url: " + url);
//            return responseString;
            return null;
        }

        HttpClient client = new DefaultHttpClient();
        HttpResponse response = null;
        try {
            response = client.execute(new HttpGet(url));
        } catch (IOException e) {
            Log.e(TAG, "Unable to download " + url);
            e.printStackTrace();
        }

        if (response != null) {
            StatusLine status = response.getStatusLine();

            HttpEntity entity = response.getEntity();
            if (entity != null && status.getStatusCode() == HttpStatus.SC_OK) {
                InputStream inputStream = null;
                try {
                    inputStream = entity.getContent();
                    bitmap = BitmapFactory.decodeStream(inputStream);
                } catch (IOException e) {
                    Log.e(TAG, "Unable to write downloaded bitmap: " + inputStream);
                    e.printStackTrace();
                } finally {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Unable to get content from entity.");
                        e.printStackTrace();
                    }
                }
            } else {
                Log.e(TAG, "Unable to get a valid entity from the response.");
            }
        }
        return bitmap;
    }
}
