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

package com.tuya.appsdk.sample.device.mgt.control.dpItem

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.Switch
import android.widget.TextView
import com.alibaba.fastjson.JSONObject
import com.tuya.appsdk.sample.device.mgt.R
import com.tuya.smart.android.device.bean.SchemaBean
import com.tuya.smart.sdk.api.IResultCallback
import com.tuya.smart.sdk.api.ITuyaDevice

/**
 * Data point(DP) Boolean type item
 *
 * @author qianqi <a href="mailto:developer@tuya.com"/>
 * @since 2021/1/21 3:06 PM
 */
class DpBooleanItem @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0,
        schemaBean: SchemaBean,
        value: Boolean,
        device: ITuyaDevice
) : FrameLayout(context, attrs, defStyle) {

    init {
        inflate(context, R.layout.device_mgt_item_dp_boolean, this)

        findViewById<TextView>(R.id.tvDpName).text = schemaBean.name

        val swDp = findViewById<Switch>(R.id.swDp)
        swDp.isChecked = value

        if (schemaBean.mode.contains("w")) {
            // Data can be issued by the cloud.
            swDp.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    val map = HashMap<String, Any>()
                    val isChecked = !swDp.isChecked
                    map[schemaBean.id] = isChecked
                    JSONObject.toJSONString(map)?.let {

                        device.publishDps(it, object : IResultCallback {
                            override fun onSuccess() {
                                swDp.isChecked = isChecked
                            }

                            override fun onError(code: String?, error: String?) {

                            }

                        })
                    }
                }
                return@setOnTouchListener true
            }
        }


    }
}