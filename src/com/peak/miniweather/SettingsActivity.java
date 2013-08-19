package com.peak.miniweather;

import java.util.ArrayList;

import com.peak.miniweather.utils.MiniWeatherUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class SettingsActivity extends Activity {
	private static final String TAG = SettingsActivity.class.getSimpleName();
	private ListView mSettingsListView = null;
	private int[] mLayoutResourceIdList;
	boolean mAutoRefresh = true;
	ArrayList<String> mRefreshTimeList = new ArrayList<String>();
	ArrayList<String> mCityList = new ArrayList<String>();
	String mRefreshCity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.settings_layout);

		initValuesFromPreferences();
		mSettingsListView = (ListView) findViewById(R.id.settings_listview);
		mLayoutResourceIdList = new int[] {
				android.R.layout.simple_list_item_multiple_choice,
				R.layout.list_item_spinner, R.layout.list_item_spinner };
		mSettingsListView.setAdapter(new SettingsListViewAdapter());
		mSettingsListView.setOnItemClickListener(new MyOnItemClickListener());
		findViewById(R.id.more_city).setVisibility(View.INVISIBLE);
	}

	class MyOnItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
				long arg3) {
			switch (pos) {
			case 1:
				if (mAutoRefresh)
					createChooseRefeshTimeDialog();
				break;
			case 2:
				if (mAutoRefresh)
					createChooseRefeshCityDialog();
				break;

			default:
				break;
			}
		}

	}

	private void initValuesFromPreferences() {
		mAutoRefresh = MiniWeatherUtils.loadAutoRefreshFromPreference(this);
		mRefreshTimeList = MiniWeatherUtils.loadRefreshTimeFromPreference(this);
		Log.d(TAG, "mAutoRefresh=" + mAutoRefresh + " mRefreshTimeLis.size()="
				+ mRefreshTimeList.size());
		mCityList = MiniWeatherUtils.loadSavedCityList(this);
		mRefreshCity = MiniWeatherUtils.loadRefreshCityFromPreference(this);
	}

	class SettingsListViewAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mLayoutResourceIdList.length;
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup arg2) {
			if (convertView == null) {
				if (position >= mLayoutResourceIdList.length) {
					return null;
				}

				LayoutInflater inflater = LayoutInflater
						.from(SettingsActivity.this);
				convertView = inflater.inflate(mLayoutResourceIdList[position],
						null);

				if (convertView == null) {
					Log.d(TAG, "convertView=null, pos=" + position);
				}

				switch (position) {
				case 0:
					final CheckedTextView tv = (CheckedTextView) convertView
							.findViewById(android.R.id.text1);
					tv.setText(R.string.auto_refresh);
					tv.setTextColor(getResources().getColor(
							android.R.color.white));
					tv.setTextSize(getResources().getDimension(
							R.dimen.large_text));
					tv.setChecked(true);
					tv.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							mAutoRefresh = tv.isChecked();
							tv.setChecked(!tv.isChecked());
						}
					});
					break;

				case 1:

					break;
				case 2:
					TextView refreshCity = (TextView) convertView
							.findViewById(R.id.spinner_text);
					if (refreshCity != null) {
						refreshCity.setText(R.string.refresh_city);
					}
					break;
				default:
					break;
				}
			}

			return convertView;
		}
	}

	public void createChooseRefeshTimeDialog() {
		final String[] refreshTimes = getResources().getStringArray(
				R.array.refresh_time);

		boolean[] isCheckedList = new boolean[refreshTimes.length];

		for (int i = 0; i < refreshTimes.length; i++) {
			if (mRefreshTimeList.contains(refreshTimes[i])) {
				isCheckedList[i] = true;
			}
		}

		final ArrayList<String> checkedList = mRefreshTimeList;

		new AlertDialog.Builder(this)
				.setMultiChoiceItems(refreshTimes, isCheckedList,
						new DialogInterface.OnMultiChoiceClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which, boolean isChecked) {
								if (isChecked) {
									checkedList.add(refreshTimes[which]);
								} else {
									checkedList.remove(refreshTimes[which]);
								}
							}
						})
				.setPositiveButton(getString(R.string.confirm),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								mRefreshTimeList = checkedList;
								MiniWeatherUtils
										.saveRefreshTimeToPreference(
												SettingsActivity.this,
												mRefreshTimeList);
							}
						})
				.setNegativeButton(getString(R.string.cancel),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
							}
						}).show();

	}

	private void createChooseRefeshCityDialog() {

		if (mCityList == null || mCityList.size() <= 0) {
			Toast.makeText(this, R.string.choose_city_msg, Toast.LENGTH_LONG)
					.show();
			return;
		}

		int checkIndex = 0;
		for (int i = 0; i < mCityList.size(); i++) {
			if (mCityList.contains(mRefreshCity)) {
				checkIndex = i;
			}
		}

		final String[] citys = new String[mCityList.size()];
		mCityList.toArray(citys);
		final int chooseIndex;

		new AlertDialog.Builder(this)
				.setTitle(R.string.choose_city_msg)
				.setSingleChoiceItems(citys, checkIndex,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								MiniWeatherUtils.saveRefreshCityToPreference(
										SettingsActivity.this, citys[which]);
								dialog.dismiss();
							}
						}).show();

	}
}
