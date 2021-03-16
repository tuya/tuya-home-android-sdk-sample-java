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

package com.tuya.appsdk.sample.device.config.main

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.tuya.appsdk.sample.device.config.R
import com.tuya.appsdk.sample.device.config.ap.DeviceConfigAPActivity
import com.tuya.appsdk.sample.device.config.ble.DeviceConfigBleActivity
import com.tuya.appsdk.sample.device.config.dual.DeviceConfigDualActivity
import com.tuya.appsdk.sample.device.config.ez.DeviceConfigEZActivity
import com.tuya.appsdk.sample.device.config.qrcode.DeviceConfigQrCodeDeviceActivity
import com.tuya.appsdk.sample.device.config.zigbee.gateway.DeviceConfigZbGatewayActivity
import com.tuya.appsdk.sample.device.config.zigbee.sub.DeviceConfigZbSubDeviceActivity
import com.tuya.appsdk.sample.resource.HomeModel

/**
 * Device configuration func Widget
 *
 * @author qianqi <a href="mailto:developer@tuya.com"/>
 * @since 2021/1/9 5:06 PM
 */
class DeviceConfigFuncWidget {


    lateinit var mContext: Context
    fun render(context: Context): View {
        val rootView =
                LayoutInflater.from(context).inflate(R.layout.device_config_view_func, null, false)
        mContext = context
        initView(rootView)
        return rootView
    }

    private fun initView(rootView: View) {
        // EZ Mode
        rootView.findViewById<TextView>(R.id.tvEzMode).setOnClickListener {
            if (!HomeModel.INSTANCE.checkHomeId(mContext)) {
                Toast.makeText(
                        mContext,
                        mContext.getString(R.string.home_current_home_tips),
                        Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            it.context.startActivity(Intent(it.context, DeviceConfigEZActivity::class.java))
        }

        // AP Mode
        rootView.findViewById<TextView>(R.id.tvApMode).setOnClickListener {
            if (!HomeModel.INSTANCE.checkHomeId(mContext)) {
                Toast.makeText(
                        mContext,
                        mContext.getString(R.string.home_current_home_tips),
                        Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            it.context.startActivity(Intent(it.context, DeviceConfigAPActivity::class.java))
        }

        // Ble Low Energy
        rootView.findViewById<TextView>(R.id.tv_ble).setOnClickListener {
            if (!HomeModel.INSTANCE.checkHomeId(mContext)) {
                Toast.makeText(
                        mContext,
                        mContext.getString(R.string.home_current_home_tips),
                        Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            it.context.startActivity(Intent(it.context, DeviceConfigBleActivity::class.java))
        }

        // Dual Mode
        rootView.findViewById<TextView>(R.id.tv_dual_mode).setOnClickListener {
            if (!HomeModel.INSTANCE.checkHomeId(mContext)) {
                Toast.makeText(
                        mContext,
                        mContext.getString(R.string.home_current_home_tips),
                        Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            it.context.startActivity(Intent(it.context, DeviceConfigDualActivity::class.java))

        }


        // ZigBee Gateway
        rootView.findViewById<TextView>(R.id.tv_zigBee_gateway).setOnClickListener {
            if (!HomeModel.INSTANCE.checkHomeId(mContext)) {
                Toast.makeText(
                        mContext,
                        mContext.getString(R.string.home_current_home_tips),
                        Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            it.context.startActivity(Intent(it.context, DeviceConfigZbGatewayActivity::class.java))

        }

        // ZigBee Sub Device
        rootView.findViewById<TextView>(R.id.tv_zigBee_subDevice).setOnClickListener {
            if (!HomeModel.INSTANCE.checkHomeId(mContext)) {
                Toast.makeText(
                        mContext,
                        mContext.getString(R.string.home_current_home_tips),
                        Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            it.context.startActivity(
                    Intent(
                            it.context,
                            DeviceConfigZbSubDeviceActivity::class.java
                    )
            )

        }

        // Qr Code
        rootView.findViewById<TextView>(R.id.tv_qrcode_subDevice).setOnClickListener {
            if (!HomeModel.INSTANCE.checkHomeId(mContext)) {
                Toast.makeText(
                    mContext,
                    mContext.getString(R.string.home_current_home_tips),
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            it.context.startActivity(
                Intent(
                    it.context,
                    DeviceConfigQrCodeDeviceActivity::class.java
                )
            )

        }



    }


}