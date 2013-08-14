package com.peak.miniweather;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.peak.miniweather.R;
import com.peak.miniweather.common.ForcastItem;
import com.peak.miniweather.common.ForecastViewHolder;
import com.peak.miniweather.common.WeatherItem;
import com.peak.miniweather.utils.MiniWeatherUtils;

public class MiniWeatherForecastGridViewAdapter extends BaseAdapter {

	private Context mContext = null;
	private ArrayList<ForcastItem> mForcastItems = null;

	public MiniWeatherForecastGridViewAdapter(Context c,
			ArrayList<ForcastItem> forcastList) {
		mContext = c;
		mForcastItems = forcastList;
	}

	@Override
	public int getCount() {
		return mForcastItems != null ? mForcastItems.size() : 0;
	}

	@Override
	public Object getItem(int pos) {
		return (mForcastItems != null && pos >= 0 && pos < mForcastItems.size()) ? mForcastItems
				.get(pos) : null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int pos, View v, ViewGroup parent) {

		ForecastViewHolder holder = null;
		if (v == null) {
			LayoutInflater inflater = LayoutInflater.from(mContext);
			v = inflater.inflate(R.layout.forcast_item_layout, null);
			holder = new ForecastViewHolder(mContext, v);
			v.setTag(holder);
		} else {
			holder = (ForecastViewHolder) v.getTag();
		}

		ForcastItem item = mForcastItems.get(pos);
		holder.setViewContent(item);
		return v;
	}
}
