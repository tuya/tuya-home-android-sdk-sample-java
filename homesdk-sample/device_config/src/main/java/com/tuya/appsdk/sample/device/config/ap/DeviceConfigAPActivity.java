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

package com.tuya.appsdk.sample.device.config.ap;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.thingclips.smart.activator.core.kit.ThingActivatorCoreKit;
import com.thingclips.smart.activator.core.kit.active.inter.IThingActiveManager;
import com.thingclips.smart.activator.core.kit.bean.ThingDeviceActiveErrorBean;
import com.thingclips.smart.activator.core.kit.bean.ThingDeviceActiveLimitBean;
import com.thingclips.smart.activator.core.kit.builder.ThingDeviceActiveBuilder;
import com.thingclips.smart.activator.core.kit.constant.ThingDeviceActiveModeEnum;
import com.thingclips.smart.activator.core.kit.devicecore.ThingActivatorDeviceCoreKit;
import com.thingclips.smart.activator.core.kit.listener.IThingDeviceActiveListener;
import com.tuya.appsdk.sample.device.config.R;
import com.tuya.appsdk.sample.device.config.ez.DeviceConfigEZActivity;
import com.tuya.appsdk.sample.resource.HomeModel;
import com.thingclips.smart.home.sdk.ThingHomeSdk;
import com.thingclips.smart.home.sdk.builder.ActivatorBuilder;
import com.thingclips.smart.sdk.api.IThingActivator;
import com.thingclips.smart.sdk.api.IThingActivatorGetToken;
import com.thingclips.smart.sdk.api.IThingSmartActivatorListener;
import com.thingclips.smart.sdk.bean.DeviceBean;
import com.thingclips.smart.sdk.enums.ActivatorModelEnum;

/**
 * Device Configuration AP Mode Sample
 *
 * @author chuanfeng <a href="mailto:developer@tuya.com"/>
 * @since 2021/2/18 9:50 AM
 */
public class DeviceConfigAPActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = "DeviceConfigAPActivity";

    public CircularProgressIndicator cpiLoading;
    public Button btnSearch;
    public Button btnStop;
    private TextView mContentTv;
    private String strSsid;
    private EditText etPassword;
    private String strPassword;
    private String mToken;

    private IThingActiveManager activeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_config_activity);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        toolbar.setTitle(getString(R.string.device_config_ap_title));

        cpiLoading = findViewById(R.id.cpiLoading);
        btnSearch = findViewById(R.id.btnSearch);
        btnStop = findViewById(R.id.btnStop);
        btnStop.setOnClickListener(this);
        btnSearch.setOnClickListener(this);
        mContentTv = findViewById(R.id.content_tv);
        mContentTv.setText(getString(R.string.device_config_ap_description));
    }

    @Override
    public void onClick(View v) {
        EditText etSsid = findViewById(R.id.etSsid);
        strSsid = etSsid.getText().toString();
        etPassword = findViewById(R.id.etPassword);
        strPassword = etPassword.getText().toString();

        if (v.getId() == R.id.btnSearch) {

            long currentHome = HomeModel.getCurrentHome(this);
            // Get Network Configuration Token
            ThingActivatorDeviceCoreKit.INSTANCE.getActivatorInstance().getActivatorToken(currentHome, new IThingActivatorGetToken() {
                @Override
                public void onSuccess(String token) {
                    // Start network configuration -- AP mode
                    Log.i(TAG, "token create success");
                    mToken = token;
                    onClickSetting();
                }


                @Override
                public void onFailure(String errorCode, String errorMsg) {

                }
            });
        } else if (v.getId() == R.id.btnStop) {
            stopActive();
        }

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //Show loading progress, disable btnSearch clickable
        btnSearch.setClickable(false);

        cpiLoading.setVisibility(View.VISIBLE);
        cpiLoading.setIndeterminate(true);

        activeManager = ThingActivatorCoreKit.INSTANCE.getActiveManager().newThingActiveManager();
        ThingDeviceActiveBuilder builder = new ThingDeviceActiveBuilder();
        builder.setActiveModel(ThingDeviceActiveModeEnum.AP);
        builder.setTimeOut(120);
        builder.setSsid(strSsid);
        builder.setPassword(strPassword);
        builder.setToken(mToken);
        builder.setRelationId(HomeModel.getCurrentHome(this));
        builder.setContext(this);
        builder.setListener(new IThingDeviceActiveListener() {

            @Override
            public void onFind(@NonNull String s) {
                Log.i(TAG, "onFind --- ");
            }

            @Override
            public void onBind(@NonNull String s) {
                Log.i(TAG, "onBind --- ");
            }

            @Override
            public void onActiveSuccess(@NonNull DeviceBean deviceBean) {
                cpiLoading.setVisibility(View.GONE);

                Log.i(TAG, "Activate success");
                Toast.makeText(DeviceConfigAPActivity.this,
                        "Activate success",
                        Toast.LENGTH_LONG
                ).show();

                finish();
            }

            @Override
            public void onActiveLimited(@NonNull ThingDeviceActiveLimitBean thingDeviceActiveLimitBean) {

            }

            @Override
            public void onActiveError(@NonNull ThingDeviceActiveErrorBean thingDeviceActiveErrorBean) {
                cpiLoading.setVisibility(View.GONE);
                btnSearch.setClickable(true);
                Toast.makeText(DeviceConfigAPActivity.this,
                        "Activate error-->" + thingDeviceActiveErrorBean.getErrMsg(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
        activeManager.startActive(builder);

    }

    private void stopActive() {
        if (activeManager != null) {
            activeManager.stopActive();
            activeManager = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Exit the page to destroy some cache data and monitoring data.
        stopActive();
    }

    /**
     * wifi setting
     */
    private void onClickSetting() {
        Intent wifiSettingsIntent = new Intent("android.settings.WIFI_SETTINGS");
        if (null != wifiSettingsIntent.resolveActivity(getPackageManager())) {
            startActivity(wifiSettingsIntent);
        } else {
            wifiSettingsIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
            if (null != wifiSettingsIntent.resolveActivity(getPackageManager())) {
                startActivity(wifiSettingsIntent);
            }
        }
    }
}