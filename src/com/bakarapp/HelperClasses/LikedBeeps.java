package com.bakarapp.HelperClasses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.bakarapp.Platform.Platform;
import com.bakarapp.provider.LikedBeepsProvider;

public class LikedBeeps {

    private static final String TAG = "com.bakarapp.HelperClasses.LikedBeeps";
    private static Uri mUriFetch = Uri.parse("content://" + LikedBeepsProvider.AUTHORITY + "/db_fetch_only");
    private static Uri mUri = Uri.parse("content://" + LikedBeepsProvider.AUTHORITY + "/likedbeeps");
    private static String[] columns = new String[] {"beepid"};

    private final String beepid;
    

    public LikedBeeps(String bbdId) {
        this.beepid = bbdId;
        
    }

    public String getBeepID() {
        return beepid;
    }
    

    public static List<LikedBeeps> getList(){
        if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG, "Fetching blocked Users");
        List<LikedBeeps> likedBeeps;
        
        ContentResolver cr = Platform.getInstance().getContext().getContentResolver();
        Cursor cursor = cr.query(mUriFetch, columns, null, null, null);

        if (cursor == null || cursor.getCount() == 0) {
            if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG, "Empty result");
            likedBeeps = Collections.emptyList();
        } else {
        	likedBeeps = new ArrayList<LikedBeeps>();
            if (cursor.moveToFirst()) {
                do {
                    LikedBeeps blockedUser = new LikedBeeps(cursor.getString(0));
                    likedBeeps.add(blockedUser);
                } while (cursor.moveToNext());
            }
        }

        if (cursor != null){
            cursor.close();
        }

        return likedBeeps;
    }

    public static boolean isBeepLiked(int beepid){
    	String BeepId = Integer.toString(beepid);
        if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG, "Checking if '" + BeepId + "' is liked");
        ContentResolver cr = Platform.getInstance().getContext().getContentResolver();
        Cursor cursor = cr.query(mUriFetch, columns, "beepid = ?", new String[]{BeepId}, null);

        boolean isBeepLiked = true;
        if (cursor == null || cursor.getCount() == 0){
        	if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG, "bbdid:" + BeepId + "' is not liked ");
        	isBeepLiked = false;
        }

        if (cursor != null) {
            cursor.close();
        }

        if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG, "bbdid:" + BeepId + "' is liked :"+isBeepLiked);
        return isBeepLiked;
    }

    public static void addtoList(final int beepid){
        if (isBeepLiked(beepid)) {
            if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG, "User already blocked");
            return;
        }

        if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG, "Adding '" +beepid + "' to like list");
        new Thread("beeplike") {
            @Override
            public void run() {
                saveHistoryBlocking(beepid);
            }
        }.start();

    }

    private static void saveHistoryBlocking(int beepid) {
        ContentResolver cr = Platform.getInstance().getContext().getContentResolver();
       
        try {
            ContentValues values = new ContentValues();
            values.put(columns[0], beepid);           
            cr.insert(mUri, values);
        } catch (RuntimeException e) {
            if (Platform.getInstance().isLoggingEnabled()) Log.e(TAG, "BeepLikeQueryError", e);
        }
    }

    public static void clearList(){
        try {
            ContentResolver cr = Platform.getInstance().getContext().getContentResolver();
            cr.delete(mUri, null, null);
        } catch (RuntimeException e) {
            if (Platform.getInstance().isLoggingEnabled()) Log.e(TAG, "ClearAllQueryError", e);
        }
    }

    public static void deleteFromList(int beepid){
    	 String BeepId = Integer.toString(beepid);
        try {
            ContentResolver cr = Platform.getInstance().getContext().getContentResolver();
            cr.delete(mUri, "beepid = ?", new String[]{BeepId});
        } catch (RuntimeException e){
            if (Platform.getInstance().isLoggingEnabled()) Log.e(TAG, "Error in deleting user : " + BeepId, e);
        }
    }
}
