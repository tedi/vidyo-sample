package com.vidyo.vidyosample.fragment;

import org.apache.http.HttpStatus;

import com.vidyo.vidyosample.entities.VidyoInfo;
import com.vidyo.vidyosample.service.SoapClientService;


import android.app.Activity;
import android.os.Bundle;

public class VidyoJoinConferenceResponderFragment extends SoapClientResponderFragment {
	
	private static final String VIDYO_INFO = "vidyoInfo";
	private static final String VIDYO_REQUEST_EID = "vidyoRequestEid";
	
	private int resultCode;
	private String resultData;
	private Boolean requestComplete;
	OnVidyoJoinConferenceUpdatedListener listener;
	
	public interface OnVidyoJoinConferenceUpdatedListener {
		public void onVidyoJoinConferenceUpdated();
		public void onVidyoJoinConferenceError(final int statusCode, final String resultData);
	}
	
	public static VidyoJoinConferenceResponderFragment newInstance(final VidyoInfo vidyoInfo, final String requestEid) {
		final VidyoJoinConferenceResponderFragment fragment = new VidyoJoinConferenceResponderFragment();
		final Bundle bundle = new Bundle();
		bundle.putParcelable(VIDYO_INFO, vidyoInfo);
		bundle.putString(VIDYO_REQUEST_EID, requestEid);
		fragment.setArguments(bundle);
		return fragment;
	}
	
	@Override
	public void onAttach(final Activity activity) {
		super.onAttach(activity);
		try {
			listener = (OnVidyoJoinConferenceUpdatedListener) activity;
		}
		catch (final ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnVidyoJoinConferenceUpdatedListener");
		}
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		listener = null;
	}
	
	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setJoinConference();
	}

	@Override
	public void onRestClientResult(int code, String result) {
		this.resultCode = code;
		this.resultData = result;
		if (code == HttpStatus.SC_OK) {
			requestComplete = Boolean.TRUE;
		}
		setJoinConference();
	}
	
	private void setJoinConference() {
		final Activity activity = getActivity();

		if (requestComplete == null && activity != null) {
			requestJoinConference();
		}
		else if (requestComplete != null && listener != null) {
			if (requestComplete == Boolean.TRUE) {
				listener.onVidyoJoinConferenceUpdated();
			}
			else {
				listener.onVidyoJoinConferenceError(resultCode, resultData);
			}	
		}
	}
	
	private void requestJoinConference() {
		
		final VidyoInfo vidyoInfo = (VidyoInfo) getArguments().get(VIDYO_INFO);
		final String requestEid = getArguments().getString(VIDYO_REQUEST_EID);
		
		final String host = vidyoInfo.getVidyoHost();
		final String url = host + "/services/v1_1/VidyoPortalUserService/";
		final String soapAction = "joinConference";
		final String authorization = "Basic " + vidyoInfo.getEncodedUsernamePassword();
		
		final String soapBody = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<env:Envelope xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:v1=\"http://portal.vidyo.com/user/v1_1\">"
				+ "<env:Body>" + "<v1:JoinConferenceRequest>" + "<v1:conferenceID>" + requestEid + "</v1:conferenceID>"
				+ "</v1:JoinConferenceRequest>" + "</env:Body>" + "</env:Envelope>";
		
		requestData(url, soapAction, soapBody, authorization, SoapClientService.POST, null);
	}
}
