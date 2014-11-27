package com.bakarapp.HelperClasses;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.bakarapp.Platform.Platform;
import com.bakarapp.Util.Logger;
import com.bakarapp.provider.ChatHistoryProvider;
import com.bakarapp.provider.FavBeepsProvider;

public class FavBeeps {
    private static final String TAG = "com.bakarapp.HelperClasses.FavBeeps";
    private static Uri mUriFetch = Uri.parse("content://" + FavBeepsProvider.AUTHORITY + "/db_fetch_only");
    private static Uri mUri = Uri.parse("content://" + FavBeepsProvider.AUTHORITY + "/favbeeps");
    private static String[] columns = new String[] {"beepid",
    												"beepstr",
    												"createdby",
                                                    "level",
                                                    "rebeeps",                                               
                                                    "likes",                                                   
                                                    "image",
                                                    "datecreated",
                                                    "dateentered"
                                                   };

    public static List<Beep> getFavBeeps(){
       
        Logger.i(TAG, "Fetching fav beeps " );
        List<Beep> beeps;

        ContentResolver cr = Platform.getInstance().getContext().getContentResolver();
        Cursor cursor = cr.query(mUriFetch, columns, null, null, columns[8]);

        if (cursor == null || cursor.getCount() == 0) {
            Logger.i(TAG, "Empty result");
            beeps = Collections.emptyList();
        } else {
        	beeps = new LinkedList<Beep>();
            if (cursor.moveToFirst()) {
                do {
                	Beep favbeep = buildBeep(cursor);
                	beeps.add(favbeep);
                	Logger.i(TAG, "Fav beep :"+favbeep.getBeep_id());
                } while (cursor.moveToNext());
            }
        }

        if (cursor != null){
            cursor.close();
        }

        return beeps;
    }

    
    public static void addtoFavBeeps(final Beep beep){
        if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG, "Saving fav beep : " + beep.getBeepStr());
        final long time = System.currentTimeMillis();
        new Thread("addFavBeep") {
            @Override
            public void run() {
            	saveBeepBlocking(beep, time);
            }
        }.start();

    }
    
    public static void deleteFromFavBeeps(Beep beep){
        try {
            ContentResolver cr = Platform.getInstance().getContext().getContentResolver();
            cr.delete(mUri, "beepid = ?", new String[]{Integer.toString(beep.getBeep_id())});
        } catch (RuntimeException e){
            if (Platform.getInstance().isLoggingEnabled()) Log.e(TAG, "Error in deleting user : " + beep.getBeep_id(), e);
        }
    }

    private static void saveBeepBlocking(Beep beep, long time) {
        ContentResolver cr = Platform.getInstance().getContext().getContentResolver();

        try {
            ContentValues values = new ContentValues();
            values.put(columns[0], beep.getBeep_id());
            values.put(columns[1], beep.getBeepStr());
            values.put(columns[2], beep.getBeepCreator());
            values.put(columns[3], beep.getBeepLevel());           
            values.put(columns[4], beep.getRebeeps());
            values.put(columns[5], beep.getLikes());
            values.put(columns[6], beep.getBeepImg());
            values.put(columns[7], beep.getDateCreated());
            values.put(columns[8], time);
            cr.insert(mUri, values);
        } catch (RuntimeException e) {
            if (Platform.getInstance().isLoggingEnabled()) Log.e(TAG, "BlockUserQueryError", e);
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
    
    public static boolean isFavourite(Beep beep)
    {
    	String beepid = Integer.toString(beep.getBeep_id());
    	if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG, "Checking if '" + beepid + "' is fav");
        ContentResolver cr = Platform.getInstance().getContext().getContentResolver();
        Cursor cursor = cr.query(mUriFetch, columns, "beepid = ?", new String[]{beepid}, null);

        boolean isFav = true;
        if (cursor == null || cursor.getCount() == 0){
        	if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG, "beepid:" + beepid + "' is not fav ");
        	isFav = false;
        }

        if (cursor != null) {
            cursor.close();
        }

        if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG, "bbdid:" + beepid + "' is blocked :"+isFav);
        return isFav;
    }

   
    private static Beep buildBeep(Cursor cursor){
    	int beepid = cursor.getInt(0) ;
        String beepstr = cursor.getString(1) ;
        int created_by = cursor.getInt(2) ;
        int level = cursor.getInt(3);       
        int rebeeps = cursor.getInt(4);
        int likes = cursor.getInt(5);       
        String img = cursor.getString(6);
        String datecreated = cursor.getString(7);
        Beep beep = new Beep();
        beep.setBeep_id(beepid) ;
        beep.setBeep_str(beepstr);
        beep.setCreator_bbdid(created_by);
        beep.setLevel(level);
        beep.setLikes(likes);
        beep.setRebeeps(rebeeps);
        beep.setBeep_img(img);
        beep.setDateCreated(datecreated);
        return beep;
    }

    
}
