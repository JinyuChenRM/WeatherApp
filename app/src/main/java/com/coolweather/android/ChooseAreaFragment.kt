package com.coolweather.android

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

import com.coolweather.android.db.City
import com.coolweather.android.db.County
import com.coolweather.android.db.Province
import com.coolweather.android.util.HttpUtil
import com.coolweather.android.util.Utility
import kotlinx.android.synthetic.main.choose_area.*

import org.litepal.crud.DataSupport

import java.io.IOException
import java.util.ArrayList

import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response

class ChooseAreaFragment : Fragment() {


    var progressDialog: ProgressDialog? = null

    private var listView: ListView? = null

    private var adapter: ArrayAdapter<String>? = null

    private val dataList = ArrayList<String>()


    /**
     * Province List
     */
    private var provinceList: List<Province>? = null

    /**
     * City List
     */
    private var cityList: List<City>? = null

    /**
     * County List
     */
    private var countyList: List<County>? = null

    /**
     * selected Province
     */
    private var selectedProvince: Province? = null

    /**
     * selected City
     */
    private var selectedCity: City? = null

    /**
     * current selected Level
     */
    private var currentLevel: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.choose_area, container, false)
        listView = view.findViewById<View>(R.id.list_view) as ListView
        adapter = ArrayAdapter(context!!, android.R.layout.simple_list_item_1, dataList)
        listView?.adapter = adapter
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        listView?.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            if (currentLevel == LEVEL_PROVINCE) {
                selectedProvince = provinceList!![position]
                queryCities()
            } else if (currentLevel == LEVEL_CITY) {
                selectedCity = cityList!![position]
                queryCounties()
            } else if (currentLevel == LEVEL_COUNTY) {

                val weatherId = countyList!![position].weatherId

                if (activity is MainActivity) {
                    val intent = Intent(activity, WeatherActivity::class.java)
                    intent.putExtra("weather_id", weatherId)
                    startActivity(intent)
                    activity!!.finish()
                } else if (activity is WeatherActivity) {
                    val activity = activity as WeatherActivity?
                    activity!!.drawerLayout.closeDrawers()
                    activity.swipeRefresh.isRefreshing = true
                    activity.requestWeather(weatherId)
                }
            }
        }

        back_button!!.setOnClickListener {
            if (currentLevel == LEVEL_COUNTY) {
                queryCities()
            } else if (currentLevel == LEVEL_CITY) {
                queryProvinces()
            }
        }
        queryProvinces()
    }

    /**
     * Quering all the provinces,database first, then query in server if it doesn't been found
     */
    private fun queryProvinces() {
        title_text!!.text = "China"
        back_button!!.visibility = View.GONE
        provinceList = DataSupport.findAll(Province::class.java)
        if (provinceList!!.size > 0) {
            dataList.clear()
            for (province in provinceList!!) {
                dataList.add(province.provinceName)
            }
            adapter!!.notifyDataSetChanged()
            listView!!.setSelection(0)
            currentLevel = LEVEL_PROVINCE
        } else {
            val address = resources.getString(R.string.url_list_province)
            queryFromServer(address, "province")
        }
    }

    /**
     * Query the city been selected, query from the database first, then query from server if it doesn't been found
     */
    private fun queryCities() {
        title_text!!.text = selectedProvince!!.provinceName
        back_button!!.visibility = View.VISIBLE
        cityList = DataSupport.where("provinceid = ?", selectedProvince!!.id.toString()).find(City::class.java)
        if (cityList!!.size > 0) {
            dataList.clear()
            for (city in cityList!!) {
                dataList.add(city.cityName)
            }
            adapter!!.notifyDataSetChanged()
            listView!!.setSelection(0)
            currentLevel = LEVEL_CITY
        } else {
            val provinceCode = selectedProvince!!.provinceCode
            val address = resources.getString(R.string.url_list_province) + provinceCode
            queryFromServer(address, "city")
        }
    }

    /**
     * Query the counties of the city been selected, query from the database first, then query from server if it doesn't been found
     */
    private fun queryCounties() {
        title_text!!.text = selectedCity!!.cityName
        back_button!!.visibility = View.VISIBLE
        countyList = DataSupport.where("cityid = ?", selectedCity!!.id.toString()).find(County::class.java)
        if (countyList!!.size > 0) {
            dataList.clear()
            for (county in countyList!!) {
                dataList.add(county.countyName)
            }
            adapter!!.notifyDataSetChanged()
            listView!!.setSelection(0)
            currentLevel = LEVEL_COUNTY
        } else {
            val provinceCode = selectedProvince!!.provinceCode
            val cityCode = selectedCity!!.cityCode
            val address = resources.getString(R.string.url_list_province) + provinceCode + "/" + cityCode
            queryFromServer(address, "county")
        }
    }


    /**
     * According the address and type to query the province, city and county information from the server
     */

    private fun queryFromServer(address: String, type: String) {

        showProgressDialog()
        HttpUtil.sendOkHttpRequest(address, object : Callback {
            override fun onFailure(call: Call, e: IOException) {

                activity!!.runOnUiThread {
                    closeProgressDialog()
                    Toast.makeText(context, "Loading Failed", Toast.LENGTH_SHORT).show()
                }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {

                val responseText = response.body()!!.string()
                var result = false
                if ("province" == type) {
                    result = Utility.handleProvinceResponse(responseText)
                } else if ("city" == type) {
                    result = Utility.handleCityResponse(responseText, selectedProvince!!.id)
                } else if ("county" == type) {
                    result = Utility.handleCountyResponse(responseText, selectedCity!!.id)
                }
                if (result) {
                    activity!!.runOnUiThread {
                        closeProgressDialog()
                        if ("province" == type) {
                            queryProvinces()
                        } else if ("city" == type) {
                            queryCities()
                        } else if ("county" == type) {
                            queryCounties()
                        }
                    }
                }
            }
        })
    }

    /**
     * show the progress dialog
     */
    private fun showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(activity)
            progressDialog!!.setMessage("Loading Now.....")
            progressDialog!!.setCanceledOnTouchOutside(false)
        }
        progressDialog!!.show()
    }

    /**
     * close the progress dialog
     */
    private fun closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog!!.dismiss()
        }
    }

    companion object {

        val LEVEL_PROVINCE = 0

        val LEVEL_CITY = 1

        val LEVEL_COUNTY = 2
    }
}