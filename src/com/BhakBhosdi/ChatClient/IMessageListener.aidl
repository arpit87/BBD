package com.BhakBhosdi.ChatClient;


import com.BhakBhosdi.ChatService.IChatAdapter;
import com.BhakBhosdi.ChatService.Message;

interface IMessageListener {

	void processMessage(in IChatAdapter chat,in Message msg);
}
