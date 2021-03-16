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

package com.tuya.appsdk.sample.device.mgt.list.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tuya.appsdk.sample.device.mgt.R
import com.tuya.appsdk.sample.device.mgt.list.adapter.DeviceMgtAdapter
import com.tuya.appsdk.sample.device.mgt.list.enum.DeviceListTypePage
import com.tuya.appsdk.sample.resource.HomeModel
import com.tuya.smart.home.sdk.TuyaHomeSdk
import com.tuya.smart.home.sdk.bean.HomeBean
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback
import com.tuya.smart.sdk.bean.DeviceBean

/**
 * Device Management initial device data sample
 *
 * @author qianqi <a href="mailto:developer@tuya.com"/>
 * @since 2021/1/21 9:58 AM
 */
class DeviceMgtListActivity : AppCompatActivity() {

    lateinit var adapter: DeviceMgtAdapter
    var type = DeviceListTypePage.NORMAL_DEVICE_LIST

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.device_mgt_activity_list)

        // Get Device List Type
        type = intent.getIntExtra("type", DeviceListTypePage.NORMAL_DEVICE_LIST)


        initToolbar()

        // Set List
        val rvList = findViewById<RecyclerView>(R.id.rvList)
        rvList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        adapter = DeviceMgtAdapter(type)
        rvList.adapter = adapter
    }

    private fun initToolbar() {
        val toolbar: Toolbar = findViewById<View>(R.id.topAppBar) as Toolbar
        toolbar.setNavigationOnClickListener {
            finish()
        }

        toolbar.title = when (type) {
            DeviceListTypePage.NORMAL_DEVICE_LIST -> getString(R.string.device_mgt_list)
            DeviceListTypePage.ZIGBEE_GATEWAY_LIST -> getString(R.string.device_zb_gateway_list)
            else -> getString(R.string.device_mgt_list)
        }
    }

    override fun onResume() {
        super.onResume()
        val homeId = HomeModel.INSTANCE.getCurrentHome(this)
        /**
         * The device control must first initialize the data,
         * and call the following method to get the device information in the home.
         * initialization only need when the begin of app lifecycle and switch home.
         */
        TuyaHomeSdk.newHomeInstance(homeId).getHomeDetail(object : ITuyaHomeResultCallback {
            override fun onSuccess(bean: HomeBean?) {
                bean?.let { it ->
                    if (type == DeviceListTypePage.NORMAL_DEVICE_LIST) {
                        adapter.data = it.deviceList as ArrayList<DeviceBean>
                        adapter.notifyDataSetChanged()
                    } else {
                        adapter.data = it.deviceList.filter {
                            it.isZigBeeWifi
                        } as ArrayList<DeviceBean>
                        adapter.notifyDataSetChanged()
                    }
                }

            }

            override fun onError(errorCode: String?, errorMsg: String?) {

            }

        })
    }
}