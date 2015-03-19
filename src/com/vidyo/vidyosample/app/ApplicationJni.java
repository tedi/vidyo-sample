package com.vidyo.vidyosample.app;
/**
 * ApplicationJni.java
 * 
 * Copyright 2011-2014 Vidyo Inc. All rights reserved.
 */

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class ApplicationJni extends com.vidyo.VidyoClientLib.LmiAndroidAppJni {
	private static String TAG = "LmiApplicationJni";

	private static ApplicationJni This = null;
	
	long address;
	
	public boolean initialize(String caFileName, Activity activity) {
		String androidInternalDir = null;
		String androidCacheDir = null;
		String logDir;
		String configDir;
		
		JniInitialize();
		
		//TODO: get directory name for XML file
		try {
			androidInternalDir = getAndroidInternalMemDir();
		} catch (IOException e) {
			androidInternalDir = null;
		}
        
		//TODO: Use this when the use of the internal memory is ready
		if (androidInternalDir != null)
			configDir = androidInternalDir;
		else
        {
            try {
                configDir = getAndroidSDcardMemDir();
            } catch (IOException e) {
                configDir = null;
            }
        }
		
		//TODO get directory name for log file
		try {
			androidCacheDir = getAndroidCacheDir();
		} catch (IOException e) {
			androidCacheDir = null;
		}
        
		//TODO: Use this when the use of the cache memory is ready
		if (androidCacheDir != null)
			logDir = androidCacheDir;
		else
        {
            try {
                logDir = getAndroidSDcardMemDir();
            } catch (IOException e) {
                logDir = null;
            }            
		}
		
		// During call to Construct there is a need for the context
		Context save_context = context;
		String MachineID = "";

		String loggingString ="fatal error warning debug@App info@AppEmcpClient debug@LmiApp debug@AppGui info@AppGui info@AppEvents";
		LmiAndroidJniSetLogging(loggingString);

		
		address = LmiAndroidJniConstruct(caFileName, MachineID, configDir, logDir, androidInternalDir, activity );
		if (address == 0) {
			return false;
		}
		
		return true;
	}
	
	public void uninitialize() {
		LmiAndroidJniDispose();
		JniUninitialize();

		address = 0;
	}

	private String getAndroidInternalMemDir()  throws IOException{
		File fileDir = getFilesDir(); //crashing
		if(fileDir != null){
			String filedir=fileDir.toString() + "/";
			Log.d(TAG, "file directory = " + filedir);
			return filedir;
		} else {
			Log.e(TAG, "Something went wrong, filesDir is null");
		}
		return null;
	}

	/**
	 * Return the directory name associated with the Cache area on the android device
	 */
	private String getAndroidCacheDir()  throws IOException{
		File cacheDir = getCacheDir(); //crashing
		if(cacheDir != null){
			String filedir=cacheDir.toString() + "/";
			Log.d(TAG, "cache directory = " + filedir);
			return filedir;
		} else {
			Log.e(TAG, "Something went wrong, cacheDir is null");
		}
		return null;
	}

	/**
	 * This function is temporary until when we start using internal memory and cache
	 */
	private String getAndroidSDcardMemDir() throws IOException{
	    File sdCard = Environment.getExternalStorageDirectory();
	    File dir = new File (sdCard.getAbsolutePath() + "/VidyoMobile");
	    dir.mkdirs();
	    
	    String sdDir = dir.toString() + "/";
	    return sdDir;
	}
	

	
	
	
	public native boolean JniInitialize();
	public native void JniUninitialize();
	
	public native void HideToolBar(boolean disablebar);
	public native void setPixelDensity(double density);

	public native void SetLimitedBandwidth(boolean bandwidthRestriction);



	public native int SendAudioFrame(byte[] frame, int numSamples,
			int sampleRate, int numChannels, int bitsPerSample);

	public native int GetAudioFrame(byte[] frame, int numSamples,
			int sampleRate, int numChannels, int bitsPerSample);

	public native int SendVideoFrame(byte[] frame, String fourcc, int width,
			int height, int orientation, boolean mirrored);

//	public native void MuteCamera(boolean muteCamera);
//	public native void DisableAllVideoStreams();
//	public native void EnableAllVideoStreams();
//	public native void StartConferenceMedia();
//	public native void SetEchoCancellation (boolean aecenable);
//	public native void SetSpeakerVolume (int volume);

	

	static {
		System.loadLibrary("ndkVidyoSample");
	}
}
