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

package com.tuya.appsdk.sample.device.mgt.control.activity

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.tuya.appsdk.sample.device.mgt.R
import com.tuya.appsdk.sample.device.mgt.control.dpItem.*
import com.tuya.smart.android.device.bean.*
import com.tuya.smart.android.device.enums.DataTypeEnum
import com.tuya.smart.home.sdk.TuyaHomeSdk
import com.tuya.smart.sdk.api.IResultCallback

/**
 * Device control sample
 *
 * @author qianqi <a href="mailto:developer@tuya.com"/>
 * @since 2021/1/21 10:30 AM
 */
class DeviceMgtControlActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.device_mgt_activity_control)

        val toolbar: Toolbar = findViewById<View>(R.id.topAppBar) as Toolbar
        toolbar.setNavigationOnClickListener {
            finish()
        }


        val llDp = findViewById<LinearLayout>(R.id.llDp)

        val deviceId = intent.getStringExtra("deviceId")
        deviceId?.let {
            val mDevice = TuyaHomeSdk.newDeviceInstance(it)
            val deviceBean = TuyaHomeSdk.getDataInstance().getDeviceBean(it)

            findViewById<Button>(R.id.btnReset).setOnClickListener {
                // device reset factory
                mDevice.resetFactory(object : IResultCallback {
                    override fun onSuccess() {
                        finish()
                    }

                    override fun onError(code: String?, error: String?) {

                    }
                })
            }

            findViewById<Button>(R.id.btnRemove).setOnClickListener {
                // remove device
                mDevice.removeDevice(object : IResultCallback {
                    override fun onSuccess() {
                        finish()
                    }

                    override fun onError(code: String?, error: String?) {

                    }

                })
            }

            findViewById<TextView>(R.id.tvDeviceName).text = deviceBean?.name

            TuyaHomeSdk.getDataInstance().getSchema(it)?.let { map ->
                for (bean in map.values) {
                    val value = deviceBean?.dps?.get(bean.id)

                    if (bean.type == DataTypeEnum.OBJ.type) {
                        // obj
                        when (bean.getSchemaType()) {
                            BoolSchemaBean.type -> {
                                val vItem = DpBooleanItem(
                                        this,
                                        schemaBean = bean,
                                        value = value as Boolean,
                                        device = mDevice
                                )
                                llDp.addView(vItem)
                            }
                            EnumSchemaBean.type -> {
                                val vItem = DpEnumItem(
                                        this,
                                        schemaBean = bean,
                                        value = value.toString(),
                                        device = mDevice
                                )
                                llDp.addView(vItem)
                            }
                            StringSchemaBean.type -> {
                                val vItem = DpCharTypeItem(
                                        this,
                                        schemaBean = bean,
                                        value = value as String,
                                        device = mDevice
                                )
                                llDp.addView(vItem)
                            }
                            ValueSchemaBean.type -> {
                                val vItem = DpIntegerItem(
                                        this,
                                        schemaBean = bean,
                                        value = value as Int,
                                        device = mDevice
                                )
                                llDp.addView(vItem)
                            }
                            BitmapSchemaBean.type -> {
                                val vItem =
                                        DpFaultItem(this, schemaBean = bean, value = value.toString())
                                llDp.addView(vItem)
                            }
                        }
                    } else if (bean.type == DataTypeEnum.RAW.type) {
                        // raw | file
                        val vItem = DpRawTypeItem(
                                this,
                                schemaBean = bean,
                                value = value.toString(),
                                device = mDevice
                        )
                        llDp.addView(vItem)
                    }

                }
            }

        }
    }
}