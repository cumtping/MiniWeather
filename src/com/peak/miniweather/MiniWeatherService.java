package com.peak.miniweather;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MiniWeatherService extends Service {

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

	@Override
	@Deprecated
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}

}
