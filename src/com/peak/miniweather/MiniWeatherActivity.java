package com.peak.miniweather;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.peak.miniweather.utils.MiniWeatherDBHelper;
import com.peak.miniweather.utils.MiniWeatherTask;
import com.peak.miniweather.utils.MiniWeatherUtils;
import com.peak.miniweather.utils.MiniWeatherWorkspaceHelper;

public class MiniWeatherActivity extends Activity {
	private static final String TAG = MiniWeatherActivity.class.getSimpleName();
	// The view contains weather info
	View mContentView = null;
	// The default city.
	String mDefaultCity = null;
	// The saved city list, which is loaded from the SharePreference.
	ArrayList<String> mSavedCityList = null;
	MiniWeatherWorkspaceHelper mWorkspaceHandler = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		// init content.
		initContent();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private boolean checkNetwork() {
		View noNetworkView = findViewById(R.id.no_network_layout);
		mContentView = findViewById(R.id.content_layout);
		if (MiniWeatherUtils.networkAvailable(this)) {
			mContentView.setVisibility(View.VISIBLE);
			noNetworkView.setVisibility(View.GONE);
			return true;
		} else {
			mContentView.setVisibility(View.GONE);
			noNetworkView.setVisibility(View.VISIBLE);
			return false;
		}
	}

	private void initContent() {
		// 1 check network
		boolean networkOK = checkNetwork();

		if (networkOK) {
			mWorkspaceHandler = new MiniWeatherWorkspaceHelper(this,
					mContentView);
			mSavedCityList = MiniWeatherUtils.loadSavedCityList(this);
			String currentCity = null;
			ArrayList<String> initCityList = mSavedCityList;

			if (mSavedCityList == null || mSavedCityList.size() <= 0) {
				currentCity = mDefaultCity = getString(R.string.default_city);
				initCityList = new ArrayList<String>();
				initCityList.add(mDefaultCity);
			} else {
				currentCity = mSavedCityList.get(0);
			}

			mWorkspaceHandler.initWorkspace(initCityList);
			mWorkspaceHandler.setCurScreenIndex(0);
			MiniWeatherUtils.executeMiniWeatherTask(this, mWorkspaceHandler,
					currentCity);
		}
	}

	public static final int SELECT_CITY_REQUEST_CODE = 1000;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_add:
			startActivityForResult(new Intent(this, SelectCityActivity.class),
					SELECT_CITY_REQUEST_CODE);
			break;
		case R.id.action_remove:
			mWorkspaceHandler.removeCurrentScreen();
			break;
		case R.id.action_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case SELECT_CITY_REQUEST_CODE:

			if (resultCode == RESULT_OK && data != null) {
				String city = data.getExtras().getString("city");
				Log.d(TAG, "onActivityResult city=" + city);

				if (TextUtils.isEmpty(city)) {
					return;
				}
				if (MiniWeatherDBHelper.getInstance(this).queryUniqueCityCode(
						city) == null) {
					Toast.makeText(this,
							getString(R.string.choose_city_unsupport),
							Toast.LENGTH_LONG).show();
					return;
				}

				mWorkspaceHandler.addOneCityToWorkspace(city);
				// snap to the added screen.
				mWorkspaceHandler.snapToScreen(city);

			}
			break;

		default:
			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mWorkspaceHandler != null) {
			MiniWeatherUtils
					.savedityList(this, mWorkspaceHandler.getCityList());
		}
	}
}
