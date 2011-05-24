package com.tylersmith.webservice.ui;

import java.util.ArrayList;

import org.json.JSONArray;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.tylersmith.webservice.R;
import com.tylersmith.webservice.service.WebService;

public class Main extends ListActivity {

	private Main activity;
	private WebService webService;
	ProgressDialog dialog;
	ArrayList<String> data;
	public final static String PREFS_NAME = "tylersmith.webservice";
	public final static String PREFS_DATA = "data";

	private final int MENU_REFRESH = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		activity = this;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		bindService();
		bindList();

	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	private void bindService() {
		Intent a = new Intent(this, WebService.class);
		a.putExtra("fromApplication", true);
		// need to use this instead of startService();
		WakefulIntentService.sendWakefulWork(getApplicationContext(), a);
		// Binding ..this block can also start service if not started already
		Intent bindIntent = new Intent(this, WebService.class);
		bindService(bindIntent, serviceConnection, Context.BIND_AUTO_CREATE);
		// Register Broadcast Receiver
		IntentFilter filter = new IntentFilter(WebService.WEB_INTENT_FILTER);
		registerReceiver(myReceiver, filter);
	}

	private void bindList() {
		try {
			loadData();
		} catch (Exception e) {
			e.printStackTrace();
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, data);
		adapter.setNotifyOnChange(true);
		setListAdapter(adapter);
	}

	// Using JSON since it serializes so nicely
	// Could also create a custom adapter extended from BaseAdapter but thats
	// outside the scope of this tutorial.
	private void loadData() throws Exception {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		JSONArray jData = new JSONArray(sp.getString(PREFS_DATA, "[]"));

		data = new ArrayList<String>();
		for (int index = 0; index < jData.length(); index++) {
			data.add(jData.getJSONObject(index).getString("from_user")
					+ jData.getJSONObject(index).getString("text"));
		}
	}

	private void refreshData() {
		if (dialog == null || !dialog.isShowing())
			dialog = ProgressDialog.show(Main.this, "",
					"Loading. Please wait...", true);
		new Thread(webService).start();
	}

	private BroadcastReceiver myReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			Bundle extras = intent.getExtras();

			boolean success = extras.getBoolean(WebService.EXTRAS_SUCCESS,
					false);
			String message = extras
					.getString(WebService.EXTRAS_RESPONSE_MESSAGE);

			if (message == null)
				message = "There was a connection problem, please try again later";

			if (success) {
				try {
					loadData();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
		        //Using a reference to the activity instead of getApplicationContext() to prevent breaking
		        //This may keep a reference to the original and cause a memory leak
		        //TODO: investigate further
				AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				builder.setMessage(message)
						.setCancelable(false)
						.setNeutralButton("Ok",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.cancel();
									}
								});

				final AlertDialog alert = builder.create();
			}
			if (dialog != null && dialog.isShowing())
				dialog.dismiss();
		}
	};

	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			webService = ((WebService.WebBinder) service).getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			webService = null;
		}
	};

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		menu.add(0, MENU_REFRESH, 0, "Refresh").setIcon(
				android.R.drawable.ic_menu_rotate);
		return true;
	};

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_REFRESH: {
			refreshData();
		}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Unregister the Broadcast receiver and unbind service
		unregisterReceiver(myReceiver);
		unbindService(serviceConnection);
	}

}