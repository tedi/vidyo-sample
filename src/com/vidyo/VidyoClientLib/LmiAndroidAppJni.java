/**
 * LmiAndroidAppJni.java
 * 
 * Copyright 2013-2014 Vidyo Inc. All rights reserved.
 */
package com.vidyo.VidyoClientLib;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class LmiAndroidAppJni extends android.app.Application {
	private static String TAG = "VidyoClientLib_ApplicationJni";

	protected static LmiAndroidAppJni This = null;

	static LmiAndroidAppJni getAppObject(){
		return This;
	}
	
	/*
	 *****************************************************************************************
	 * VidyoMobile Updated definitions
	 *****************************************************************************************
	 */

	protected static Context context;

	protected static String fontFileName = null;

	protected static Handler conferenceMessageHandle = null; //conference activity message handle

	/************************************************************************************************
	 * LOGIN Section Begin:
	 * 
	 * This section contains methods and definitions used by the Login related operations of the 
	 * application
	 ************************************************************************************************/

	/**
	 * Message handler used by the external link related activities (ExternalLinkHandler.java)
	 */
	protected static Handler guestLinkHandle = null;
	

	/*
	 *****************************************************************************************
	 * Public ClientLib APIs
	 *****************************************************************************************
	 */

	/*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
	 * Public ClientLib: 
	 * Initialization, shutdown and administrative APIs
	 *=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*/

	/**
	 * This function needs to be called before any other JNI API to initialize the Android JNI environment.
	 * @return true is returned if the Android JNI environment is initialized successfully.
	 */
	public native boolean LmiAndroidJniInitialize();

	/**
	 * This function should be called when the Android JNI environment is no longer needed.
	 */
	public native void LmiAndroidJniUninitialize();

	/**
	 * Set the logging string.
	 * NOTE: should be called before Construct()
	 * @param logString
	 */
	public native void LmiAndroidJniSetLogging(String logString);


	/**
	 * Calls AndroidVidyoClientStart() and subsequently VidyoClientStart()
	 * @param caFileName
	 * @param MachineID
	 * @param androidConfigDirName
	 * @param androidLogDirName
	 * @param installedDirName
	 * @param activity
	 * @return
	 */
	public native long LmiAndroidJniConstruct(String caFileName, String MachineID, String androidConfigDirName, String androidLogDirName, String installedDirName, Activity activity);
	
	/**
	 * Calls VidyoClientUninitialize()
	 */
	public native void LmiAndroidJniDispose();

	/**
	 * Calls VIDYO_CLIENT_IN_EVENT_SET_FONT_FILE
	 * @param fontFileName path name of the System font file to use
	 */
	public native void LmiAndroidJniSetSystemFont(String fontFileName);

	/**
	 * Calls VIDYO_CLIENT_REQUEST_SET_CONFIGURATION
	 * Sets config.enableBackgrounding
	 * @param enableBackgrounding true if backgrounding should be turned on
	 */
	public native void LmiAndroidJniSetEnableBackgrounding(boolean enableBackgrounding);

	/**
	 * Calls VIDYO_CLIENT_REQUEST_GET_CONFIGURATION
	 * @return config.enableBackgrounding
	 */
	public native boolean LmiAndroidJniGetEnableBackgrounding();

	/**
	 * Calls LmiAndroidRegisterDefaultApp()
	 * Must be called before can use DeviceManager functionality.  Also, must be called if the
	 * previous Activity has been destroyed.
	 * @param conferenceAct
	 */
	public native void LmiAndroidJniRegisterDefaultActivity(Activity conferenceAct);
	
	/**
	 * Calls LmiAndroidUnregisterDefautApp()
	 * Should be called before the Activity is destroyed.
	 */
	public native void LmiAndroidJniUnregisterDefaultActivity();

	/*
	 * Startup/Shutdown Callback
	 */
	
	/**
	 * This callback is called when a VIDYO_CLIENT_OUT_EVENT_LOGIC_STARTED event is received
	 * 
	 * 
	 * TODO: THIS NEEDS TO BE MOVED TO A CALLBACK
	 * 
	 * 
	 */
	private void LmiAndroidAppEventLogicStartedCallback() {
		Log.d(TAG, "eventLogicStartedCallback called");
		
		// Set the system font file
		if (fontFileName != null)
			LmiAndroidJniSetSystemFont(fontFileName);
	}
	


	/*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
	 * Public ClientLib: 
	 * Logging In/Out Related APIs
	 *=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*/
	
	/**
	 * This method is called to setup callback from the Login processing. The LmiAndroidJniLoginCallbacks
	 * class is used to define the methods in the application that will be called for each of the
	 * specific login callbacks.
	 * @param callbacks
	 */
	public native void LmiAndroidJniLoginSetCallbacks(LmiAndroidJniLoginCallbacks callbacks);
	
	
	/**
	 * Calls VIDYO_CLIENT_IN_EVENT_LOGIN
	 * Maintains the login state information within the VidyoClient JNI area.
	 * @param server
	 * @param user
	 * @param password
	 * @return
	 */
	public native int LmiAndroidJniLogin(String server, String user, String password);

	/**
	 * Calls VIDYO_CLIENT_IN_EVENT_SIGNOFF
	 */
	public native void LmiAndroidJniLogout();

	/**
	 * Calls VIDYO_CLIENT_IN_EVENT_LOGIN_CANCEL
	 */
	public native void LmiAndroidJniLoginCancel();

	/**
	 * Calls VIDYO_CLIENT_IN_EVENT_ROOM_LINK
	 * @param host
	 * @param port
	 * @param key
	 * @param userName
	 * @param pin
	 * @param useHTTP
	 */
	public native void LmiAndroidJniHandleGuestLink(String host, int port, String key, String userName, String pin, boolean useHTTP);

	
	// Public ClientLib: Login Configuration APIs

	/**
	 * Calls VIDYO_CLIENT_REQUEST_GET_CONFIGURATION 
	 * @return config.enableAutoLoginIn
	 */
	public native boolean LmiAndroidJniGetAutomaticLogin();
	
	/**
	 * Calls VIDYO_CLIENT_REQUEST_SET_CONFIGURATION to set config.enableAutoLoginIn
	 * @param enable
	 * @return
	 */
	public native boolean LmiAndroidJniSetAutomaticLogin(boolean enable);

	/**
	 * Calls VIDYO_CLIENT_REQUEST_GET_CONNECTIVITY_INFO
	 * @return info.portalVersion
	 */
	public native String LmiAndroidJniGetPortalVersion();

	/**
	 * Calls VIDYO_CLIENT_REQUEST_GET_SECURE_CONNECTION
	 * @return secureConnection.secureConnectionState
	 */
	public native boolean LmiAndroidJniIsSecureConnection();

	/**
	 * Calls VIDYO_CLIENT_REQUEST_GET_CONFIGURATION
	 * @return configuration.portalAddress
	 */
	public native String LmiAndroidJniGetPortalAddress();

	/**
	 * Calls VIDYO_CLIENT_REQUEST_GET_CONFIGURATION
	 * @return config.userID
	 */
	public native String LmiAndroidJniGetUserID();

	/**
	 * Calls VIDYO_CLIENT_REQUEST_SET_CONFIGURATION
	 * Sets the config.enableEulaAgreed value
	 * @param EulaAgreed whether EULA was agreed to or not
	 */
	public native void LmiAndroidJniSetEulaAgreed(boolean EulaAgreed);
	
	/**
	 * Calls VIDYO_CLIENT_REQUEST_GET_CONFIGURATION
	 * @return the current value of the config.enableEulaAgreed value
	 */
	public native boolean LmiAndroidJniGetEulaAgreed();
	
	/**
	 * Calls VIDYO_CLIENT_REQUEST_SET_CONFIGURATION
	 * Sets the config.verifyCertPortalAcct value
	 * @param portalCertString
	 */
	public native void LmiAndroidJniSetAcceptPortalCertFailure(String portalCertString);

	
	// Public ClientLib: Login: Portal History Related Public APIs
	
	/**
	 * Calls VIDYO_CLIENT_REQUEST_GET_PORTAL_HISTORY
	 * @return array of PortalHistoryEntity objects
	 */
	public native PortalHistoryEntity [] LmiAndroidJniGetPortalHistory();


	
	// Public ClientLib: Login: Proxy Related Public APIs

	/**
	 * Calls VIDYO_CLIENT_REQUEST_SET_CONFIGURATION
	 * Sets the config.proxySettings value
	 * @param forceVidyoProxy
	 */
	public native void LmiAndroidJniSetForceVidyoProxy(boolean forceVidyoProxy);

	/**
	 * Calls VIDYO_CLIENT_REQUEST_SET_WEBPROXY_USERNAME_PASSWORD
	 * @param username
	 * @param password
	 * @param isSaved
	 */
	public native void LmiAndroidJniSetWebProxyUserPassword(String username, String password, boolean isSaved);
	
	/**
	 * Calls VIDYO_CLIENT_REQUEST_GET_CONFIGURATION
	 * To return config.webProxy settings
	 * @param webProx Web Proxy entity record to update
	 */
	public native void LmiAndroidJniGetWebProxySettings(WebProxyJniEntity webProx);

	/**
	 * Calls VIDYO_CLIENT_REQUEST_SET_CONFIGURATION
	 * Uses Private ClientLib APIs to set Web Proxy settings
	 * @param webProxySetting Web Proxy entity record to use to update settings
	 */
	public native void LmiAndroidJniSetWebProxySettings(WebProxyJniEntity webProxySetting);

	/*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
	 * Public ClientLib: 
	 * Conference Starting and Stopping related APIs
	 *=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*/

	/**
	 * This method is called to setup callback from the Conference processing. The
	 * LmiAndroidJniConferenceCallbacks class is used to define the methods in the application
	 * that will be called for each of the specific Conference callbacks.
	 * @param callbacks
	 */
	public native void LmiAndroidJniConferenceSetCallbacks(LmiAndroidJniConferenceCallbacks callbacks);
	

	/**
	 * Calls VIDYO_CLIENT_IN_EVENT_LEAVE
	 */
	public native void LmiAndroidJniLeave();

	/**
	 * Calls VIDYO_CLIENT_IN_EVENT_ANSWER
	 */
	public native void LmiAndroidJniAcceptCall();

	/**
	 * Calls VIDYO_CLIENT_IN_EVENT_CANCEL or VIDYO_CLIENT_IN_EVENT_LEAVE
	 */
	public native void LmiAndroidJniCancelCall();

	/**
	 * Calls VIDYO_CLIENT_IN_EVENT_DECLINE or VIDYO_CLIENT_IN_EVENT_LEAVE
	 */
	public native void LmiAndroidJniRejectCall();
	
	/*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
	 * Public ClientLib: 
	 * Conference Configuration related APIs
	 *=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*/

	/**
	 * Calls 
	 * @param videoPreference
	 */
	public native void LmiAndroidJniSetVideoPreferences(int videoPreference);

	/**
	 * Calls VIDYO_CLIENT_IN_EVENT_CONFERENCE_MEDIA_START
	 * @return
	 */
	public native boolean LmiAndroidJniStartMedia();

	
	/*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
	 * Public ClientLib: 
	 * Conference in call related APIs
	 *=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*/

	/**
	 * Calls VIDYO_CLIENT_IN_EVENT_SET_ORIENTATION
	 * @param orientation
	 */
	public native void LmiAndroidJniSetOrientation(int orientation);
	
	/**
	 * Calls VIDYO_CLIENT_IN_EVENT_RESIZE
	 * @param width
	 * @param height
	 */
	public native void LmiAndroidJniResize(int width, int height);

	/**
	 * Calls VIDYO_CLIENT_IN_EVENT_START_PLAYING_CONTINUOUS_DIAL_TONE
	 * @param dtmfKey
	 */
	public native void LmiAndroidJniSendDTMFKey(char dtmfKey);
	
	/**
	 * Calls VIDYO_CLIENT_PRIVATE_REQUEST_ANDROID_DO_RENDER
	 */
	public native void LmiAndroidJniRender();
	
	/**
	 * Calls VIDYO_CLIENT_PRIVATE_REQUEST_ANDROID_DO_SCENE_RESET
	 */
	public native void LmiAndroidJniRenderRelease();

	/**
	 * Calls one of VIDYO_CLIENT_PRIVATE_REQUEST_ANDROID_TOUCH_START_EVENT,
	 * VIDYO_CLIENT_PRIVATE_REQUEST_ANDROID_TOUCH_STOP_EVENT, or
	 * VIDYO_CLIENT_PRIVATE_REQUEST_ANDROID_TOUCH_MOVE_EVENT
	 * @param id
	 * @param type
	 * @param x
	 * @param y
	 */
	public native void LmiAndroidJniTouchEvent(int id, int type, int x, int y);

	/*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
	 * Public ClientLib: 
	 * Conference Far End Camera Control related APIs and callbacks
	 *=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*/

	/**
	 * Calls VIDYO_CLIENT_IN_EVENT_CONTROL_CAMERA
	 * @param command
	 * @return
	 */
	public native boolean LmiAndroidJniFeccSendControlCommand(int command);


	/*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
	 * Public ClientLib: 
	 * Conference camera related APIs
	 *=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*/

	/**
	 * Calls VIDYO_CLIENT_REQUEST_GET_CONFIGURATION
	 * @return the config.enableHideCameraOnJoin value
	 */
	public native boolean LmiAndroidJniGetAutoStartCamera();

	/**
	 * Calls VIDYO_CLIENT_REQUEST_SET_CONFIGURATION
	 * Sets the config.enableHideCameraOnJoin value
	 * @param autoStart whether to automatically start the camera when conference starts
	 */
	public native void LmiAndroidJniAutoStartCamera(boolean autoStart);
	
	/**
	 * Used to set whether the front or rear camera is used
	 * Calls VIDYO_CLIENT_REQUEST_SET_CONFIGURATION
	 * Sets config.currentCamera (0 or 1)
	 * @param camera
	 */
	public native void LmiAndroidJniSetCameraDevice(int camera);

	/**
	 * Used to mute or unmute the camera
	 * Calls VIDYO_CLIENT_IN_EVENT_MUTE_VIDEO
	 * @param muteCamera
	 */
	public native void LmiAndroidJniMuteCamera(boolean muteCamera);


	/**
	 * Calls VIDYO_CLIENT_REQUEST_GET_MUTED_VIDEO
	 * @return
	 */
	public native boolean LmiAndroidJniGetCameraMuted();

	/*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
	 * Public ClientLib: 
	 * Conference speaker related APIs
	 *=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*/

	/**
	 * Calls VIDYO_CLIENT_REQUEST_GET_CONFIGURATION
	 * @return the config.enableMuteSpeakerOnJoin value
	 */
	public native boolean LmiAndroidJniGetAutoStartSpeaker();
	
	/**
	 * Calls VIDYO_CLIENT_REQUEST_SET_CONFIGURATION
	 * Sets the config.enableMuteSpeakerOnJoin value
	 * @param autoStart whether to automatically start the speaker when conference starts
	 */
	public native void LmiAndroidJniAutoStartSpeaker(boolean autoStart);
	
	/**
	 * Calls VIDYO_CLIENT_REQUEST_SET_VOLUME_AUDIO_OUT
	 * @param volume
	 */
	public native void LmiAndroidJniSetSpeakerVolume(int volume);

	/**
	 * Calls VIDYO_CLIENT_IN_EVENT_MUTE_AUDIO_OUT
	 * @param muteSpeaker
	 */
	public native void LmiAndroidJniMuteSpeaker(boolean muteSpeaker);
	
	/*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
	 * Public ClientLib: 
	 * Conference microphone related APIs
	 *=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*/

	/**
	 * Calls VIDYO_CLIENT_REQUEST_GET_CONFIGURATION
	 * @return the config.enableMuteMicrophoneOnJoin value 
	 */
	public native boolean LmiAndroidJniGetAutoStartMicrophone();

	/**
	 * Calls VIDYO_CLIENT_REQUEST_SET_CONFIGURATION
	 * Sets the config.enableMuteMicrophoneOnJoin value
	 * @param autoStart whether to automatically start the microphone when conference starts
	 */
	public native void LmiAndroidJniAutoStartMicrophone(boolean autoStart);
	
	/**
	 * Calls VIDYO_CLIENT_IN_EVENT_MUTE_AUDIO_IN
	 * @param muteMicrophone
	 */
	public native void LmiAndroidJniMuteMicrophone(boolean muteMicrophone);

	/**
	 * Calls VIDYO_CLIENT_REQUEST_GET_MUTED_SERVER_AUDIO_IN
	 * @return
	 */
	public native boolean LmiAndroidJniGetMicrophoneMutedServer();

	/*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
	 * Public ClientLib: 
	 * Conference Echo Cancellation and Gain Control related APIs
	 *=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*/
	
	/**
	 * Calls VIDYO_CLIENT_REQUEST_SET_CONFIGURATION
	 * Sets the config.enableEchoCancellation value
	 * @param enable whether Echo Cancellation should be enabled or disabled
	 */
	public native void LmiAndroidJniSetEchoCancellation(boolean enable);
	
	/**
	 * Calls VIDYO_CLIENT_REQUEST_GET_CONFIGURATION
	 * @return the current config.enableEchoCancellation value
	 */
	public native boolean LmiAndroidJniGetEchoCancellation();
	
	/**
	 * Calls VIDYO_CLIENT_REQUEST_SET_CONFIGURATION
	 * Sets the config.enableAudioAGC value
	 * @param enable whether Automatic Gain Control should be enabled or disabled
	 */
	public native void LmiAndroidJniSetAutomaticGainControl(boolean enable);
	
	/**
	 * Calls VIDYO_CLIENT_REQUEST_GET_CONFIGURATION
	 * @return the current config.enableAudioAGC value
	 */
	public native boolean LmiAndroidJniGetAutomaticGainControl();


	/*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
	 * Public ClientLib: 
	 * Conference GUI/Menubar related APIs
	 *=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*/

	/**
	 * Calls VIDYO_CLIENT_IN_EVENT_ENABLE_BUTTON_BAR
	 * @param enableMenuBar
	 */
	public native void LmiAndroidJniEnableMenuBar(boolean enableMenuBar);

	/**
	 * Call this function to turn OFF Preview Mode
	 * Calls VIDYO_CLIENT_IN_EVENT_PREVIEW
	 */
	public native void LmiAndroidJniSetPreviewModeOFF();

	/**
	 * Call this function to turn on Preview Mode.
	 * Calls VIDYO_CLIENT_IN_EVENT_PREVIEW
	 * @param pip true if Preview Mode should be Picture in Picture
	 */
	public native void LmiAndroidJniSetPreviewModeON(boolean pip);

	/**
	 * Calls VIDYO_CLIENT_REQUEST_SET_BACKGROUND and VIDYO_CLIENT_PRIVATE_IN_EVENT_LAYOUT
	 */
	public native void LmiAndroidJniDisableAllVideoStreams();

	/**
	 * Calls VIDYO_CLIENT_REQUEST_SET_BACKGROUND
	 */
	public native void LmiAndroidJniSetBackground(boolean willBackground);

	/**
	 * Calls VIDYO_CLIENT_REQUEST_SET_BACKGROUND and VIDYO_CLIENT_PRIVATE_IN_EVENT_LAYOUT
	 */
	public native void LmiAndroidJniEnableAllVideoStreams();

	/**
	 * Calls VIDYO_CLIENT_REQUEST_GET_NUM_PARTICIPANTS
	 * @return
	 */
	public native int LmiAndroidJniGetParticiapntsNumber();

	/**
	 * Calls VIDYO_CLIENT_REQUEST_GET_DOCK_COUNTS
	 * @return
	 */
	public native int LmiAndroidJniGetVideoDockCount();

	/**
	 * Calls VIDYO_CLIENT_REQUEST_GET_DOCK_COUNTS
	 * @return
	 */
	public native boolean LmiAndroidJniGetVideoDockVisibility();

	/**
	 * Calls VIDYO_CLIENT_IN_EVENT_TOGGLE_VIDEO_DOCK
	 */
	public native void LmiAndroidJniToggleVideoDock();

	/**
	 * Calls VIDYO_CLIENT_REQUEST_GET_DOCK_COUNTS
	 * @return
	 */
	public native int LmiAndroidJniGetApplicationDockCount();
	
	/**
	 * Calls VIDYO_CLIENT_REQUEST_GET_DOCK_COUNTS
	 * @return
	 */
	public native boolean LmiAndroidJniGetApplicationDockVisibility();
	
	/**
	 * Calls VIDYO_CLIENT_IN_EVENT_TOGGLE_APPLICATION_DOCK
	 */
	public native void LmiAndroidJniToggleApplicationDock();


	/*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
	 * Public ClientLib: 
	 * Conference Chat related APIs
	 *=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*/
	
	/**
	 * This method is called to setup callback from the Chat processing. The
	 * LmiAndroidJniChatCallbacks class is used to define the methods in the application
	 * that will be called for each of the specific Chat callbacks.
	 * @param callbacks
	 */
	public native void LmiAndroidJniChatSetCallbacks(LmiAndroidJniChatCallbacks callbacks);
	
	/**
	 * Calls VIDYO_CLIENT_IN_EVENT_PRIVATE_CHAT
	 * @param uri
	 * @param message
	 */
	public native void LmiAndroidJniSendChatMsg(String uri, String message);
	
	/**
	 * Calls VIDYO_CLIENT_IN_EVENT_GROUP_CHAT
	 * @param message
	 */
	public native void LmiAndroidJniSendGroupChatMsg(String message);
	
	
	/*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
	 * Public ClientLib: 
	 * Misc. APIs
	 *=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*/
	
	/**
	 * Calls VIDYO_CLIENT_IN_EVENT_PLAY_SOUND
	 * @param audio
	 */
	public native void LmiAndroidJniPlayAudioWav(byte[] audio);

	/**
	 * This method is called to setup callback from the Chat processing. The
	 * LmiAndroidJniChatCallbacks class is used to define the methods in the application
	 * that will be called for each of the specific Chat callbacks.
	 * @param callbacks
	 */
	public native void LmiAndroidJniMessageSetCallbacks(LmiAndroidJniMessageCallbacks callbacks);
	
	
	/*
	 * this is used to load the 'VidyoClient' library on application startup. The
	 * library has already been unpacked into
	 * /data/data/com.vidyo.VidyoClient/lib/libdemolient.so at installation time
	 * by the package manager.
	 */
	static {
		System.loadLibrary("VidyoClientApp");
	}
}
