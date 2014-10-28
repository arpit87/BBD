package com.BhakBhosdi.ChatService;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.concurrent.LinkedBlockingQueue;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smackx.jiveproperties.JivePropertiesManager;

import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import com.BhakBhosdi.ChatClient.ChatMessage;
import com.BhakBhosdi.ChatClient.IMessageListener;
import com.BhakBhosdi.HelperClasses.BlockedUser;
import com.BhakBhosdi.HelperClasses.ChatHistory;
import com.BhakBhosdi.HelperClasses.ThisUserConfig;
import com.BhakBhosdi.Platform.Platform;
import com.BhakBhosdi.Util.Logger;
import com.BhakBhosdi.Util.StringUtils;

/***
 * There is a chatAdapter for every chat which stores a list of all chat msgs
 * for this chat + a list of all msgs sent but not yet delivered in a hashmap.
 * the processMessage method here is the first one to receive any incoming msg
 * and it prcesses the message to refresh its list of sent/delivered msg and
 * calls back listeners of chatwindow(IMessageListeners) i.e. processMessage of
 * chatwindow in IMessageListener.Stub is called
 * 
 * @author arpit87
 * 
 */

class ChatAdapter extends IChatAdapter.Stub {

	private static final long DELAY = 20 * 1000;
	private static final long THRESHOLD_DELAY = 15 * 1000;
	private static final int HISTORY_MAX_SIZE = 50;
	private static final int MAX_RECVD_IDS = 10;
	private static final String TAG = "com.BhakBhosdi.ChatService.ChatAdapter";
	private Boolean mIsOpen = false;
	private Chat mSmackChat;
	private final String mParticipant;	
	private List<Message> mMessages;
	private final Map<Long, Message> mSentButServerAckNotReceivedMap;
	private final Map<Long, Message> mSentNotDeliveredMsgsMap;
	private BBDChatManager mChatManager;
	SBMsgListener mMsgListener = null;
	//int notificationid = 0;
	String mImageURL = "";
	private final RemoteCallbackList<IMessageListener> mRemoteListeners = new RemoteCallbackList<IMessageListener>();
	private LinkedBlockingQueue<Message> mMsgqueue = null;
	SenderThread mSenderThread = null;
	private Timer timer;
	private TreeSet<Long> receivedChatIds = new TreeSet<Long>();	
	
	// small chat participant should be complete as to is overridden inside
	// sendMsg by smack to participant
	public ChatAdapter(final Chat chat, BBDChatManager chatManager) {
		mSmackChat = chat;
		mParticipant = chat.getParticipant();		
		mMessages = ChatHistory.getChatHistory(mParticipant);
		if(mMessages.size()==0)
			mMessages = new LinkedList<Message>();
		mMsgListener = new SBMsgListener();
		mMsgqueue = new LinkedBlockingQueue<Message>();
		mSmackChat.addMessageListener(mMsgListener);
		mChatManager = chatManager;
		mSentButServerAckNotReceivedMap = new LinkedHashMap<Long, Message>();
	    mSentNotDeliveredMsgsMap = new LinkedHashMap<Long, Message>();
		for(int i=mMessages.size()-1;i>=0;i--)
		{						
			Message thisMessage = mMessages.get(i);
			if(thisMessage.getFrom().equals(mParticipant))
			{
				// here we are loading previous received msgs
				if(receivedChatIds.size() >= MAX_RECVD_IDS)
					continue;
				receivedChatIds.add(thisMessage.getUniqueMsgIdentifier());				
			}
			else if(thisMessage.getStatus()==ChatMessage.SENDING)
			{			
				//here we are sending pre unsent msgs,it loads in queue too
				Logger.i(TAG, "Loaded prev unsent msg:"+thisMessage.getBody());				
				sendChatMessage(thisMessage);
			}else if(thisMessage.getStatus()==ChatMessage.SENT)
			{
				Logger.i(TAG, "Loaded prev sent but not delivered msg:"+thisMessage.getBody()+",u_id:"+thisMessage.getUniqueMsgIdentifier());
				mSentNotDeliveredMsgsMap.put(thisMessage.getUniqueMsgIdentifier(), thisMessage);
			}
		}		
		String fbid = ThisUserConfig.getInstance().getString(ThisUserConfig.BBD_ID);
		mImageURL = StringUtils.getFBPicURLFromFBID(fbid);					
		mSenderThread = new SenderThread();
		mSenderThread.start();
		
		Logger.i(TAG, "chatadapter created for:" + mParticipant);
	}

