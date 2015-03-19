package com.vidyo.VidyoClientLib;

import java.io.Serializable;

public class LmiAndroidJniConferenceCallbacks extends LmiAndroidJniCallback implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * This string identifies the Conference Status callback method.
	 * 
	 * void ConferenceStatusCallback(int status, int error, String message)
	 * Where:
	 *   status  : one of the STATUS values defined below
	 *   error   : one of the FAILURE values defined below, or specific to the STATUS
	 *   message : string message specific to the status or value of NULL
	 */
	String mConferenceStatusCallback;
	
	/**
	 * This string identifies the Conference Event callback method.
	 * 
	 * void ConferenceEventCallback(int event, boolean state)
	 * where:
	 *   event : one of the EVENT values defined beloe
	 *   state : boolean values specific to the EVENT
	 */
	String mConferenceEventCallback;
	
	/**
	 * This string identifies the Conference Share Event callback method.
	 * 
	 * void ConferenceShareEventCallback(int event, boolean state)
	 * where:
	 *   event : one of the EVENT_SHARE values defined below
	 *   URI   : String value of the URI associated with the share added/removed
	 */
	String mConferenceShareEventCallback;
	
	/**
	 * This string identifies the FECC Camera Control callback method.
	 * 
	 * void FECCCameraControl(String commandId, int cameraCommand)
	 * where:
	 *   commandId :
	 *   cameraCommand :
	 */
	String mConferenceFECCCommandCallback;
	
	/**
	 * This string identifies the Camera Switch callback method. This method will be called
	 * when the user changes the camera via the incall GUI.
	 * 
	 * void ConferenceCameraSwitchCallback(String name)
	 * where:
	 *   name : string name of the camera switched to
	 */
	String mConferenceCameraSwitchCallback;
	
	/**
	 * This method identifies the Number of Participants Changed callback method. This method
	 * will be called when the number of participants in the conference has changed. The 
	 * number of participants will be identified.
	 * 
	 * void ConferenceParticipantsChangedCallback(int number)
	 * where:
	 *  number : the number of participants in the conference   
	 */
	String mConferenceParticipantsChangedCallback;
	
	public LmiAndroidJniConferenceCallbacks() {
		super();
		mConferenceStatusCallback = "";
		mConferenceEventCallback = "";
		mConferenceShareEventCallback = "";
		mConferenceFECCCommandCallback = "";
		mConferenceCameraSwitchCallback = "";
		mConferenceParticipantsChangedCallback = "";
	}
	
	/**
	 * Constructor which identifies the instance of this object to use when communicating with the JNI as well
	 * as the names of all of the JNI callbacks to be used for Conference related communications from the JNI.
	 * @param object object of string that will identify the context that the callbacks are located
	 * @param confStatusCallback string that identifies the callback method for Conference Status callbacks
	 * @param confEventCallback string that identifies the callback method for Conference Event callbacks
	 * @param confShareEventCallback string that identifies the callback method for Conference Share Event callbacks
	 * @param confFECCCallback string that identifies the callback method for Conference FECC callbacks
	 * @param confCamSwitchCallback string that identifies the callback method for Conference Camera Switch callbacks
	 * @param confPartChgCallback string that identifies the callback method for Conference Participant Change callbacks
	 */
	public LmiAndroidJniConferenceCallbacks(Object object, String confStatusCallback, String confEventCallback, String confShareEventCallback,
			String confFECCCmdCallback, String confCamSwitchCallback, String confPartChgCallback) {
		super(object);
		mConferenceStatusCallback = confStatusCallback;
		mConferenceEventCallback = confEventCallback;
		mConferenceShareEventCallback = confShareEventCallback;
		mConferenceFECCCommandCallback = confFECCCmdCallback;
		mConferenceCameraSwitchCallback = confCamSwitchCallback;
		mConferenceParticipantsChangedCallback = confPartChgCallback;
	}
	
	/*
	 * Access functions to get the callback method names
	 */
	
	public String getConferenceStatusCallback() {
		return mConferenceStatusCallback;
	}

	public String getConferenceEventCallback() {
		return mConferenceEventCallback;
	}
	
	public String getConferenceShareEventCallback() {
		return mConferenceShareEventCallback;
	}
	
	public String getConferenceFECCCommandCallback() {
		return mConferenceFECCCommandCallback;
	}
	
	public String getConferenceCameraSwitchCallback() {
		return mConferenceCameraSwitchCallback;
	}
	
	public String getConferenceParticipantsChangeCallback() {
		return mConferenceParticipantsChangedCallback;
	}
	
	public static final int STATUS_JOIN_COMPLETE = 101;
	public static final int STATUS_JOIN_PROGRESS = 102;
	public static final int STATUS_GUEST_JOIN_ERROR = 103;
	public static final int STATUS_CALL_ENDED = 104;
	public static final int STATUS_INCOMING_CALL_REQUEST = 105;
	public static final int STATUS_INCOMING_CALL_CANCELLED = 106;
	public static final int STATUS_INCOMING_END_CALLING = 107;
	
	public static final int FAILURE_NONE = 0;
	public static final int FAILURE_UNKNOWN = -1;
	
	public static final int EVENT_RECORDING_STATUS = 1201;
	public static final int EVENT_WEBCASTING_STATUS = 1202;
	public static final int EVENT_SERVER_VIDEO_MUTE = 1203;
	public static final int EVENT_CAMERA_ENABLED = 1204;
	public static final int EVENT_MIC_ENABLED = 1205;
	public static final int EVENT_SPEAKER_ENABLED = 1206;
	public static final int EVENT_GUI_CHANGED = 1207;
	
	// Events associated with VIDYO_CLIENT_OUT_EVENT_PARTICIPANT_BUTTON_CLICK
	public static final int EVENT_FECC_BUTTON_CLICK = 1208;
	public static final int EVENT_PREFERRED_BUTTON_CLICK = 1209;
	public static final int EVENT_HIDE_BUTTON_CLICK = 1210;

	public static final int EVENT_SHARE_ADDED = 1211;
	public static final int EVENT_SHARE_REMOVED = 1212;

	public static final int JNI_CONFERENCE_FAIL_NONE = 1;
	public static final int JNI_CONFERENCE_FAIL_INVALID_ARGUMENT = 2;
	public static final int JNI_CONFERENCE_FAIL_NOT_LICENSED = 3;
	public static final int JNI_CONFERENCE_FAIL_GENERAL = 4;
	public static final int JNI_CONFERENCE_FAIL_CONFERENCE_LOCKED = 5;
	public static final int JNI_CONFERENCE_FAIL_LICENSE_EXPIRED = 6;
	public static final int JNI_CONFERENCE_FAIL_WRONG_PIN = 7;
	public static final int JNI_CONFERENCE_FAIL_USER_NOT_FOUND = 8;
	public static final int JNI_CONFERENCE_FAIL_END_POINT_NOT_FOUND = 9;
	public static final int JNI_CONFERENCE_FAIL_MEMBER_NOT_ONLINE = 10;
	public static final int JNI_CONFERENCE_FAIL_FAILED_PLACE_DIRECT_CALL = 11;
	public static final int JNI_CONFERENCE_FAIL_SEAT_LICENSE_EXPIRED = 12;
	public static final int JNI_CONFERENCE_FAIL_ROOM_DISABLED = 13;
	public static final int JNI_CONFERENCE_FAIL_NOT_OWNER_OF_ROOM = 14;
	public static final int JNI_CONFERENCE_FAIL_UNEXPECTED_SUBELEMENT_IN_MESSAGE = 15;
	public static final int JNI_CONFERENCE_FAIL_IPC_JOIN_FAILURE = 16;
	public static final int JNI_CONFERENCE_FAIL_ALL_LINES_IN_USE = 17;
}
