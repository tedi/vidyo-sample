package com.vidyo.vidyosample.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.vidyo.LmiDeviceManager.LmiDeviceManagerView;
import com.vidyo.VidyoClientLib.LmiAndroidJniChatCallbacks;
import com.vidyo.VidyoClientLib.LmiAndroidJniConferenceCallbacks;
import com.vidyo.VidyoClientLib.LmiAndroidJniLoginCallbacks;
import com.vidyo.VidyoClientLib.LmiAndroidJniMessageCallbacks;
import com.vidyo.vidyosample.R;
import com.vidyo.vidyosample.app.ApplicationJni;
import com.vidyo.vidyosample.entities.VidyoInfo;
import com.vidyo.vidyosample.entities.VidyoResponse;
import com.vidyo.vidyosample.fragment.VidyoJoinConferenceResponderFragment;
import com.vidyo.vidyosample.fragment.VidyoJoinConferenceResponderFragment.OnVidyoJoinConferenceUpdatedListener;
import com.vidyo.vidyosample.fragment.VidyoMyAccountResponderFragment;
import com.vidyo.vidyosample.fragment.VidyoMyAccountResponderFragment.OnVidyoMyAccountUpdatedListener;
import com.vidyo.vidyosample.util.Utils;

public class VidyoSampleActivity extends Activity implements
	LmiDeviceManagerView.Callback, SensorEventListener, 
	OnVidyoJoinConferenceUpdatedListener, OnVidyoMyAccountUpdatedListener {
	
	private static final String TAG = "VidyoSampleActivity";
	
	private final String VIDYO_MY_ACCOUNT_RESPONDER_TAG = "VidyoMyAccountResponder";
	private final String VIDYO_JOIN_CONFERENCE_RESPONDER_TAG = "VidyoJoinConferenceResponder";
	
	// member status for vidyo myAccount call
	private final String MEMBER_STATUS_ONLINE = "Online";
	
	// Offsets to place the video window below the top buttons.
	private final int landscapeOffset = 70;
	private final int portraitOffset = 70;
	
	// Time between engagement status requests
	private final long statusUpdateInterval = 10000; 
	
	// Device managers
	private LmiDeviceManagerView bcView;
	
	// MESSAGES
	public static final int MSG_BOX = 1;
	public static final int CALL_RECEIVED = 2;
	public static final int GET_ENGAGEMENT_STATUS = 3;
	public static final int MEMBER_CONNECTED = 4;
	public static final int END_ENGAGEMENT = 5;
	public static final int JOIN_CONFERENCE = 6;
	public static final int CONFERENCE_ENDED = 7;
	public static final int CONFERENCE_ERROR = 8;
	public static final int CONFERENCE_STARTED = 9;

	// CAMERA ORIENTATION VARIABLES
	final float degreePerRadian = (float) (180.0f / Math.PI);
	final int ORIENTATION_UP = 0;
	final int ORIENTATION_DOWN = 1;
	final int ORIENTATION_LEFT = 2;
	final int ORIENTATION_RIGHT = 3;

	private int currentRotation;
	private SensorManager sensorManager;
	private AudioManager audioManager;
	private TelephonyManager telephonyManager; 
	private boolean sensorListenerStarted = false;
	private boolean telephonyListenerStarted = false;
	private boolean audioReceiverRegistered = false;
	private boolean blockingCallReceiverRegistered = false;
	private boolean timeoutHandlerRegistered = false;

	static Handler message_handler;
	static Handler timeoutHandler;
	Timer engagementTimer;
	
	private VidyoResponse vidyoResponse;
	private VidyoInfo vidyoInfo;
	private int vidyoRetryCount;
	private int vidyoRetryAttempts = 5;
	
	Button refreshVideoBtn;
	Button endBtn;

	// Application
	static ApplicationJni app;

		
	// Engagement flags
	private boolean loginStatus = false;
	private boolean engagementStarted = false;
	private boolean conferenceStarted = false;
	private boolean conferenceEnded = false;
	private boolean conferenceEnding = false;
	private boolean memberConnected = false;
	private boolean engagementComplete = false;
	private boolean confirmCancel = false;
	private boolean endNearAlertShown = false;
	private boolean joinedRoom = false;
	private boolean refreshVideo = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.conference);
		
		message_handler = new MessageHandler(this);

		Object object = (Object)getApplication();
		if (object instanceof ApplicationJni) {
			app = (ApplicationJni)object;
		} else {
			app = null;
		}
		
		if (app != null) {
			Log.d(TAG, "ApplicationJni has been set correct!!!");
			constructJniInterface();
		}
		
		final Bundle bundle = getIntent().getExtras();
		
		final String server = bundle.getString("server");
		final String username = bundle.getString("username");
		final String password = bundle.getString("password");
		
		vidyoInfo = new VidyoInfo();
		vidyoInfo.setVidyoHost(server);
		vidyoInfo.setVidyoUsername(username);
		vidyoInfo.setVidyoPassword(password);
		
		Log.d(TAG, "server: " + server);
		Log.d(TAG, "username: " + username);
		Log.d(TAG, "password: " + password);
				
		// Hook-up exit button
		final Button btnEnd = (Button) findViewById(R.id.button_cancel);
		btnEnd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View view) {
				confirmCancelButton();
			}
		});
		
		final View view = (View) findViewById(R.id.engagement_layout);
		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				confirmCancel = false;
				btnEnd.setText(R.string.misc_cancel);
			}	
		});
		
		final Display display = getWindowManager().getDefaultDisplay();
		currentRotation = display.getRotation();
		
		
		setupVideo();
		startEngagmentTimer();
	}
	
	@Override
	public void onResume() {
		Log.d(TAG, "onResume called");
		super.onResume();
		
		if (joinedRoom) {
			Log.d(TAG, "refreshing video from background mode");
			refreshVideo();
		}
	
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
	    Sensor gSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorListenerStarted = sensorManager.registerListener(this,
				gSensor, SensorManager.SENSOR_DELAY_NORMAL);
		
		audioReceiverRegistered = true;
		registerReceiver(audioReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
		audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		
		if (!blockingCallReceiverRegistered) {
			registerReceiver(blockingCallReceiver, new IntentFilter("android.intent.action.PHONE_STATE"));
			blockingCallReceiverRegistered = true;
		}
	}
	
	@Override
	public void onStop() {
		super.onStop();
		
		Log.d(TAG, "onStop called");
		if (sensorListenerStarted) {
			sensorManager.unregisterListener(this);
			sensorListenerStarted = false;
		}
		
		if (audioReceiverRegistered) {
			unregisterReceiver(audioReceiver);
			audioReceiverRegistered = false;
			audioManager = null;
		}		
	}
		
	@Override
	protected void onDestroy() {
		stopDevices();
		killEngagmentTimer();
		app.uninitialize();
		super.onDestroy();
	}
	
	@Override
	public boolean onKeyDown(final int keyCode, final KeyEvent event) {
		// User should not be able to click back button on negative use cases or during the conversation.
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			Log.d(TAG, "onKeyDown Called");
			return false; 
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onConfigurationChanged(final Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Log.d(TAG, "Configuration changed being handled.");
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		final Display display = getWindowManager().getDefaultDisplay();
		final int rotation = display.getRotation();
		if (rotation == currentRotation) {
			return;
		}
		rotateScreen(rotation);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) { }
	
	private void setupVideo() {		
	    bcView = new LmiDeviceManagerView(this, this);
	    
		final String caFileName = writeCaCertificates();
		
		app.initialize(caFileName, this);
	}
	
	private void startEngagement() {

		switchToVideoView();
		
		setupAudioForEngagement();
		
		// Start listening for sensor events...
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
	    Sensor gSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorListenerStarted = sensorManager.registerListener(this,
				gSensor, SensorManager.SENSOR_DELAY_NORMAL);
		
		if (!loginStatus) {	
			
			if (!Utils.isWifiConnected(this)) {
				Log.d(TAG, "Setting Vidyo Config to LIMITED_BANDWIDTH");
				app.SetLimitedBandwidth(true);
			}
			else {
				Log.d(TAG, "Setting Vidyo Config to BEST_QUALITY");
				app.SetLimitedBandwidth(false);
			}
			
			app.HideToolBar(false);
			//SetEchoCancellation(true);
			app.LmiAndroidJniLogin(vidyoInfo.getVidyoHost(), vidyoInfo.getVidyoUsername(), vidyoInfo.getVidyoPassword());
			loginStatus = true;
		}
				
		engagementStarted = true;
	}
	
	private void startEngagmentTimer() {
		engagementTimer = new Timer();
		engagementTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				message_handler.sendEmptyMessage(GET_ENGAGEMENT_STATUS);
			}
		}, 0L, statusUpdateInterval);
	}
	
	private void killEngagmentTimer() {
		if (engagementTimer != null) {
			engagementTimer.cancel();
			engagementTimer.purge();
			engagementTimer = null;
		}
	}
	
	private String writeCaCertificates() {
		try {
			
			final InputStream caCertStream = getResources().openRawResource(R.raw.ca_certificates);
			
			File caCertDirectory = null;
			try {
				String pathDir = Utils.getAndroidInternalMemDir(this);
				caCertDirectory = new File(pathDir);
			} catch (Exception e) {
				Log.e(TAG, "Something went wrong getting the pathDir");
				return null;
			}
			
 			File caFile = new File(caCertDirectory,"ca-certificates.crt");
			
			final FileOutputStream caCertFile = new FileOutputStream(caFile);
			final byte buf[] = new byte[1024];
			int len;
			while ((len = caCertStream.read(buf)) != -1) {
				caCertFile.write(buf, 0, len);
			}
			caCertStream.close();
			caCertFile.close();

			return caFile.getPath();
		}
		catch (final Exception e) {
			return null;
		}
	}
	
	private void confirmCancelButton() {
		final Button btnEnd = (Button) findViewById(R.id.button_cancel);
		if (confirmCancel) {
			btnEnd.setEnabled(false);
			finish();
		}
		else {
			confirmCancel = true;
			btnEnd.setText(R.string.confirm_cancel);
		}
	}
		
	private void cancelConfirmCancelButton() {
		if (confirmCancel) {
			confirmCancel = false;
			final Button btnEnd = (Button) findViewById(R.id.button_cancel);
			btnEnd.setText(R.string.misc_cancel);
			final ViewGroup.LayoutParams params = btnEnd.getLayoutParams();
			params.width = 110;
			btnEnd.setLayoutParams(params);
		}
	}
	
	private void refreshVideo() {
		Log.d(TAG, "Refresh Video button pushed");
		
		final RelativeLayout layout = (RelativeLayout) findViewById(R.id.refresh_video_content);
		layout.setVisibility(View.VISIBLE);
				
		vidyoRetryCount = 0;
		
		refreshVideoBtn.setEnabled(false);
		refreshVideo = true;
		conferenceEnding = true;
		
		app.LmiAndroidJniLeave();
		return;
	}

		
	private void stopDevices() {
		Log.d(TAG, "Stopping devices");
		loginStatus = false;
		
		if (timeoutHandlerRegistered) {
			timeoutHandler.removeCallbacks(engagementTimeoutRunnable);
		}
		
		if (blockingCallReceiverRegistered) {
			unregisterReceiver(blockingCallReceiver);
			blockingCallReceiverRegistered = false;
		}
	}
	
	private void rotateScreen(final int rotation) {
		switch (rotation) {
		case Surface.ROTATION_0:
			app.LmiAndroidJniSetOrientation(ORIENTATION_UP);
			break;
		case Surface.ROTATION_90:
			app.LmiAndroidJniSetOrientation(ORIENTATION_RIGHT);
			break;
		case Surface.ROTATION_180:
			app.LmiAndroidJniSetOrientation(ORIENTATION_DOWN);
			break;
		case Surface.ROTATION_270:
			app.LmiAndroidJniSetOrientation(ORIENTATION_LEFT);
			break;
		}

		currentRotation = rotation;
		return;
	}
	
	private void switchToWaitingRoom() {
		final TextView message = (TextView) findViewById(R.id.just_relax);
		message.setText(String.format(getString(R.string.be_there_shortly)));
		
		final ImageView image = (ImageView) findViewById(R.id.connecting_image);
		//image.setImageResource(R.drawable.img_video_engagement_waiting);
	}

	private void switchToVideoView() {
		
		final ViewFlipper vf = (ViewFlipper) findViewById(R.id.ViewFlipper01);
		vf.showNext();

		final RelativeLayout layout = (RelativeLayout) findViewById(R.id.engagement_layout);
		final RelativeLayout layout2 = (RelativeLayout) findViewById(R.id.engagement_layout2);

		layout.removeAllViews();
		layout2.addView(bcView, 0);
		
		resizeVideo();
		
		// fire off a timer and time out after one minute if the conversation hasn't started
		timeoutHandler = new Handler();
		Log.d(TAG, "The engagement timer has started");
		timeoutHandler.postDelayed(engagementTimeoutRunnable, 120000);	
		timeoutHandlerRegistered = true;
		
		endBtn = (Button) findViewById(R.id.button_end);
		endBtn.setEnabled(false);
		endBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View view) {
				sendEndEngagement();
			}
		});

		// Hook-up refresh video button
		refreshVideoBtn = (Button) findViewById(R.id.button_refresh_video);
		refreshVideoBtn.setEnabled(false);
		refreshVideoBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View view) {
				refreshVideo();
			}
		});
	}
	
	private void engagementTimeoutDialog() {
		Log.d(TAG, "engagement has timed out");
	}
	
	private void sendMemberConnected() {
		// code to send the member connected message to server would go here
		onMemberConnectedResponse();
	}
	
	private void sendEndEngagement() {
		final RelativeLayout wrapUpLayout = (RelativeLayout) findViewById(R.id.wrapup_content);
		wrapUpLayout.setVisibility(View.VISIBLE);
		
		// code to request the status of the engagement would go here
		onEndEngagement();	
	}
	
	private void sendCancelEngagement() {
		// code to request to cancel the engagement would go here
		onCancelEngagement();
	}	
	
	private void joinConference() {
		
		if (vidyoRetryCount < vidyoRetryAttempts) {
			if (vidyoResponse == null || vidyoResponse.getRequestEid() == null) {
				getVidyoAccountInfo();
				return;
			}			
			
			Log.d(TAG, "request id is: " + vidyoResponse.getRequestEid());
			Log.d(TAG, "status is: " + vidyoResponse.getMemberStatus());
			
			
			// THIS CODE IS IN OUR REAL APP, ITS COMMENTED OUT BECAUSE ON
			// SUBSEQUENT RE-ENTRY THE VIDPORTAL RESPONDS WITH A SUCCESSFUL
			// LOGIN AND REQUESTEID BUT SAYS THE MEMBERSTATUS IS OFFLINE...
			// we have an eid and the member is online, join the room
			if (vidyoResponse.getRequestEid() != null && MEMBER_STATUS_ONLINE.equals
					(vidyoResponse.getMemberStatus())) {
				joinRoom();
			}
			// the member is not online, attempt to login again
			else if (!MEMBER_STATUS_ONLINE.equals(vidyoResponse.getMemberStatus())) {
				Log.e(TAG, "Retrying attempt " + vidyoRetryCount + " of " + vidyoRetryAttempts + 
						" : memberStatus is not set to Online");
				vidyoRetryCount++;
				app.LmiAndroidJniLogin(vidyoInfo.getVidyoHost(), vidyoInfo.getVidyoUsername(), vidyoInfo.getVidyoPassword());
			}
			// the eid may not have been retrieved, try again
			else {
				Log.e(TAG, "Retrying attempt " + vidyoRetryCount + " of " + vidyoRetryAttempts + 
						" : Failed to get eid");
				vidyoRetryCount++;
				joinConference();
			}
		}
		// max retries have been hit, send error.
		else {
			message_handler.sendEmptyMessage(CONFERENCE_ERROR);
		}
	}
	
	private void conferenceEnded() {
		Log.d(TAG, "conferenceEnded");
		if (!refreshVideo) {
			loginStatus = false;
			conferenceEnded = true;
			stopDevices();
		
			final RelativeLayout wrapUpLayout = (RelativeLayout) findViewById(R.id.wrapup_content);
			wrapUpLayout.setVisibility(View.GONE);
			
			finish();
		}
		else {
			conferenceEnding = false;
		}
	}
	
	private void requestEngagmentStatus() {
		if (!this.isFinishing()) {
			// code to request the status of the engagement would go here
			onEngagementStatus();
		}
	}
	
	private void conferenceError() {
		killEngagmentTimer();
		sendCancelEngagement();
	}
	
	private void conferenceStarted() {
		if (conferenceStarted) {
			Log.d(TAG, "Resetting audio state");
			setupAudioForEngagement();
		}	
		app.LmiAndroidJniStartMedia();
		app.LmiAndroidJniSetPreviewModeON(false);
		app.LmiAndroidJniSetCameraDevice(1);
		app.LmiAndroidJniMuteCamera(false);
		resizeVideo();
		
		// Set the screen's pixel density
		double density = getResources().getDisplayMetrics().density;
		app.setPixelDensity(density);
		
		conferenceStarted = true;
	}
	

	
	public void onEndEngagement() {
		Log.d(TAG, "Engagement ended on server.");
		
		final RelativeLayout wrapUpLayout = (RelativeLayout) findViewById(R.id.wrapup_content);
		wrapUpLayout.setVisibility(View.VISIBLE);
		
		conferenceEnded = true;
		
	}
	
	public void onCancelEngagement() {
		Log.i(TAG, "Engagement canceled by member on server.");
		finish();
	}
	
	private void onEngagementStatus() {
		
		// Much simplified version here

		final RelativeLayout refreshVideoLayout = (RelativeLayout) findViewById(R.id.refresh_video_content);
		refreshVideoLayout.setVisibility(View.GONE);
		
		Log.d(TAG, "Engagement started: " + engagementStarted);
		
		if (conferenceEnded) {
			finish();
		}
		else if (!conferenceEnded && !conferenceEnding && refreshVideo) {
			refreshVideoBtn.setEnabled(true);
			joinRoom();
		}
		else if (!conferenceEnded && conferenceEnding && refreshVideo) {
			Log.d(TAG, "waiting for conference to end on refresh");
		}
		else if (!engagementStarted){
			startEngagement();
		}
	}
	
	private void onMemberConnectedResponse() {
		
		Log.d(TAG, "Application acknowledged that we are connected.  Converstaion should be starting up.");
		// Do some Vidyo type stuff here? Disable button, echo cancellation etc...
		rotateScreen(currentRotation);
		
		endBtn.setEnabled(true);
		refreshVideoBtn.setEnabled(true);
		memberConnected = true;
	}
	
	private Runnable engagementTimeoutRunnable = new Runnable() {
		@Override
		public void run() {
			Log.d(TAG, "The engagement timer has timeout out.");
			if (!joinedRoom) {
				engagementTimeoutDialog();
			}
		}
	};
	
	// //////////////////////////////////////////////////////////////////
	// RESPONDER FRAGMENTS and RESPONDER FRAGMENT CALLBACKS
	// //////////////////////////////////////////////////////////////////

	private LmiAndroidJniLoginCallbacks loginCallbacks;
	private LmiAndroidJniConferenceCallbacks conferenceCallbacks;
	private LmiAndroidJniChatCallbacks chatCallbacks;
	private LmiAndroidJniMessageCallbacks messageCallbacks;
	
	public void constructJniInterface() {
		loginCallbacks = new LmiAndroidJniLoginCallbacks("com/vidyo/vidyosample/activity/VidyoSampleActivity", "vidyoLoginStatusCallback");
		app.LmiAndroidJniLoginSetCallbacks(loginCallbacks);
		
		conferenceCallbacks = new LmiAndroidJniConferenceCallbacks("com/vidyo/vidyosample/activity/VidyoSampleActivity",
				"vidyoConferenceStatusCallback",
				"vidyoConferenceEventCallback",
				"vidyoConferenceShareEventCallback",
				"vidyoFeccCameraControl",
				"vidyoCameraSwitchCallback",
				"vidyoNotifyParticipantsChanged");
		app.LmiAndroidJniConferenceSetCallbacks(conferenceCallbacks);

		chatCallbacks = new LmiAndroidJniChatCallbacks("com/vidyo/vidyosample/activity/VidyoSampleActivity", "vidyoChatMsgRcvCallback");
		app.LmiAndroidJniChatSetCallbacks(chatCallbacks);
		
		messageCallbacks = new LmiAndroidJniMessageCallbacks("com/vidyo/vidyosample/activity/VidyoSampleActivity", "vidyoMessageOutMsgCallback");
		app.LmiAndroidJniMessageSetCallbacks(messageCallbacks);
	}
	
	/*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
	 * Login Related Definitions and Methods
	 *=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*/

	public void vidyoLoginStatusCallback(int loginStatus, int loginError, String loginMsg) {
		Log.d(TAG, "applicationJniLoginStatusCallback: loginStatus="+loginStatus+", loginError="+loginError);
		switch (loginStatus) {
		case LmiAndroidJniLoginCallbacks.STATUS_LOGIN_COMPLETE:
			vidyoSignedInCallback(loginError, loginMsg);
			break;
		case LmiAndroidJniLoginCallbacks.STATUS_LOGGING_IN:
			break;
		case LmiAndroidJniLoginCallbacks.STATUS_LOGGED_OUT:
			signedOutCallback(loginMsg);
			break;
		case LmiAndroidJniLoginCallbacks.STATUS_PORTAL_PREFIX:
			break;
		case LmiAndroidJniLoginCallbacks.STATUS_DISCONNECT_FROM_GUESTLINK:
			break;
		case LmiAndroidJniLoginCallbacks.STATUS_GUEST_LOGIN_CONFERENCE_ENDED:
			break;
		}
	}

	private void vidyoSignedInCallback(int loginStatus, String loginMsg) {
		Log.d(TAG, "Signed into Vidyo Portal.");
		// reset the vidyoResponse
		vidyoResponse = null;
		message_handler.sendEmptyMessage(JOIN_CONFERENCE);
	}
	
	private void signedOutCallback(String loginMsg) {
		Log.d(TAG, "Signed Out received!");
		loginStatus = false;
	}
	
	/*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
	 * Conference Related Definitions and Methods
	 *=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*/

	public void vidyoConferenceStatusCallback(int status, int error, String message) {
		Log.d(TAG, "applicationJniConferenceStatusCallback: loginStatus="+status+", loginError="+error);
		switch (status) {
		case LmiAndroidJniConferenceCallbacks.STATUS_JOIN_COMPLETE:
			callStartedCallback(error);
			break;
		case LmiAndroidJniConferenceCallbacks.STATUS_JOIN_PROGRESS:
			callStartingCallback();
			break;
		case LmiAndroidJniConferenceCallbacks.STATUS_GUEST_JOIN_ERROR:
			break;
		case LmiAndroidJniConferenceCallbacks.STATUS_CALL_ENDED:
			vidyoConferenceEnded();
			break;
		case LmiAndroidJniConferenceCallbacks.STATUS_INCOMING_CALL_REQUEST:
			vidyoIncomingCallRequest(message);
			break;
		case LmiAndroidJniConferenceCallbacks.STATUS_INCOMING_CALL_CANCELLED:
			break;
		case LmiAndroidJniConferenceCallbacks.STATUS_INCOMING_END_CALLING:
			break;
		}
	}



	private void callStartedCallback(int error) {	
		Log.d(TAG, "Call started received!");
		message_handler.sendEmptyMessage(CONFERENCE_STARTED);
	}
	
	private void callStartingCallback() {
	}
	
	private void vidyoIncomingCallRequest(String caller) {
		Log.d(TAG, "GOT INCOMING CALL FROM "+caller);
	}
	
	private void vidyoConferenceEnded() {
		message_handler.sendEmptyMessage(CONFERENCE_ENDED);
	}
	
	public void vidyoConferenceEventCallback(int event, boolean state) {
		Log.d(TAG, "applicationJniConferenceEventCallback: event="+event+", state="+state);
		switch (event) {
		case LmiAndroidJniConferenceCallbacks.EVENT_RECORDING_STATUS:
			break;
		case LmiAndroidJniConferenceCallbacks.EVENT_WEBCASTING_STATUS:
			break;
		case LmiAndroidJniConferenceCallbacks.EVENT_SERVER_VIDEO_MUTE:
			break;
		case LmiAndroidJniConferenceCallbacks.EVENT_CAMERA_ENABLED:
			break;
		case LmiAndroidJniConferenceCallbacks.EVENT_MIC_ENABLED:
			break;
		case LmiAndroidJniConferenceCallbacks.EVENT_SPEAKER_ENABLED:
			break;
		case LmiAndroidJniConferenceCallbacks.EVENT_GUI_CHANGED:
			resizeVideo();
			break;
		case LmiAndroidJniConferenceCallbacks.EVENT_FECC_BUTTON_CLICK:
			break;
		}
	}
	
	public void vidyoConferenceShareEventCallback(int eventType, String shareURI) {
		switch (eventType) {
			case LmiAndroidJniConferenceCallbacks.EVENT_SHARE_ADDED:
				break;

			case LmiAndroidJniConferenceCallbacks.EVENT_SHARE_REMOVED:
				break;

			default: {
			}
		}
	}
	
	private void vidyoFeccCameraControl(String commandId, int cameraCommand) {
	}

	private void vidyoCameraSwitchCallback(String name) {
		Log.d(TAG, "Switch camera: " + name);
	}
	
	private void vidyoNotifyParticipantsChanged(int numOfParticipants) {
		Log.d(TAG, "notifyParticipantsChanged Begin");
	}

	
	
	/*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
	 * Chat Related Definitions and Methods
	 *=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*/

	private void vidyoChatMsgRcvCallback(boolean groupChat, String uri, String name, String message) {
		Log.d(TAG, "Got chat message from: "+name);
		Log.d(TAG, "Chat msg: "+message);
	}

	/*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*
	 * Message Related Definitions and Methods
	 *=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*/

	public void vidyoMessageOutMsgCallback(String s) {
		Log.d(TAG, "Got Message: "+s);
	}

	
	
	
	private void resizeVideo() {
		if (bcView != null) {
			int width = bcView.getWidth();
			int height = bcView.getHeight();
			app.LmiAndroidJniResize(width, height);
		}
//		final Display display = getWindowManager().getDefaultDisplay();
//		LmiDeviceManagerViewResize(display.getWidth(), display.getHeight());
	}
	
	
	
	

	private void getVidyoAccountInfo() {

		final FragmentManager fm = getFragmentManager();
		final FragmentTransaction ft = fm.beginTransaction();
		
		VidyoMyAccountResponderFragment responder = VidyoMyAccountResponderFragment.newInstance(vidyoInfo);
		ft.add(responder, VIDYO_MY_ACCOUNT_RESPONDER_TAG);
		ft.commit();
	}	
	
	private void joinRoom() {
		Log.d(TAG, "Attempting to join room with eid: " + vidyoResponse.getRequestEid());

		final FragmentManager fm = getFragmentManager();
		final FragmentTransaction ft = fm.beginTransaction();
		
		Fragment responder = VidyoJoinConferenceResponderFragment.newInstance(vidyoInfo, vidyoResponse.getRequestEid());
		ft.add(responder, VIDYO_JOIN_CONFERENCE_RESPONDER_TAG);
		ft.commit();
	}
	
	/**
	 * Callback for VidPortal Soap request 'myAccount'
	 */
	@Override
	public void onVidyoMyAccountUpdated(final VidyoResponse vidyoResponse) {
		Log.d(TAG, "onVidyoMyAccountUpdated called");
		this.vidyoResponse = vidyoResponse;
		joinConference();
	}
	
	/**
	 * Callback for VidPortal Soap request 'joinConference' with 200 status code
	 */
	@Override
	public void onVidyoJoinConferenceUpdated() {
		Log.d(TAG, "onVidyoJoinConferenceUpdated Called.");
		joinedRoom = true;
		refreshVideo = false;
		 
		Log.d(TAG, "The engagement timer has been stopped.");
		timeoutHandler.removeCallbacks(engagementTimeoutRunnable);
		timeoutHandlerRegistered = false;
		 								 
		// need to get rid of the 'please waiting text' since the provider has just joined the room
		final RelativeLayout textContent = (RelativeLayout) findViewById(R.id.engagement_text_content);
		textContent.setVisibility(View.GONE); 
		
		if (!memberConnected) {
			message_handler.sendEmptyMessage(MEMBER_CONNECTED);
		}
	}
	
	/**
	 * Callback for VidPortal Soap request 'joinConference' with failed status
	 */
	@Override
	public void onVidyoJoinConferenceError(final int statusCode, final String resultData) {
		Log.d(TAG, "onVidyoJoinConferenceError called");
		Log.e(TAG, "Soap response = " + resultData);
		Log.e(TAG, "Attempting to join room with eid: " + vidyoResponse.getRequestEid() + 
				" failed with status code " + statusCode);
		vidyoRetryCount++;
		joinConference();
	}
	
	
	
	// NATIVE LAYER
	//////////////////////////////////////////////////////////////////
	
	public void LmiDeviceManagerViewRender() {
		app.LmiAndroidJniRender();
	}

	public void LmiDeviceManagerViewResize(final int width, final int height) {
		app.LmiAndroidJniResize(width, height);
	}

	public void LmiDeviceManagerViewRenderRelease() {
		app.LmiAndroidJniRenderRelease();
		resizeVideo();
	}

	public void LmiDeviceManagerViewTouchEvent(final int id, final int type, final int x, final int y) {
		if (!engagementStarted) {
			cancelConfirmCancelButton();
		}
		app.LmiAndroidJniTouchEvent(id, type, x, y);
	}

	public int LmiDeviceManagerCameraNewFrame(final byte[] frame, final String fourcc, final int width,
			final int height, final int orientation, final boolean mirrored) {
		return app.SendVideoFrame(frame, fourcc, width, height, orientation, mirrored);
	}

	public int LmiDeviceManagerMicNewFrame(final byte[] frame, final int numSamples, final int sampleRate,
			final int numChannels, final int bitsPerSample) {
		return app.SendAudioFrame(frame, numSamples, sampleRate, numChannels, bitsPerSample);
	}

	public int LmiDeviceManagerSpeakerNewFrame(final byte[] frame, final int numSamples, final int sampleRate,
			final int numChannels, final int bitsPerSample) {
		return app.GetAudioFrame(frame, numSamples, sampleRate, numChannels, bitsPerSample);
	}

	// AUDIO MANAGEMENT
	// //////////////////////////////////////////////////////////////////
			
	private void setupAudioForEngagement() {
		
		setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
		
		final int mode = audioManager.getMode();
		
		if (mode == AudioManager.MODE_NORMAL) {			
			final int volume = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
			audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, volume, 0);
			audioManager.setMode(AudioManager.MODE_NORMAL);
			audioManager.setSpeakerphoneOn(true);
		}
		else {
			audioManager.setMode(AudioManager.MODE_IN_CALL);
			audioManager.setSpeakerphoneOn(false);
			audioManager.setMicrophoneMute(false);
		}  
	}
	
	// A receiver that detects when the headphones have been plugged/unplugged
	// and diverts the audio to the correct speaker
	private BroadcastReceiver audioReceiver = new BroadcastReceiver() {
	
		@Override
		public void onReceive(Context context, Intent intent) {
			
			if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
								
				// 0 = unplugged, 1 = plugged
				if (intent.getIntExtra("state", 0) == 0) {
					
					// audio stream during the waiting room video needs to change
					if (!engagementStarted) {
						final int volume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2;
						audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
					}
					// audio stream during the engagement needs to change
					else {
						final int volume = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
						audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, volume, 0);
					}
					
					audioManager.setMode(AudioManager.MODE_NORMAL);
					audioManager.setSpeakerphoneOn(true);
				}
				else {
					audioManager.setMode(AudioManager.MODE_IN_CALL);
					audioManager.setSpeakerphoneOn(false);
					audioManager.setMicrophoneMute(false);
				}
			}
		}	  
	};
	
	// This receiver will block all incoming calls from interrupting an engagement
	private BroadcastReceiver blockingCallReceiver = new BroadcastReceiver() {
	
		@Override
		public void onReceive(Context context, Intent intent) {	
			try {
			
				if (intent.getAction().equals("android.intent.action.PHONE_STATE")) {
					String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
					
					if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
						
						String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
						Log.d(TAG, "Incoming Phone Call Ignored: " + incomingNumber);
						
						TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
					
						Class<?> classTelephony = Class.forName(telephonyManager.getClass().getName());
	                    Method methodGetITelephony = classTelephony.getDeclaredMethod("getITelephony");
	                    methodGetITelephony.setAccessible(true);
	                      
	                    Object telephonyInterface = methodGetITelephony.invoke(telephonyManager);
	                    Class<?> telephonyInterfaceClass = Class.forName(telephonyInterface.getClass().getName());
	                    Method methodEndCall = telephonyInterfaceClass.getDeclaredMethod("endCall");
	                    methodEndCall.invoke(telephonyInterface);
					}
				}
			}
			catch (Exception e) {
				Log.d(TAG, "An error occurred: " + e.getMessage());
			}	
		}
	};
	
	private static final class MessageHandler extends Handler {
		VidyoSampleActivity activity;

		public MessageHandler(final VidyoSampleActivity activity) {
			super();
			this.activity = activity;
		}

		@Override
		public void handleMessage(final Message msg) {
			if (activity.isFinishing()) {
				return;
			}
			switch (msg.what) {
			case GET_ENGAGEMENT_STATUS:
				activity.requestEngagmentStatus();
				break;
			case MEMBER_CONNECTED:
				activity.sendMemberConnected();
				break;
			case END_ENGAGEMENT:
				activity.sendEndEngagement();
				break;
			case JOIN_CONFERENCE:
				activity.joinConference();
				break;
			case CONFERENCE_ENDED:
				activity.conferenceEnded();
				break;
			case CONFERENCE_ERROR:
				activity.conferenceError();
				break;
			case CONFERENCE_STARTED:
				activity.conferenceStarted();
				break;
			}
		}
	}
}
