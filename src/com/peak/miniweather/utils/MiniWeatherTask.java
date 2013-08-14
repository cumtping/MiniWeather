package com.peak.miniweather.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.peak.miniweather.common.WeatherItem;

/**
 * AsyncTask to get weather information from the network.
 * 
 * @author ping
 * 
 */
public class MiniWeatherTask extends
		AsyncTask<String, WeatherItem, WeatherItem> {
	private static String TAG = MiniWeatherTask.class.getSimpleName();
	private Context mContext = null;
	private MiniWeatherWorkspaceHelper mWorkspaceHandler = null;

	public MiniWeatherTask(Context c,
			MiniWeatherWorkspaceHelper workspaceHandler) {
		mContext = c;
		mWorkspaceHandler = workspaceHandler;
	}

	@Override
	protected WeatherItem doInBackground(String... params) {
		Log.d(TAG, "doInBackground");
		if (params == null) {
			return null;
		}
		int total = params.length;
		if (total <= 0 || TextUtils.isEmpty(params[0])) {
			return null;
		}
		return MiniWeatherUtils.getWeatheFromrNMC(mContext, params[0]);
	}

	@Override
	protected void onPostExecute(WeatherItem result) {
		Log.d(TAG, "onPostExecute");
		super.onPostExecute(result);
		if (result == null) {
			return;
		}
		mWorkspaceHandler.updateCityWeather(result);
	}
}
