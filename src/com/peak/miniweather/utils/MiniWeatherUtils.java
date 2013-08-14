package com.peak.miniweather.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import com.peak.miniweather.R;
import com.peak.miniweather.common.ForcastItem;
import com.peak.miniweather.common.WeatherItem;

/**
 * Utility methods.
 * 
 * @author ping
 */
public class MiniWeatherUtils {

	private static final String TAG = MiniWeatherUtils.class.getSimpleName();

	public static final String PREF_NAME = "weather";

	public static final String PREF_KEPT_CITY_NUM_KEY = "kept_city_num";

	public static final String PREF_KEPT_CITY_ITEM_BASE = "kept_city_item_";

	public static boolean networkAvailable(Context context) {

		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (connectivity != null) {

			NetworkInfo info = connectivity.getActiveNetworkInfo();

			if (info != null && info.isConnected()) {

				if (info.getState() == NetworkInfo.State.CONNECTED) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Get Today's weather info from www.weather.com.cn. eg.
	 * {"weatherinfo":{"city"
	 * :"海口","cityid":"101310101","temp":"25","WD":"东风","WS"
	 * :"1级","SD":"95%","WSE"
	 * :"1","time":"20:50","isRadar":"1","Radar":"JC_RADAR_AZ9898_JB"}}
	 * 
	 * Get Forcast weather from m.weather.com.cn. eg.
	 * {"weatherinfo":{"city":"海口"
	 * ,"city_en":"haikou","date_y":"2013年7月24日","date"
	 * :"","week":"星期三","fchh":"18"
	 * ,"cityid":"101310101","temp1":"24℃~31℃","temp2"
	 * :"25℃~32℃","temp3":"25℃~32℃"
	 * ,"temp4":"25℃~32℃","temp5":"25℃~33℃","temp6":"25℃~33℃"
	 * ,"tempF1":"75.2℉~87.8℉"
	 * ,"tempF2":"77℉~89.6℉","tempF3":"77℉~89.6℉","tempF4"
	 * :"77℉~89.6℉","tempF5":"77℉~91.4℉"
	 * ,"tempF6":"77℉~91.4℉","weather1":"雷阵雨","weather2"
	 * :"雷阵雨","weather3":"雷阵雨","weather4"
	 * :"雷阵雨","weather5":"雷阵雨","weather6":"多云转雷阵雨"
	 * ,"img1":"4","img2":"99","img3":
	 * "4","img4":"99","img5":"4","img6":"99","img7"
	 * :"4","img8":"99","img9":"4","img10"
	 * :"99","img11":"1","img12":"4","img_single"
	 * :"4","img_title1":"雷阵雨","img_title2"
	 * :"雷阵雨","img_title3":"雷阵雨","img_title4"
	 * :"雷阵雨","img_title5":"雷阵雨","img_title6"
	 * :"雷阵雨","img_title7":"雷阵雨","img_title8"
	 * :"雷阵雨","img_title9":"雷阵雨","img_title10"
	 * :"雷阵雨","img_title11":"多云","img_title12"
	 * :"雷阵雨","img_title_single":"雷阵雨","wind1"
	 * :"西南风3-4级","wind2":"西南风3-4级","wind3"
	 * :"西南风3-4级","wind4":"西南风3-4级","wind5":"西南风3-4级"
	 * ,"wind6":"东南风3-4级","fx1":"西南风"
	 * ,"fx2":"西南风","fl1":"3-4级","fl2":"3-4级","fl3"
	 * :"3-4级","fl4":"3-4级","fl5":"3-4级"
	 * ,"fl6":"3-4级","index":"热","index_d":"天气热，建议着短裙、短裤、短薄外套、T恤等夏季服装。"
	 * ,"index48"
	 * :"炎热","index48_d":"天气炎热，建议着短衫、短裙、短裤、薄型T恤衫等清凉夏季服装。","index_uv":"中等"
	 * ,"index48_uv"
	 * :"中等","index_xc":"不宜","index_tr":"一般","index_co":"较不舒适","st1"
	 * :"28","st2":"20"
	 * ,"st3":"29","st4":"21","st5":"29","st6":"21","index_cl":"较不宜"
	 * ,"index_ls":"不宜","index_ag":"极不易发"}}
	 * 
	 * @param city
	 * @return
	 */
	public static WeatherItem getWeatheFromrNMC(Context c, String city) {
		Log.i(TAG, Thread.currentThread().getName() + " is now thread");
		String cityCode = MiniWeatherDBHelper.getInstance(c)
				.queryUniqueCityCode(city);

		if (cityCode == null || cityCode.equals("")) {
			return null;
		}

		WeatherItem weatherItem = null;

		try {
			/** Get current weather condition JSON string from the network */
			URL currentWeatherUrl = new URL(
					"http://www.weather.com.cn/data/sk/" + cityCode + ".html");
			InputStreamReader isr = new InputStreamReader(
					currentWeatherUrl.openStream());
			BufferedReader br = new BufferedReader(isr, 16);
			String currentWeatherJSONString = "";
			String temp;
			while ((temp = br.readLine()) != null) {
				currentWeatherJSONString = currentWeatherJSONString + temp;
			}
			isr.close();
			br.close();
			/** Get forcast weather condition JSON string from the network */
			URL forecastUrl = new URL("http://m.weather.com.cn/data/"
					+ cityCode + ".html");
			isr = new InputStreamReader(forecastUrl.openStream());
			br = new BufferedReader(isr, 16);
			String forcastWeatherJSONString = "";
			while ((temp = br.readLine()) != null) {
				forcastWeatherJSONString = forcastWeatherJSONString + temp;
			}
			isr.close();
			br.close();

			weatherItem = getWeatherDataFromJSON(c, currentWeatherJSONString,
					forcastWeatherJSONString);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return weatherItem;
	}

	/**
	 * 
	 * @param currentWeatherStr
	 * @param forcastWeatherStr
	 * @return
	 */
	public static WeatherItem getWeatherDataFromJSON(Context c,
			String currentWeatherStr, String forcastWeatherStr) {
		Log.d(TAG, "currentWeatherStr=" + currentWeatherStr);
		Log.d(TAG, "forcastWeatherStr=" + forcastWeatherStr);

		WeatherItem weatherData = null;
		try {
			JSONObject jsonObject = new JSONObject(currentWeatherStr)
					.getJSONObject("weatherinfo");

			if (jsonObject == null) {
				Log.d(TAG, "jsonObject is null");
				return null;
			}

			weatherData = new WeatherItem();
			weatherData.setCityName(jsonObject.optString("city"));
			weatherData.setTemp(jsonObject.optString("temp"));
			weatherData.setWD(jsonObject.optString("WD"));
			weatherData.setWS(jsonObject.optString("WS"));
			weatherData.setPublishTime(jsonObject.optString("time"));

			jsonObject = new JSONObject(forcastWeatherStr)
					.getJSONObject("weatherinfo");
			if (jsonObject == null) {
				return null;
			}
			String todayWeek = null;
			weatherData.setDate_y(jsonObject.optString("date_y"));
			weatherData.setDate(jsonObject.optString("date"));
			weatherData.setWeek(todayWeek = jsonObject.optString("week"));
			weatherData.setFchh(jsonObject.optString("fchh"));
			weatherData.setWeather(jsonObject.optString("weather1"));
			weatherData.setTempRange(jsonObject.optString("temp1"));
			ArrayList<ForcastItem> forcastList = new ArrayList<ForcastItem>();

			for (int i = 1; i <= 6; i++) {
				ForcastItem forcastItem = new ForcastItem();
				forcastItem.setTemp(jsonObject.optString("temp" + i));
				forcastItem.setWeather(jsonObject.optString("weather" + i));
				forcastItem.setWind(jsonObject.optString("wind" + i));
				forcastItem.setmSt(jsonObject.optString("st" + i));
				forcastItem.setDayOfWeek(getNextDayofWeek(c, todayWeek, i - 1));
				forcastList.add(forcastItem);
			}
			weatherData.setForcastList(forcastList);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return weatherData;
	}

	public static String getNextDayofWeek(Context c, String dayOfWeek,
			int offset) {

		if (dayOfWeek == null) {
			return "";
		}
		int[] daysIds = { R.string.dayOfWeek1, R.string.dayOfWeek2,
				R.string.dayOfWeek3, R.string.dayOfWeek4, R.string.dayOfWeek5,
				R.string.dayOfWeek6, R.string.dayOfWeek7 };
		for (int i = 0; i < 7; i++) {
			if (dayOfWeek.equals(c.getString(daysIds[i]))) {
				return c.getString(daysIds[(i + offset) % 7]);
			}
		}
		return "";
	}

	/**
	 * Weather icon related.
	 */

	private static final int SUNNY = 1;
	private static final int PARTLY_SUNNY = 2;
	private static final int SCATTERED_THUNDERSTORMS = 3;
	private static final int SHOWERS = 4;
	private static final int SCATTERED_SHOWERS = 5;
	private static final int RAIN_AND_SNOW = 6;
	private static final int OVERCAST = 7;
	private static final int LIGHT_SNOW = 8;
	private static final int FREEZING_DRIZZLE = 9;
	private static final int CHANCE_OF_RAIN = 10;
	private static final int MOSTLY_SUNNY = 11;
	private static final int PARTLY_CLOUDY = 12;
	private static final int MOSTLY_CLOUDY = 13;
	private static final int CHANCE_OF_STORM = 14;
	private static final int RAIN = 15;
	private static final int CHANCE_OF_SNOW = 16;
	private static final int CLOUDY = 17;
	private static final int MIST = 18;
	private static final int STORM = 19;
	private static final int THUNDERSTORM = 20;
	private static final int CHANCE_OF_TSTORM = 21;
	private static final int SLEET = 22;
	private static final int SNOW = 23;
	private static final int ICY = 24;
	private static final int DUST = 25;
	private static final int FOG = 26;
	private static final int SMOKE = 27;
	private static final int HAZE = 28;
	private static final int FLURRIES = 29;
	private static final int LIGHT_RAIN = 30;
	private static final int SNOW_SHOWERS = 31;
	private static final int HAIL = 32;
	private static final int POUR = 33;
	private static final int SNOW_STORM = 34;
	private static final int NOT_ACCESS = 0;

	private static final int[] mWeatherDrawables = {
			R.drawable.weather_not_access, R.drawable.weather_sunny,
			R.drawable.weather_partly_sunny,
			R.drawable.weather_scattered_thunderstorms,
			R.drawable.weather_showers, R.drawable.weather_scattered_showers,
			R.drawable.weather_rain_and_snow, R.drawable.weather_overcast,
			R.drawable.weather_light_snow, R.drawable.weather_freezing_drizzle,
			R.drawable.weather_chance_of_rain, R.drawable.weather_mostly_sunny,
			R.drawable.weather_partly_cloudy, R.drawable.weather_mostly_cloudy,
			R.drawable.weather_chance_of_storm, R.drawable.weather_rain,
			R.drawable.weather_chance_of_snow, R.drawable.weather_cloudy,
			R.drawable.weather_mist, R.drawable.weather_storm,
			R.drawable.weather_thunder_storm, R.drawable.weather_thunder_storm,
			R.drawable.weather_sleet, R.drawable.weather_snow,
			R.drawable.weather_icy, R.drawable.weather_dust,
			R.drawable.weather_fog, R.drawable.weather_smoke,
			R.drawable.weather_haze, R.drawable.weather_flurries,
			R.drawable.weather_light_rain, R.drawable.weather_snow_showers,
			R.drawable.weather_hail, R.drawable.weather_pour,
			R.drawable.weather_snow_storm };

	private static final int[] mWeatherDrawablesD = {
			R.drawable.weather_not_access_d, R.drawable.weather_sunny_d,
			R.drawable.weather_partly_sunny_d,
			R.drawable.weather_scattered_thunderstorms_d,
			R.drawable.weather_showers_d,
			R.drawable.weather_scattered_showers_d,
			R.drawable.weather_rain_and_snow_d, R.drawable.weather_overcast_d,
			R.drawable.weather_light_snow_d,
			R.drawable.weather_freezing_drizzle_d,
			R.drawable.weather_chance_of_rain_d,
			R.drawable.weather_mostly_sunny_d,
			R.drawable.weather_partly_cloudy_d,
			R.drawable.weather_mostly_cloudy_d,
			R.drawable.weather_chance_of_storm_d, R.drawable.weather_rain_d,
			R.drawable.weather_chance_of_snow_d, R.drawable.weather_cloudy_d,
			R.drawable.weather_mist_d, R.drawable.weather_storm_d,
			R.drawable.weather_thunder_storm_d,
			R.drawable.weather_thunder_storm_d, R.drawable.weather_sleet_d,
			R.drawable.weather_snow_d, R.drawable.weather_icy_d,
			R.drawable.weather_dust_d, R.drawable.weather_fog_d,
			R.drawable.weather_smoke_d, R.drawable.weather_haze_d,
			R.drawable.weather_flurries_d, R.drawable.weather_light_rain_d,
			R.drawable.weather_snow_showers_d, R.drawable.weather_hail_d,
			R.drawable.weather_pour_d, R.drawable.weather_snow_storm_d };

	/**
	 * 
	 * @param c
	 * @param weather
	 * @param bigIcon
	 * @return
	 */
	public static int getWeatherIconFlag(Context c, String weather,
			boolean bigIcon) {

		if (c == null || TextUtils.isEmpty(weather)) {
			return -1;
		}

		int mWeatherIconFlg = 0;
		if (weather.equals(c.getResources().getString(R.string.sunny))
				|| weather.equals("Clear") || weather.equals("Sunny")
				|| weather.equals("Fine")) {
			mWeatherIconFlg = SUNNY;
		} else if (weather.equals(c.getResources().getString(
				R.string.mostly_sunny))
				|| weather.equals("Mostly Sunny")) {
			mWeatherIconFlg = MOSTLY_SUNNY;
		} else if (weather.equals(c.getResources().getString(
				R.string.partly_sunny))
				|| weather.equals("Partly Sunny")) {
			mWeatherIconFlg = PARTLY_SUNNY;
		} else if (weather.equals(c.getResources().getString(
				R.string.mostly_cloudy))
				|| weather.equals("Mostly Cloudy")) {
			mWeatherIconFlg = MOSTLY_CLOUDY;
		} else if (weather.equals(c.getResources().getString(R.string.cloudy))
				|| weather.equals("Cloudy")) {
			mWeatherIconFlg = CLOUDY;
		} else if (weather.equals(c.getResources().getString(
				R.string.partly_cloudy))
				|| weather.equals("Partly Cloudy")) {
			mWeatherIconFlg = PARTLY_CLOUDY;
		} else if (weather.equals(c.getResources().getString(R.string.haze))
				|| weather.equals("Haze")) {
			mWeatherIconFlg = HAZE;

		} else if (weather.equals(c.getResources().getString(R.string.smoke))
				|| weather.equals("Smoke")

		) {
			mWeatherIconFlg = SMOKE;
		} else if (weather
				.equals(c.getResources().getString(R.string.overcast))
				|| weather.equals("Overcast")) {
			mWeatherIconFlg = OVERCAST;
		} else if (weather.equals(c.getResources().getString(
				R.string.light_snow))
				|| weather.equals("Light snow")) {
			mWeatherIconFlg = LIGHT_SNOW;
		} else if (weather.equals(c.getResources().getString(R.string.snow))
				|| weather.equals("Snow")
				|| weather
						.equals(c.getResources().getString(R.string.snow_two))) {
			mWeatherIconFlg = SNOW;
		} else if (weather.equals(c.getResources().getString(
				R.string.snow_showers))
				|| weather.equals(c.getResources().getString(
						R.string.snow_showers_two))) {
			mWeatherIconFlg = SNOW_SHOWERS;
		} else if (weather.equals(c.getResources().getString(R.string.sleet))
				|| weather.equals("Sleet")) {
			mWeatherIconFlg = SLEET;
		} else if (weather.equals(c.getResources().getString(
				R.string.thunder_storm))
				|| weather.equals("Thunderstorm")) {
			mWeatherIconFlg = THUNDERSTORM;
		} else if (weather.equals(c.getResources().getString(R.string.showers))
				|| weather.equals("Storm") || weather.equals("Showers")) {
			mWeatherIconFlg = SHOWERS;
		} else if (weather.equals(c.getResources().getString(
				R.string.light_rain))
				|| weather.equals("Light rain")) {
			mWeatherIconFlg = LIGHT_RAIN;

		} else if (weather.equals(c.getResources().getString(
				R.string.chance_of_rain))
				|| weather.equals("Chance of Rain")) {
			mWeatherIconFlg = CHANCE_OF_RAIN;

		} else if (weather.equals(c.getResources().getString(
				R.string.chance_of_storm))
				|| weather.equals("Chance of Storm")) {
			mWeatherIconFlg = CHANCE_OF_STORM;
		} else if (weather.equals(c.getResources().getString(
				R.string.moderate_rain))
				|| weather.equals(c.getResources().getString(R.string.rain))
				|| weather.equals(c.getResources().getString(
						R.string.ligth_to_moderate_rain))
				|| weather.equals("Rain") || weather.equals("Moderate rain")) {
			mWeatherIconFlg = RAIN;
		} else if (weather.equals(c.getResources().getString(R.string.pour))
				|| weather.equals(c.getResources().getString(
						R.string.moderate_rain_to_pour))
				|| weather.equals("Pour")) {
			mWeatherIconFlg = POUR;
		} else if (weather.equals(c.getResources().getString(
				R.string.rain_storm))
				|| weather.equals(c.getResources().getString(
						R.string.pour_to_rain_storm))
				|| weather.equals("Rainstorm")) {
			mWeatherIconFlg = STORM;
		} else if (weather.equals(c.getResources().getString(R.string.fog))
				|| weather.equals("Fog")) {
			mWeatherIconFlg = FOG;
		} else if (weather.equals("Icy")
				|| weather.equals(c.getResources().getString(R.string.icy))) {
			mWeatherIconFlg = ICY;
		} else if (weather.equals("Mist")) {
			mWeatherIconFlg = MIST;
		} else if (weather.equals("Dust")
				|| weather.equals(c.getResources().getString(R.string.dust))) {
			mWeatherIconFlg = DUST;
		} else if (weather.equals(c.getResources().getString(
				R.string.snow_storm))
				|| weather.equals("Snow Storm")) {
			mWeatherIconFlg = SNOW_STORM;
		} else {
			mWeatherIconFlg = NOT_ACCESS;
		}
		if (bigIcon) {
			return mWeatherDrawables[mWeatherIconFlg];
		} else {
			return mWeatherDrawablesD[mWeatherIconFlg];
		}

	}

	public static ArrayList<String> loadSavedCityList(Context c) {
		ArrayList<String> list = new ArrayList<String>();

		SharedPreferences preferences = c.getSharedPreferences(PREF_NAME,
				Context.MODE_PRIVATE);
		int cityNum = preferences.getInt(PREF_KEPT_CITY_NUM_KEY, 0);

		for (int i = 0; i < cityNum; i++) {
			String keptCity = preferences.getString(PREF_KEPT_CITY_ITEM_BASE
					+ (i + 1), "");

			if (!TextUtils.isEmpty(keptCity)) {
				list.add(keptCity);
			}
		}
		return list;
	}

	public static void savedityList(Context c, ArrayList<String> cityList) {
		if (cityList == null) {
			return;
		}

		SharedPreferences preferences = c.getSharedPreferences(PREF_NAME,
				Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		int cityNum = cityList.size();
		editor.putInt(PREF_KEPT_CITY_NUM_KEY, cityNum);

		for (int i = 0; i < cityNum; i++) {
			editor.putString(PREF_KEPT_CITY_ITEM_BASE + (i + 1),
					cityList.get(i));
		}

		editor.apply();
	}

	public static void executeMiniWeatherTask(Context c,
			MiniWeatherWorkspaceHelper handler, String cityName) {
		if (TextUtils.isEmpty(cityName)) {
			return;
		}
		MiniWeatherTask task = new MiniWeatherTask(c, handler);
		task.execute(cityName);
	}

}
