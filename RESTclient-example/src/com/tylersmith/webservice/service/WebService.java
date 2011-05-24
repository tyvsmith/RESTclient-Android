package com.tylersmith.webservice.service;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.tylersmith.restclient.RequestMethod;
import com.tylersmith.restclient.RestClient;
import com.tylersmith.webservice.ui.Main;


public class WebService extends WakefulIntentService implements Runnable {

	public static final String EXTRAS_RESPONSE_MESSAGE = "tylersmith.webservice.response_message";
	public static final String EXTRAS_SUCCESS = "tylersmith.webservice.success";

	public static final String WEB_INTENT_FILTER = "tylersmith.webservice.intent.filter";
	private final IBinder binder = new WebBinder();

	private Handler handler = new Handler();
	protected Context context;


	public WebService() {
		super("UpdateService");
	}

	@Override
	protected void doWakefulWork(Intent intent) {
		context = getApplicationContext();
		//preventing this from running unless specifically called from within the activity or through the alarm manager
//		if(!intent.getBooleanExtra("fromApplication", false))
			new Thread(this).start();
	}

	@Override
	public void run() {
		String twitterUrl = "http://search.twitter.com/search.json";
		RestClient client = new RestClient(twitterUrl);
		client.addParam("q", "android");
		try {
			client.execute(RequestMethod.GET);
			if(client.getResponseCode() == 200) {
				//Successfully connected
				JSONObject jObj = new JSONObject(client.getResponse());
				JSONArray jResults = jObj.getJSONArray("results");

				SharedPreferences.Editor editPrefs =
	                PreferenceManager.getDefaultSharedPreferences(context).edit();
	            editPrefs.putString(Main.PREFS_DATA, jResults.toString());
	            editPrefs.commit();
	            broadCast(true, "Success");

			} else {
				//error connecting to server, lets just return an error
				broadCast(false, "Error Connecting");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}


	}

	private void broadCast(boolean success, String message) {
		Intent intent = new Intent();
		intent.putExtra(EXTRAS_SUCCESS, success);
		intent.putExtra(EXTRAS_RESPONSE_MESSAGE, message);
		intent.setAction(WEB_INTENT_FILTER);
		sendBroadcast(intent);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		context = getApplicationContext();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	public class WebBinder extends Binder {
		public WebService getService() {
			return WebService.this;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
