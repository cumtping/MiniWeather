package com.peak.miniweather.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.peak.miniweather.R;

/**
 * Methods to get data from city_code.db & locations_info.db
 * 
 * @author ping
 * 
 */
public class MiniWeatherDBHelper {
	private SQLiteDatabase mCityCodeDb;
	private SQLiteDatabase mLocationsDb;
	private String mCityCodeDbRawName = "city_code.db";
	private String mLocationsDbRawName = "locations_info.db";
	// public static final String LOCATION_TABLE_NAME = "locations";
	public static final String CITY_CODE_TABLE_NAME = "citys";
	public static final String LOCATION_TABLE_NAME = "locations";
	public static final int CITY_NAME_INDEX = 2;
	public static final int CITY_CODE_INDEX = 3;
	public static final String CITY_NAME = "name";
	public static final String CITY_CODE = "city_num";
	public static final int NAME_CHS_INDEX = 2;
	public static final int NAME_EN_INDEX = 5;
	private Context mContext;
	private static MiniWeatherDBHelper mInstance = null;
	private String CITY_CODE_DB_PATH = null;
	private String LOCATIONS_DB_PATH = null;
	public static final String COUNTRY_EN = "country_en";
	public static final String STATE_CHS = "state_chs";
	public static final String NAME_CHS = "name_chs";

	/**
	 * Constructor
	 * 
	 * @param c
	 */
	private MiniWeatherDBHelper(Context c) {
		mContext = c;
		writeRawResToFile(mCityCodeDbRawName, R.raw.city_code);
		writeRawResToFile(mLocationsDbRawName, R.raw.locations_info);
		CITY_CODE_DB_PATH = "/data/data/" + mContext.getPackageName()
				+ "/databases/" + mCityCodeDbRawName;
		LOCATIONS_DB_PATH = "/data/data/" + mContext.getPackageName()
				+ "/databases/" + mLocationsDbRawName;
	}

	/**
	 * Get single instance
	 * 
	 * @param c
	 * @return
	 */
	public static MiniWeatherDBHelper getInstance(Context c) {
		if (mInstance == null) {
			mInstance = new MiniWeatherDBHelper(c);
		}
		return mInstance;
	}

	/**
	 * open city_code.db
	 */
	public void openCityCodeDb() {
		closeCityCodeDb();
		mCityCodeDb = SQLiteDatabase.openOrCreateDatabase(CITY_CODE_DB_PATH,
				null);
	}

	/**
	 * close city_code.db
	 */
	public void closeCityCodeDb() {
		if (mCityCodeDb != null && mCityCodeDb.isOpen()) {
			mCityCodeDb.close();
		}
	}

	/**
	 * open locations_info.db
	 */
	public void openLocationsDb() {
		closeLocationsDb();
		mLocationsDb = SQLiteDatabase.openOrCreateDatabase(LOCATIONS_DB_PATH,
				null);
	}

	/**
	 * close locations_info.db
	 */
	public void closeLocationsDb() {
		if (mLocationsDb != null && mLocationsDb.isOpen()) {
			mLocationsDb.close();
		}
	}

	/**
	 * Write raw database resource to the package path.
	 * 
	 * @param rawName
	 * @param rawId
	 */
	private void writeRawResToFile(String rawName, int rawId) {
		String database_path = "/data/data/" + mContext.getPackageName()
				+ "/databases";
		String path = database_path + "/" + rawName;
		File dir = new File(database_path);
		if (!dir.exists()) {
			dir.mkdir();
		}

		if (!(new File(path)).exists()) {
			InputStream in = mContext.getResources().openRawResource(rawId);
			try {
				FileOutputStream f_out = new FileOutputStream(path);
				byte[] buffer = new byte[8192];
				int count = 0;
				while ((count = in.read(buffer)) > 0) {
					f_out.write(buffer, 0, count);
				}
				f_out.close();
				in.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	/**
	 * Query the city code of input city.
	 * 
	 * @param city
	 * @return
	 */
	public String queryUniqueCityCode(String city) {
		openCityCodeDb();
		Cursor c = null;
		String cityCode = null;
		if (mCityCodeDb != null) {
			String where = CITY_NAME + " =?";
			c = mCityCodeDb.query(CITY_CODE_TABLE_NAME, null, where,
					new String[] { city }, null, null, null);

			if (c != null) {
				if (c.getCount() > 0) {
					c.moveToFirst();
					cityCode = c.getString(CITY_CODE_INDEX);
					c.close();
				} else {
					c.close();
				}
			} else {
				where = CITY_NAME + " like '%" + city + "%'";
				c = mCityCodeDb.query(CITY_CODE_TABLE_NAME, null, where, null,
						null, null, null);
				if (c != null) {
					if (c.getCount() > 0) {
						c.moveToFirst();
						cityCode = c.getString(CITY_CODE_INDEX);
						c.close();
					} else {
						c.close();
					}
				}
			}
		}
		closeCityCodeDb();
		return cityCode;
	}

	public ArrayList<String> queryProvinceInChina() {
		openLocationsDb();
		ArrayList<String> provinceList = null;
		Cursor cursor = null;
		if (mLocationsDb != null && mLocationsDb.isOpen()) {
			String where = COUNTRY_EN + "=?";
			cursor = mLocationsDb.query(true, LOCATION_TABLE_NAME,
					new String[] { STATE_CHS }, where,
					new String[] { "China" }, null, null, null, null);

			if (cursor == null || cursor.getCount() == 0) {
				return null;
			} else {
				cursor.moveToFirst();
				provinceList = new ArrayList<String>();

				while (!cursor.isLast()) {
					provinceList.add(cursor.getString(0));
					cursor.moveToNext();
				}

				cursor.close();

			}
		}
		closeLocationsDb();
		return provinceList;
	}

	public ArrayList<String> queryCityInProvince(String province) {
		openLocationsDb();
		ArrayList<String> cityList = null;
		Cursor cursor = null;
		if (mLocationsDb != null && mLocationsDb.isOpen()) {
			String where = COUNTRY_EN + "=? and " + STATE_CHS + "=?";
			cursor = mLocationsDb.query(LOCATION_TABLE_NAME,
					new String[] { NAME_CHS }, where, new String[] { "China",
							province }, null, null, null);

			if (cursor == null || cursor.getCount() == 0) {
				return null;
			} else {
				cursor.moveToFirst();
				cityList = new ArrayList<String>();

				while (!cursor.isLast()) {
					cityList.add(cursor.getString(0));
					cursor.moveToNext();
				}

				cursor.close();

			}
		}
		return cityList;
	}

}
