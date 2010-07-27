package org.instedd.geochat;

import org.instedd.geochat.api.GeoChatApi;
import org.instedd.geochat.api.IGeoChatApi;
import org.instedd.geochat.api.RestClient;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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
	    
	    final EditText uiUser = (EditText) findViewById(R.id.user);
	    final EditText uiPassword = (EditText) findViewById(R.id.password);
	    final TextView uiFeedback = (TextView) findViewById(R.id.feedback);
	    
	    uiUser.setText(settings.getUser());
	    uiPassword.setText(settings.getPassword());
	    
	    ((Button) findViewById(R.id.login_button)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				uiFeedback.setTextColor(Color.GREEN);
				uiFeedback.setText(R.string.logging_in_to_geochat);
				
				String user = uiUser.getText().toString();
				String password = uiPassword.getText().toString();
				
				IGeoChatApi api = new GeoChatApi(new RestClient(), user, password);
				try {
					if (api.credentialsAreValid()) {
						settings.setUserAndPassword(user, password);
						startActivity(new Intent().setClass(LoginActivity.this, GeoChatTabsActivity.class));
					} else {
						uiFeedback.setTextColor(Color.RED);
						uiFeedback.setText(R.string.invalid_credentials);
					}
				} catch (Exception e) {
					uiFeedback.setTextColor(Color.RED);
					uiFeedback.setText(R.string.unknown_error_maybe_no_connection);
				}
			}
		});
	}

}
