/*
 * class that defines an entity to control the Portal History settings, between the GUI and the JNI coding
 */
package com.vidyo.VidyoClientLib;

import java.io.Serializable;

import android.content.Context;

/**
 * The PortalHistoryEntity contains the values associated with the Portal History capability.
 * This class is used to pass values from the JNI to the Java layer.
 */
public class PortalHistoryEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final String TAG = "PortalHistoryEntity";
	
	private String server;
	private String user;
	
	private Context context;							// Used to set preferences
	
	/**
	 * Initialize the Web Proxy entity values to initial values.  All data is lost
	 */
	public void portalHistoryInit() {
		context = null;
	}

	public PortalHistoryEntity() {
		this.portalHistoryInit();
	}
	
	public PortalHistoryEntity(Context context) {
		this.portalHistoryInit();
		this.context = context;
	}

	public void setContext(Context context) {
		this.context = context;
	}
	
	public String getServer() {
		return server;
	}
	public void setServer(String server) {
		this.server = server;
	}
	
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
}
