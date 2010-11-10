package org.instedd.geochat;

import org.instedd.geochat.api.Group;
import org.instedd.geochat.data.GeoChatData;
import org.instedd.geochat.data.GeoChat.Groups;
import org.instedd.geochat.map.LatLng;
import org.instedd.geochat.map.LocationTracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class ComposeActivity extends Activity {
	
	private final static String[] COMMANDS = {
		"#block ", ".block ",
		"#create ", ".create ",
		"#invite ", ".invite ",
		"#help ", ".help ",
		"#join ", ".join ",
		"#lang ", ".lang ",
		"#leave ", ".leave ",
		"#login ", ".login ",
		"#logout ", ".logout ",
		"#my email ", ".my email ",
		"#my group ", ".my group ",
		"#my groups ", ".my groups ",
		"#my name ", ".my name ",
		"#my number ", ".my number ",
		"#my password ", ".my password ",
		"#name ", ".name ",
		"#off ", ".off ",
		"#on ", ".on ",
		"#owner ", ".owner ",
		"#ping ", ".ping ",
		"#whereis ", ".whereis ",
		"#whois ", ".whois ",
	};
    
    private final static int DIALOG_SENDING_MESSAGE = 1;
    private final static int DIALOG_CANNOT_SEND_MESSAGE = 2;
    
    private final Handler handler = new Handler();
    
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.compose);
	    
	    String composeGroup = new GeoChatData(this).getGroupAlias(getIntent().getData());
	    if (composeGroup == null) {
	    	composeGroup = new GeoChatSettings(this).getComposeGroup();
	    }
	    int composeGroupIndex = 0;
	    
	    String[] PROJECTION = new String[] {
	            Groups._ID,
	            Groups.NAME,
	            Groups.ALIAS,
	    };
	    Cursor c = getContentResolver().query(Groups.CONTENT_URI, PROJECTION, null, null, "lower(" + Groups.NAME + ")");
	    final int nameIndex = c.getColumnIndex(Groups.NAME);
	    final int aliasIndex = c.getColumnIndex(Groups.ALIAS);
	    
	    Group[] groups = new Group[c.getCount() + 1];
	    groups[0] = new Group();
	    groups[0].name = "(No group)";
	    for (int i = 0; c.moveToNext(); i++) {
			groups[i + 1] = new Group();
			groups[i + 1].name = c.getString(nameIndex);
			groups[i + 1].alias = c.getString(aliasIndex);
			
			if (composeGroup != null && composeGroup.equals(groups[i + 1].alias)) {
				composeGroupIndex = i + 1;
			}
		}
	    c.close();
	    
	    ArrayAdapter<Group> adapter = new ArrayAdapter<Group>(this, android.R.layout.simple_spinner_item, groups);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    
	    final AutoCompleteTextView uiMessage = (AutoCompleteTextView) findViewById(R.id.message);
	    final Spinner uiGroup = (Spinner) findViewById(R.id.group);
	    final Button uiSend = (Button) findViewById(R.id.send);
	    
	    uiGroup.setAdapter(adapter);
	    uiGroup.setSelection(composeGroupIndex);
	    
	    uiSend.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final Group group = (Group) uiGroup.getSelectedItem();
				final CharSequence message = uiMessage.getText();
				
				if (TextUtils.getTrimmedLength(message) == 0) {
					return;
				}
				
				new Thread() {
					public void run() {
						sendMessage(group, message.toString());
					}
				}.start();
			}
		});
	    
	    ArrayAdapter autocompleteAdapter = new ArrayAdapter(this,
	            android.R.layout.simple_dropdown_item_1line, COMMANDS);
	    uiMessage.setAdapter(autocompleteAdapter);
	}

	private void sendMessage(Group group, String message) {
		handler.post(new Runnable() {
			public void run() {
				showDialog(DIALOG_SENDING_MESSAGE);
			}
		});
		
		try {
			Messenger messenger = new Messenger(this);
			int result;
			if (group == null) {
				result = messenger.sendMessage(message);
			} else {
				result = messenger.sendMessage(group.alias, message);
			}
			
			switch(result) {
			case Messenger.SENT_VIA_API:
			case Messenger.SENT_VIA_SMS:
				new GeoChatSettings(this).setComposeGroup(group == null ? null : group.alias);
				handler.post(new Runnable() {
					public void run() {
						Toast.makeText(ComposeActivity.this, R.string.message_sent, Toast.LENGTH_LONG).show();
					}
				});
				Actions.home(this);
				break;
			case Messenger.NOT_SENT_NO_GEOCHAT_NUMBER:
				handler.post(new Runnable() {
					public void run() {
						showDialog(DIALOG_CANNOT_SEND_MESSAGE);
					}
				});
				break;
			}
		} finally {
			handler.post(new Runnable() {
				public void run() {
					dismissDialog(DIALOG_SENDING_MESSAGE);
				}
			});
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
		case DIALOG_SENDING_MESSAGE:
			String title = getResources().getString(R.string.sending_message);
			String message = getResources().getString(R.string.sending_elipsis);
			
			ProgressDialog progressDialog = new ProgressDialog(this);
			progressDialog.setTitle(title);
			progressDialog.setMessage(message);
			progressDialog.setCancelable(false);
			return progressDialog;
		case DIALOG_CANNOT_SEND_MESSAGE:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.message_could_not_be_sent)
				.setTitle(R.string.message_not_sent)
				.setCancelable(true)
				.setNeutralButton(android.R.string.ok, null);
			return builder.create();
		default:
			return null;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Menues.home(menu);
		Menues.pasteMyLocation(menu);
		Menues.settings(menu);
		Menues.signout(menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Menues.executeAction(this, handler, item.getItemId());
		if (item.getItemId() == Menues.PASTE_MY_LOCATION) {
			final Toast toast = Toast.makeText(this, getResources().getString(R.string.retrieving_your_location), Toast.LENGTH_LONG);
			toast.show();
			
			new Thread() {
				public void run() {
					final LatLng location = LocationTracker.getLocation(ComposeActivity.this);
					
					handler.post(new Runnable() {
						public void run() {
							toast.cancel();
							
							if (location == null) {
								Toast.makeText(ComposeActivity.this, getResources().getString(R.string.could_not_retrieve_your_location), Toast.LENGTH_LONG).show();
							} else {
								EditText uiMessage = (EditText) findViewById(R.id.message);
								int start = uiMessage.getSelectionStart();
								int end = uiMessage.getSelectionEnd();
								Editable text = uiMessage.getText();
								if (text == null || TextUtils.getTrimmedLength(text) == 0) {
									text.append("at " + location.lat + ", " + location.lng + " * ");
								} else {
									if (start > 0 && text.charAt(start - 1) == '/') {
										text.replace(start, end, location.lat + ", " + location.lng + "/");
									} else {
										text.replace(start, end, "/" + location.lat + ", " + location.lng + "/");
									}
								}
							}		
						}
					});
				};
			}.start();
		}
		return true;
	}

}
