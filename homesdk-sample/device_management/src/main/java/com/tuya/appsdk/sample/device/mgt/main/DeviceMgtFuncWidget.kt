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

package com.tuya.appsdk.sample.device.mgt.main

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.tuya.appsdk.sample.device.mgt.R
import com.tuya.appsdk.sample.device.mgt.list.activity.DeviceMgtListActivity
import com.tuya.appsdk.sample.device.mgt.list.enum.DeviceListTypePage
import com.tuya.appsdk.sample.resource.HomeModel

/**
 * Device Management Widget
 *
 * @author qianqi <a href="mailto:developer@tuya.com"/>
 * @since 2021/1/9 5:06 PM
 */
class DeviceMgtFuncWidget {


    fun render(context: Context): View {
        val rootView =
                LayoutInflater.from(context).inflate(R.layout.device_mgt_view_func, null, false)
        initView(rootView)
        return rootView
    }

    private fun initView(rootView: View) {
        // Device list
        rootView.findViewById<TextView>(R.id.tvDeviceList).setOnClickListener {
            if (!HomeModel.INSTANCE.checkHomeId(rootView.context)) {
                Toast.makeText(
                        rootView.context,
                        rootView.context.getString(R.string.home_current_home_tips),
                        Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            val intent = Intent(it.context, DeviceMgtListActivity::class.java)
            intent.putExtra("type", DeviceListTypePage.NORMAL_DEVICE_LIST)
            it.context.startActivity(intent)
        }


        // ZigBee Gateway List
        rootView.findViewById<TextView>(R.id.tv_zb_gateway_list).setOnClickListener {
            if (!HomeModel.INSTANCE.checkHomeId(rootView.context)) {
                Toast.makeText(
                        rootView.context,
                        rootView.context.getString(R.string.home_current_home_tips),
                        Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            val intent = Intent(it.context, DeviceMgtListActivity::class.java)
            intent.putExtra("type", DeviceListTypePage.ZIGBEE_GATEWAY_LIST)
            it.context.startActivity(intent)
        }

    }

}