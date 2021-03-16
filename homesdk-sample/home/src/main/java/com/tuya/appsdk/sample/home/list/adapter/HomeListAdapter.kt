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

package com.tuya.appsdk.sample.home.list.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tuya.appsdk.sample.home.detail.HomeDetailActivity
import com.tuya.appsdk.sample.home.list.enum.HomeListPageType
import com.tuya.appsdk.sample.resource.HomeModel
import com.tuya.appsdk.sample.user.R
import com.tuya.smart.home.sdk.TuyaHomeSdk
import com.tuya.smart.home.sdk.bean.HomeBean

/**
 * Home List Adapter
 *
 * @author qianqi <a href="mailto:developer@tuya.com"/>
 * @since 2021/1/20 2:32 PM
 */
class HomeListAdapter(val type: Int) : RecyclerView.Adapter<HomeListAdapter.ViewHolder>() {

    var data: ArrayList<HomeBean> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val holder = ViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.home_item_list, parent, false)
        )

        if (type == HomeListPageType.LIST) {
            holder.ivIcon.setImageResource(R.drawable.ic_next)
            holder.itemView.setOnClickListener {
                // Home Detail
                val intent = Intent(it.context, HomeDetailActivity::class.java)
                intent.putExtra("homeId", data[holder.adapterPosition].homeId)
                it.context.startActivity(intent)
            }
        } else if (type == HomeListPageType.SWITCH) {
            holder.itemView.setOnClickListener {
                // Switch Home
                val bean = data[holder.adapterPosition]
                TuyaHomeSdk.newHomeInstance(bean.homeId)
                HomeModel.INSTANCE.setCurrentHome(it.context, bean.homeId)
                notifyDataSetChanged()
            }
        }

        return holder
    }


    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bean = data[position]
        holder.tvName.text = bean.name

        if (type == HomeListPageType.SWITCH) {
            // Switch Home Type
            if (HomeModel.INSTANCE.getCurrentHome(holder.itemView.context) == bean.homeId) {
                holder.ivIcon.setImageResource(R.drawable.ic_check)
            } else {
                holder.ivIcon.setImageResource(0)
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val ivIcon: ImageView = itemView.findViewById(R.id.ivIcon)
    }
}