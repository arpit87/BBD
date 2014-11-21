package com.bakarapp.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import com.bakarapp.Util.Logger;

public class ChatHistoryProvider extends ContentProvider {
    private static final String TAG = "com.bakarapp.provider.ChatHistoryProvider";
    public final static String AUTHORITY = "com.bakarapp.provider.ChatHistoryProvider";

    private Uri mUri;
    private UriMatcher mUriMatcher;

    private static final int DATABASE_VERSION = 1;
    private static final String mDatabaseName = "chathistory.db";
    private static final String mTableName = "chathistory";
    private static final int URI_MATCH_DB_FETCH_ONLY = 1;
    public static final String DB_FETCH_ONLY ="db_fetch_only";

    private SQLiteOpenHelper mOpenHelper;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, mDatabaseName, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            StringBuilder builder = new StringBuilder();
            builder.append("CREATE TABLE chathistory (" +
                    "_id INTEGER PRIMARY KEY" +
                    ",bbdIdTo TEXT" +                    
                    ",bbdIdFrom TEXT" +
                    ",body TEXT" +                  
                    ",groupId INTEGER" +
                    ",timestamp TEXT" +
                    ",status INTEGER" +
                    ",uniqueId LONG" +
                    ",date LONG" +
                    ",fromName TEXT" +
                    ",imageUrl TEXT" +
                    ");");
            db.execSQL(builder.toString());
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Logger.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ".");
            if (oldVersion == 1 && newVersion > 1) {
                db.execSQL("ALTER TABLE chathistory ADD COLUMN imageUrl TEXT;");
            } else {
                //Write your own implementation
            }
        }
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        mUri = Uri.parse("content://" + AUTHORITY + "/" + mTableName);
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(AUTHORITY, DB_FETCH_ONLY, URI_MATCH_DB_FETCH_ONLY);
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        int match = mUriMatcher.match(uri);
        if (match == URI_MATCH_DB_FETCH_ONLY){
            Cursor cSavedSug = db.query(mTableName, projection,
                    selection, selectionArgs, null, null, sortOrder, null);
            cSavedSug.setNotificationUri(getContext().getContentResolver(), uri);
            return  cSavedSug;
        }
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        int length = uri.getPathSegments().size();
        if (length < 1) {
            throw new IllegalArgumentException("Unknown Uri");
        }

        long rowID = -1;
        String base = uri.getPathSegments().get(0);
        Uri newUri = null;
        if (base.equals(mTableName)) {
            if (length == 1) {
                rowID = db.insert(mTableName, null, contentValues);
                if (rowID > 0) {
                    newUri = Uri.withAppendedPath(mUri, String.valueOf(rowID));
                }
            }
        }
        if (rowID < 0) {
            throw new IllegalArgumentException("Unknown Uri");
        }
        getContext().getContentResolver().notifyChange(newUri, null);
        return newUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        final int length = uri.getPathSegments().size();
        if (length != 1) {
            throw new IllegalArgumentException("Unknown Uri");
        }

        final String base = uri.getPathSegments().get(0);
        int count = 0;
        if (base.equals(mTableName)) {
            count = db.delete(mTableName, selection, selectionArgs);
        } else {
            throw new IllegalArgumentException("Unknown Uri");
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int length = uri.getPathSegments().size();
        if (length != 1) {
            throw new IllegalArgumentException("Unknown Uri");
        }

        final String base = uri.getPathSegments().get(0);
        int count = 0;
        if (base.equals(mTableName)) {
            count = db.update(mTableName, contentValues, selection, selectionArgs);
        } else {
            throw new IllegalArgumentException("Unknown Uri");
        }

        if (count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return count;
    }
}
