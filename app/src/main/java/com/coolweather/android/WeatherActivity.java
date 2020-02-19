package com.coolweather.android;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.core.widget.TextViewCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coolweather.android.gson.Forecast;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.service.UpdateService;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import java.io.IOException;
import java.net.HttpURLConnection;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    public DrawerLayout drawerLayout;

    private Button navButton;

    public SwipeRefreshLayout swipeRefresh;

    private String mWeatherId;

    private ScrollView weatherLayout;

    private TextView titleCity;

    private TextView titleUpdateTime;

    private TextView degreeText;

    private TextView weatherInfoText;

    private TextView humText;

    private TextView windDirText;

    private TextView windSpdText;

    private LinearLayout forecastLayout;

    private TextView aqiText;

    private TextView pm25Text;

    private TextView qltyText;

    private TextView comfortText;

    private TextView carWashText;

    private TextView sportText;

    private ImageView bingPicImg;

    private Weather weather;

     private String newWeatherId;

     private String sss;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>=21) {
            View view = getWindow().getDecorView();
            view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        navButton = (Button)findViewById(R.id.nav_button);
        swipeRefresh = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        weatherLayout = (ScrollView)findViewById(R.id.weather_layout);
        titleCity = (TextView)findViewById(R.id.title_city);
        titleUpdateTime = (TextView)findViewById(R.id.title_update_time);
        degreeText = (TextView)findViewById(R.id.degree_text);
        weatherInfoText = (TextView)findViewById(R.id.weather_info_text);
        humText = (TextView)findViewById(R.id.hum_text);
        windDirText = (TextView)findViewById(R.id.wind_dir_text);
        windSpdText = (TextView)findViewById(R.id.wind_speed_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText = (TextView)findViewById(R.id.aqi_text);
        pm25Text = (TextView)findViewById(R.id.pm25_text);
        qltyText = (TextView)findViewById(R.id.quality_review);
        comfortText = (TextView)findViewById(R.id.comfort_text);
        carWashText = (TextView)findViewById(R.id.car_wash_text);
        sportText = (TextView)findViewById(R.id.sport_text);
        bingPicImg = (ImageView)findViewById(R.id.bing_pic_img);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather",null);
        newWeatherId = prefs.getString("key_id",null);
        if(weatherString != null){//Parsing the weather information if caching is available
            weather = Utility.handleWeatherResponse(weatherString);
            showWeatherInfo(weather);
        }else{//Quering the weather if no caching
            mWeatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        }
        String bingPic = prefs.getString("bing_pic",null);
        if(bingPic != null){
                Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
        }else{
            loadBingPic();
        }

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){

            @Override
            public void onRefresh() {
                if(mWeatherId != null) {
                    requestWeather(mWeatherId);
                }else{
                    requestWeather(newWeatherId);
                }
            }
        });

        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    /**
     * Request weather information according the weatherid
     */

    public void requestWeather(final String weatherId){

        String weatherUrl = "http://guolin.tech/api/weather?cityid="+weatherId+"&key=a878b25475674793a7f88e968a416b10";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "Fail to get the information", Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String responseText = response.body().string();
                weather = Utility.handleWeatherResponse(responseText);


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather != null && "ok".equals(weather.status)){
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            SharedPreferences.Editor editor1 = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor1.putString("key_id",weatherId);
                            editor.apply();
                            editor1.apply();
                            mWeatherId = weather.basic.weatherId;
                            sss= weather.now.more.information;
                            showWeatherInfo(weather);
                        }else{
                            Toast.makeText(WeatherActivity.this,"Fail to get the information",Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
                loadBingPic();
            }
        });
    }

    /**
     * Loading the background picture from Bing
     */
    private void loadBingPic() {
            String requestBingPic = "http://guolin.tech/api/bing_pic";

            HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    SystemClock.sleep(80);
                    final String bingPic = response.body().string();
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();

                    if(sss.contains("阴")){
                        editor.putString("bing_pic", Integer.toString(R.drawable.yintian));
                        editor.apply();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Glide.with(WeatherActivity.this).load(R.drawable.yintian).into(bingPicImg);
                            }
                        });
                    }else if(sss.contains("中雨")){
                        editor.putString("bing_pic", Integer.toString(R.drawable.xiayu));
                        editor.apply();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Glide.with(WeatherActivity.this).load(R.drawable.xiayu).into(bingPicImg);
                            }
                        });

                    }
                    else if(sss.contains("多云")){
                        editor.putString("bing_pic", Integer.toString(R.drawable.duoyun));
                        editor.apply();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Glide.with(WeatherActivity.this).load(R.drawable.duoyun).into(bingPicImg);
                            }
                        });

                    }else{
                        editor.putString("bing_pic", bingPic);
                        editor.apply();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);

                            }
                        });
                    }
                }
            });
    }

    /**
     * Handle and show the data which already store in the weather class
     */
    private void showWeatherInfo(Weather weather){

        String cityName = weather.basic.cityName;
        String updateTime  = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "°C";
        String weatherInfo = weather.now.more.information;
        String humidity = weather.now.hum + "%";
        String windDir = weather.now.windDirection;
        String windSpd = weather.now.windSpeed + "km/h";
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        humText.setText(humidity);
        windDirText.setText(windDir);
        windSpdText.setText(windSpd);
        forecastLayout.removeAllViews();
        for(Forecast forecast: weather.forecastList){
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dateText = (TextView)view.findViewById(R.id.date_text);
            TextView infoText = (TextView)view.findViewById(R.id.info_text);
            TextView maxText = (TextView)view.findViewById(R.id.max_text);
            TextView minText = (TextView)view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.information);
            maxText.setText(forecast.temperature.max + "°C");
            minText.setText(forecast.temperature.min + "°C");
            forecastLayout.addView(view);

        }

        if(weather.aqi != null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
            qltyText.setText(weather.aqi.city.qlty);
        }

        String comfort = "Comfort: "+weather.suggestion.comfort.information;
        String carWash = "CarWash Index: "+weather.suggestion.carwash.information;
        String sport = "Sport Suggestions: "+weather.suggestion.sport.information;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);

        //start service
        Intent intent = new Intent(this, UpdateService.class);
        startService(intent);



    }
}
