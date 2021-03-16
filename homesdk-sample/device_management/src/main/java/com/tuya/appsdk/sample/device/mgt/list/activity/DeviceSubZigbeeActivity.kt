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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tuya.appsdk.sample.device.mgt.R
import com.tuya.appsdk.sample.device.mgt.list.adapter.DeviceMgtAdapter
import com.tuya.appsdk.sample.device.mgt.list.enum.DeviceListTypePage
import com.tuya.smart.home.sdk.TuyaHomeSdk
import com.tuya.smart.sdk.api.ITuyaDataCallback
import com.tuya.smart.sdk.bean.DeviceBean


/**
 * ZigBee Sub-device List Sample
 *
 * @author aiwen <a href="mailto:developer@tuya.com"/>
 * @since 2/25/21 2:26 PM
 */
class DeviceSubZigbeeActivity : AppCompatActivity() {


    lateinit var adapter: DeviceMgtAdapter

    lateinit var deviceId: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.device_mgt_activity_list)
        deviceId = intent.getStringExtra("deviceId").toString()



        initToolbar()

        val rvList = findViewById<RecyclerView>(R.id.rvList)
        rvList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        adapter = DeviceMgtAdapter(DeviceListTypePage.ZIGBEE_SUB_DEVICE_LIST)
        rvList.adapter = adapter


        getZbSubDeviceList()

    }


    // Get Sub-devices
    private fun getZbSubDeviceList() {
        TuyaHomeSdk.newGatewayInstance(deviceId)
                .getSubDevList(object : ITuyaDataCallback<List<DeviceBean>> {
                    override fun onSuccess(result: List<DeviceBean>?) {
                        result?.let {
                            adapter.data = it as ArrayList<DeviceBean>
                            adapter.notifyDataSetChanged()
                        }
                    }

                    override fun onError(errorCode: String?, errorMessage: String?) {
                        Toast.makeText(
                                this@DeviceSubZigbeeActivity,
                                "Error->$errorMessage",
                                Toast.LENGTH_LONG
                        ).show()
                    }

                })

    }


    private fun initToolbar() {
        val toolbar: Toolbar = findViewById<View>(R.id.topAppBar) as Toolbar
        toolbar.setNavigationOnClickListener {
            finish()
        }

        toolbar.title = getString(R.string.device_zb_sub_device_list)
    }

}