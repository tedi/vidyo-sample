/*
 * class that defines an entity to control the WebProxy settings, between the GUI and the JNI coding
 */
package com.vidyo.VidyoClientLib;

import java.io.Serializable;

import android.content.Context;

/**
 * The WebProxyEntity contains the values associated with the Web Proxy capability.
 * Specifically, the values that are exposed to the user, and the OS values that can
 * be set within the Android device.
 * 
 * @author paulcushman
 *
 */
public class WebProxyJniEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final String TAG = "WebProxyEntity";
	
	protected Context context;							// Used to set preferences
	
	protected boolean useSettingsFromOS;					// true if using Android settings
	protected boolean useAutoConfigScript;				// true if an Auto-Config script is defined
	protected String configScript;						// the Auto-config script
	protected boolean useProxyServer;						// true if user defines a Proxy Server and Port
	protected String address;								// User defined Web Proxy server address
	protected String port;								// User defined Web Proxy port number
	protected String osAddress;							// Web Proxy address from Android 
	protected String osPort;								// Web Proxy port number from Android
	protected String username;							// user entered User Name
	protected String password;							// user entered password
	
	protected String pacDestURL;							// url of the destination host
	protected String [] pacHosts;							// These are the PAC hosts

	/**
	 * Initialize the Web Proxy entity values to initial values.  All data is lost
	 */
	public void webProxyInit() {
		useSettingsFromOS = true;
		useAutoConfigScript = false;
		configScript = "";
		useProxyServer = false;
		address = "";
		port = "";
		username = "";
		password = "";
		osAddress = "";
		osPort = "";
		pacDestURL = "";
		pacHosts = null;
		context = null;
	}

	public WebProxyJniEntity(String configScript, String address, String port, String username, String password) {
		this.webProxyInit();
		this.configScript = configScript;
		this.address = address;
		this.port = port;
		this.username = username;
		this.password = password;
	}

	public WebProxyJniEntity() {
		this.webProxyInit();
	}
	
	public WebProxyJniEntity(Context context) {
		this.webProxyInit();
		this.context = context;
	}

	public void setContext(Context context) {
		this.context = context;
	}
	
	public boolean getUseSettingsFromOS() {
		return useSettingsFromOS;
	}
	public void setUseSettingsFromOS(boolean useSettingsFromOS) {
		this.useSettingsFromOS = useSettingsFromOS;
	}
	
	public boolean getUseAutoConfigScript() {
		return useAutoConfigScript;
	}
	public void setUseAutoConfigScript(boolean useAutoConfigScript) {
		this.useAutoConfigScript = useAutoConfigScript;
	}
	
	public boolean getUseProxyServer() {
		return useProxyServer;
	}
	public void setUseProxyServer(boolean useProxyServer) {
		this.useProxyServer = useProxyServer;
	}
	
	
	/**
	 * Get the Web Proxy auto-config script
	 * @return string value of the web proxy auto-config script
	 */
	public String getConfigScript() {
		return configScript;
	}

	/**
	 * Set the Web Proxy auto-config script value with the input value
	 * @param configScript value to set the Web Proxy auto-config script to
	 */
	public void setConfigScript(String configScript) {
		this.configScript = configScript;
	}

	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}

	public String getOSAddress() {
		return osAddress;
	}
	public void setOSAddress(String osAddress) {
		this.osAddress = osAddress;
	}
	
	public String getOSPort() {
		return osPort;
	}
	public void setOSPort(String osPort) {
		this.osPort = osPort;
	}
	

	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	/**
	 * This method returns the URL of the destination host used to run the PAC Script with
	 * @return string representation of the destination host
	 */
	public String getPacDestURL() {
		return pacDestURL;
	}
	public void setPacDestURL(String pacDestURL) {
		this.pacDestURL = pacDestURL;
	}
	
	public String [] getPacHosts() {
		return pacHosts;
	}
	public void clearPacHosts() {
		pacHosts = null;
	}
	public String getPacHost(int index) {
		if (pacHosts == null || index >= pacHosts.length || index < 0 || pacHosts.length < index)
			return "";
		return pacHosts[index];
	}
	public void setPacHosts(String [] pacHosts) {
		this.pacHosts = pacHosts;
	}
}
