package com.BhakBhosdi.HelperClasses;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.BhakBhosdi.Platform.Platform;
import com.BhakBhosdi.provider.ActiveChatProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ActiveChat {

    private static final String TAG = "com.BhakBhosdi.HelperClasses.ActiveChat";
    private static Uri mUriFetch = Uri.parse("content://" + ActiveChatProvider.AUTHORITY + "/db_fetch_only");
    private static Uri mUri = Uri.parse("content://" + ActiveChatProvider.AUTHORITY + "/activechat");
    private static String[] columns = new String[] {"bbdId", "name", "lastMessage"};

    private final String userId;
    private final String name;
    private final String lastMessage;

    public ActiveChat(String userId, String name, String lastMessage){
        this.userId = userId;
        this.name = name;
        this.lastMessage = lastMessage;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public static List<ActiveChat> getActiveChats(){
        if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG, "Fetching active chats");
        List<ActiveChat> activeChats;

        ContentResolver cr = Platform.getInstance().getContext().getContentResolver();
        Cursor cursor = cr.query(mUriFetch, columns, null, null, null);

        if (cursor == null || cursor.getCount() == 0) {
            if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG, "Empty result");
            activeChats = Collections.emptyList();
        } else {
            activeChats = new ArrayList<ActiveChat>();
            if (cursor.moveToFirst()) {
                do {
                    ActiveChat activeChat = new ActiveChat(cursor.getString(0), cursor.getString(1), cursor.getString(2));
                    activeChats.add(activeChat);
                } while (cursor.moveToNext());
            }
        }

        if (cursor != null){
            cursor.close();
        }

        return activeChats;
    }

    public static void addChat(final String bbdId, final String name, final String lastMessage){
        if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG, "Saving chat for bbdId : " + bbdId );
        new Thread("addlastchat") {
            @Override
            public void run() {
                saveChat(bbdId, name, lastMessage);
            }
        }.start();

    }

    private static void saveChat(String bbdId, String name, String lastMessage) {
        ContentResolver cr = Platform.getInstance().getContext().getContentResolver();
        Cursor cursor = cr.query(mUriFetch, columns, "bbdId = ?", new String[] {bbdId}, null);

        boolean isPresent = false;
        if (!(cursor == null || cursor.getCount() == 0)){
            if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG, "History Exists");
            isPresent = true;
        }

        if (cursor != null) {
            cursor.close();
        }

        ContentValues contentValues = new ContentValues();
        if (isPresent) {
            //not updating the name
            contentValues.put(columns[2], lastMessage);
            cr.update(mUri, contentValues, "bbdId = ?", new String[] {bbdId});
        } else {
            contentValues.put(columns[0], bbdId);
            contentValues.put(columns[1], name);
            contentValues.put(columns[2], lastMessage);
            cr.insert(mUri, contentValues);
        }
    }
}
