package com.bakarapp.ChatClient;


import com.bakarapp.ChatService.IChatAdapter;
import com.bakarapp.ChatService.Message;

interface IMessageListener {

	void processMessage(in Message msg);
}
