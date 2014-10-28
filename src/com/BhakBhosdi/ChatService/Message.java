package com.BhakBhosdi.ChatService;



import org.jivesoftware.smackx.jiveproperties.JivePropertiesManager;

import android.os.Parcel;
import android.os.Parcelable;

import com.BhakBhosdi.ChatClient.ChatMessage;
import com.BhakBhosdi.HTTPServer.ServerConstants;
import com.BhakBhosdi.Util.StringUtils;


//mFrom,to should have ip appended
//getParticipant gives name widout ip
public class Message implements Parcelable {

	//private static String TAG = "com.BhakBhosdi.ChatService.Message";
/** Normal message type. Theese messages are like an email, with subject. */
public static final int MSG_TYPE_NORMAL = 100;

/** Chat message type. */
public static final int MSG_TYPE_CHAT = 200;

public static final int MSG_TYPE_NEWUSER_BROADCAST = 300;

/** Error message type. */
public static final int MSG_TYPE_ERROR = 400;

public static final int MSG_TYPE_INFO = 500;

public static final int MSG_TYPE_ACKFOR_DELIVERED = 600;
public static final int MSG_TYPE_ACKFOR_BLOCKED = 700;
public static final int MSG_TYPE_ACKFOR_SENT = 800;

public static final String USERID = "user_id";
public static final String FBID = "fb_id";
public static final String UNIQUEID = "unique_id";
public static final String SBMSGTYPE = "sb_msg_type";
public static final String TIME = "time";
public static final String DAILYINSTATYPE = "daily_insta_type";


/** Parcelable.Creator needs by Android. */
public static final Parcelable.Creator<Message> CREATOR = new Parcelable.Creator<Message>() {

@Override
public Message createFromParcel(Parcel source) {
    return new Message(source);
}

@Override
public Message[] newArray(int size) {
    return new Message[size];
}
};



private int mType = (int)Message.MSG_TYPE_CHAT;
private String mBody = "";
private String mSubject=""; //we are sending participant name in subject
private String mTo ="";
private String mFrom ="";
private String mThread = "";
private String mTime = "";
private String mImageURL = "";
private int mStatus = ChatMessage.UNKNOWN;
private long mUniqueMsgIdentifier = 0;
private int mDailyInstaType = -1;


// TODO ajouter l'erreur

/**
 * Constructor.
 * @param to the destinataire of the message
 * @param type the message type
 */
public Message(final String to, final int type) {
mTo = to ;
mType = type;
mBody = "";
mSubject = "";
mThread = "";
mFrom = "";
mTime = "";
}

public Message(final String to, final String from, final String body, final String time,
               final int type, final int status, final long unique_id,final String subject ){
    mTo = to;
    mFrom = from;
    mBody = body;    
    mTime = time;
    mType = type;
    mStatus = status;
    mUniqueMsgIdentifier = unique_id;
    mSubject = subject ;
}
/**
 * Constructor a message of type chat.
 * @param to the destination of the message
 */
public Message(final String to) {
this(to, MSG_TYPE_CHAT);
}

/**
 * Construct a message from a smack message packet.
 * @param smackMsg Smack message packet
 */
public Message(final org.jivesoftware.smack.packet.Message smackMsg) {
mTo = smackMsg.getTo();
mFrom = smackMsg.getFrom();
mBody = smackMsg.getBody();
mSubject = smackMsg.getSubject();
if(mSubject!= null && mSubject.equals(ServerConstants.CHATADMINACKFROM))
{
	mType = MSG_TYPE_ACKFOR_SENT;
    mUniqueMsgIdentifier = Long.parseLong(smackMsg.getBody());
}
else 
{
	mType = (Integer) JivePropertiesManager.getProperty(smackMsg,SBMSGTYPE);    
    mThread = smackMsg.getThread();    
    mUniqueMsgIdentifier = (Long) JivePropertiesManager.getProperty(smackMsg,UNIQUEID); //this also doubles up as time
}
}


/**
 * Construct a message from a parcel.
 * @param in parcel to use for construction
 */
private Message(final Parcel in) {
mType = in.readInt();
mTo = in.readString();
mBody = in.readString();
mSubject = in.readString(); //contains full name of participant
mThread = in.readString();
mFrom = in.readString();
mStatus = in.readInt();
mUniqueMsgIdentifier = in.readLong();
mTime = in.readString();
mImageURL = in.readString();
}

/**
 * {@inheritDoc}
 */
@Override
public void writeToParcel(Parcel dest, int flags) {
// TODO Auto-generated method stub
dest.writeInt(mType);
dest.writeString(mTo);
dest.writeString(mBody);
dest.writeString(mSubject);
dest.writeString(mThread);
dest.writeString(mFrom);
dest.writeInt(mStatus);
dest.writeLong(mUniqueMsgIdentifier);
dest.writeString(mTime);
dest.writeString(mImageURL);
}

/**
 * Get the type of the message.
 * like ACK,CHAT
 * @return the type of the message.
 */
public int getType() {
return mType;
}

/**
 * Set the type of the message.
 * @param type the type to set
 */
public void setType(int type) {
mType = type;
}

/**
 * Get the body of the message.
 * @return the Body of the message
 */
public String getBody() {
return mBody;
}

/**
 * Set the body of the message.
 * @param body the body to set
 */
public void setBody(String body) {
mBody = body;
}

/**
 * Get the subject of the message.
 * @return the subject
 */
public String getSubject() {
return mSubject;
}

/**
 * Set the subject of the message.
 * @param subject the subject to set
 */
public void setSubject(String subject) {
mSubject = subject;
}

/**
 * Get the destinataire of the message.
 * @return the destinataire of the message
 */
public String getTo() {
return mTo;
}

/**
 * Set the destinataire of the message.
 * @param to the destinataire to set
 */
public void setTo(String to) {
mTo = to;
}

/**
 * Set the from field of the message.
 * @param from the mFrom to set
 */
public void setFrom(String from) {
this.mFrom = from;
}

/**
 * Get the from field of the message.
 * @return the mFrom
 */
public String getFrom() {
return mFrom;
}

/**
 * Get the from field of the message.
 * @return the mFrom
 */
public String getReceiver() {
return StringUtils.parseName(mTo);
}

public String getInitiator() {
return StringUtils.parseName(mFrom);
}

/**
 * Get the thread of the message.
 * @return the thread
 */
public String getThread() {
return mThread;
}

/**
 * Set the thread of the message.
 * @param thread the thread to set
 */
public void setThread(String thread) {
mThread = thread;
}

/**
 * {@inheritDoc}
 */
@Override
public int describeContents() {
// TODO Auto-generated method stub
return 0;
}

public String getTimestamp() {
	// TODO Auto-generated method stub
	return mTime;
}

public int getStatus() {
	return mStatus;
}

public void setStatus(int sent) {
	this.mStatus = sent;
}

public long getUniqueMsgIdentifier() {
	return mUniqueMsgIdentifier;
}

public void setUniqueMsgIdentifier(long l) {
	this.mUniqueMsgIdentifier = l;
}


public void setTimeStamp(String mTime) {
	this.mTime = mTime;
}


public String getImageURL() {
	return mImageURL;
}

public void setImageURL(String mImageURL) {
	this.mImageURL = mImageURL;
}

public void setDailyInstaType(int mDailyInstaType) {
	this.mDailyInstaType = mDailyInstaType;
}

public int getDailyInstaType() {
	return mDailyInstaType;
}

}
