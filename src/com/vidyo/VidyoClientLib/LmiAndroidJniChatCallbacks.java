package com.vidyo.VidyoClientLib;

import java.io.Serializable;

public class LmiAndroidJniChatCallbacks extends LmiAndroidJniCallback implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * This string identifies the Conference Status callback method.
	 * 
	 * void ChatMsgReceivedCallback(int status, int error, String message)
	 * Where:
	 *   status  : one of the STATUS values defined below
	 *   error   : one of the FAILURE values defined below, or specific to the STATUS
	 *   message : string message specific to the status or value of NULL
	 */
	String mChatMsgReceivedCallback;
	
	public LmiAndroidJniChatCallbacks() {
		super();
		mChatMsgReceivedCallback = "";
	}
	
	/**
	 * Constructor which identifies the instance of this object to use when communicating with the JNI as well
	 * as the names of all of the JNI callbacks to be used for Chat related communications from the JNI.
	 * @param object object of string that will identify the context that the callbacks are located
	 * @param chatMsgReceivedCallback string that identifies the callback method for Chat Message Received callbacks
	 */
	public LmiAndroidJniChatCallbacks(Object object, String chatMsgReceivedCallback) {
		super(object);
		mChatMsgReceivedCallback = chatMsgReceivedCallback;
	}
	
	/*
	 * Access functions to get the callback method names
	 */
	
	public String getChatMsgReceivedCallback() {
		return mChatMsgReceivedCallback;
	}
}
