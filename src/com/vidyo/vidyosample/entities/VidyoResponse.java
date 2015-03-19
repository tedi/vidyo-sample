package com.vidyo.vidyosample.entities;

public class VidyoResponse {
	
	private String requestEid;
	private String memberStatus;
	
	public VidyoResponse() {
		
	}
	
	public VidyoResponse(final String requestEid, final String memberStatus) {
		this.requestEid = requestEid;
		this.memberStatus = memberStatus;
	}
	
	public String getRequestEid() {
		return requestEid;
	}
	
	public void setRequestEid(String requestEid) {
		this.requestEid = requestEid;
	}
	
	public String getMemberStatus() {
		return memberStatus;
	}
	
	public void setMemberStatus(String memberStatus) {
		this.memberStatus = memberStatus;
	}
}