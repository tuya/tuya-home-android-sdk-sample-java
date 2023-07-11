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
package com.tuya.appsdk.sample.device.config.zigbee.gateway;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.thingclips.smart.activator.core.kit.ThingActivatorCoreKit;
import com.thingclips.smart.activator.core.kit.active.inter.IThingActiveManager;
import com.thingclips.smart.activator.core.kit.bean.ThingActivatorScanDeviceBean;
import com.thingclips.smart.activator.core.kit.bean.ThingActivatorScanFailureBean;
import com.thingclips.smart.activator.core.kit.bean.ThingDeviceActiveErrorBean;
import com.thingclips.smart.activator.core.kit.bean.ThingDeviceActiveLimitBean;
import com.thingclips.smart.activator.core.kit.builder.ThingDeviceActiveBuilder;
import com.thingclips.smart.activator.core.kit.callback.ThingActivatorScanCallback;
import com.thingclips.smart.activator.core.kit.constant.ThingDeviceActiveModeEnum;
import com.thingclips.smart.activator.core.kit.listener.IThingDeviceActiveListener;
import com.thingclips.smart.activator.core.kit.scan.ThingActivatorScanDeviceManager;
import com.thingclips.smart.android.hardware.bean.HgwBean;
import com.thingclips.smart.home.sdk.ThingHomeSdk;
import com.thingclips.smart.home.sdk.builder.ThingGwActivatorBuilder;
import com.thingclips.smart.sdk.api.IThingActivator;
import com.thingclips.smart.sdk.api.IThingActivatorGetToken;
import com.thingclips.smart.sdk.api.IThingSmartActivatorListener;
import com.thingclips.smart.sdk.bean.DeviceBean;
import com.tuya.appsdk.sample.device.config.R;
import com.tuya.appsdk.sample.resource.HomeModel;

/**
 * Device Configuration ZbGateway Mode Sample
 *
 * @author yueguang <a href="mailto:developer@tuya.com"/>
 * @since 2021/3/3 10:57 AM
 */
public class DeviceConfigZbGatewayActivity extends AppCompatActivity implements View.OnClickListener {

    private MaterialToolbar topAppBar;
    private TextView tv_hint_info;
    private Button bt_search;
    private CircularProgressIndicator cpiLoading;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_config_info_hint_activity);
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
        topAppBar.setTitle(R.string.device_config_zb_gateway_title);
        tv_hint_info = findViewById(R.id.tv_hint_info);
        tv_hint_info.setText(R.string.device_config_zb_gateway_hint);
        bt_search = findViewById(R.id.bt_search);
        cpiLoading = findViewById(R.id.cpiLoading);

        bt_search.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_search) {
            searchGatewayDevice();
        }
    }

    // Search ZigBee Gateway Device
    private void searchGatewayDevice() {
        setPbViewVisible(true);

        ThingActivatorScanDeviceManager.INSTANCE.startLocalGatewayDeviceSearch(
                60 * 1000L, new ThingActivatorScanCallback() {
                    @Override
                    public void deviceFound(@NonNull ThingActivatorScanDeviceBean thingActivatorScanDeviceBean) {
                        startNetworkConfig(thingActivatorScanDeviceBean);
                    }

                    @Override
                    public void deviceUpdate(@NonNull ThingActivatorScanDeviceBean thingActivatorScanDeviceBean) {

                    }

                    @Override
                    public void deviceRepeat(@NonNull ThingActivatorScanDeviceBean thingActivatorScanDeviceBean) {

                    }

                    @Override
                    public void scanFinish() {

                    }

                    @Override
                    public void scanFailure(@NonNull ThingActivatorScanFailureBean thingActivatorScanFailureBean) {

                    }
                }
        );
    }

    // Start network configuration -- ZigBee Gateway
    private void startNetworkConfig(ThingActivatorScanDeviceBean scanDeviceBean) {
        IThingActiveManager activeManager = ThingActivatorCoreKit.INSTANCE.getActiveManager().newThingActiveManager();
        ThingDeviceActiveBuilder builder = new ThingDeviceActiveBuilder();
        builder.setActivatorScanDeviceBean(scanDeviceBean);
        builder.setContext(this);
        builder.setActiveModel(ThingDeviceActiveModeEnum.WN);
        builder.setTimeOut(60);
        builder.setListener(new IThingDeviceActiveListener() {
            @Override
            public void onFind(@NonNull String s) {

            }

            @Override
            public void onBind(@NonNull String s) {

            }

            @Override
            public void onActiveSuccess(@NonNull DeviceBean deviceBean) {
                setPbViewVisible(false);
                Toast.makeText(
                        DeviceConfigZbGatewayActivity.this,
                        "Activate success",
                        Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onActiveError(@NonNull ThingDeviceActiveErrorBean thingDeviceActiveErrorBean) {
                setPbViewVisible(false);
                Toast.makeText(
                        DeviceConfigZbGatewayActivity.this,
                        "Activate Error" + thingDeviceActiveErrorBean.getErrMsg(),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onActiveLimited(@NonNull ThingDeviceActiveLimitBean thingDeviceActiveLimitBean) {

            }
        });
        activeManager.startActive(builder);
    }

    private void setPbViewVisible(boolean isShow) {
        cpiLoading.setVisibility(isShow ? View.VISIBLE : View.GONE);
        bt_search.setEnabled(!isShow);
    }
}