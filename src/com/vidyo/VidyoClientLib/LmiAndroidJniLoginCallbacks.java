package com.vidyo.VidyoClientLib;

import java.io.Serializable;

public class LmiAndroidJniLoginCallbacks extends LmiAndroidJniCallback implements Serializable {
	private static final long serialVersionUID = 1L;
	
	String mLoginStatusCallback;
	
	public LmiAndroidJniLoginCallbacks() {
		super();
		mLoginStatusCallback = "";
	}
	
	public LmiAndroidJniLoginCallbacks(Object object, String loginStatusCallback) {
		super(object);
		mLoginStatusCallback = loginStatusCallback;
	}
	
	public String getLoginStatusCallback() {
		return mLoginStatusCallback;
	}
	
	public static final int STATUS_LOGGING_IN = 1;
	public static final int STATUS_LOGGED_OUT = 2;
	public static final int STATUS_LOGIN_COMPLETE = 3;
	public static final int STATUS_DISCONNECT_FROM_GUESTLINK = 4;
	public static final int STATUS_GUEST_LOGIN_CONFERENCE_ENDED = 5;
	public static final int STATUS_GUEST_LOGOUT = 6;
	public static final int STATUS_PORTAL_PREFIX = 7;
	
	public static final int STATUS_GUEST_LOGIN_FAILED = -2;
	
	public static final int FAILURE_NONE = 0;
	public static final int FAILURE_SECURITY_CERTIFICATE = 1001;
	public static final int FAILURE_LOGIN_FAILED = 1002;
	public static final int FAILURE_LOGIN_INCORRECT = 1003;
	
	
	
	/**
	 * This method is called from the Native code to set the Portal Prefix value.
	 * @param portalPrefix string value of the Portal's Prefix value
	 * 
	 * LmiAndroidAppSetPortalPrefix(String portalPrefix) {
	 * loginStatus = STATUS_PORTAL_PREFIX;
	 * loginError = FAILURE_NONE;
	 * loginMsg = Portal Prefix;
	 */

	/**
	 * This method is called, from the JNI, when a login attempt failed due to an insecure Security Certificate.
	 * A message is sent to the login activity to allow the user to determine if they want to proceed.
	 * 
	 * LmiAndroidAppLoginFailedSecurityCertificate(String message)
	 * loginStatus = STATUS_LOGIN_COMPLETE;
	 * loginError = FAILURE_SECURITY_CERTIFICATE;
	 */
	
	/**
	 * this is called from the JNI layer to suggest a disconnect from a guest link occurred 
	 * a disconnected from guest link does imply a logged out state
	 * 
	 * LmiAndroidAppDisconnectFromGuestLink
	 * loginStatus = STATUS_DISCONNECT_FROM_GUESTLINK;
	 * loginError = FAILURE_NONE;
	 */
	
	/**
	 * this is called from the JNI layer to suggest a guest link conference just ended
	 * currently no special handling
	 * 
	 * LmiAndroidAppGuestLoginConferenceEnded()
	 * loginStatus = STATUS_GUEST_LOGIN_CONFERENCE_ENDED;
	 * loginError = FAILURE_NONE;
	 */

	/**
	 * this is called from the JNI layer to signal a logout after a guest session
	 * handled by calling disconnectFromGuestLink
	 * 
	 * LmiAndroidAppLogoutAfterGuestSession
	 * loginStatus = STATUS_GUEST_LOGOUT;
	 * loginError = FAILURE_NONE;
	 */

	/**
	 * Called from JNI layer when a previous request to join a room failed
	 * @param errCode soap error code result for the join operation
	 * 
	 * LmiAndroidAppErrorWhileJoiningRoomAsGuest
	 * loginStatus = STATUS_GUEST_LOGIN_FAILED;
	 * loginError = PRIVATE ERROR CODE;
	 */	

}
