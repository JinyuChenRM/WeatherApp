package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

public class Now {

    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;

    public String hum;

    @SerializedName("wind_dir")
    public String windDirection;

    @SerializedName("wind_spd")
    public String windSpeed;

    public class More{

        @SerializedName("txt")
        public String information;


    }


}
