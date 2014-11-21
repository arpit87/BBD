package com.bakarapp.ChatService;

import com.bakarapp.ChatService.Message;
import com.bakarapp.ChatClient.IMessageListener;

interface IChatAdapter {

void sendMessage(in Message message);
void setOpen(in boolean value);
void addMessageListener(in IMessageListener listener);
void removeMessageListener(IMessageListener listener) ;
boolean isOpen();
String getParticipant();
List<Message> getMessages();
}