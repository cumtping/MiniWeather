package com.peak.miniweather;

import java.util.ArrayList;

import com.peak.miniweather.utils.MiniWeatherUtils;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

public class MiniWeatherService extends Service {
	BroadcastReceiver mRefreshDataChangedReceiver = null;

	@Override
	public IBinder onBind(Intent arg0) {

		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void regsiterReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(MiniWeatherUtils.INTENT_REFRESH_DATA_CHANGED);

		mRefreshDataChangedReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				boolean autoRefresh = intent.getBooleanExtra("auto_fresh",
						false);
				ArrayList<CharSequence> refreshTimeList = intent
						.getCharSequenceArrayListExtra("refresh_time");
				String city = intent.getStringExtra("refresh_city");
			}
		};

		registerReceiver(mRefreshDataChangedReceiver, filter);
	}
}