    public void resetChatOnConnection(final Chat chat) {
        mSmackChat.removeMessageListener(mMsgListener);
        mSmackChat = chat;
        mSmackChat.addMessageListener(mMsgListener);
    }
	
	private void addMessageToList(Message msg)
	{
		mMessages.add(msg);
		ChatHistory.addtoChatHistory(msg);
	}
	
	private void updateMessageStatusInList(Message msg, int status)
	{
		msg.setStatus(status);
		ChatHistory.updateStatus(msg.getUniqueMsgIdentifier(), status);
	}
	
	@Override
	public void sendMessage(Message msg) throws RemoteException {
		// here we just put on queue
		try {
			mMsgqueue.put(msg);
			Logger.i(TAG, "added msg in queue of:" + mParticipant);
			if (msg.getType() == Message.MSG_TYPE_CHAT)
				addMessageToList(msg);
		} catch (InterruptedException e) {
			Logger.e(TAG, "unable to put msg on queue of:" + mParticipant);
			e.printStackTrace();
		}

	}
	
	@Override
	public void setOpen(boolean value) throws RemoteException {
		Logger.i(TAG, "chat open set to"+value+" for "+mParticipant	);
		mIsOpen = value;

	}
  
	@Override
	public void addMessageListener(IMessageListener listener)
			throws RemoteException {
		if (listener != null)
			mRemoteListeners.register(listener);

	}

	@Override
	public void removeMessageListener(IMessageListener listener) {
		if (listener != null) {
			mRemoteListeners.unregister(listener);
		}
	}

	private class SenderThread extends Thread {

	@Override
	public void run() {
		Message m = null;
		while (true) {
			boolean msgsent = false;
			try {
				if (m == null)
					m = mMsgqueue.take();

				switch (m.getType()) {
				case Message.MSG_TYPE_CHAT:
					msgsent = sendChatMessage(m);
					break;
				case Message.MSG_TYPE_ACKFOR_DELIVERED:
				case Message.MSG_TYPE_ACKFOR_BLOCKED:	
					msgsent = sendAck(m);
					break;						
				case Message.MSG_TYPE_NEWUSER_BROADCAST:
					msgsent = sendBroadCastMessage(m);
					break;

				}
			} catch (InterruptedException e1) {
				if (Platform.getInstance().isLoggingEnabled()) Log.e(TAG, "not able to take msg from queue");
				e1.printStackTrace();
			}
			if (!msgsent)
				try {
					synchronized (mMsgqueue) {
						mMsgqueue.wait();
					}

				} catch (InterruptedException e) {
					if (Platform.getInstance().isLoggingEnabled()) Log.e(TAG,"couldnt wait on msg queue after trying to send");
					e.printStackTrace();
				}
			else
				m = null; // if sent put m = null so it picks next msg
		}
	}
	}

	public void notifyMsgQueue() {
		synchronized (mMsgqueue) {
			mMsgqueue.notify();
		}
		
		//start prev msg thread too
		if(!mSentButServerAckNotReceivedMap.isEmpty())
		{
			timer = new Timer();
            timer.schedule(new ReSenderThread(), DELAY);
		}
	}

	private class SBMsgListener implements MessageListener {

		// first of all the msg comes here
		// we have different msg types

