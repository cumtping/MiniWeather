package com.peak.miniweather.common;

public class ForcastItem {
	private String dayOfWeek;
	private String temp;
	private String weather;
	private String wind;
	private String fl;
	private String st;

	public String getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(String dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public String getTemp() {
		return temp;
	}

	public void setTemp(String mTemp) {
		this.temp = mTemp;
	}

	public String getWeather() {
		return weather;
	}

	public void setWeather(String mWeather) {
		this.weather = mWeather;
	}

	public String getWind() {
		return wind;
	}

	public void setWind(String mWind) {
		this.wind = mWind;
	}

	public String getFl() {
		return fl;
	}

	public void setFl(String mFl) {
		this.fl = mFl;
	}

	public String getmSt() {
		return st;
	}

	public void setmSt(String mSt) {
		this.st = mSt;
	}

}
