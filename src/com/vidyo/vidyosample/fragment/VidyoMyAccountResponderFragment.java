package com.vidyo.vidyosample.fragment;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.vidyo.vidyosample.entities.VidyoInfo;
import com.vidyo.vidyosample.entities.VidyoResponse;
import com.vidyo.vidyosample.service.SoapClientService;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class VidyoMyAccountResponderFragment extends SoapClientResponderFragment {
	
	private static final String LOG_TAG = VidyoMyAccountResponderFragment.class.getName();
	private static final String VIDYO_INFO = "vidyoInfo";

	private VidyoResponse vidyoResponse;
	OnVidyoMyAccountUpdatedListener listener;
	
	public interface OnVidyoMyAccountUpdatedListener {
		public void onVidyoMyAccountUpdated(final VidyoResponse vidyoResponse);
	}
	
	public static VidyoMyAccountResponderFragment newInstance(final VidyoInfo vidyoInfo) {
		final VidyoMyAccountResponderFragment fragment = new VidyoMyAccountResponderFragment();
		final Bundle bundle = new Bundle();
		bundle.putParcelable(VIDYO_INFO, vidyoInfo);
		fragment.setArguments(bundle);
		return fragment;
	}
	
	@Override
	public void onAttach(final Activity activity) {
		super.onAttach(activity);
		try {
			listener = (OnVidyoMyAccountUpdatedListener) activity;
		}
		catch (final ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnVidyoMyAccountUpdatedListener");
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
		setMyAccount();
	}
	
	@Override
	public void onRestClientResult(final int code, final String result) {
		
		try {
			
			String requestEid = null;
			String memberStatus = null;
				
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
		    InputSource is = new InputSource(new StringReader(result));
		    
		    final Document doc = builder.parse(is);			
			final NodeList entityNodes =  doc.getElementsByTagName("ns1:entityID");
			final NodeList memberStatusNodes = doc.getElementsByTagName("ns1:MemberStatus");
			if (entityNodes.getLength() > 0) {
				final Element element = (Element) entityNodes.item(0);
				final NodeList entityIDs = element.getChildNodes();
				final Node entityID = entityIDs.item(0);
				requestEid = entityID.getNodeValue();
				Log.d(LOG_TAG, "Got users EID: " + requestEid);
			}
			
			if (memberStatusNodes.getLength() > 0) {
				final Element element = (Element) memberStatusNodes.item(0);
				final NodeList memberStatuses = element.getChildNodes();
				final Node memberStatusNode = memberStatuses.item(0);
				memberStatus = memberStatusNode.getNodeValue();
				Log.d(LOG_TAG, "Got users MemberStatus: " + memberStatus);
			}	
			
			vidyoResponse = new VidyoResponse(requestEid, memberStatus);
			setMyAccount();
		    
		} catch (ParserConfigurationException e) {
			Log.d(LOG_TAG, "ParserConfigurationException: " + e.getMessage());
		} catch (Exception e) {
			Log.d(LOG_TAG, "Exception: " + e.getMessage());
		}
	}
	
	private void setMyAccount() {
		final Activity activity = getActivity();

		if (vidyoResponse == null && activity != null) {
			requestMyAccount();
		}
		else if (vidyoResponse != null && listener != null) {
			listener.onVidyoMyAccountUpdated(vidyoResponse);
		}
	}
	
	private void requestMyAccount() {
		
		final VidyoInfo vidyoInfo = (VidyoInfo) getArguments().get(VIDYO_INFO);
		
		final String host = vidyoInfo.getVidyoHost();
		final String url = host + "/services/v1_1/VidyoPortalUserService/";
		final String soapAction = "myAccount";
		final String authorization = "Basic " + vidyoInfo.getEncodedUsernamePassword();

		Log.d(LOG_TAG, "url=" + url);
		Log.d(LOG_TAG, "auth=" + authorization);
		
		final String soapBody = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<env:Envelope xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\" xmlns:v1=\"http://portal.vidyo.com/user/v1_1\">"
				+ "<env:Body>" + "<v1:MyAccountRequest/>" + "</env:Body>" + "</env:Envelope>";		
				
		requestData(url, soapAction, soapBody, authorization, SoapClientService.POST, null);
	}
}
