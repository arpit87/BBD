package com.bakarapp.ChatService;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.harmony.javax.security.sasl.SaslException;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.IQTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;

import android.os.AsyncTask;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import com.bakarapp.ChatClient.ISBChatConnAndMiscListener;
import com.bakarapp.HelperClasses.SBConnectivity;
import com.bakarapp.HelperClasses.ThisUserConfig;
import com.bakarapp.Platform.Platform;
import com.bakarapp.Util.Logger;
import com.bakarapp.Util.StringUtils;

public class XMPPConnectionListenersAdapter {
	
	private final XMPPConnection mXMPPConnection;
	private String TAG = "com.bakarapp.ChatService.XMPPConnectionListenersAdapter";
	ChatService mService = null;
	private String mLogin;
    private String mPassword;
    private SBChatConnectionListener mConnectionListener = new SBChatConnectionListener();
    private String mErrorMsg = "";
    public AtomicBoolean tryinLogging = new AtomicBoolean(false); 
    public AtomicBoolean tryinConnecting = new AtomicBoolean(false); 
	private ConnectToChatServerTask connectToServer = null;
	private LoginToChatServerTask loginToServer = null;
	private BBDChatManager mChatManager = null;
	private final RemoteCallbackList<ISBChatConnAndMiscListener> mRemoteMiscListeners = new RemoteCallbackList<ISBChatConnAndMiscListener>();
    private AtomicBoolean wasConnectionLost = new AtomicBoolean(false);    

    public void resetOnConnection() {
        mChatManager.resetOnConnection();
    }

    public void setWasConnectionLost(boolean val){
        wasConnectionLost.set(val);
    }
	
	
 public XMPPConnectionListenersAdapter(final ConnectionConfiguration config,  final ChatService service) {
		this(new XMPPTCPConnection(config), service);
	    }
	
 public void addMiscCallBackListener(ISBChatConnAndMiscListener listener) throws RemoteException {
	if (listener != null)
		mRemoteMiscListeners.register(listener);
 }
	
public void removeMiscCallBackListener(ISBChatConnAndMiscListener listener) throws RemoteException {
	if (listener != null)
		mRemoteMiscListeners.unregister(listener);
 }


	 public XMPPConnectionListenersAdapter(final XMPPConnection con,
			     final ChatService service) {
		 mXMPPConnection = con;

		mLogin = ThisUserConfig.getInstance().getString(ThisUserConfig.CHATUSERID);		
		mPassword = ThisUserConfig.getInstance().getString(ThisUserConfig.CHATPASSWORD);		
		mService = service;	
		mChatManager = new BBDChatManager(mXMPPConnection, mService);
		if (Platform.getInstance().isLoggingEnabled()) Log.d(TAG, "xmpp connection listener will connect");
		loginAsync(mLogin, mPassword);
		//Toast.makeText(mService, "connecting to xmpp", Toast.LENGTH_SHORT).show();
		if (Platform.getInstance().isLoggingEnabled()) Log.d(TAG, "connecting to xmpp");		
		registerPacketListenerForPingResponse();
	
	 }
	 
	 
	 
