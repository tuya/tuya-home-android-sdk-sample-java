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
package com.tuya.appsdk.sample.device.config.dual;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.tuya.appsdk.sample.device.config.R;
import com.tuya.appsdk.sample.resource.HomeModel;
import com.tuya.smart.android.ble.api.BleConfigType;
import com.tuya.smart.android.ble.api.ITuyaBleConfigListener;
import com.tuya.smart.android.ble.api.ScanDeviceBean;
import com.tuya.smart.android.ble.api.ScanType;
import com.tuya.smart.android.ble.api.TyBleScanResponse;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.ITuyaActivatorGetToken;
import com.tuya.smart.sdk.bean.DeviceBean;

import java.util.HashMap;

/**
 * Device Configuration Dual Mode Sample
 *
 * @author yueguang <a href="mailto:developer@tuya.com"/>
 * @since 2021/3/3 10:33 AM
 */
public class DeviceConfigDualActivity extends AppCompatActivity implements View.OnClickListener {

    String TAG = "DeviceConfigDualMode";
    int REQUEST_CODE = 1002;
    private MaterialToolbar topAppBar;
    private TextInputEditText etSsid;
    private TextInputEditText etPassword;
    private Button btnSearch;
    private CircularProgressIndicator cpiLoading;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_config_activity);
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
        topAppBar.setTitle(R.string.device_config_dual_title);
        etSsid = findViewById(R.id.etSsid);
        etPassword = findViewById(R.id.etPassword);
        btnSearch = findViewById(R.id.btnSearch);
        cpiLoading = findViewById(R.id.cpiLoading);

        btnSearch.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnSearch) {
            // validate
            String etSsidString = etSsid.getText().toString().trim();
            if (TextUtils.isEmpty(etSsidString)) {
                Toast.makeText(this, "etSsidString不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            String etPasswordString = etPassword.getText().toString().trim();
            if (TextUtils.isEmpty(etPasswordString)) {
                Toast.makeText(this, "etPasswordString不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!TuyaHomeSdk.getBleOperator().isBluetoothOpened()) {
                Toast.makeText(
                        this,
                        "Please turn on bluetooth",
                        Toast.LENGTH_LONG).show();
            }
            scanDualBleDevice();
        }
    }

    // Scan Ble Device
    private void scanDualBleDevice() {
        setPbViewVisible(true);
        long homeId = HomeModel.getCurrentHome(this);

        TuyaHomeSdk.getBleOperator().startLeScan(60 * 1000, ScanType.SINGLE, new TyBleScanResponse() {
            @Override
            public void onResult(ScanDeviceBean bean) {
                // Start configuration -- Dual Device
                if (bean.getConfigType() == BleConfigType.CONFIG_TYPE_WIFI.getType()) {
                    TuyaHomeSdk.getActivatorInstance().getActivatorToken(homeId,
                            new ITuyaActivatorGetToken() {
                                @Override
                                public void onSuccess(String token) {
                                    // Start configuration -- Dual Ble Device
                                    HashMap map = new HashMap();
                                    map.put("ssid", etSsid.toString().trim());
                                    map.put("password", etPassword.toString().trim());
                                    map.put("token", token);

                                    TuyaHomeSdk.getBleManager().startBleConfig(
                                            homeId, bean.getUuid(), map,
                                            new ITuyaBleConfigListener() {
                                                @Override
                                                public void onSuccess(DeviceBean bean) {
                                                    setPbViewVisible(false);
                                                    Toast.makeText(
                                                            DeviceConfigDualActivity.this,
                                                            "Config success",
                                                            Toast.LENGTH_SHORT
                                                    ).show();
                                                    finish();
                                                }

                                                @Override
                                                public void onFail(String code, String msg, Object handle) {
                                                    Toast.makeText(
                                                            DeviceConfigDualActivity.this,
                                                            "Config error" + msg,
                                                            Toast.LENGTH_SHORT
                                                    ).show();
                                                }
                                            }
                                    );
                                }

                                @Override
                                public void onFailure(String errorCode, String errorMsg) {
                                    setPbViewVisible(false);
                                    Toast.makeText(
                                            DeviceConfigDualActivity.this,
                                            "error" + errorMsg,
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
    }

    private void setPbViewVisible(boolean isShow) {
        cpiLoading.setVisibility(isShow ? View.VISIBLE : View.GONE);
        btnSearch.setEnabled(!isShow);
    }

}

