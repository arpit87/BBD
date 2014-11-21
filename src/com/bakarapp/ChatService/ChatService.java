package com.bakarapp.ChatService;

import java.util.Timer;
import java.util.TimerTask;

import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.Roster.SubscriptionMode;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.disco.packet.DiscoverInfo.Identity;
import org.jivesoftware.smackx.ping.PingManager;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import com.bakarapp.R;
import com.bakarapp.Activities.BhakBhosdiActivity;
import com.bakarapp.Fragments.ChatWindowFrag;
import com.bakarapp.HelperClasses.ActiveChat;
import com.bakarapp.HelperClasses.SBConnectivity;
import com.bakarapp.HelperClasses.ThisAppConfig;
import com.bakarapp.HelperClasses.ThisUserConfig;
import com.bakarapp.Platform.Platform;
import com.bakarapp.Util.Logger;

public class ChatService extends Service {

    private static String TAG = "com.bakarapp.ChatService.ChatService";
    private static final int POLL_FREQ = 2  * 60 * 1000;
    private static final int UPLOAD_FREQUENCY = 10 * 1000;
    private XMPPConnection mXMPPConnection = null;
    NotificationManager mNotificationManager = null;
    private ConnectionConfiguration mConnectionConfiguration = null;
    private XMPPConnectionListenersAdapter mConnectionAdapter;
    private XMPPAPIs mXMPPAPIs = null;
    private int DEFAULT_XMPP_PORT = 5222;
    int mPort;
    private SBChatBroadcastReceiver mReceiver = new SBChatBroadcastReceiver();
    private String mHost = "54.169.77.16";//ServerConstants.CHATSERVERIP;
    String mErrorMsg = "";
   
    private Timer timer;
    private Timer mLogtimer;
    private PingManager mPingManager;
    private ChatManager chatManager;

    /**
     * Broadcast intent type.
     */
    public static final String BBDCHAT_CONNECTION_CLOSED = "SBConnectionClosed";
    public static final String BBDLOGIN_TO_CHAT = "BBDLoginToChatServer";


