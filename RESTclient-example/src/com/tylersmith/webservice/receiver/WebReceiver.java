package com.tylersmith.webservice.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.tylersmith.webservice.service.WebService;


public class WebReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		WakefulIntentService.sendWakefulWork(context, WebService.class);
	}
}
