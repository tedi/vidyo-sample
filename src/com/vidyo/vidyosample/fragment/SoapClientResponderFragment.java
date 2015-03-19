package com.vidyo.vidyosample.fragment;

import com.vidyo.vidyosample.service.SoapClientService;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

public abstract class SoapClientResponderFragment extends Fragment {
	
	protected final ResultReceiver resultReceiver;

	// We are going to use a constructor here to make our ResultReceiver,
	// but be careful because Fragments are required to have only zero-arg
	// constructors. Normally you don't want to use constructors at all
	// with Fragments.
	public SoapClientResponderFragment() {
		resultReceiver = new ResultReceiver(new Handler()) {

			@Override
			protected void onReceiveResult(final int resultCode,
					final Bundle resultData) {
				
				if (resultData != null && resultData.containsKey(SoapClientService.SOAP_RESULT)) {
					
					final String result = resultData.getString(SoapClientService.SOAP_RESULT);
					onRestClientResult(resultCode, result);
					
				} else {
					onRestClientResult(resultCode, "");
				}
			}

		};
	}
	
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// This tells our Activity to keep the same instance of this
		// Fragment when the Activity is re-created during lifecycle
		// events. This is what we want because this Fragment should
		// be available to receive results from our RESTService no
		// matter what the Activity is doing.
		setRetainInstance(true);
	}
		
	public void requestData(final String url, final String soapAction, final String soapBody,
			final String authorization, final int verb, final Bundle params) {

		final Activity activity = getActivity();
		if (activity == null) {
			return;
		}

		final Intent intent = new Intent(activity, SoapClientService.class);
		intent.setData(Uri.parse(url));
		
		intent.putExtra(SoapClientService.AUTHORIZATION, authorization);
		intent.putExtra(SoapClientService.SOAP_ACTION, soapAction);
		intent.putExtra(SoapClientService.SOAP_BODY, soapBody);
		intent.putExtra(SoapClientService.EXTRA_HTTP_VERB, verb);
		intent.putExtra(SoapClientService.EXTRA_PARAMS, params);
		intent.putExtra(SoapClientService.EXTRA_RESULT_RECEIVER, resultReceiver);

		activity.startService(intent);
	}

	// Implementers of this Fragment will handle the result here.
	abstract public void onRestClientResult(int code, String result);
}
