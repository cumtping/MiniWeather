package com.peak.miniweather.utils;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.peak.miniweather.MiniWeatherActivity;
import com.peak.miniweather.MiniWeatherForecastGridViewAdapter;
import com.peak.miniweather.R;
import com.peak.miniweather.SelectCityActivity;
import com.peak.miniweather.common.ForecastViewHolder;
import com.peak.miniweather.common.WeatherItem;
import com.peak.miniweather.common.WeatherViewHolder;
import com.peak.miniweather.launcher.DragLayer;
import com.peak.miniweather.launcher.Workspace;

public class MiniWeatherWorkspaceHelper implements
		Workspace.OnScreenChangedListener {
	private static final String TAG = MiniWeatherWorkspaceHelper.class
			.getSimpleName();
	private Context mContext = null;
	private Activity mAct = null;
	DragLayer mDragLayer = null;
	Workspace mWorkspace = null;
	LinearLayout mPagingLayout = null;
	int mCurScreenIndex = 0;
	int mDefaultScreen = 0;
	// private int WORKSPACE_OFFSET = 0;

	HashMap<String, WeatherItem> mCityWeatherItemMap = null;
	ArrayList<String> mWorkspaceCityList = null;

	/**
	 * Constructor
	 * 
	 * @param c
	 * @param content
	 */
	public MiniWeatherWorkspaceHelper(Activity act, View content) {
		if (content == null || act == null) {
			return;
		}
		mAct = act;
		mContext = act.getApplicationContext();
		mWorkspaceCityList = new ArrayList<String>();
		mDragLayer = (DragLayer) content.findViewById(R.id.drag_layer);
		mWorkspace = (Workspace) content.findViewById(R.id.content);
		mWorkspace.setOnScreenChangedListener(this);
		mPagingLayout = (LinearLayout) content.findViewById(R.id.paging);
		mDefaultScreen = mWorkspace.getDefaultScreen();
		setCurScreenIndex(mDefaultScreen);

		int totalChild = mWorkspace.getChildCount();
		Log.d(TAG, "totalChild=" + totalChild);
		mCityWeatherItemMap = new HashMap<String, WeatherItem>();
	}

	/**
	 * 
	 * @param index
	 */
	public void setCurScreenIndex(int index) {
		if (index < 0 || index >= mPagingLayout.getChildCount()) {
			return;
		}
		mCurScreenIndex = index;
		mPagingLayout.getChildAt(mCurScreenIndex).setBackgroundResource(
				R.drawable.dian_select);
	}

	/**
	 * 
	 * @param newScreenNum
	 */
	public void initWorkspace(ArrayList<String> cityList) {
		int totalScreen = cityList.size();
		if (cityList == null || 0 == cityList.size()) {
			return;
		}
		mWorkspaceCityList = cityList;
		mWorkspace.setTotalScreen(totalScreen);
		for (int i = 0; i < totalScreen; i++) {
			addOneCityToWorkspace(cityList.get(i));
		}
	}

	/**
	 * Add a city weather information container to the workspace.
	 * 
	 * @param city
	 *            : city name.
	 */
	public void addOneCityToWorkspace(String city) {
		if (TextUtils.isEmpty(city)) {
			return;
		}

		if (mWorkspaceCityList.size() == 0) {
			mWorkspace.removeAllViews();
			mWorkspace.setLayoutParams(new FrameLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		}

		if (!mWorkspaceCityList.contains(city)) {
			mWorkspaceCityList.add(city);
		}

		LayoutInflater inflater = LayoutInflater.from(mContext);
		/**
		 * Add a weather item to the weather layout, increase the workspace's
		 * total screen by 1.
		 */
		View child = (View) inflater
				.inflate(R.layout.weather_item_layout, null);
		child.setTag(city);
		mWorkspace.addView(child);
		mWorkspace.setTotalScreen(mWorkspace.getTotalScreen() + 1);
		// Add an icon to the paging layout.
		ViewGroup.LayoutParams param = new ViewGroup.LayoutParams(12, 12);
		ImageView imageView = new ImageView(mContext);
		imageView.setLayoutParams(param);
		imageView.setBackgroundResource(R.drawable.dian_normal);
		mPagingLayout.addView(imageView);
		// invalidate view.
		mWorkspace.invalidate();
		mPagingLayout.invalidate();
	}

	/**
	 * Update the weather information of a city in the workspace.
	 * 
	 * @param item
	 */
	public void updateCityWeather(WeatherItem item) {
		if (item == null) {
			return;
		}
		mCityWeatherItemMap.put(item.getCityName(), item);
		int index = mWorkspaceCityList.indexOf(item.getCityName());

		if (index < 0) {
			Log.e(TAG, "updateCityWeather error, city=" + item.getCityName());
			return;
		}

		View child = mWorkspace.getChildAt(index);
		// Set waitingLayout gone, weather layout visible.
		View waitingLayout = child.findViewById(R.id.waiting_layout);
		View todayLayout = child.findViewById(R.id.today_weather_layout);
		View forcastLayout = child.findViewById(R.id.forcast_drag_layer);
		waitingLayout.setVisibility(View.GONE);
		todayLayout.setVisibility(View.VISIBLE);
		forcastLayout.setVisibility(View.VISIBLE);
		WeatherViewHolder viewHolder = new WeatherViewHolder(child);
		// Today's weather information.
		viewHolder.cityNameTextView.setText(item.getCityName());
		viewHolder.tempTextView.setText(item.getTemp());
		viewHolder.timeTextView.setText(item.getDate_y());
		viewHolder.weatherTextView.setText(item.getWeather());
		viewHolder.weekTextView.setText(item.getWeek());
		viewHolder.windTextView.setText(item.getWD() + item.getWS());
		viewHolder.picImageView.setBackgroundResource(MiniWeatherUtils
				.getWeatherIconFlag(mContext, item.getWeather(), true));
		// Weather forecast information.
		if (viewHolder.gridView != null) {
			viewHolder.gridView
					.setAdapter(new MiniWeatherForecastGridViewAdapter(
							mContext, item.getForecastList()));
		}
		if (viewHolder.forecastWorkspace != null) {
			viewHolder.forecastWorkspace.setTotalScreen(3);

			for (int i = 0; i < 6; i++) {
				View forecastGroupView = viewHolder.forecastWorkspace
						.findViewById(R.id.forcast1 + i);
				ForecastViewHolder forcastHolder = new ForecastViewHolder(
						mContext, forecastGroupView);
				forcastHolder.setViewContent(item.getForecastList().get(i));
			}
		}
	}

	@Override
	public void onScreenChanded(int index) {
		int pageNum = mPagingLayout.getChildCount();
		if (index < 0 || index >= pageNum) {
			return;
		}
		for (int i = 0; i < pageNum; i++) {
			mPagingLayout.getChildAt(i).setBackgroundResource(
					R.drawable.dian_normal);
		}
		mPagingLayout.getChildAt(index).setBackgroundResource(
				R.drawable.dian_select);

		// execute getting weather task
		String city = mWorkspaceCityList.get(index);
		if (mCityWeatherItemMap.get(city) == null) {
			MiniWeatherUtils.executeMiniWeatherTask(mContext, this, city);
		}
	}

	public void snapToScreen(String cityName) {
		int index = mWorkspaceCityList.indexOf(cityName);
		if (index < 0) {
			Log.e(TAG, "snapToScreen error, city=" + cityName);
			return;
		}
		mWorkspace.snapToScreen(index);
	}

	public void removeCurrentScreen() {
		int cur = mWorkspace.getCurrentScreen();
		removeOneScreen(cur);
	}

	public void removeOneScreen(int index) {
		if (index < 0 || index >= mWorkspaceCityList.size()) {
			Log.e(TAG, "removeOneScreen index out of bounds");
			return;
		}
		String city = mWorkspaceCityList.get(index);
		// snap screen
		int screenSize = mWorkspaceCityList.size();
		Log.d(TAG, "removeOneScreen index=" + index + " city=" + city
				+ " screenSize=" + screenSize);
		// remove from list
		mWorkspaceCityList.remove(index);
		mCityWeatherItemMap.remove(city);
		// remove views
		if (mWorkspaceCityList.size() == 0) {
			// If no city left, show no_city_layout.
			mWorkspace.addView(inflateNoCityLayout());
		}
		mWorkspace.removeViewAt(index);
		mPagingLayout.removeViewAt(index);
	}

	public View inflateNoCityLayout() {
		mWorkspace.setLayoutParams(new FrameLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		LayoutInflater inflater = LayoutInflater.from(mContext);
		View noCityLayout = inflater.inflate(R.layout.no_city_layout, null);
		TextView tv = (TextView) noCityLayout
				.findViewById(R.id.no_city_notice_text);
		tv.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mAct.startActivityForResult(new Intent(mAct,
						SelectCityActivity.class),
						MiniWeatherActivity.SELECT_CITY_REQUEST_CODE);
			}
		});
		return noCityLayout;
	}

	public ArrayList<String> getCityList() {
		return mWorkspaceCityList;
	}
}
