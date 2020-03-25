package com.coolweather.android.db;

import org.litepal.crud.DataSupport;

public class City extends DataSupport {

    String text = "nizhenshuai";
    String text3 = "nizhenshuai";
    String text4 = "nizhenshuai";






    private int id;

    private String cityName;

    private int cityCode;

    private int provinceId;

    String text1 = "nizhenshuai67w61e9128912897231";

    String text2 = "nihaoma123123123123123";

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getCityCode() {
        return cityCode;
    }

    public void setCityCode(int cityCode) {
        this.cityCode = cityCode;
    }

    public int getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(int provinceId) {
        this.provinceId = provinceId;
    }
}
