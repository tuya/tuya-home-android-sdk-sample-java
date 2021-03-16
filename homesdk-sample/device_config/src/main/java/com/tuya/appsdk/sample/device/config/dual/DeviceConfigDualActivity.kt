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
package com.tuya.appsdk.sample.device.config.dual

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.tuya.appsdk.sample.device.config.R
import com.tuya.appsdk.sample.device.config.ble.DeviceConfigBleActivity
import com.tuya.appsdk.sample.resource.HomeModel
import com.tuya.smart.android.ble.api.BleConfigType
import com.tuya.smart.android.ble.api.ITuyaBleConfigListener
import com.tuya.smart.android.ble.api.ScanType
import com.tuya.smart.home.sdk.TuyaHomeSdk
import com.tuya.smart.sdk.api.ITuyaActivatorGetToken
import com.tuya.smart.sdk.bean.DeviceBean

/**
 * Device Configuration Dual Device Sample
 *
 * @author aiwen <a href="mailto:developer@tuya.com"/>
 * @since 2/24/21 11:08 AM
 */
class DeviceConfigDualActivity : AppCompatActivity() {


    companion object {
        private const val TAG = "DeviceConfigDualMode"
        const val REQUEST_CODE = 1002
    }

    private val homeId: Long by lazy {
        HomeModel.INSTANCE.getCurrentHome(this)
    }

    lateinit var etSsid: TextInputEditText
    lateinit var etPassword: TextInputEditText
    lateinit var btSearch: Button
    lateinit var cpiLoading: CircularProgressIndicator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.device_config_activity)
        initToolbar()

        initView()
        checkPermission()
    }

    private fun initView() {
        etSsid = findViewById(R.id.etSsid)
        etPassword = findViewById(R.id.etPassword)
        btSearch = findViewById(R.id.btnSearch)
        cpiLoading = findViewById(R.id.cpiLoading)

        btSearch.setOnClickListener {


            val strSsid = etSsid.text.toString()
            val strPassword = etPassword.text.toString()

            if (strSsid.isEmpty() || strPassword.isEmpty()) {
                return@setOnClickListener
            }
            if (!TuyaHomeSdk.getBleOperator().isBluetoothOpened) {
                Toast.makeText(this, "Please turn on bluetooth", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            scanDualBleDevice()

        }

    }


    private fun initToolbar() {
        val toolbar: Toolbar = findViewById<View>(R.id.topAppBar) as Toolbar
        toolbar.setNavigationOnClickListener {
            finish()
        }
        toolbar.setTitle(R.string.device_config_dual_title)
    }


    // You need to check permissions before using Bluetooth devices
    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    ),
                    DeviceConfigBleActivity.REQUEST_CODE
            )
        }
    }


    private fun setPbViewVisible(isShow: Boolean) {
        cpiLoading.visibility = if (isShow) View.VISIBLE else View.GONE
        btSearch.isEnabled = !isShow
    }


    // Scan Ble Device
    private fun scanDualBleDevice() {
        setPbViewVisible(true)
        TuyaHomeSdk.getBleOperator().startLeScan(60 * 1000, ScanType.SINGLE) { bean ->
            Log.i(TAG, "scanDualBleDevice: beanType=${bean.configType},uuid=${bean.uuid}")
            // Start configuration -- Dual Device
            if (bean.configType == BleConfigType.CONFIG_TYPE_WIFI.type) {

                //  Get Network Configuration Token
                TuyaHomeSdk.getActivatorInstance().getActivatorToken(homeId,
                        object : ITuyaActivatorGetToken {
                            override fun onSuccess(token: String) {

                                // Start configuration -- Dual Ble Device
                                val param = mutableMapOf<String, String>()
                                param["ssid"] = etSsid.text.toString()
                                param["password"] = etPassword.text.toString()
                                param["token"] = token
                                TuyaHomeSdk.getBleManager()
                                        .startBleConfig(homeId, bean.uuid, param as Map<String, String>,
                                                object : ITuyaBleConfigListener {
                                                    override fun onSuccess(bean: DeviceBean?) {
                                                        setPbViewVisible(false)
                                                        Toast.makeText(
                                                                this@DeviceConfigDualActivity,
                                                                "Config Success",
                                                                Toast.LENGTH_LONG
                                                        ).show()
                                                        finish()
                                                    }

                                                    override fun onFail(
                                                            code: String?,
                                                            msg: String?,
                                                            handle: Any?
                                                    ) {
                                                        setPbViewVisible(false)
                                                        finish()
                                                        Toast.makeText(
                                                                this@DeviceConfigDualActivity,
                                                                "Config Failed",
                                                                Toast.LENGTH_LONG
                                                        ).show()
                                                    }
                                                })
                            }

                            override fun onFailure(errorCode: String?, errorMsg: String?) {
                                setPbViewVisible(false)
                                Toast.makeText(
                                        this@DeviceConfigDualActivity,
                                        "Error->$errorMsg",
                                        Toast.LENGTH_LONG
                                ).show()
                            }
                        })
            }
        }
    }


    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "onRequestPermissionsResult: agree")
                } else {
                    finish()
                    Log.e(TAG, "onRequestPermissionsResult: denied")
                }
            }
        }
    }
}