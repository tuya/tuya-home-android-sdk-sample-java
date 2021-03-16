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

package com.tuya.appsdk.sample.device.mgt.list.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tuya.appsdk.sample.device.mgt.R
import com.tuya.appsdk.sample.device.mgt.control.activity.DeviceMgtControlActivity
import com.tuya.appsdk.sample.device.mgt.list.activity.DeviceSubZigbeeActivity
import com.tuya.appsdk.sample.device.mgt.list.enum.DeviceListTypePage
import com.tuya.smart.sdk.bean.DeviceBean

/**
 * Device list adapter
 *
 * @author qianqi <a href="mailto:developer@tuya.com"/>
 * @since 2021/1/21 10:06 AM
 */
class DeviceMgtAdapter(val type: Int) : RecyclerView.Adapter<DeviceMgtAdapter.ViewHolder>() {
    var data: ArrayList<DeviceBean> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val holder = ViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.device_mgt_item, parent, false)
        )
        holder.itemView.setOnClickListener {
            when (type) {
                DeviceListTypePage.ZIGBEE_GATEWAY_LIST -> {
                    // Navigate to zigBee sub device list
                    val intent = Intent(it.context, DeviceSubZigbeeActivity::class.java)
                    intent.putExtra("deviceId", data[holder.adapterPosition].devId)
                    it.context.startActivity(intent)
                }
                DeviceListTypePage.NORMAL_DEVICE_LIST -> {
                    // Navigate to device management
                    val intent = Intent(it.context, DeviceMgtControlActivity::class.java)
                    intent.putExtra("deviceId", data[holder.adapterPosition].devId)
                    it.context.startActivity(intent)
                }
                else -> {
                    // Navigate to zigBee sub device management
                    val intent = Intent(it.context, DeviceMgtControlActivity::class.java)
                    intent.putExtra("deviceId", data[holder.adapterPosition].devId)
                    it.context.startActivity(intent)
                }
            }
        }
        return holder
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bean = data[position]
        holder.tvDeviceName.text = bean.name
        holder.tvStatus.text =
                holder.itemView.context.getString(if (bean.isOnline) R.string.device_mgt_online else R.string.device_mgt_offline)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDeviceName = itemView.findViewById<TextView>(R.id.tvDeviceName)
        val tvStatus = itemView.findViewById<TextView>(R.id.tvDeviceStatus)
    }
}