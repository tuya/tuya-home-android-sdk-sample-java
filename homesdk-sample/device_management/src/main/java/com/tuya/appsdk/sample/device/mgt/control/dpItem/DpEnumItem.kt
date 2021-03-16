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
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.ListPopupWindow
import com.alibaba.fastjson.JSONObject
import com.tuya.appsdk.sample.device.mgt.R
import com.tuya.smart.android.device.bean.SchemaBean
import com.tuya.smart.home.sdk.utils.SchemaMapper
import com.tuya.smart.sdk.api.IResultCallback
import com.tuya.smart.sdk.api.ITuyaDevice

/**
 * Data point(DP) Enum type item
 *
 * @author qianqi <a href="mailto:developer@tuya.com"/>
 * @since 2021/1/21 3:06 PM
 */
class DpEnumItem @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0,
        schemaBean: SchemaBean,
        value: String,
        device: ITuyaDevice
) : FrameLayout(context, attrs, defStyle) {

    init {
        inflate(context, R.layout.device_mgt_item_dp_enum, this)

        findViewById<TextView>(R.id.tvDpName).text = schemaBean.name

        val btnDp = findViewById<Button>(R.id.btnDp)
        btnDp.text = value

        if (schemaBean.mode.contains("w")) {
            // Data can be issued by the cloud.
            val listPopupWindow = ListPopupWindow(context, null, R.attr.listPopupWindowStyle)
            listPopupWindow.anchorView = btnDp

            val enumSchemaBean = SchemaMapper.toEnumSchema(schemaBean.property)
            val items = enumSchemaBean.range.toList()
            val adapter = ArrayAdapter(context, R.layout.device_mgt_item_dp_enum_popup_item, items)
            listPopupWindow.setAdapter(adapter)
            listPopupWindow.setOnItemClickListener { parent, view, position, id ->

                val map = HashMap<String, Any>()
                map[schemaBean.id] = items[position]
                JSONObject.toJSONString(map)?.let {
                    device.publishDps(it, object : IResultCallback {
                        override fun onSuccess() {
                            btnDp.text = items[position]
                        }

                        override fun onError(code: String?, error: String?) {
                            Log.e("DpEnumItem", "$code --> $error")
                        }

                    })
                }

                listPopupWindow.dismiss()
            }

            btnDp.setOnClickListener {
                listPopupWindow.show()
            }
        }
    }
}