package com.BhakBhosdi.ChatService;

import com.BhakBhosdi.ChatService.Message;
import com.BhakBhosdi.ChatClient.IMessageListener;

interface IChatAdapter {

void sendMessage(in Message message);
void setOpen(in boolean value);
void addMessageListener(in IMessageListener listener);
void removeMessageListener(IMessageListener listener) ;
boolean isOpen();
String getParticipant();
List<Message> getMessages();
}