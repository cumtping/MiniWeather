package com.peak.miniweather.common;

import java.util.ArrayList;

public class WeatherItem {

	private String cityName;

	private String cityNameEn;

	private String date_y; // date in Western year

	private String date; // date in Chinese year

	private String week;

	private String fchh;

	private String cityId;

	private String temp;
	
	private String tempRange;

	private String weather;

	private String fx1;

	private String fx2;

	private String sd;

	private String wd;

	private String ws;

	private String vitalight;

	private String publishTime;

	ArrayList<ForcastItem> forcastList = null;

	public WeatherItem() {

	}

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String mCity_Name) {
		this.cityName = mCity_Name;
	}

	public String getCityNameEn() {
		return cityNameEn;
	}

	public void setCityNameEn(String mCity_Name_En) {
		this.cityNameEn = mCity_Name_En;
	}

	public String getDate_y() {
		return date_y;
	}

	public void setDate_y(String mDate_y) {
		this.date_y = mDate_y;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String mDate) {
		this.date = mDate;
	}

	public String getWeek() {
		return week;
	}

	public void setWeek(String mWeek) {
		this.week = mWeek;
	}

	public String getCity_Id() {
		return cityId;
	}

	public void setCity_Id(String mCity_Id) {
		this.cityId = mCity_Id;
	}

	public String getFx1() {
		return fx1;
	}

	public void setFx1(String mFx1) {
		this.fx1 = mFx1;
	}

	public String getFx2() {
		return fx2;
	}

	public void setFx2(String Fx2) {
		this.fx2 = Fx2;
	}

	public String getFchh() {
		return fchh;
	}

	public void setFchh(String Fchh) {
		this.fchh = Fchh;
	}

	public String getSD() {
		return sd;
	}

	public void setSD(String SD) {
		this.sd = SD;
	}

	public String getVitalight() {
		return vitalight;
	}

	public void setVitalight(String Vitalight) {
		this.vitalight = Vitalight;
	}

	public String getWD() {
		return wd;
	}

	public void setWD(String WD) {
		this.wd = WD;
	}

	public String getWS() {
		return ws;
	}

	public void setWS(String WS) {
		this.ws = WS;
	}

	public void setTemp(String Tep) {
		this.temp = Tep;
	}

	public String getTemp() {
		return temp;
	}

	public String getTempRange() {
		return tempRange;
	}

	public void setTempRange(String tempRange) {
		this.tempRange = tempRange;
	}

	public void setPublishTime(String PublishTie) {
		this.publishTime = PublishTie;
	}

	public String getPublishTime() {
		return publishTime;
	}

	public void setWeather(String Weather) {
		this.weather = Weather;
	}

	public String getWeather() {
		return weather;
	}

	public ArrayList<ForcastItem> getForecastList() {
		return forcastList;
	}

	public void setForcastList(ArrayList<ForcastItem> ForcastList) {
		this.forcastList = ForcastList;
	}

}
