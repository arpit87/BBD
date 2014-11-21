package com.bakarapp.ChatService;

import com.bakarapp.ChatClient.IMessageListener;
import com.bakarapp.ChatClient.IChatManagerListener;
import com.bakarapp.ChatService.IChatAdapter;

interface IChatManager {

	IChatAdapter createChat(in String participant, in IMessageListener listener);
	void deleteChatNotification(IChatAdapter chat);
	void addMessageListener(IMessageListener listener);
    void removeMessageListener(IMessageListener listener);
    IChatAdapter getChat(String participant);
    void setOpen(in boolean value);
    boolean isOpen();
}