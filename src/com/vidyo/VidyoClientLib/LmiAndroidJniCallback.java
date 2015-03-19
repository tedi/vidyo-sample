package com.vidyo.VidyoClientLib;

import java.io.Serializable;

public class LmiAndroidJniCallback implements Serializable {
	private static final long serialVersionUID = 1L;
	
	String mObjectName = "";
	
	public LmiAndroidJniCallback() {
	}
	
	public LmiAndroidJniCallback(Object object) {
		if (object instanceof String) {
			mObjectName = (String)object;
		} else {
			mObjectName = object.getClass().getName();
		}
	}
	
	public String getClassName() {
		return mObjectName;
	}
}