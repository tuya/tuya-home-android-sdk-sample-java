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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.tuya.appsdk.sample.device.config.R;
import com.tuya.appsdk.sample.resource.HomeModel;
import com.tuya.smart.android.hardware.bean.HgwBean;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.api.IGwSearchListener;
import com.tuya.smart.home.sdk.api.ITuyaGwSearcher;
import com.tuya.smart.home.sdk.builder.TuyaGwActivatorBuilder;
import com.tuya.smart.sdk.api.ITuyaActivator;
import com.tuya.smart.sdk.api.ITuyaActivatorGetToken;
import com.tuya.smart.sdk.api.ITuyaSmartActivatorListener;
import com.tuya.smart.sdk.bean.DeviceBean;

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
        ITuyaGwSearcher iTuyaGwSearcher = TuyaHomeSdk.getActivatorInstance().newTuyaGwActivator().newSearcher();
        iTuyaGwSearcher.registerGwSearchListener(new IGwSearchListener() {
            @Override
            public void onDevFind(HgwBean gw) {
                getNetworkConfigToken(gw);
            }
        });
    }

    // Get Network Configuration Token
    private void getNetworkConfigToken(HgwBean gw) {
        long homeId = HomeModel.getCurrentHome(this);

        TuyaHomeSdk.getActivatorInstance().getActivatorToken(
                homeId, new ITuyaActivatorGetToken() {
                    @Override
                    public void onSuccess(String token) {

                        startNetworkConfig(token, gw);
                    }

                    @Override
                    public void onFailure(String errorCode, String errorMsg) {
                        setPbViewVisible(false);
                    }
                }
        );
    }

    // Start network configuration -- ZigBee Gateway
    private void startNetworkConfig(String token, HgwBean hgwBean) {
        ITuyaActivator activator = TuyaHomeSdk.getActivatorInstance().newGwActivator(
                new TuyaGwActivatorBuilder()
                        .setContext(DeviceConfigZbGatewayActivity.this)
                        .setTimeOut(100)
                        .setToken(token)
                        .setHgwBean(hgwBean)
                        .setListener(new ITuyaSmartActivatorListener() {
                            @Override
                            public void onError(String errorCode, String errorMsg) {
                                setPbViewVisible(false);
                                Toast.makeText(
                                        DeviceConfigZbGatewayActivity.this,
                                        "Activate Error" + errorMsg,
                                        Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onActiveSuccess(DeviceBean devResp) {
                                setPbViewVisible(false);
                                Toast.makeText(
                                        DeviceConfigZbGatewayActivity.this,
                                        "Activate success",
                                        Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            @Override
                            public void onStep(String step, Object data) {

                            }
                        })
        );
        activator.start();
    }

    private void setPbViewVisible(boolean isShow) {
        cpiLoading.setVisibility(isShow ? View.VISIBLE : View.GONE);
        bt_search.setEnabled(!isShow);
    }
}
