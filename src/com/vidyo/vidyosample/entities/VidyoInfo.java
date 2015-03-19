package com.vidyo.vidyosample.entities;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;

import com.google.gson.Gson;

public class VidyoInfo implements Parcelable {

	String vidyoUsername;
	String vidyoPassword;
	String vidyoPAK;
	String vidyoHost;
	String vidyoRoomId;
	
	public String getVidyoUsername() {
		return vidyoUsername;
	}
	
	public void setVidyoUsername(String vidyoUsername) {
		this.vidyoUsername = vidyoUsername;
	}
	
	public String getVidyoPassword() {
		return vidyoPassword;
	}
	
	public void setVidyoPassword(String vidyoPassword) {
		this.vidyoPassword = vidyoPassword;
	}
	
	public String getVidyoPAK() {
		return vidyoPAK;
	}
	
	public void setVidyoPAK(String vidyoPAK) {
		this.vidyoPAK = vidyoPAK;
	}
	
	public String getVidyoHost() {
		return vidyoHost;
	}
	
	public void setVidyoHost(String vidyoHost) {
		this.vidyoHost = vidyoHost;
	}
	
	public String getVidyoRoomId() {
		return vidyoRoomId;
	}
	
	public void setVidyoRoomId(String vidyoRoomId) {
		this.vidyoRoomId = vidyoRoomId;
	}
	
	public String getEncodedUsernamePassword() {
		 final String userPass = vidyoUsername + ":" + vidyoPassword;
		 return Base64.encodeToString(userPass.getBytes(), Base64.NO_WRAP);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		final Gson gson = new Gson();
		dest.writeString(gson.toJson(this));
	}

    public static final Parcelable.Creator<VidyoInfo> CREATOR = new Parcelable.Creator<VidyoInfo>() {
        @Override
		public VidyoInfo createFromParcel(final Parcel in) {
        	final Gson gson = new Gson();
            return gson.fromJson(in.readString(), VidyoInfo.class);
        }

        @Override
		public VidyoInfo[] newArray(final int size) {
            return new VidyoInfo[size];
        }
    };
}
