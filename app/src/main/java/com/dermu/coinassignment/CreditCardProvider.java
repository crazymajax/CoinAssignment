package com.dermu.coinassignment;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import java.util.HashMap;

/**
 * The content provider used for the credit card database.
 * Created by Francois on 8/31/2015.
 */
public class CreditCardProvider extends ContentProvider {
    private static final String TAG = "CoinContProv";

    public static final String PROVIDER_NAME = "com.dermu.coinassignment.CreditCardProvider";
    public static final String URI_PATH = "cc";
    public static final String URL = "content://" + PROVIDER_NAME + "/" + URI_PATH;
    public static final Uri CONTENT_URI = Uri.parse(URL);
    public static final int uriCode = 1;

    private static HashMap<String, String> values;
    public static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, URI_PATH, uriCode);
    }

    private SQLiteDatabase sqlDB;

    public static final String _ID              = "_id";
    public static final String CARD_NUMBER      = "card_number";
    public static final String EXPIRATION       = "expiration";
    public static final String FIRST_NAME       = "first_name";
    public static final String LAST_NAME        = "last_name";
    public static final String BG_IMAGE         = "bg_image";
    public static final String ENABLED          = "enabled";
    public static final String CREATION_DATE    = "creation_date";
    public static final String UPDATE_DATE      = "update_date";
    public static final String GUID             = "guid";

    public static final int    DB_VERSION       = 1;
    public static final String DB_NAME          = "coin";
    public static final String TABLE_NAME       = "creditCards";

    public static final String CREATE_DB_TABLE  = " CREATE TABLE IF NOT EXISTS " + TABLE_NAME
            + " ( "
            + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + CARD_NUMBER + " TEXT NOT NULL, "
            + EXPIRATION + " DATE, "
            + FIRST_NAME + " TEXT NOT NULL, "
            + LAST_NAME + " TEXT NOT NULL, "
            + BG_IMAGE + " TEXT NOT NULL, "
            + ENABLED + " BIT, "
            + CREATION_DATE + " TEXT NOT NULL, "
            + UPDATE_DATE + " TEXT NOT NULL, "
            + GUID + " TEXT NOT NULL"
            + ");";


    @Override
    public boolean onCreate() {
        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        sqlDB = dbHelper.getWritableDatabase();
        return sqlDB != null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(TABLE_NAME);
        switch (uriMatcher.match(uri)) {
            case uriCode:
                queryBuilder.setProjectionMap(values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        Cursor cursor = queryBuilder.query(sqlDB, projection, selection,
                                            selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case uriCode:
                return "vnd.android.cursor.dir/creditcards";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long rowId = sqlDB.insert(TABLE_NAME, null, values);

        if (rowId > 0) {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        } else {
            Log.e(TAG, "Insert failed !");
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int rowsDeleted;
        switch (uriMatcher.match(uri)) {
            case uriCode:
                rowsDeleted = sqlDB.delete(TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int rowsUpdated = 0;
        switch (uriMatcher.match(uri)) {
            case uriCode:
                rowsUpdated = sqlDB.update(TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_DB_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
}
