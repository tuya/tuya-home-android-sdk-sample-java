/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2021 Tuya Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NO
 */
package com.tuya.appsdk.sample.device.config.zigbee.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tuya.appsdk.sample.device.config.R;
import com.tuya.appsdk.sample.device.config.util.sp.SpUtils;
import com.tuya.appsdk.sample.device.config.zigbee.sub.DeviceConfigZbSubDeviceActivity;
import com.tuya.smart.sdk.bean.DeviceBean;

import java.util.ArrayList;

/**
 * Device Configuration Zigbee Mode adapter
 *
 * @author yueguang <a href="mailto:developer@tuya.com"/>
 * @since 2021/3/4 10:59 AM
 */
public class ZigBeeGatewayListAdapter extends RecyclerView.Adapter<ZigBeeGatewayListAdapter.ViewHolder> {

    ArrayList<DeviceBean> data = new ArrayList();
    String currentGatewayId;
    String currentGatewayName;
    Context context;

    public ZigBeeGatewayListAdapter(Context context) {
        this.context = context;
    }

    public ArrayList<DeviceBean> getData() {
        return data;
    }

    public void setData(ArrayList<DeviceBean> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View inflate = LayoutInflater.from(context)
                .inflate(R.layout.device_zb_gateway_list, parent, false);

        ViewHolder viewHolder = new ViewHolder(inflate);

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeviceBean deviceBean = data.get(viewHolder.getAdapterPosition());
                currentGatewayId = deviceBean.devId;
                currentGatewayName = deviceBean.name;

                SpUtils.getInstance().putString(DeviceConfigZbSubDeviceActivity.CURRENT_GATEWAY_ID, currentGatewayId);
                SpUtils.getInstance().putString(DeviceConfigZbSubDeviceActivity.CURRENT_GATEWAY_NAME, currentGatewayName);

                notifyDataSetChanged();
            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DeviceBean deviceBean = data.get(position);
        holder.itemName.setText(deviceBean.getName());
        // Switch ZigBee Gateway
        if (currentGatewayId == deviceBean.devId) {
            holder.itemIcon.setImageResource(R.drawable.ic_check);
        } else {
            holder.itemIcon.setImageResource(0);
        }

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView itemName;
        ImageView itemIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.tvName);
            itemIcon = itemView.findViewById(R.id.ivIcon);
        }
    }
}
