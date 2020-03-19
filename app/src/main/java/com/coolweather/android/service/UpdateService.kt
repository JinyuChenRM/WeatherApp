package com.coolweather.android.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import android.os.SystemClock
import android.preference.PreferenceManager

import com.coolweather.android.gson.Weather
import com.coolweather.android.util.HttpUtil
import com.coolweather.android.util.Utility

import java.io.IOException

import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response

class UpdateService : Service() {
    
    var text4 = "text4"

    override fun onBind(intent: Intent): IBinder? {
        return null
    }


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        updateWeather()
        updateBingPic()
        val manager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val eightHour = 60 * 60 * 8 * 1000
        val triggerAtTime = SystemClock.elapsedRealtime() + eightHour
        val i = Intent(this, UpdateService::class.java)
        val pi = PendingIntent.getService(this, 0, i, 0)
        manager.cancel(pi)
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi)
        return super.onStartCommand(intent, flags, startId)
    }

    /**
     * Update weather information
     */
    private fun updateWeather() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val weatherString = prefs.getString("weather", null)
        if (weatherString != null) {
            val weather = Utility.handleWeatherResponse(weatherString)
            val weatherId = weather!!.basic.weatherId
            val weatherUrl = "http://guolin.tech/api/weather?cityid=$weatherId&key=a878b25475674793a7f88e968a416b10"
            HttpUtil.sendOkHttpRequest(weatherUrl, object : Callback {
                override fun onFailure(call: Call, e: IOException) {

                    e.printStackTrace()
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {

                    val responseText = response.body()!!.string()
                    val weather = Utility.handleWeatherResponse(responseText)
                    if (weather != null && "ok" == weather.status) {
                        val editor = PreferenceManager.getDefaultSharedPreferences(this@UpdateService).edit()
                        editor.putString("weather", responseText)
                        editor.apply()
                    }

                }
            })
        }
    }

    /**
     * Update Bing background image
     */
    private fun updateBingPic() {
        val requestBingPic = "http://guolin.tech/api/bing_pic"
        HttpUtil.sendOkHttpRequest(requestBingPic, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val bingPic = response.body()!!.string()
                val editor = PreferenceManager.getDefaultSharedPreferences(this@UpdateService).edit()
                editor.putString("bing_pic", bingPic)
                editor.apply()
            }
        })
    }
}
