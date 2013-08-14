package com.peak.miniweather.common;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.peak.miniweather.R;
import com.peak.miniweather.utils.MiniWeatherUtils;

public class ForecastViewHolder {
	private View mView = null;
	private Context mContext = null;
	public TextView dayOfWeekTextView = null;
	public TextView weatherTextView = null;
	public TextView tmpRangeTextView = null;
	public ImageView picImageView = null;

	public ForecastViewHolder(Context c, View v) {
		mView = v;
		mContext = c;
		findViewById();
	}

	public void setViewContent(ForcastItem item) {
		if (dayOfWeekTextView != null) {
			dayOfWeekTextView.setText(item.getDayOfWeek());
		}
		if (tmpRangeTextView != null) {
			tmpRangeTextView.setText(item.getTemp());
		}
		if (weatherTextView != null) {
			weatherTextView.setText(item.getWeather());
		}
		if (picImageView != null) {
			picImageView
					.setBackgroundResource(MiniWeatherUtils
							.getWeatherIconFlag(mContext,
									item.getWeather(), false));
		}

	}

	private void findViewById() {
		dayOfWeekTextView = (TextView) mView.findViewById(R.id.dayOfWeek);
		weatherTextView = (TextView) mView
				.findViewById(R.id.forcast_weather);
		tmpRangeTextView = (TextView) mView
				.findViewById(R.id.forcast_temp_range);
		picImageView = (ImageView) mView.findViewById(R.id.forcast_pic);
	}

}
