package com.bakarapp.ChatService;

import android.os.RemoteException;

import com.bakarapp.ChatService.IChatManager;
import com.bakarapp.ChatService.IXMPPAPIs;
import com.bakarapp.ChatClient.ISBChatConnAndMiscListener;


public class XMPPAPIs extends IXMPPAPIs.Stub {
	
	private final XMPPConnectionListenersAdapter mConnectionAdapter;
	
	
	public XMPPAPIs(final XMPPConnectionListenersAdapter connection) {
			this.mConnectionAdapter = connection;			
		    }

	@Override
	public void connect() throws RemoteException {
		mConnectionAdapter.connect();
		
	}

	

	@Override
	public void disconnect() throws RemoteException {
		mConnectionAdapter.disconnect();
		
	}

	@Override
	public IChatManager getChatManager() throws RemoteException {		
		return mConnectionAdapter.getChatManager();
	}

	
	@Override
	public void loginAsync(String login,String password) throws RemoteException {
		mConnectionAdapter.loginAsync(login, password);
		
	}
	
	@Override
	public void loginWithCallBack(String login,String password,ISBChatConnAndMiscListener listener) throws RemoteException {
		mConnectionAdapter.addMiscCallBackListener(listener);
		mConnectionAdapter.loginAsync(login, password);
		
	}

		

}
