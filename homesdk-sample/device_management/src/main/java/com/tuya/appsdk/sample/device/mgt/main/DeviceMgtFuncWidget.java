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

package com.tuya.appsdk.sample.device.mgt.main;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.tuya.appsdk.sample.device.mgt.R;
import com.tuya.appsdk.sample.device.mgt.group.GroupListActivity;
import com.tuya.appsdk.sample.device.mgt.list.activity.DeviceMgtListActivity;
import com.tuya.appsdk.sample.device.mgt.list.tag.DeviceListTypePage;
import com.tuya.appsdk.sample.resource.HomeModel;

/**
 * Device Management Widget
 *
 * @author chuanfeng <a href="mailto:developer@tuya.com"/>
 * @since 2021/2/9 5:06 PM
 */
public class DeviceMgtFuncWidget {
    public final View render(Context context) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.device_mgt_view_func, null, false);
        this.initView(rootView);
        return rootView;
    }

    private void initView(final View rootView) {
        // Device list
        rootView.findViewById(R.id.tvDeviceList).setOnClickListener(v -> {
            if ((HomeModel.getCurrentHome(v.getContext()) == 0)) {
                Toast.makeText(
                        rootView.getContext(),
                        rootView.getContext().getString(R.string.home_current_home_tips),
                        Toast.LENGTH_LONG
                ).show();
            } else {
                Intent intent = new Intent(v.getContext(), DeviceMgtListActivity.class);
                intent.putExtra("type", DeviceListTypePage.NORMAL_DEVICE_LIST);
                v.getContext().startActivity(intent);
            }

        });

        // ZigBee Gateway List
        rootView.findViewById(R.id.tv_zb_gateway_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((HomeModel.getCurrentHome(v.getContext()) == 0)) {
                    Toast.makeText(
                            rootView.getContext(),
                            rootView.getContext().getString(R.string.home_current_home_tips),
                            Toast.LENGTH_LONG
                    ).show();
                } else {
                    Intent intent = new Intent(v.getContext(), DeviceMgtListActivity.class);
                    intent.putExtra("type", DeviceListTypePage.ZIGBEE_GATEWAY_LIST);
                    v.getContext().startActivity(intent);
                }

            }
        });

        // ZigBee Gateway List
        rootView.findViewById(R.id.tv_zb_gateway_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((HomeModel.getCurrentHome(v.getContext()) == 0)) {
                    Toast.makeText(
                            rootView.getContext(),
                            rootView.getContext().getString(R.string.home_current_home_tips),
                            Toast.LENGTH_LONG
                    ).show();
                } else {
                    Intent intent = new Intent(v.getContext(), DeviceMgtListActivity.class);
                    intent.putExtra("type", DeviceListTypePage.ZIGBEE_GATEWAY_LIST);
                    v.getContext().startActivity(intent);
                }

            }
        });

        // group List
        rootView.findViewById(R.id.tv_group).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((HomeModel.getCurrentHome(v.getContext()) == 0)) {
                    Toast.makeText(
                            rootView.getContext(),
                            rootView.getContext().getString(R.string.home_current_home_tips),
                            Toast.LENGTH_LONG
                    ).show();
                } else {
                    Intent intent = new Intent(v.getContext(), GroupListActivity.class);
                    v.getContext().startActivity(intent);
                }

            }
        });
    }
}
