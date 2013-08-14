package com.peak.miniweather;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class SettingsActivity extends Activity {
	private static final String TAG = SettingsListViewAdapter.class
			.getSimpleName();
	private ListView mSettingsListView = null;
	private int[] mLayoutResourceIdList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.settings_layout);

		mSettingsListView = (ListView) findViewById(R.id.settings_listview);
		mLayoutResourceIdList = new int[] {
				android.R.layout.simple_list_item_multiple_choice,
				R.layout.list_item_spinner };
		mSettingsListView.setAdapter(new SettingsListViewAdapter());
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
					tv.setChecked(true);
					tv.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							tv.setChecked(!tv.isChecked());
						}
					});
					break;

				case 1:
					/*
					 * Spinner s1 = (Spinner) convertView
					 * .findViewById(R.id.spinner1); ArrayAdapter<CharSequence>
					 * adapter = ArrayAdapter
					 * .createFromResource(SettingsActivity.this,
					 * R.array.refresh_time,
					 * android.R.layout.simple_spinner_item);
					 * adapter.setDropDownViewResource
					 * (android.R.layout.simple_list_item_multiple_choice);
					 * s1.setAdapter(adapter); s1.setOnItemSelectedListener(new
					 * OnItemSelectedListener() { public void
					 * onItemSelected(AdapterView<?> parent, View view, int
					 * position, long id) { }
					 * 
					 * public void onNothingSelected(AdapterView<?> parent) { }
					 * });
					 */
					break;

				default:
					break;
				}
			}

			return convertView;
		}
	}
}
