/**
 * @author Deepak Ravishankar Ramkumar
 */
package com.deepak.expensetracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.gsm.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SMSReceiver extends BroadcastReceiver {
	@SuppressWarnings("deprecation")
	public void onReceive(Context context, Intent intent) {
		Bundle bundle=intent.getExtras();
		SmsMessage message[];
		String str=null;
		if(bundle != null){
			Object[] object=(Object[]) bundle.get("pdus");
			message=new SmsMessage[object.length];
			for (int i=0; i<message.length; i++){
				message[i] = SmsMessage.createFromPdu((byte[])object[i]);
				str += "SMS from "+ message[i].getOriginatingAddress();
				str += ":";
				str += message[i].getMessageBody().toString();
				str += "\n";
				}
				Log.v("nice", str);
				Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
		}
	}
}
