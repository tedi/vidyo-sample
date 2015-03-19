package com.vidyo.vidyosample.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.w3c.dom.Document;

import com.vidyo.vidyosample.util.Utils;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

public class SoapClientService extends IntentService {
	
	private static final String TAG = SoapClientService.class.getName();
	
	public static final int GET    = 0x1;
	public static final int POST   = 0x2;
	public static final int PUT    = 0x3;
	public static final int DELETE = 0x4;
	
	public static final String AUTHORIZATION = "Authorization";
	public static final String SOAP_ACTION = "SOAPAction";
	public static final String SOAP_BODY = "SOAPBody";
	public static final String CONTENT_TYPE = "Content-Type";
	
	public static final String EXTRA_HTTP_VERB = "com.americanwell.android.restws.EXTRA_HTTP_VERB";
	public static final String EXTRA_PARAMS = "com.americanwell.android.restws.EXTRA_PARAMS";
	public static final String EXTRA_RESULT_RECEIVER = "com.americanwell.android.restws.RESULT_RECEIVER";
	
	public static final String SOAP_RESULT = "com.americanwell.android.restws.SOAP_RESULT";

	public SoapClientService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(final Intent intent) {
	    // When an intent is received by this Service, this method
        // is called on a new thread.

        final Uri    action = intent.getData();
        final Bundle extras = intent.getExtras();
                
        final int            verb     = extras.getInt(EXTRA_HTTP_VERB);
        final ResultReceiver receiver = extras.getParcelable(EXTRA_RESULT_RECEIVER);
        
        
        try {
            // Here we define our base request object which we will
            // send to our REST service via HttpClient.
        
	        // Here we define our base request object which we will
	        // send to our REST service via HttpClient.
	        HttpRequestBase request = null;
	        
	        // Let's build our request based on the HTTP verb we were
	        // given.
	        switch (verb) {
	 
	            case POST: {
	            	final HttpPost postRequest = new HttpPost();
	                postRequest.setURI(new URI(action.toString()));
	                
	                final String soapBody = extras.getString(SOAP_BODY);
	                
	    			final StringEntity se = new StringEntity(soapBody, HTTP.UTF_8);
	    			se.setContentType("text/xml");
	                postRequest.setEntity(se);
	                
	                request = postRequest;
	            }
	
	            break;
	        }
	        
	        if (request != null) {
	        	
                final HttpClient client = new DefaultHttpClient();
                
                final String authorization = extras.getString(AUTHORIZATION);
                final String soapAction = extras.getString(SOAP_ACTION);
                
                request.setHeader(AUTHORIZATION, authorization);
                request.setHeader(SOAP_ACTION, soapAction);
                request.setHeader(CONTENT_TYPE,"application/soap+xml;charset=UTF-8");
            
            	// Let's send some useful debug information so we can monitor things in LogCat.
                Log.d(TAG, "Executing request: "+ verbToString(verb) +": "+ action.toString());

                // Finally, we send our request using HTTP. This is the synchronous
                // long operation that we need to run on this thread.
                final HttpResponse response = client.execute(request);

                final HttpEntity responseEntity = response.getEntity();
                final StatusLine responseStatus = response.getStatusLine();
                final int        statusCode     = responseStatus != null ? responseStatus.getStatusCode() : 0;

                Log.d(TAG, "Request complete: Status " + statusCode + " : "+ verbToString(verb) +": "+ action.toString());
                // Our ResultReceiver allows us to communicate back the results to the caller. This
                // class has a method named send() that can send back a code and a Bundle
                // of data. ResultReceiver and IntentService abstract away all the IPC code
                // we would need to write to normally make this work.
                if (responseEntity != null) {
                	
                	String result = null;
                	
                	try {
                	
	                	final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	        			dbf.setNamespaceAware(true);
	        			final DocumentBuilder db = dbf.newDocumentBuilder();
	        			final InputStream is = response.getEntity().getContent();
	        			
	        			final Document doc = db.parse(is);
	        			result = Utils.prettyPrint(doc);
	        			
                	} catch (Exception e) {
                		Log.e(TAG, "Exception=" + e.getMessage());
                	}
                	
                    final Bundle resultData = new Bundle();
                    resultData.putString(SOAP_RESULT, result);
                    receiver.send(statusCode, resultData);
                }
            }
        } catch (final URISyntaxException e) {
            Log.e(TAG, "URI syntax was incorrect. "+ verbToString(verb) +": "+ action.toString(), e);
            receiver.send(0, null);
        } catch (final UnsupportedEncodingException e) {
            Log.e(TAG, "A UrlEncodedFormEntity was created with an unsupported encoding.", e);
            receiver.send(0, null);
        } catch (IOException e) {
            Log.e(TAG, "There was a problem when sending the request.", e);
            receiver.send(0, null);
		}        
    }
	
	private static String verbToString(final int verb) {
        switch (verb) {
            case GET:
                return "GET";

            case POST:
                return "POST";

            case PUT:
                return "PUT";

            case DELETE:
                return "DELETE";
        }
        return "";
    }
}
