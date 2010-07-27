package org.instedd.geochat;

import org.instedd.geochat.api.GeoChatApi;
import org.instedd.geochat.api.IGeoChatApi;
import org.instedd.geochat.api.RestClient;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends Activity {
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.login);
	    
	    final GeoChatSettings settings = new GeoChatSettings(this);
	    
	    String existingUser = settings.getUser();
	    String existingPassword = settings.getPassword();
	    
	    final EditText uiUser = (EditText) findViewById(R.id.user);
	    final EditText uiPassword = (EditText) findViewById(R.id.password);
	    final TextView uiFeedback = (TextView) findViewById(R.id.feedback);
	    final Button uiLogin = (Button) findViewById(R.id.login_button); 
	    
	    uiUser.setText(existingUser);
	    uiPassword.setText(existingPassword);
	    
	    // Automatic login (no server authentication so that the user can view offline content)
	    if (!TextUtils.isEmpty(existingUser) && !TextUtils.isEmpty(existingPassword)) {
	    	uiLogin.setEnabled(false);
	    	uiFeedback.setTextColor(Color.GREEN);
			uiFeedback.setText(R.string.logging_in_to_geochat);
			enterGeoChat();
	    	return;
	    }
	    
	    uiLogin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				uiLogin.setEnabled(false);
				uiFeedback.setTextColor(Color.GREEN);
				uiFeedback.setText(R.string.logging_in_to_geochat);
				
				String user = uiUser.getText().toString();
				String password = uiPassword.getText().toString();
				
				IGeoChatApi api = new GeoChatApi(new RestClient(), user, password);
				try {
					if (api.credentialsAreValid()) {
						settings.setUserAndPassword(user, password);
						enterGeoChat();
					} else {
						uiLogin.setEnabled(true);
						uiFeedback.setTextColor(Color.RED);
						uiFeedback.setText(R.string.invalid_credentials);
					}
				} catch (Exception e) {
					uiLogin.setEnabled(true);
					uiFeedback.setTextColor(Color.RED);
					uiFeedback.setText(R.string.unknown_error_maybe_no_connection);
				}
			}
		});
	}
	
	private void enterGeoChat() {
		startActivity(new Intent()
			.setClass(LoginActivity.this, GeoChatTabsActivity.class));
	}

}
