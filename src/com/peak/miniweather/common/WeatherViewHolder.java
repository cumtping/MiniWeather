package com.peak.miniweather.common;

import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.peak.miniweather.R;
import com.peak.miniweather.launcher.Workspace;

public class WeatherViewHolder {
	View mView;
	public TextView cityNameTextView;
	public TextView weekTextView;
	public TextView timeTextView;
	public TextView tempTextView;
	public TextView windTextView;
	public TextView weatherTextView;
	public ImageView picImageView;
	public GridView gridView;
	public Workspace forecastWorkspace;

	public WeatherViewHolder(View v) {
		mView = v;
		findViewByIds();
	}

	private void findViewByIds() {
		cityNameTextView = (TextView) mView.findViewById(R.id.city_name);
		weekTextView = (TextView) mView.findViewById(R.id.week);
		timeTextView = (TextView) mView.findViewById(R.id.time);
		tempTextView = (TextView) mView.findViewById(R.id.today_temp);
		windTextView = (TextView) mView.findViewById(R.id.today_wind);
		weatherTextView = (TextView) mView.findViewById(R.id.today_weather);
		picImageView = (ImageView) mView.findViewById(R.id.today_pic);
		gridView = (GridView) mView.findViewById(R.id.forcast_grid);
		forecastWorkspace = (Workspace) mView
				.findViewById(R.id.forcast_workspace);
	}

}