		@Override
		public void processMessage(Chat chat,
				org.jivesoftware.smack.packet.Message message) {			
			Message msg = new Message(message);
			if (Platform.getInstance().isLoggingEnabled()) Log.d(TAG, "new msg of type:"+msg.getType());
			if (Platform.getInstance().isLoggingEnabled()) Log.d(TAG, "chat is open?" + mIsOpen);
            if (msg.getInitiator() == null) {
                Logger.i(TAG, "Dropping message as no initiator present");
                return;
            }
			// if broadcast message from new user then do getMatch req
			if (msg.getType() == Message.MSG_TYPE_NEWUSER_BROADCAST) {
                //do nothing, broadcast through chat deprecated

				/*// caution..call back listener to chatwindow might not be
				// registered yet for this chat
				// listener get registered only when chat window opens				
				
				if(!ThisAppConfig.getInstance().getBool(ThisAppConfig.NEWUSERPOPUP) || BlockedUser.isUserBlocked(msg.getInitiator()))
				{
					//ToastTracker.showToast("new user broadcast received but its disabled");
					return;
				}			
				
				String thisNearbyUserUSERID = (String) message
						.getProperty(Message.USERID);
				int daily_insta_type = (Integer) message
						.getProperty(Message.DAILYINSTATYPE);
				
				//ToastTracker.showToast("got broadcast from userid:"	+ thisNearbyUserUSERID);
				GetNewUserInfoAndShowPopupRequest req = new GetNewUserInfoAndShowPopupRequest(
						thisNearbyUserUSERID, daily_insta_type);
				SBHttpClient.getInstance().executeRequest(req);
				return;*/
			}else if(msg.getType() == Message.MSG_TYPE_ACKFOR_SENT)
			{
				// this is ack from server for msg reached server
				//Logger.d(TAG, "sent but not acked size:"+mSentButServerAckNotReceivedMap.size());
				Message origMessage = mSentButServerAckNotReceivedMap.get(msg.getUniqueMsgIdentifier());
				//Logger.d(TAG, "sent but not acked orig msg:"+origMessage.getBody());
				updateMessageStatusInList(origMessage, ChatMessage.SENT);				
				synchronized (mSentButServerAckNotReceivedMap)
				{
					mSentButServerAckNotReceivedMap.remove(origMessage.getUniqueMsgIdentifier());
				}				
				synchronized (mSentNotDeliveredMsgsMap){
					mSentNotDeliveredMsgsMap.put(msg.getUniqueMsgIdentifier(), origMessage);
				}
				Logger.d(TAG, "got ack from server for:"+origMessage.getBody());
				if (mIsOpen) {
					if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG, "chat is open,sending ack to window ");
					callListeners(msg);
				}
				return;
			}			
			else if (msg.getType() == Message.MSG_TYPE_ACKFOR_DELIVERED ||
				msg.getType() == Message.MSG_TYPE_ACKFOR_BLOCKED) 
			{
				// ack has same unique id as the msg
				// we are doing so as we cant send long in body
                if (("").equals(msg.getInitiator()) || ("").equals(msg.getBody()))
					return;
				// do not add ack to list
				// ack msgs have time in body
                Message origMsg;
                synchronized (mSentNotDeliveredMsgsMap) {
                    origMsg = mSentNotDeliveredMsgsMap.get(msg
						.getUniqueMsgIdentifier());
                }               
				if (origMsg != null) {
					origMsg.setTimeStamp((String)JivePropertiesManager.getProperty(message,Message.TIME));
					if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG, "got ack for msg: " + origMsg.getBody());
					if(msg.getType() == Message.MSG_TYPE_ACKFOR_BLOCKED)
						updateMessageStatusInList(origMsg, ChatMessage.BLOCKED);						
					else
						updateMessageStatusInList(origMsg, ChatMessage.DELIVERED);					
                    synchronized (mSentNotDeliveredMsgsMap) {
                        mSentNotDeliveredMsgsMap.remove(msg.getUniqueMsgIdentifier());
                    }
				} else {
					Logger.d(TAG,"got ack but not msg uniqid: "	+ msg.getUniqueMsgIdentifier()+",sent not del size:"+mSentNotDeliveredMsgsMap.size());
				}
				if (mIsOpen) {
					if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG, "chat is open,sending ack to window ");
					callListeners(msg);
				}
				return;
			}
			else if (msg.getType() == Message.MSG_TYPE_CHAT) {
				// this is chat coming from outside,send ack to this msg
                Logger.i(TAG, "Got chat with msg id: " + msg.getUniqueMsgIdentifier() + " and body: " + msg.getBody());
				try {
					Message ackmsg = null;					
					if (BlockedUser.isUserBlocked(msg.getInitiator()))
					{
					 ackmsg = new Message(msg.getFrom(),Message.MSG_TYPE_ACKFOR_BLOCKED);
					 ackmsg.setUniqueMsgIdentifier(msg.getUniqueMsgIdentifier());
					 sendMessage(ackmsg);
					 //return early if this user blocked
					 return;
					}
					else
						ackmsg = new Message(msg.getFrom(),Message.MSG_TYPE_ACKFOR_DELIVERED);	
					ackmsg.setUniqueMsgIdentifier(msg.getUniqueMsgIdentifier());
					sendMessage(ackmsg);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                if (receivedChatIds.contains(msg.getUniqueMsgIdentifier())) {
                    Logger.i(TAG, "Already delivered message with id : " + msg.getUniqueMsgIdentifier() + " and body :" + msg.getBody());
                    //already delivered message
                    return;
                }
				//active chat for incoming added here and for outgoing added in chatwindow				
				if (mMessages.size() == HISTORY_MAX_SIZE)
					mMessages.remove(0);
				msg.setStatus(ChatMessage.RECEIVED);				
				msg.setTimeStamp((String) JivePropertiesManager.getProperty(message,Message.TIME));			
				
				addMessageToList(msg);
                if (receivedChatIds.size() == MAX_RECVD_IDS) {
                    receivedChatIds.pollFirst();
                }
                receivedChatIds.add(msg.getUniqueMsgIdentifier());

				if (mIsOpen) {
					if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG, "chat is open");
					callListeners(msg);
				} else {
					if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG, "chat not open,Sending notification");
					String participant_name = msg.getSubject();
                    if (participant_name.equals(""))
						participant_name = "Unknown";
					
					mChatManager.notifyChat(msg.getInitiator().hashCode(), msg.getInitiator(),participant_name,msg.getBody());

				}

			}
		}

	}

	private boolean sendAck(Message msg) {
		org.jivesoftware.smack.packet.Message msgToSend = new org.jivesoftware.smack.packet.Message();
		// msg type is overritten by smack so add property so need to set as
		// property
		// msgToSend.setType(org.jivesoftware.smack.packet.Message.Type.headline);
		JivePropertiesManager.addProperty(msgToSend,Message.UNIQUEID, msg.getUniqueMsgIdentifier());		
		JivePropertiesManager.addProperty(msgToSend,Message.SBMSGTYPE,msg.getType());
		JivePropertiesManager.addProperty(msgToSend,Message.TIME,StringUtils.gettodayDateInFormat("hh:mm"));
		try {
			mSmackChat.sendMessage(msgToSend);
			if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG, "ack message sent  ");
		} catch (NotConnectedException e) {
			// TODO retry sending msg?
			if (Platform.getInstance().isLoggingEnabled()) Log.e(TAG, "couldnt send ack");
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

    private org.jivesoftware.smack.packet.Message createSmackMessage(Message msg) {
		// every chat msg should havae:
		// 1) unique id
		// 2) msg type
		// 3) time		
        org.jivesoftware.smack.packet.Message smackMsg = new org.jivesoftware.smack.packet.Message();
		String msgBody = msg.getBody();
        smackMsg.setBody(msgBody);
        smackMsg.setSubject(msg.getSubject());
        JivePropertiesManager.addProperty(smackMsg, Message.UNIQUEID, msg.getUniqueMsgIdentifier());    
        JivePropertiesManager.addProperty(smackMsg,Message.SBMSGTYPE, Message.MSG_TYPE_CHAT);
        JivePropertiesManager.addProperty(smackMsg,Message.TIME, msg.getTimestamp());
        return smackMsg;
    }

    private boolean sendChatMessage(Message msg) {
        Logger.i(TAG, "message sending to " + msg.getTo() + " Body :" + msg.getBody());

        org.jivesoftware.smack.packet.Message msgToSend = createSmackMessage(msg);
	
		try {							
            synchronized (mSentButServerAckNotReceivedMap) {
                boolean wasEmpty = mSentButServerAckNotReceivedMap.isEmpty();
                mSentButServerAckNotReceivedMap.put(msg.getUniqueMsgIdentifier(), msg);  
                if (wasEmpty) {
                    timer = new Timer();
                    timer.schedule(new ReSenderThread(), DELAY);
                }
            }
			mSmackChat.sendMessage(msgToSend);
            Logger.i(TAG, "chat message sent to " + msg.getTo());
			
			
		} catch (NotConnectedException e) {
			// TODO retry sending msg?
			if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG, "message sending to had xmpp exception" + msg.getTo());
			updateMessageStatusInList(msg, ChatMessage.SENDING_FAILED);
			try {
				if (isOpen())
					callListeners(msg);
			} catch (RemoteException e1) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			e.printStackTrace();
		} catch (IllegalStateException e) {
            Logger.e(TAG, "Error in sending message", e);
			return false;
		}
		// we do a callback to update this msg status to sent or sending failed
		// no user might have switched chat after sending msg, in that case we
		// wont get a
		// message in current chat window with this unique identifier. But later
		// when it fetches all
		// msgs it ll get status as sent/failed		
		return true;
	}

	private boolean sendBroadCastMessage(Message msg) {
		// every broadcast msg should havae:
		// 1) unique id
		// 2) msg type
		// 3) user id
		// 4) dailyinstatype
		
		org.jivesoftware.smack.packet.Message msgToSend = new org.jivesoftware.smack.packet.Message();
		JivePropertiesManager.addProperty(msgToSend,Message.SBMSGTYPE,Message.MSG_TYPE_NEWUSER_BROADCAST);
		JivePropertiesManager.addProperty(msgToSend,Message.UNIQUEID, System.currentTimeMillis());
		msgToSend.setSubject("");
		msgToSend.setBody("");
		
		try {
			mSmackChat.sendMessage(msgToSend);
			if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG, "broadcast message sent to " + msg.getTo());
		} catch (NotConnectedException e) {
			// TODO retry sending msg?
			if (Platform.getInstance().isLoggingEnabled()) Log.e(TAG, "couldnt send broadcast");
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private void callListeners(Message msg) {
		int n = mRemoteListeners.beginBroadcast();
		for (int i = 0; i < n; i++) {
			IMessageListener listener = mRemoteListeners.getBroadcastItem(i);
			try {
				if (listener != null)
					listener.processMessage(ChatAdapter.this, msg);
			} catch (RemoteException e) {
				if (Platform.getInstance().isLoggingEnabled()) Log.w(TAG, "Error while diffusing message to listener", e);
			}
		}
		mRemoteListeners.finishBroadcast();
	}

	@Override
	public boolean isOpen() throws RemoteException {
		return mIsOpen;
	}

	@Override
	public String getParticipant() throws RemoteException {

		return mParticipant;
	}

	@Override
	public List<Message> getMessages() throws RemoteException {
		return mMessages;
    }

    class ReSenderThread extends TimerTask {
    	
        @Override
        public void run() {
            Logger.i(TAG, "Resender thread resumed..");
            boolean wasAnyMsgResent = false;
           
            long now = System.currentTimeMillis();
            synchronized (mSentButServerAckNotReceivedMap) {
                for (Map.Entry<Long, Message> entry : mSentButServerAckNotReceivedMap.entrySet()) {
                    Logger.i(TAG, "Retrieved message with Id :" + entry.getKey());
                    if (entry.getKey() > (now - THRESHOLD_DELAY)) {
                        Logger.i(TAG, "Message not old enough");
                        break;
                    }
                    
                    try {
                        Logger.i(TAG, "Resending message with id :" + entry.getKey());
                        mSmackChat.sendMessage(createSmackMessage(entry.getValue()));
                        wasAnyMsgResent = true;
                    } catch (Exception e) {
                    	//we might have gone offline. stop thread then
                        Logger.e(TAG, "Encountered exception while resending message", e);
                    }
                }
            }

            if (wasAnyMsgResent) {
                timer = new Timer();
                timer.schedule(new ReSenderThread(), DELAY);
            }
        }
	}

}
