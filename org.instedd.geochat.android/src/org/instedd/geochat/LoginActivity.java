package org.instedd.geochat;

import org.instedd.geochat.api.GeoChatApi;
import org.instedd.geochat.api.Group;
import org.instedd.geochat.api.IGeoChatApi;
import org.instedd.geochat.api.RestClient;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends Activity {
	
	private final Handler handler = new Handler();
	private ProgressDialog progressDialog;
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.login);
	    
	    final GeoChatSettings settings = new GeoChatSettings(this);
	    final String existingUser = settings.getUser();
	    final String existingPassword = settings.getPassword();
	    
	    final EditText uiUser = (EditText) findViewById(R.id.user);
	    final EditText uiPassword = (EditText) findViewById(R.id.password);
	    final TextView uiFeedback = (TextView) findViewById(R.id.feedback);
	    final Button uiLogin = (Button) findViewById(R.id.login_button); 
	    
	    uiUser.setText(existingUser);
	    uiPassword.setText(existingPassword);
	    
	    // Automatic login (no server authentication so that the user can view offline content)
//	    if (!TextUtils.isEmpty(existingUser) && !TextUtils.isEmpty(existingPassword)) {
//	    	uiLogin.setEnabled(false);
//	    	uiFeedback.setTextColor(Color.GREEN);
//			uiFeedback.setText(R.string.logging_in_to_geochat);
//			enterGeoChat();
//	    	return;
//	    }
	    
	    uiLogin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(0);
				
				new Thread() {
					public void run() {
						handler.post(new Runnable() {
							@Override
							public void run() {
								uiFeedback.setVisibility(View.GONE);
								uiLogin.setEnabled(false);
							}
						});
						
						String user = uiUser.getText().toString();
						String password = uiPassword.getText().toString();
						
						boolean userChanged = user != null && !user.equals(existingUser);
						boolean passwordChanged = password != null && !password.equals(existingPassword);
						boolean credentialsAreValid = true;
						
						try {
							if (userChanged || passwordChanged) {
								IGeoChatApi api = new GeoChatApi(new RestClient(), user, password);
								credentialsAreValid = api.credentialsAreValid();
							}
							
							if (credentialsAreValid) {
								settings.setUserAndPassword(user, password);
								if (userChanged) {
									resync();
								}
								enterGeoChat();
							} else {
								handler.post(new Runnable() {
									@Override
									public void run() {
										dismissDialog(0);
										uiLogin.setEnabled(true);
										uiFeedback.setVisibility(View.VISIBLE);
										uiFeedback.setText(R.string.invalid_credentials);
									}
								});
							}
						} catch (Exception e) {
							handler.post(new Runnable() {
								@Override
								public void run() {
									dismissDialog(0);
									uiLogin.setEnabled(true);
									uiFeedback.setVisibility(View.VISIBLE);
									uiFeedback.setText(R.string.unknown_error_maybe_no_connection);
								}
							});
						}
					};
				}.start();
			}			
		});
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		progressDialog = new ProgressDialog(this);
		progressDialog.setTitle("Logging into GeoChat...");
		progressDialog.setMessage("Logging into GeoChat...");
		progressDialog.setCancelable(false);
		return progressDialog;
	}
	
	private void resync() {
		handler.post(new Runnable() {
			@Override
			public void run() {
				progressDialog.setMessage("First time login.\nThis might take several minutes.\nFetching groups...");
			}
		});
		
		Synchronizer synchronizer = new Synchronizer(LoginActivity.this);
		synchronizer.clearExistingData();
		
		final Group[] groups = synchronizer.syncGroups();
		
		handler.post(new Runnable() {
			@Override
			public void run() {
				progressDialog.setMessage("First time login.\nThis might take several minutes.\nFetching users...");
			}
		});
		
		synchronizer.syncUsers(groups);
		
		handler.post(new Runnable() {
			@Override
			public void run() {
				progressDialog.setMessage("First time login.\nThis might take several minutes.\nFetching messages...");
			}
		});
		
		synchronizer.syncMessages(groups);
		
		return;
	}
	
	private void enterGeoChat() {
		startActivity(new Intent()
			.setClass(LoginActivity.this, GeoChatTabsActivity.class));
	}

}
