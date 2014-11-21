package com.bakarapp.HelperClasses;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.bakarapp.Platform.Platform;
import com.bakarapp.provider.BlockedUsersProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlockedUser {

    private static final String TAG = "com.bakarapp.HelperClasses.BlockedUser";
    private static Uri mUriFetch = Uri.parse("content://" + BlockedUsersProvider.AUTHORITY + "/db_fetch_only");
    private static Uri mUri = Uri.parse("content://" + BlockedUsersProvider.AUTHORITY + "/blockedUsers");
    private static String[] columns = new String[] {"bbdId", "name"};

    private final String bbdId;
    private final String name;

    public BlockedUser(String bbdId, String name) {
        this.bbdId = bbdId;
        this.name = name;
    }

    public String getBBDId() {
        return bbdId;
    }

    public String getName() {
        return name;
    }

    public static List<BlockedUser> getList(){
        if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG, "Fetching blocked Users");
        List<BlockedUser> blockedUsers;
        
        ContentResolver cr = Platform.getInstance().getContext().getContentResolver();
        Cursor cursor = cr.query(mUriFetch, columns, null, null, null);

        if (cursor == null || cursor.getCount() == 0) {
            if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG, "Empty result");
            blockedUsers = Collections.emptyList();
        } else {
            blockedUsers = new ArrayList<BlockedUser>();
            if (cursor.moveToFirst()) {
                do {
                    BlockedUser blockedUser = new BlockedUser(cursor.getString(0), cursor.getString(1));
                    blockedUsers.add(blockedUser);
                } while (cursor.moveToNext());
            }
        }

        if (cursor != null){
            cursor.close();
        }

        return blockedUsers;
    }

    public static boolean isUserBlocked(String bbdId){
        if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG, "Checking if '" + bbdId + "' is blocked");
        ContentResolver cr = Platform.getInstance().getContext().getContentResolver();
        Cursor cursor = cr.query(mUriFetch, columns, "bbdId = ?", new String[]{bbdId}, null);

        boolean isUserBlocked = true;
        if (cursor == null || cursor.getCount() == 0){
        	if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG, "bbdid:" + bbdId + "' is not blocked ");
            isUserBlocked = false;
        }

        if (cursor != null) {
            cursor.close();
        }

        if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG, "bbdid:" + bbdId + "' is blocked :"+isUserBlocked);
        return isUserBlocked;
    }

    public static void addtoList(final String bbdId, final String name){
        if (isUserBlocked(bbdId)) {
            if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG, "User already blocked");
            return;
        }

        if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG, "Adding '" +bbdId + "' to blocked users list");
        new Thread("blockUser") {
            @Override
            public void run() {
                saveHistoryBlocking(bbdId, name);
            }
        }.start();

    }

    private static void saveHistoryBlocking(String bbdId, String name) {
        ContentResolver cr = Platform.getInstance().getContext().getContentResolver();

        try {
            ContentValues values = new ContentValues();
            values.put(columns[0], bbdId);
            values.put(columns[1], name);
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

    public static void deleteFromList(String bbdId){
        try {
            ContentResolver cr = Platform.getInstance().getContext().getContentResolver();
            cr.delete(mUri, "bbdId = ?", new String[]{bbdId});
        } catch (RuntimeException e){
            if (Platform.getInstance().isLoggingEnabled()) Log.e(TAG, "Error in deleting user : " + bbdId, e);
        }
    }
}