	 private void registerPacketListenerForPingResponse() {
		 /* packet listener: listen for incoming messages of type IQ on the connection (whatever the buddy) */
		    PacketFilter filter = new IQTypeFilter (IQ.Type.GET); // or IQ.Type.GET etc. according to what you like to filter. 

		    mXMPPConnection.addPacketListener(new PacketListener() {				
				@Override
				public void processPacket(Packet paramPacket) {
					Logger.i(TAG, "got ping from server");
					IQ pingResponse = IQ.createResultIQ((IQ) paramPacket);
					try {
						mXMPPConnection.sendPacket(pingResponse);
					} catch (NotConnectedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}, filter);
		
	}

	public BBDChatManager getChatManager() {
			return mChatManager;
		}
	 
	 	 
	 public XMPPConnection getmXMPPConnection() {
		return mXMPPConnection;
	}


	public boolean connect() throws RemoteException {
		if (!wasConnectionLost.get() && mXMPPConnection.isConnected())
		{
			if (Platform.getInstance().isLoggingEnabled()) Log.e(TAG, "Already connected..smpp.IsConnect is true");
			return true;
		}
		else {
		    try {
		    	if(SBConnectivity.isConnected())
		    	{
		    		mXMPPConnection.connect();
		    		mXMPPConnection.addConnectionListener(mConnectionListener);
		    		if (Platform.getInstance().isLoggingEnabled()) Log.e(TAG, "Isconnect is false...making new connection");
		    	}
		    	else
		    		Logger.d(TAG,"Not connected to internet");
		    } catch (Exception e) {
			Logger.e(TAG, "Error while connecting", e);
			mErrorMsg = e.getMessage();
			return false;
		    }  
		    return true;
		}		    
	    }

	public boolean isConnected() {
        return mXMPPConnection.isConnected();
    }

	public boolean isAuthenticated()
	{
        return mXMPPConnection.isAuthenticated();
	}
	
	public boolean disconnect() {
        if (mXMPPConnection != null && mXMPPConnection.isConnected()) {
            mXMPPConnection.removeConnectionListener(mConnectionListener);
            try {
				mXMPPConnection.disconnect();
			} catch (NotConnectedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        return true;
    }

	public void loginAsync(String login,String password)
	{		
		
		mLogin = login;
		mPassword = password;
		if (Platform.getInstance().isLoggingEnabled()) Log.d(TAG, "login async called");
		if(StringUtils.isEmpty(mLogin) || StringUtils.isEmpty(mPassword))
			return;
		if(wasConnectionLost.get() || !mXMPPConnection.isConnected())
		{
			if(tryinConnecting.getAndSet(true))
				return;
			if (Platform.getInstance().isLoggingEnabled()) Log.d(TAG, "xmpp isconnected is false and none tryin to connect so i will");
			connectToServer = new ConnectToChatServerTask();
			connectToServer.execute(this);
			
		}
		else if(wasConnectionLost.get() || !mXMPPConnection.isAuthenticated())
		{	
			if(tryinLogging.getAndSet(true))
				return;
			if (Platform.getInstance().isLoggingEnabled()) Log.d(TAG, "xmpp isAuthenticated is false and none tryin to login so i will");
			loginToServer = new LoginToChatServerTask();
			loginToServer.execute(this);
		}
		else if(mChatManager!=null)				
			mChatManager.notifyAllPendingQueue(); ///means we already logged in so send the msgs
			
	}
	
	//this should be called in separate thread
	    private boolean login() throws RemoteException {
	    	if (Platform.getInstance().isLoggingEnabled()) Log.d(TAG, "login called ");
	    if(StringUtils.isEmpty(mLogin) || StringUtils.isEmpty(mPassword))
	    		return false;	    
		if (!wasConnectionLost.get() && mXMPPConnection.isAuthenticated())
		{
			if (Platform.getInstance().isLoggingEnabled()) Log.d(TAG, "login called and is already authenticated");
			return true;
		}
	    	//ToastTracker.showToast("tryin login is not authenticated", Toast.LENGTH_SHORT);
		if (!mXMPPConnection.isConnected())
		{			
			if (Platform.getInstance().isLoggingEnabled()) Log.d(TAG, "tryin login but xmpp not connected,ll return false");
			return false; //blocking
		}		
		try {
			if (Platform.getInstance().isLoggingEnabled()) Log.d(TAG, "login called and willl login using:"+mLogin);
			try {
				mXMPPConnection.login(mLogin, mPassword);
			} catch (SaslException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SmackException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		    
		} catch (XMPPException e) {
		    if (Platform.getInstance().isLoggingEnabled()) Log.e(TAG, "Error while log in", e);
		    mErrorMsg = "Error while log in";
		    return false;
		}catch (IllegalStateException e) {
		    if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG, "Already logged in", e);		    
		    return true;
		}
		return true;
	    }
	    
	    public String getErrorMessage() {
	    	return mErrorMsg;
	        }
	 
	
private class SBChatConnectionListener implements ConnectionListener {
		
		@Override
		public void connectionClosed() {
		    if (Platform.getInstance().isLoggingEnabled()) Log.d(TAG, "closing connection,should reconnect?");
		    //ToastTracker.showToast("xmpp connection closed,should reconnect");	    
		    
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void connectionClosedOnError(Exception exception) {
		    if (Platform.getInstance().isLoggingEnabled()) Log.d(TAG, "connectionClosedOnError,should try reconnect");
		    
		    //ToastTracker.showToast("Chat server connection closed on error,should try reconnect");		   
		    //Intent intent = new Intent(BeemBroadcastReceiver.BEEM_CONNECTION_CLOSED);
		    //intent.putExtra("message", exception.getMessage());
		    //mService.sendBroadcast(intent);
		    //mService.stopSelf();
		}

		/**
		 * Connection failed callback.
		 * @param errorMsg smack failure message
		 */
		public void connectionFailed(String errorMsg) {
		    if (Platform.getInstance().isLoggingEnabled()) Log.d(TAG, "Connection Failed");
		    //ToastTracker.showToast("xmpp connection failed");		   
		  /*  final int n = mRemoteConnListeners.beginBroadcast();

		    for (int i = 0; i < n; i++) {
			IBeemConnectionListener listener = mRemoteConnListeners.getBroadcastItem(i);
			try {
			    if (listener != null)
				listener.connectionFailed(errorMsg);
			} catch (RemoteException e) {
			    // The RemoteCallbackList will take care of removing the
			    // dead listeners.
			    if (Platform.getInstance().isLoggingEnabled()) Log.w(TAG, "Error while triggering remote connection listeners", e);
			}
		    }
		    mRemoteConnListeners.finishBroadcast();*/
		   // mService.stopSelf();
		    
		}
		    @Override
			public void reconnectingIn(int paramInt) {
		    	if (Platform.getInstance().isLoggingEnabled()) Log.d(TAG, "reconnectingIn"+paramInt);
		    	 //ToastTracker.showToast("xmpp reconnecing in:"+paramInt);
				
			}

			@Override
			public void reconnectionSuccessful() {
				if (Platform.getInstance().isLoggingEnabled()) Log.d(TAG, "reconnection success");				
				//ToastTracker.showToast("xmpp reconnection successful");
				
			}

			@Override
			public void reconnectionFailed(Exception paramException) {
				if (Platform.getInstance().isLoggingEnabled()) Log.d(TAG, "reconnectionFailed Failed");
				
			}

			@Override
			public void authenticated(XMPPConnection arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void connected(XMPPConnection arg0) {
				// TODO Auto-generated method stub
				
			}
		}

private class ConnectToChatServerTask extends AsyncTask<XMPPConnectionListenersAdapter, Integer, Boolean>
{
	XMPPConnectionListenersAdapter adapter;
	@Override
	protected Boolean doInBackground(XMPPConnectionListenersAdapter... connection) {
		boolean result = true;	
		adapter = connection[0];
		if (Platform.getInstance().isLoggingEnabled()) Log.d(TAG, "connecting on separate thread");
		try {
		    publishProgress(25);			    
		    if (!adapter.connect()) {				
			return false;
		    }		    
		    publishProgress(100);			    
		   
        } catch (Exception e) {
		    result = false;
		}
		return result;
	}	
	
	protected void onPostExecute(Boolean connected) {
		tryinConnecting.set(false);
	if(connected)
	{		
		if(!("".equals(mLogin)) && (!"".equals(mPassword))){
			if(tryinLogging.getAndSet(true))
				return;
			loginToServer = new LoginToChatServerTask();
			loginToServer.execute(adapter);
			//ToastTracker.showToast("connected to xmpp,logging");
			if (Platform.getInstance().isLoggingEnabled()) Log.d(TAG, "connected to xmpp,logging");
		}
		else
		{
			if (Platform.getInstance().isLoggingEnabled()) Log.d(TAG, "connected to xmpp,but not logging");
			//ToastTracker.showToast("connected to xmpp but not logging");
			tryinLogging.set(false);
		}
		
	}
	}
	
	@Override
    protected void onCancelled() {
	if (mXMPPConnection != null && mXMPPConnection.isAuthenticated()) {
		try {
			mXMPPConnection.disconnect();
		} catch (NotConnectedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    }

	
}

private class LoginToChatServerTask extends AsyncTask<XMPPConnectionListenersAdapter, Integer, Boolean>
{
	
	@Override
	protected Boolean doInBackground(XMPPConnectionListenersAdapter... connection) {
		boolean result = true;	
		XMPPConnectionListenersAdapter adapter = connection[0];	
		if (Platform.getInstance().isLoggingEnabled()) Log.d(TAG, "logging on separate thread");
		   try{ 
		    if (!adapter.login()) {				
			publishProgress(25);
			return false;
		    }
		    //ToastTracker.showToast("logged in to xmpp");
		    publishProgress(100);
		} catch (Exception e) {			    
		    result = false;
		}
		return result;
	}	
	
	protected void onPostExecute(Boolean connected) {
			tryinLogging.set(false);
			if(!connected)
			{
				if (Platform.getInstance().isLoggingEnabled()) Log.d(TAG, "in login post exe but is not connected");
				//TODO try relogin here for 2,3 times
				//Toast.makeText(mService, "logged failed in postexecute,may be user not yet fb logged in,its ok", Toast.LENGTH_SHORT).show();
				return;
			}
			if (Platform.getInstance().isLoggingEnabled()) Log.d(TAG, "logged in to xmpp");
            setWasConnectionLost(false);
			//ToastTracker.showToast("logged in  to xmpp", Toast.LENGTH_SHORT);	
			if(mChatManager!=null)				
				mChatManager.notifyAllPendingQueue();
		 	Runnable sendPresence = new Runnable() {
				
				@Override
				public void run() {
					try{
						if (Platform.getInstance().isLoggingEnabled()) Log.d(TAG, "sending presence packet");
						Presence presence = new Presence(Presence.Type.available);
					 	try {
							mXMPPConnection.sendPacket(presence);
						} catch (NotConnectedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}			
					}catch (IllegalStateException e) {
					    if (Platform.getInstance().isLoggingEnabled()) Log.e(TAG, "Problem sending presence packet", e);
					}
				}
			};
			sendPresence.run();
		 	
		 	int n = mRemoteMiscListeners.beginBroadcast();

			for (int i = 0; i < n; i++) {
				ISBChatConnAndMiscListener listener = mRemoteMiscListeners.getBroadcastItem(i);
			    try {
					listener.loggedIn();
					//??can remove before finishbroadcast?
					
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			mRemoteMiscListeners.finishBroadcast();		
			
			
		}

	
}


	

}
