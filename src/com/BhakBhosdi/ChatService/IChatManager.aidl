package com.BhakBhosdi.ChatService;

import com.BhakBhosdi.ChatClient.IMessageListener;
import com.BhakBhosdi.ChatClient.IChatManagerListener;
import com.BhakBhosdi.ChatService.IChatAdapter;

interface IChatManager {

	IChatAdapter createChat(in String participant, in IMessageListener listener);
	void deleteChatNotification(IChatAdapter chat);
	void addChatCreationListener(IChatManagerListener listener);
    void removeChatCreationListener(IChatManagerListener listener);
    IChatAdapter getChat(String participant);
}