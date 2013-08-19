package com.peak.miniweather;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.peak.miniweather.utils.MiniWeatherDBHelper;

public class SelectCityActivity extends Activity {
	private static final String TAG = SelectCityActivity.class.getSimpleName();
	private BaseAdapter adapter;
	private ListView mListView;
	private ArrayList<String> mProvinceList = new ArrayList<String>();
	private ArrayList<String> mCityList = new ArrayList<String>();
	private int mListFlag = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.add_city_layout);

		mListView = (ListView) findViewById(R.id.listview);
		mListView.setOnItemClickListener(new ListViwOnItemClick());
		findViewById(R.id.more_city).setVisibility(View.INVISIBLE);
		setupProvinceListView();
	}

	class ListViwOnItemClick implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> adapter, View v, int pos,
				long arg3) {
			if (mListFlag == 0) {
				String provinceName = (String) adapter.getItemAtPosition(pos);
				Log.d(TAG, "provinceName=" + provinceName);
				setupCityListView(provinceName);
			} else {
				String cityName = (String) adapter.getItemAtPosition(pos);
				Toast.makeText(SelectCityActivity.this, cityName,
						Toast.LENGTH_SHORT).show();
				Intent intent = new Intent();
				intent.putExtra("city", cityName);
				setResult(RESULT_OK, intent);
				finish();
			}
		}

	}

	private class ListAdapter extends BaseAdapter {
		private LayoutInflater inflater;
		private List<String> list = null;

		public ListAdapter(Context context, List<String> list) {
			this.inflater = LayoutInflater.from(context);
			this.list = list;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.list_item_city_name, null);
				holder = new ViewHolder();
				holder.name = (TextView) convertView.findViewById(R.id.name);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			if(holder != null){
				holder.name.setText(list.get(position)); 
			}

			return convertView;
		}

		private class ViewHolder {
			TextView name;
		}

	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mListFlag == 1) {
				setupProvinceListView();
				return true;
			}
		}
		return super.onKeyUp(keyCode, event);
	}

	private void setupProvinceListView() {
		mProvinceList = MiniWeatherDBHelper.getInstance(this)
				.queryProvinceInChina();
		if (mProvinceList != null && mProvinceList.size() > 0) {
			mListView.setAdapter(new ListAdapter(this, mProvinceList));
			mListView.invalidate();
			mListFlag = 0;
		}
	}

	private void setupCityListView(String province) {
		mCityList = MiniWeatherDBHelper.getInstance(SelectCityActivity.this)
				.queryCityInProvince(province);
		if (mCityList != null && mCityList.size() > 0) {
			mListView.setAdapter(new ListAdapter(SelectCityActivity.this,
					mCityList));
			mListView.invalidate();
			mListFlag = 1;
		}
	}
}
