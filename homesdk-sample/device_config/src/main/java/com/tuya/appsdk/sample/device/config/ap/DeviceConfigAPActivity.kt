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

package com.tuya.appsdk.sample.device.config.ap

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.tuya.appsdk.sample.device.config.R
import com.tuya.appsdk.sample.resource.HomeModel
import com.tuya.smart.home.sdk.TuyaHomeSdk
import com.tuya.smart.home.sdk.builder.ActivatorBuilder
import com.tuya.smart.sdk.api.ITuyaActivatorGetToken
import com.tuya.smart.sdk.api.ITuyaSmartActivatorListener
import com.tuya.smart.sdk.bean.DeviceBean
import com.tuya.smart.sdk.enums.ActivatorModelEnum


/**
 * Device Configuration AP Mode Sample
 *
 * @author qianqi <a href="mailto:developer@tuya.com">Contact me.</a>
 * @since 2021/1/5 5:13 PM
 */
class DeviceConfigAPActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        const val TAG = "DeviceConfigEZ"
    }

    lateinit var cpiLoading: CircularProgressIndicator
    lateinit var btnSearch: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.device_config_activity)

        val toolbar: Toolbar = findViewById<View>(R.id.topAppBar) as Toolbar
        toolbar.setNavigationOnClickListener {
            finish()
        }
        toolbar.title = getString(R.string.device_config_ap_title)

        cpiLoading = findViewById(R.id.cpiLoading)
        btnSearch = findViewById(R.id.btnSearch)
        btnSearch.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        val strSsid = findViewById<EditText>(R.id.etSsid).text.toString()
        val strPassword = findViewById<EditText>(R.id.etPassword).text.toString()

        v?.id?.let {
            if (it == R.id.btnSearch) {
                val homeId = HomeModel.INSTANCE.getCurrentHome(this)
                // Get Network Configuration Token
                TuyaHomeSdk.getActivatorInstance().getActivatorToken(homeId,
                        object : ITuyaActivatorGetToken {
                            override fun onSuccess(token: String) {

                                // Start network configuration -- AP mode
                                val builder = ActivatorBuilder()
                                        .setSsid(strSsid)
                                        .setContext(v.context)
                                        .setPassword(strPassword)
                                        .setActivatorModel(ActivatorModelEnum.TY_AP)
                                        .setTimeOut(100)
                                        .setToken(token)
                                        .setListener(object : ITuyaSmartActivatorListener {

                                            @Override
                                            override fun onStep(step: String?, data: Any?) {
                                                Log.i(TAG, "$step --> $data")
                                            }

                                            override fun onActiveSuccess(devResp: DeviceBean?) {
                                                cpiLoading.visibility = View.GONE

                                                Log.i(TAG, "Activate success")
                                                Toast.makeText(
                                                        this@DeviceConfigAPActivity,
                                                        "Activate success",
                                                        Toast.LENGTH_LONG
                                                ).show()

                                                finish()
                                            }

                                            override fun onError(
                                                    errorCode: String?,
                                                    errorMsg: String?
                                            ) {
                                                cpiLoading.visibility = View.GONE
                                                btnSearch.isClickable = true

                                                Toast.makeText(
                                                        this@DeviceConfigAPActivity,
                                                        "Activate error-->$errorMsg",
                                                        Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                        )

                                val mTuyaActivator =
                                        TuyaHomeSdk.getActivatorInstance().newActivator(builder)

                                //Start configuration
                                mTuyaActivator.start()

                                //Show loading progress, disable btnSearch clickable
                                cpiLoading.visibility = View.VISIBLE
                                btnSearch.isClickable = false

                                //Stop configuration
//                                mTuyaActivator.stop()
                                //Exit the page to destroy some cache data and monitoring data.
//                                mTuyaActivator.onDestroy()
                            }

                            override fun onFailure(s: String, s1: String) {}
                        })
            }
        }
    }
}