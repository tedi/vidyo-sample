package com.vidyo.VidyoClientLib;

import java.io.Serializable;

public class LmiAndroidJniMessageCallbacks extends LmiAndroidJniCallback implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * This string identifies the Message callback method.
	 * 
	 * void MessageOutMsgCallback(String message)
	 * Where:
	 *   message : output message string
	 */
	String mMessageOutMsgCallback;
	
	public LmiAndroidJniMessageCallbacks() {
		super();
		mMessageOutMsgCallback = "";
	}
	
	/**
	 * Constructor which identifies the instance of this object to use when communicating with the JNI as well
	 * as the names of all of the JNI callbacks to be used for Message related communications from the JNI.
	 * @param object object of string that will identify the context that the callbacks are located
	 * @param messageOutMsgCallback string that identifies the callback method for Message Received callbacks
	 */
	public LmiAndroidJniMessageCallbacks(Object object, String messageOutMsgCallback) {
		super(object);
		mMessageOutMsgCallback = messageOutMsgCallback;
	}
	
	/*
	 * Access functions to get the callback method names
	 */
	
	public String getMessageOutMsgCallback() {
		return mMessageOutMsgCallback;
	}
}
