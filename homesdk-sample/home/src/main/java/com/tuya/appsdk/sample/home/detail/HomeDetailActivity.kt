/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2021 Tuya Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.tuya.appsdk.sample.home.detail

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.tuya.appsdk.sample.home.edit.HomeEditActivity
import com.tuya.appsdk.sample.user.R
import com.tuya.smart.home.sdk.TuyaHomeSdk
import com.tuya.smart.home.sdk.bean.HomeBean
import com.tuya.smart.home.sdk.bean.WeatherBean
import com.tuya.smart.home.sdk.callback.IIGetHomeWetherSketchCallBack
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback
import com.tuya.smart.sdk.api.IResultCallback

/**
 * Home Detail Sample
 *
 * @author yueguang [](mailto:developer@tuya.com)
 * @since 2021/1/18 6:11 PM
 */
class HomeDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_acitivty_detail)

        val toolbar: Toolbar = findViewById<View>(R.id.topAppBar) as Toolbar
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val homeId = intent.getLongExtra("homeId", 0)

        // Get home info
        TuyaHomeSdk.newHomeInstance(homeId).getHomeDetail(object : ITuyaHomeResultCallback {
            override fun onSuccess(bean: HomeBean?) {
                bean?.let {
                    findViewById<TextView>(R.id.tvHomeId).text = bean.homeId.toString()
                    findViewById<TextView>(R.id.tvHomeName).text = bean.name
                    findViewById<TextView>(R.id.tvHomeCity).text = bean.geoName

                    // Get home weather info
                    TuyaHomeSdk.newHomeInstance(homeId).getHomeWeatherSketch(bean.lon,
                            bean.lat,
                            object : IIGetHomeWetherSketchCallBack {
                                override fun onSuccess(result: WeatherBean?) {
                                    result?.let {
                                        findViewById<TextView>(R.id.tvWeather).text = it.condition
                                        findViewById<TextView>(R.id.tvHomeTemperature).text = it.temp
                                    }
                                }

                                override fun onFailure(errorCode: String?, errorMsg: String?) {
                                    Toast.makeText(
                                            this@HomeDetailActivity,
                                            "get home weather error->$errorMsg",
                                            Toast.LENGTH_LONG
                                    ).show()
                                }

                            })
                }

            }

            override fun onError(errorCode: String?, errorMsg: String?) {

            }

        })

        // Edit home
        findViewById<Button>(R.id.btnEdit).setOnClickListener {
            val intent = Intent(this, HomeEditActivity::class.java)
            intent.putExtra("homeId", homeId)
            startActivity(intent)
        }

        // Dismiss home
        findViewById<Button>(R.id.btnDismiss).setOnClickListener {
            TuyaHomeSdk.newHomeInstance(homeId).dismissHome(object : IResultCallback {
                override fun onSuccess() {
                    finish()
                }

                override fun onError(code: String?, error: String?) {

                }

            })
        }


    }
}