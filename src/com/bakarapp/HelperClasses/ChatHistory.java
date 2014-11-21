package com.bakarapp.HelperClasses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.bakarapp.ChatService.Message;
import com.bakarapp.HTTPServer.ServerConstants;
import com.bakarapp.Platform.Platform;
import com.bakarapp.Util.Logger;
import com.bakarapp.provider.ChatHistoryProvider;

public class ChatHistory {
    private static final String TAG = "com.bakarapp.HelperClasses.ChatHistory";
    private static Uri mUriFetch = Uri.parse("content://" + ChatHistoryProvider.AUTHORITY + "/db_fetch_only");
    private static Uri mUri = Uri.parse("content://" + ChatHistoryProvider.AUTHORITY + "/chathistory");
    private static String[] columns = new String[] {"bbdIdTo",
                                                    "bbdIdFrom",
                                                    "body",                                                 
                                                    "groupId",
                                                    "timestamp",
                                                    "status",
                                                    "uniqueId",
                                                    "date",
                                                    "fromName",
                                                    "imageUrl"
                                                   };

    public static List<Message> getChatHistory(String userId){
        String bbdid = getBBDId(userId);
        Logger.i(TAG, "Fetching chat history for " + bbdid);
        List<Message> messages;

        ContentResolver cr = Platform.getInstance().getContext().getContentResolver();
        Cursor cursor = cr.query(mUriFetch, columns, "bbdIdTo = ? or bbdIdFrom = ?", new String[]{bbdid, bbdid}, columns[7]);

        if (cursor == null || cursor.getCount() == 0) {
            Logger.i(TAG, "Empty result");
            messages = Collections.emptyList();
        } else {
            messages = new LinkedList<Message>();
            if (cursor.moveToFirst()) {
                do {
                    messages.add(buildMessage(cursor));
                } while (cursor.moveToNext());
            }
        }

        if (cursor != null){
            cursor.close();
        }

        return messages;
    }

    public static Map<String, List<Message>> getCompleteChatHistory(String thisUserBBDId){
        if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG, "Fetching complete chat history");
        Map<String, List<Message>> chatHistory = new HashMap<String, List<Message>>();
        ContentResolver cr = Platform.getInstance().getContext().getContentResolver();
        Cursor cursor = cr.query(mUriFetch, columns, null, null, columns[5]);

        if (!(cursor == null || cursor.getCount() == 0)){
            Message message = buildMessage(cursor);
            String userId = (message.getFrom().equals(thisUserBBDId) ? message.getTo() :
                    message.getTo().equals(thisUserBBDId) ? message.getFrom() : null);
            if (userId != null) {
                List<Message> messages = chatHistory.get(userId);
                if (messages == null){
                    messages = new ArrayList<Message>();
                    chatHistory.put(userId, messages);
                }
                messages.add(message);
            }
        }

        if (cursor != null) {
            cursor.close();
        }

        return chatHistory;
    }

    public static void addtoChatHistory(final Message message){
        if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG, "Saving chathistory for user " + message.getFrom());
        final long time = System.currentTimeMillis();
        new Thread("addchathistory") {
            @Override
            public void run() {
                saveChatHistoryBlocking(message, time);
            }
        }.start();

    }

    private static void saveChatHistoryBlocking(Message message, long time) {
        ContentResolver cr = Platform.getInstance().getContext().getContentResolver();

        try {
            ContentValues values = new ContentValues();
            values.put(columns[0], getBBDId(message.getTo()));
            values.put(columns[1], getBBDId(message.getFrom()));
            values.put(columns[2], message.getBody());           
            values.put(columns[3], -1);
            values.put(columns[4], message.getTimestamp());
            values.put(columns[5], message.getStatus());
            values.put(columns[6], message.getUniqueMsgIdentifier());
            values.put(columns[7], time);
            values.put(columns[8], message.getSubject());
            values.put(columns[9], message.getImageName());
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

    public static void updateStatus(long messageUniqueId, int status){
        ContentResolver cr = Platform.getInstance().getContext().getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(columns[5],status);
        cr.update(mUri, contentValues, "uniqueId = ?", new String[]{Long.toString(messageUniqueId)});
    }

    private static Message buildMessage(Cursor cursor){
        String to = cursor.getString(0) + "@" + ServerConstants.CHATSERVERIP;
        String from = cursor.getString(1) + "@" + ServerConstants.CHATSERVERIP;
        String body = cursor.getString(2);       
        String time = cursor.getString(4);
        int status = cursor.getInt(5);
        long uniqueID = cursor.getLong(6);
        String subject = cursor.getString(8);
        String imageurl = cursor.getString(9);
        Message newMsg = new Message(to, from, body, time, Message.MSG_TYPE_CHAT, status, uniqueID,subject,imageurl);       
        return newMsg;
    }

    private static String getBBDId(String userid){
        return userid.split("@")[0];
    }
}