    @Override
    public void onCreate() {
        super.onCreate();
        
        if (Platform.getInstance().isLoggingEnabled()) Log.d(TAG, "Started ChatService");
        //Toast.makeText(this, "started service", Toast.LENGTH_SHORT).show();
        registerReceiver(mReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        registerReceiver(mReceiver, new IntentFilter(BBDLOGIN_TO_CHAT));
        //registerReceiver(mReceiver, new IntentFilter(BroadCastConstants.NEARBY_USER_UPDATED));
        mPort = DEFAULT_XMPP_PORT;
        SmackAndroid.init(Platform.getInstance().getContext());
        initializeConfigration();
       
        mXMPPConnection = new XMPPTCPConnection(mConnectionConfiguration);

        Logger.d(TAG, "made xmpp connection");

        Identity i = new Identity("Android","Hopin","Bhakbhosdi");
        ServiceDiscoveryManager.setDefaultIdentity(i);

        //service has connection adapter which has all listeners
        mConnectionAdapter = new XMPPConnectionListenersAdapter(mXMPPConnection, ChatService.this);
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Roster.setDefaultSubscriptionMode(SubscriptionMode.accept_all);

        mXMPPAPIs = new XMPPAPIs(mConnectionAdapter);
        if (Platform.getInstance().isLoggingEnabled()) Log.d(TAG, "Created ChatService");
        
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Platform.getInstance().isLoggingEnabled())
            Log.i("LocalService", "Received start id " + startId + ": " + intent);
        //ToastTracker.showToast("service strted with id:"+startId);

        String login = ThisUserConfig.getInstance().getString(ThisUserConfig.CHATUSERID);
        String password = ThisUserConfig.getInstance().getString(ThisUserConfig.CHATPASSWORD);
        if (!login.equals("") && !password.equals(""))
            mConnectionAdapter.loginAsync(login, password);

        timer = new Timer();
        timer.schedule(new ConnectionMonitorTask(), POLL_FREQ, POLL_FREQ);
        
       // mLogtimer = new Timer();
       // mLogtimer.schedule(new UploadTimerTask(), UPLOAD_FREQUENCY, UPLOAD_FREQUENCY);

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //ToastTracker.showToast("stopping service and xmpp disconnecting");
       
        mNotificationManager.cancelAll();
        unregisterReceiver(mReceiver);
        if (mConnectionAdapter.isAuthenticated() && SBConnectivity.isConnected())
            mConnectionAdapter.disconnect();
        if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG, "Stopping the service");
        mLogtimer.cancel();
        timer.cancel();
    }

    private void initializeConfigration() {
        mConnectionConfiguration = new ConnectionConfiguration(mHost,mPort);
       // mConnectionConfiguration.setReconnectionAllowed(true);
        mConnectionConfiguration.setDebuggerEnabled(false);
        mConnectionConfiguration.setSendPresence(true);
        mConnectionConfiguration.setRosterLoadedAtLogin(false);
       SmackConfiguration.setDefaultPacketReplyTimeout(10000); 
       //SASLAuthentication.supportSASLMechanism("PLAIN", 0);
        //SmackAndroid.init(getApplicationContext());
        mConnectionConfiguration.setSecurityMode(SecurityMode.disabled);
        

    }

    @Override
    public IBinder onBind(Intent intent) {
        if (Platform.getInstance().isLoggingEnabled()) Log.d(TAG, "ONBIND()");
        return mXMPPAPIs;
    }


    public void sendNotification(int id, String participant, String participant_name, String chatMessage, String image_name) {

    	ActiveChat.addChat(participant, participant_name, chatMessage, 0);
    	
    	//notify if notification on
    	if(ThisAppConfig.getInstance().getInt(ThisAppConfig.NOTIFICATION_SETTINGS)==0)
    		return;
    	
        Intent chatIntent = new Intent(this, BhakBhosdiActivity.class);
        chatIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //chatIntent.putExtra(ChatWindowFrag.PARTICIPANT, participant);
       // chatIntent.putExtra(ChatWindowFrag.PARTICIPANT_NAME, participant_name);
    	
    	Uri sound_uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    	NotificationCompat.Builder mBuilder =
    	        new NotificationCompat.Builder(this)
    	        .setSmallIcon(R.drawable.ic_finger_white_solid)
    	        .setContentTitle(participant_name)
    	        .setContentText(chatMessage)    	        
    	        .setLights(0xff0000ff, 1000, 1000)
    	        .setAutoCancel(true);
    	if(ThisAppConfig.getInstance().getInt(ThisAppConfig.SOUND_SETTINGS)==1)
    		mBuilder.setSound(sound_uri);
        
    	// The stack builder object will contain an artificial back stack for the
    	// started Activity.
    	// This ensures that navigating backward from the Activity leads out of
    	// your application to the Home screen.
    	TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
    	// Adds the back stack for the Intent (but not the Intent itself)
    	stackBuilder.addParentStack(BhakBhosdiActivity.class);
    	// Adds the Intent that starts the Activity to the top of the stack
    	stackBuilder.addNextIntent(chatIntent);
    	PendingIntent resultPendingIntent =
    	        stackBuilder.getPendingIntent(
    	            0,
    	            PendingIntent.FLAG_UPDATE_CURRENT
    	        );
    	mBuilder.setContentIntent(resultPendingIntent);
    	NotificationManager mNotificationManager =
    	    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    	// mId allows you to update the notification later on.
    	mNotificationManager.notify(id, mBuilder.build());
    	
        if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG, "Sent notification");
       
    }

    public void deleteNotification(int id) {
        mNotificationManager.cancel(id);
    }


    class SBChatBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            String intentAction = intent.getAction();
            
            if (intentAction.equals(BBDCHAT_CONNECTION_CLOSED)) {
                CharSequence message = intent.getCharSequenceExtra("message");
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                if (context instanceof Activity) {
                    Activity act = (Activity) context;
                    act.finish();
                    // The service will be unbinded in the destroy of the activity.
                }
            } else if (intentAction.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                if (Platform.getInstance().isLoggingEnabled()) Log.d(TAG, "connectivity changed");
                if (!SBConnectivity.isOnline()) {
                    if (Platform.getInstance().isLoggingEnabled()) Log.d(TAG, "Connectivity Lost");
                } else {
                    //network came up again
                    //ToastTracker.showToast("NEtwork up yippe,ll login",  Toast.LENGTH_SHORT);
                    String login = ThisUserConfig.getInstance().getString(ThisUserConfig.CHATUSERID);
                    String password = ThisUserConfig.getInstance().getString(ThisUserConfig.CHATPASSWORD);
                    mConnectionAdapter.loginAsync(login, password);
                }
            } else if (intentAction.equals(BBDLOGIN_TO_CHAT)) {
                String login = intent.getStringExtra("username");
                String password = intent.getStringExtra("password");
                mConnectionAdapter.loginAsync(login, password);
            } /*else if (intentAction.equals(BroadCastConstants.NEARBY_USER_UPDATED)) {
                if (Platform.getInstance().isLoggingEnabled())
                    Log.i(TAG, "update intent in chat rece ,might broadcast");
                //send broad chat msg to all fb loggeged in nearby users
                if (!ThisUserConfig.getInstance().getBool(ThisUserConfig.FBINFOSENTTOSERVER))
                    return;

                SBChatManager chatManager = mConnectionAdapter.getChatManager();

                if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG, "Starting broadcast");
                List<NearbyUser> nearbyUserList = CurrentNearbyUsers.getInstance().getAllNearbyUsers();
                if (nearbyUserList != null)
                    for (NearbyUser n : nearbyUserList) {
                        String fbid = n.getUserFBInfo().getFbid();
                        if (!fbid.equals(""))
                            try {
                                Message msg = new Message(fbid, Message.MSG_TYPE_NEWUSER_BROADCAST);
                                if (chatManager != null) {
                                    if (Platform.getInstance().isLoggingEnabled())
                                        Log.i(TAG, "broadcasting to fbid:" + fbid);
                                    chatManager.getChat(fbid).sendMessage(msg);
                                }
                            } catch (RemoteException e) {
                                if (Platform.getInstance().isLoggingEnabled())
                                    Log.i(TAG, "Unable to send broadcast msg");
                                e.printStackTrace();
                            }
                    }

            }*/
        }
    }

    class ConnectionMonitorTask extends TimerTask {

        public void run() {
            Logger.i(TAG, "ConnectivityMonitor task resumed");
            //ToastTracker.showToast("ConnectivityMonitor task resumed");
            if (mPingManager == null) {
                if (ServiceDiscoveryManager.getInstanceFor(mXMPPConnection) != null) {
                	try{
                    mPingManager = PingManager.getInstanceFor(mXMPPConnection);
                	}
                	catch(IllegalStateException e)
                	{
                		Logger.e(TAG, "Server not reachable.");
                        //ToastTracker.showToast("Server not reachable.");
                        mConnectionAdapter.setWasConnectionLost(true);
                        login();
                        return;
                	}
                } else {
                    Logger.e(TAG, "No service discovery manager found");
                    return;
                }
            }

            boolean isServerReachable=false;
			try {
				isServerReachable = mPingManager.pingMyServer();
			} catch (NotConnectedException e) {
				// TODO Auto-generated catch block
				Logger.e(TAG, "Cant ping server");
				e.printStackTrace();
			}
            Logger.i(TAG, "Is server reachable? " + isServerReachable);

            if (isServerReachable) {
                //ToastTracker.showToast("Connected. Trying to login");
                login();
            } else {
                Logger.d(TAG, "Server not reachable.");
                //ToastTracker.showToast("Server not reachable.");
                mConnectionAdapter.setWasConnectionLost(true);
                login();
            }
        }
        
        public void login() {
            final String login = ThisUserConfig.getInstance().getString(ThisUserConfig.CHATUSERID);
            final String password = ThisUserConfig.getInstance().getString(ThisUserConfig.CHATPASSWORD);
            if (!login.equals("") && !password.equals("")) {
                mConnectionAdapter.loginAsync(login, password);
            }

            ChatManager tempChatManager = ChatManager.getInstanceFor(mXMPPConnection);
            if (tempChatManager != chatManager && tempChatManager != null) {
                mConnectionAdapter.resetOnConnection();
            }
            chatManager = tempChatManager;
        }
    }
}
