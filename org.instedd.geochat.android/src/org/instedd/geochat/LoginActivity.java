package org.instedd.geochat;

import org.instedd.geochat.api.GeoChatApi;
import org.instedd.geochat.api.GeoChatApiException;
import org.instedd.geochat.api.Group;
import org.instedd.geochat.api.IGeoChatApi;
import org.instedd.geochat.api.RestClient;
import org.instedd.geochat.sync.Synchronizer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends Activity {
	
	public final static String EXTRA_WRONG_CREDENTIALS = "WrongCredentials";
	
	private final static int DIALOG_LOGGING_IN = 1;
	private final static int DIALOG_WRONG_CREDENTIALS = 2;
	private final static int DIALOG_UNKNOWN_ERROR = 3;
	
	private final Handler handler = new Handler();
	private ProgressDialog progressDialog;
	private boolean wrongCredentials;
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.login);
	    
	    Intent intent = getIntent();
	    wrongCredentials = intent != null && intent.getBooleanExtra(EXTRA_WRONG_CREDENTIALS, false);
	    
	    final GeoChatSettings settings = new GeoChatSettings(this);
	    final String existingUser = settings.getUser();
	    final String existingPassword = settings.getPassword();
	    
	    final EditText uiUser = (EditText) findViewById(R.id.user);
	    final EditText uiPassword = (EditText) findViewById(R.id.password);
	    final Button uiLogin = (Button) findViewById(R.id.login_button); 
	    
	    uiUser.setText(existingUser);
	    uiPassword.setText(existingPassword);
	    
	    uiLogin.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				new Thread() {
					public void run() {
						handler.post(new Runnable() {
							public void run() {
								showDialog(DIALOG_LOGGING_IN);
								uiLogin.setEnabled(false);
							}
						});
						
						String user = uiUser.getText().toString();
						String password = uiPassword.getText().toString();
						
						boolean userChanged = user != null && !user.equals(existingUser);
						boolean passwordChanged = password != null && !password.equals(existingPassword);
						boolean credentialsAreValid = true;
						
						try {
							if (userChanged || passwordChanged || wrongCredentials) {
								IGeoChatApi api = new GeoChatApi(new RestClient(), user, password);
								credentialsAreValid = api.credentialsAreValid();
							}
							
							if (credentialsAreValid) {
								settings.setUserAndPassword(user, password);
								if (userChanged) {
									resync();
								}
								Actions.home(LoginActivity.this);
							} else {
								handler.post(new Runnable() {
									public void run() {
										uiLogin.setEnabled(true);
										dismissDialog(DIALOG_LOGGING_IN);
										showDialog(DIALOG_WRONG_CREDENTIALS);
									}									
								});
							}
						} catch (GeoChatApiException e) {
							handler.post(new Runnable() {
								public void run() {
									uiLogin.setEnabled(true);
									dismissDialog(DIALOG_LOGGING_IN);
									showDialog(DIALOG_UNKNOWN_ERROR);
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
		if (id == DIALOG_LOGGING_IN) {
			String message = getResources().getString(R.string.logging_in_to_geochat);
			
			progressDialog = new ProgressDialog(this);
			progressDialog.setTitle(message);
			progressDialog.setMessage(message);
			progressDialog.setCancelable(false);
			return progressDialog;	
		} else {
			int messageResourceId = id == DIALOG_WRONG_CREDENTIALS ? R.string.invalid_credentials : R.string.cannott_login_maybe_no_connection; 

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(messageResourceId)
				.setTitle(R.string.cannot_login_to_geochat)
				.setCancelable(true)
				.setNeutralButton(android.R.string.ok, null);
			return builder.create();
		}
	}
	
	private void resync() throws GeoChatApiException {
		handler.post(new Runnable() {
			public void run() {
				progressDialog.setMessage(getResources().getString(R.string.first_time_logging_fetching_groups));
			}
		});
		
		Synchronizer synchronizer = new Synchronizer(LoginActivity.this, handler);
		synchronizer.clearExistingData();
		
		final Group[] groups = synchronizer.syncGroups();
		
		handler.post(new Runnable() {
			public void run() {
				progressDialog.setMessage(getResources().getString(R.string.first_time_logging_fetching_users));
			}
		});
		
		synchronizer.syncUsers(groups);
		
		handler.post(new Runnable() {
			public void run() {
				progressDialog.setMessage(getResources().getString(R.string.first_time_logging_fetching_messages));
			}
		});
		
		synchronizer.syncMessages(groups);
	}

}
