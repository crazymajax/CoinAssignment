package com.dermu.coinassignment;

import android.content.Context;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * A utility class that can parse the json reply of the API into credit cards
 * Created by Francois on 9/1/2015.
 */
public class JsonUtils {
    private static final String TAG = "CoinJsonUtils";

    public static final String RESULTS          = "results";

    public static final String CARD_NUMBER      = "card_number";
    public static final String EXPIRATION       = "expiration_date";
    public static final String FIRST_NAME       = "first_name";
    public static final String LAST_NAME        = "last_name";
    public static final String BG_IMAGE         = "background_image_url";
    public static final String ENABLED          = "enabled";
    public static final String CREATION_DATE    = "created";
    public static final String UPDATE_DATE      = "updated";
    public static final String GUID             = "guid";

    public static void parseAndSaveJson(String responseFile, Context ctx) {
        if (TextUtils.isEmpty(responseFile)) {
            Log.v(TAG, "The list of credit cards is empty.");
            return;
        }

        try {
            InputStream in = ctx.openFileInput(responseFile);
            JsonReader jr = new JsonReader(new InputStreamReader(in));
            readCardList(jr, ctx);
        } catch (Exception e) {
            Log.e(TAG, "Something went wrong while trying to parse json data.");
            e.printStackTrace();
        }
    }

    private static void readCardList(JsonReader reader, Context ctx) throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {

            JsonToken token = reader.peek();
            String name;
            if ("NAME".equals(token.name())) {
                name = reader.nextName();
            } else {
                name = reader.nextString();
            }
            if (RESULTS.equals(name)) {
                reader.beginArray();
                while (reader.hasNext()) {
                    insertInCardDb(readOneCard(reader), ctx);
                }
                reader.endArray();
            }
        }
        reader.endObject();
    }

    private static CreditCard readOneCard(JsonReader reader) throws IOException {
        if (reader == null || !reader.hasNext()) {
            return null;
        }

        boolean enabled = false;
        String firstName = "";
        String lastName = "";
        String cardNumber = "";
        String expirationDate = "";
        String guid = "";
        String created = "";
        String updated = "";
        String bg_image = "";

        reader.beginObject();
        while (reader.hasNext()) {
            JsonToken token = reader.peek();
            String name;
            if ("NAME".equals(token.name())) {
                name = reader.nextName();
            } else {
                name = reader.nextString();
            }
            if (CARD_NUMBER.equals(name)) {
                cardNumber = reader.nextString();
                cardNumber = cardNumber.replace(" ", "\u0020\u0020\u0020");
            } else if (EXPIRATION.equals(name)) {
                expirationDate = reader.nextString();
                String[] date = expirationDate.split("/");
                if (date.length == 2 && date[1].length() == 4) {
                    date[1] = date[1].substring(2,4);
                    expirationDate = TextUtils.join("\u0020/\u0020", date);
                } else {
                    expirationDate = expirationDate.replace("/", "\u0020/\u0020");
                }
            } else if (FIRST_NAME.equals(name)) {
                firstName = reader.nextString();
            } else if (LAST_NAME.equals(name)) {
                lastName = reader.nextString();
            } else if (BG_IMAGE.equals(name)) {
                bg_image = reader.nextString();
            } else if (ENABLED.equals(name)) {
                enabled = reader.nextBoolean();
            } else if (CREATION_DATE.equals(name)) {
                created = reader.nextString();
            } else if (UPDATE_DATE.equals(name)) {
                updated = reader.nextString();
            } else if (GUID.equals(name)) {
                guid = reader.nextString();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return new CreditCard(enabled, firstName, lastName, cardNumber,
                expirationDate, guid, created, updated, bg_image);
    }

    private static void insertInCardDb(CreditCard cc, Context ctx) {
        if (cc == null || ctx == null) {
            return;
        }
        ctx.getContentResolver().insert(CreditCardProvider.CONTENT_URI, cc.getContentValues());
    }
}
