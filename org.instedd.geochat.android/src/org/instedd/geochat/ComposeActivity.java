package org.instedd.geochat;

import org.instedd.geochat.GeoChat.Groups;
import org.instedd.geochat.api.Group;
import org.instedd.geochat.api.IGeoChatApi;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class ComposeActivity extends Activity {
	
    private static final String[] PROJECTION = new String[] {
            Groups._ID,
            Groups.NAME,
            Groups.ALIAS,
    };
    
    private final static int MENU_HOME = 1;
    
    private final Handler handler = new Handler();
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.compose);
	    
	    Cursor c = getContentResolver().query(Groups.CONTENT_URI, PROJECTION, null, null, "lower(" + Groups.NAME + ")");
	    Group[] groups = new Group[c.getCount() + 1];
	    groups[0] = new Group();
	    groups[0].name = "(No group)";
	    for (int i = 0; c.moveToNext(); i++) {
			groups[i + 1] = new Group();
			groups[i + 1].name = c.getString(c.getColumnIndex(Groups.NAME));
			groups[i + 1].alias = c.getString(c.getColumnIndex(Groups.ALIAS));
		}
	    c.close();
	    
	    ArrayAdapter<Group> adapter = new ArrayAdapter<Group>(this, android.R.layout.simple_spinner_item, groups);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    
	    final EditText uiMessage = (EditText) findViewById(R.id.message);
	    final Spinner uiGroup = (Spinner) findViewById(R.id.group);
	    final Button uiSend = (Button) findViewById(R.id.send);
	    
	    uiGroup.setAdapter(adapter);
	    
	    uiSend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Group group = (Group) uiGroup.getSelectedItem();
				final Editable message = uiMessage.getText();
				
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
	}
	
	private void sendMessage(Group group, String message) {
		handler.post(new Runnable() {
			@Override
			public void run() {
				showDialog(0);
			}
		});
		
		try {
			IGeoChatApi api = new GeoChatSettings(ComposeActivity.this).newApi();
			if (group.alias == null) {
				api.sendMessage(message);
			} else {
				api.sendMessage(group.alias, message);
			}
			goHome();
		} catch (Exception e) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(ComposeActivity.this, getResources().getString(R.string.message_could_not_be_sent_maybe_no_connection), Toast.LENGTH_LONG);
				}
			});
		} finally {
			handler.post(new Runnable() {
				@Override
				public void run() {
					dismissDialog(0);
				}
			});
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		String title = getResources().getString(R.string.sending_message);
		String message = getResources().getString(R.string.sending_elipsis);
		
		ProgressDialog progressDialog = new ProgressDialog(this);
		progressDialog.setTitle(title);
		progressDialog.setMessage(message);
		progressDialog.setCancelable(false);
		return progressDialog;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_HOME, 0, R.string.home)
			.setIcon(R.drawable.ic_menu_home);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case MENU_HOME:
			goHome();
			break;
		}
		return true;
	}
	
	private void goHome() {
		startActivity(new Intent().setClass(this, HomeActivity.class)
				.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
	}

}
