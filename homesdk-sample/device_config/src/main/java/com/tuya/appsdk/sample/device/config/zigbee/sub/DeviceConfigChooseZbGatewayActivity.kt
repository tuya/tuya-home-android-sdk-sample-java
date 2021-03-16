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
package com.tuya.appsdk.sample.device.config.zigbee.sub

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tuya.appsdk.sample.device.config.R
import com.tuya.appsdk.sample.device.config.zigbee.adapter.ZigBeeGatewayListAdapter
import com.tuya.appsdk.sample.resource.HomeModel
import com.tuya.smart.home.sdk.TuyaHomeSdk
import com.tuya.smart.home.sdk.bean.HomeBean
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback
import com.tuya.smart.sdk.bean.DeviceBean

/**
 * Choose Gateway
 *
 * @author aiwen <a href="mailto:developer@tuya.com"/>
 * @since 2/25/21 9:45 AM
 */
class DeviceConfigChooseZbGatewayActivity : AppCompatActivity() {

    lateinit var adapter: ZigBeeGatewayListAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.device_config_zb_choose_gateway_activity)
        initToolbar()
        initView()
    }

    private fun initView() {
        val rvList = findViewById<RecyclerView>(R.id.rvList)
        adapter = ZigBeeGatewayListAdapter(this)


        // Set List
        val linearLayoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rvList.layoutManager = linearLayoutManager
        rvList.adapter = adapter
        getZigBeeGatewayList()
    }

    // Get ZigBee Gateway List
    private fun getZigBeeGatewayList() {
        val currentHomeId = HomeModel.INSTANCE.getCurrentHome(this)
        TuyaHomeSdk.newHomeInstance(currentHomeId).getHomeDetail(object : ITuyaHomeResultCallback {
            override fun onSuccess(bean: HomeBean?) {
                val deviceList = bean?.deviceList as ArrayList<DeviceBean>
                val zigBeeGatewayList = deviceList.filter {
                    it.isZigBeeWifi
                }
                adapter.data = zigBeeGatewayList as ArrayList<DeviceBean>
                adapter.notifyDataSetChanged()
            }

            override fun onError(errorCode: String?, errorMsg: String?) {
                Toast.makeText(
                        this@DeviceConfigChooseZbGatewayActivity,
                        "Error->$errorMsg",
                        Toast.LENGTH_LONG
                ).show()
            }
        })
    }


    // init Toolbar
    private fun initToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.topAppBar)
        toolbar.setNavigationOnClickListener {
            finish()
        }
        toolbar.setTitle(R.string.device_config_choose_gateway_title)
    }
}