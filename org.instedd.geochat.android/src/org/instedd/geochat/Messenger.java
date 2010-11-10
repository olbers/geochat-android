package org.instedd.geochat;

import java.util.ArrayList;

import org.instedd.geochat.api.GeoChatApiException;
import org.instedd.geochat.api.IGeoChatApi;

import android.content.Context;
import android.telephony.SmsManager;
import android.text.TextUtils;

public class Messenger {
	
	public final static int SENT_VIA_API = 1;
	public final static int SENT_VIA_SMS = 2;
	public final static int NOT_SENT_NO_GEOCHAT_NUMBER = -1;
	
	private final Context context;

	public Messenger(Context context) {
		this.context = context;		
	}
	
	public int sendMessage(String message) {
		return sendMessage(null, message);
	}

	public int sendMessage(String groupAlias, String message) {
		// First try sending using API
		GeoChatSettings settings = new GeoChatSettings(context);
		
		if (Connectivity.hasConnectivity(context)) {
			if (sendMessageUsingApi(settings, groupAlias, message)) {
				return SENT_VIA_API;
			}
		}
		
		// Try sending an sms
		String number = settings.getGeoChatNumber();
		if (number == null || TextUtils.getTrimmedLength(number) == 0) {
			return NOT_SENT_NO_GEOCHAT_NUMBER;
		}
		
		if (groupAlias != null) {
			message = "@" + groupAlias + " " + message;
		}
		
		SmsManager sms = SmsManager.getDefault();
		ArrayList<String> parts = sms.divideMessage(message);
		
		sms.sendMultipartTextMessage(number, null, parts, null, null);
		
		return SENT_VIA_SMS;
	}
	
	private boolean sendMessageUsingApi(GeoChatSettings settings, String groupAlias, String message) {
		try {
			
			IGeoChatApi api = settings.newApi();
			if (groupAlias == null) {
				api.sendMessage(message);
			} else {
				api.sendMessage(groupAlias, message);
			}
			return true;
		} catch (GeoChatApiException e) {
			return false;
		}
	}

}
