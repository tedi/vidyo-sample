package com.vidyo.vidyosample.activity;

import com.vidyo.vidyosample.R;
import com.vidyo.vidyosample.app.ApplicationJni;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class StartActivity extends Activity {
	
	private static final String TAG = "StartActivity";
		
	@Override
    public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "entering onCreate");
        super.onCreate(savedInstanceState);
        
        // Only do this the first time
        if (savedInstanceState == null) {
			Object object = (Object)getApplication();
			ApplicationJni app;
			if (object instanceof ApplicationJni) {
				app = (ApplicationJni)object;
				
				
				// THIS MUST BE CALLED TO INITIALIZE THE VIDYOCLIENT JNI LIBRARY
				Log.d(TAG, "Initializing JNI");
				app.LmiAndroidJniInitialize();
			}
        }

        
        
		this.requestWindowFeature(Window.FEATURE_NO_TITLE); 
						
		setContentView(R.layout.custom_dialog); 	
    	
		String portalInfoArray[] = { "http://vimal.sandboxga.vidyo.com", "vimal1", "vimal1"};

		final StringBuffer serverString = new StringBuffer(portalInfoArray[0]);
		final StringBuffer usernameString = new StringBuffer(portalInfoArray[1]);
		final StringBuffer passwordString = new StringBuffer(portalInfoArray[2]);

		final Button login_button = (Button) findViewById(R.id.login_button);
		
		final TextView server = (TextView) findViewById(R.id.vidyoportal_edit);
		final TextView username = (TextView) findViewById(R.id.username_edit);
		final TextView password = (TextView) findViewById(R.id.password_edit);

		server.setText(serverString.subSequence(0, serverString.length()));
		username.setText(usernameString.subSequence(0, usernameString.length()));
		password.setText(passwordString.subSequence(0, passwordString.length()));
		
		login_button.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {

				final Intent intent = new Intent(StartActivity.this, VidyoSampleActivity.class);
				intent.putExtra("server", server.getText().toString());
				intent.putExtra("username", username.getText().toString());
				intent.putExtra("password", password.getText().toString());
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});
		
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
}
