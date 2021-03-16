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
package com.tuya.appsdk.sample.device.config.ble;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.tuya.appsdk.sample.device.config.R;
import com.tuya.appsdk.sample.resource.HomeModel;
import com.tuya.smart.android.ble.api.BleConfigType;
import com.tuya.smart.android.ble.api.ITuyaBleConfigListener;
import com.tuya.smart.android.ble.api.ScanDeviceBean;
import com.tuya.smart.android.ble.api.ScanType;
import com.tuya.smart.android.ble.api.TyBleScanResponse;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.bean.DeviceBean;

/**
 * Device Configuration Ble Mode Sample
 *
 * @author yueguang <a href="mailto:developer@tuya.com"/>
 * @since 2021/3/3 10:29 AM
 */
public class DeviceConfigBleActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = "DeviceConfigBleActivity";
    int REQUEST_CODE = 1001;
    private MaterialToolbar topAppBar;
    private TextView tv_hint_info;
    private Button bt_search;
    private CircularProgressIndicator cpiLoading;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_config_info_hint_activity);
        initView();

        checkPermission();
    }

    // You need to check permissions before using Bluetooth devices
    private final void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_COARSE_LOCATION") != 0 || ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") != 0) {
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"}, 1001);
        }

    }

    private void initView() {
        topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        topAppBar.setTitle(R.string.device_config_ble_title);

        tv_hint_info = findViewById(R.id.tv_hint_info);
        tv_hint_info.setText(R.string.device_config_ble_hint);
        bt_search = findViewById(R.id.bt_search);
        cpiLoading = findViewById(R.id.cpiLoading);

        bt_search.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_search) {
            // Check Bluetooth is Opened
            if (!TuyaHomeSdk.getBleOperator().isBluetoothOpened()) {
                Toast.makeText(this,
                        "Please turn on bluetooth",
                        Toast.LENGTH_LONG).show();
            }
            scanSingleBleDevice();
        }
    }

    private void scanSingleBleDevice() {
        long currentHomeId = HomeModel.getCurrentHome(this);
        setPbViewVisible(true);

        // Scan Single Ble Device
        TuyaHomeSdk.getBleOperator().startLeScan(60 * 1000,
                ScanType.SINGLE, new TyBleScanResponse() {
                    @Override
                    public void onResult(ScanDeviceBean bean) {
                        Log.i(TAG, "scanSingleBleDevice: deviceUUID=${bean.uuid}");
                        // Start configuration -- Single Ble Device
                        if (bean.getConfigType() == BleConfigType.CONFIG_TYPE_SINGLE.getType()) {
                            TuyaHomeSdk.getBleManager().startBleConfig(currentHomeId, bean.getUuid(), null,
                                    new ITuyaBleConfigListener() {
                                        @Override
                                        public void onSuccess(DeviceBean bean) {
                                            setPbViewVisible(false);
                                            Toast.makeText(
                                                    DeviceConfigBleActivity.this,
                                                    "Config Success",
                                                    Toast.LENGTH_SHORT
                                            ).show();
                                            finish();
                                        }

                                        @Override
                                        public void onFail(String code, String msg, Object handle) {
                                            setPbViewVisible(false);
                                            Toast.makeText(
                                                    DeviceConfigBleActivity.this,
                                                    "Config error" + msg,
                                                    Toast.LENGTH_SHORT
                                            ).show();
                                        }
                                    });
                        }

                    }
                });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1001:
                if (grantResults.length != 0 && grantResults[0] == 0) {
                    Log.i("DeviceConfigBleActivity", "onRequestPermissionsResult: agree");
                } else {
                    this.finish();
                    Log.e("DeviceConfigBleActivity", "onRequestPermissionsResult: denied");
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + requestCode);
        }
    }

    private void setPbViewVisible(boolean isShow) {
        cpiLoading.setVisibility(isShow ? View.VISIBLE : View.GONE);
        bt_search.setEnabled(!isShow);
    }
}
