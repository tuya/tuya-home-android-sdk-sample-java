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
package com.tuya.appsdk.sample.device.config.zigbee.sub;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.tuya.appsdk.sample.device.config.R;
import com.tuya.appsdk.sample.device.config.zigbee.adapter.ZigBeeGatewayListAdapter;
import com.tuya.appsdk.sample.resource.HomeModel;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;
import com.tuya.smart.sdk.bean.DeviceBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


/**
 * Device Configuration ChooseZbGateway Sample
 *
 * @author yueguang <a href="mailto:developer@tuya.com"/>
 * @since 2021/3/3 11:02 AM
 */
public class DeviceConfigChooseZbGatewayActivity extends AppCompatActivity {

    private MaterialToolbar topAppBar;
    private RecyclerView rvList;
    ZigBeeGatewayListAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_config_zb_choose_gateway_activity);
        initView();

    }

    private void initView() {
        topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        topAppBar.setTitle(R.string.device_config_choose_gateway_title);
        rvList = findViewById(R.id.rvList);
        adapter = new ZigBeeGatewayListAdapter(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        rvList.setLayoutManager(linearLayoutManager);
        rvList.setAdapter(adapter);
        getZigBeeGatewayList();
    }

    // Get ZigBee Gateway List
    private void getZigBeeGatewayList() {
        long currentHomeId = HomeModel.getCurrentHome(this);
        TuyaHomeSdk.newHomeInstance(currentHomeId).getHomeDetail(new ITuyaHomeResultCallback() {
            @Override
            public void onSuccess(HomeBean bean) {

                List<DeviceBean> deviceBeans = bean != null ? bean.getDeviceList() : null;
                ArrayList deviceList = (ArrayList) deviceBeans;
                Iterable iterable = deviceList;
                Collection collection = (new ArrayList());
                Iterator iterator = iterable.iterator();

                while (iterator.hasNext()) {
                    Object next = iterator.next();
                    DeviceBean it = (DeviceBean) next;
                    if (it.isZigBeeWifi()) {
                        collection.add(next);
                    }
                }

                List zigBeeGatewayList = (List) collection;

                adapter.setData((ArrayList<DeviceBean>) zigBeeGatewayList);

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String errorCode, String errorMsg) {
                Toast.makeText(DeviceConfigChooseZbGatewayActivity.this,
                        "error" + errorCode,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}

