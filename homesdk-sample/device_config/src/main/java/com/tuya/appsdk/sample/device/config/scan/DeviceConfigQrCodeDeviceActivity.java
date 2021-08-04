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
package com.tuya.appsdk.sample.device.config.scan;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.tuya.appsdk.sample.device.config.R;
import com.tuya.appsdk.sample.resource.HomeModel;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.builder.TuyaQRCodeActivatorBuilder;
import com.tuya.smart.sdk.api.ITuyaActivator;
import com.tuya.smart.sdk.api.ITuyaDataCallback;
import com.tuya.smart.sdk.api.ITuyaSmartActivatorListener;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Qr Code
 *
 * @author yueguang <a href="mailto:developer@tuya.com"/>
 * @since 2021/3/11 1:23 PM
 */
public class DeviceConfigQrCodeDeviceActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE_SCAN = 1;

    private MaterialToolbar topAppBar;
    private Button bt_search;
    private String mUuid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_config_info_hint_activity);
        initView();

    }

    private void initView() {
        topAppBar = (MaterialToolbar) findViewById(R.id.topAppBar);
        topAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        topAppBar.setTitle(getString(R.string.device_qr_code_service_title));
        bt_search = (Button) findViewById(R.id.bt_search);


        bt_search.setOnClickListener(this);
        bt_search.setText(R.string.device_qr_code_service_title);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_search) {
            startQrCode();
        }
    }

    private void startQrCode() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_SCAN);
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_SCAN);
            return;
        }

        Intent intent = new Intent(this, CaptureActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SCAN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SCAN) {

            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    String result = bundle.getString(CodeUtils.RESULT_STRING);
                    Toast.makeText(this, "result:" + result, Toast.LENGTH_LONG).show();
                    deviceQrCode(result);
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    Toast.makeText(this, "Failed to parse QR code", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void deviceQrCode(String result) {
        HashMap<String, Object> postData = new HashMap<>();
        postData.put("code", result);
        TuyaHomeSdk.getRequestInstance().requestWithApiNameWithoutSession(
                "tuya.m.qrcode.parse", "4.0", postData, String.class, new ITuyaDataCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        initQrCode(result);
                    }

                    @Override
                    public void onError(String errorCode, String errorMessage) {

                    }
                }
        );
    }

    private void initQrCode(String result) {
        long homeId = HomeModel.getCurrentHome(this);
        try {
            JSONObject obj = new JSONObject(result);
            JSONObject actionObj = obj.optJSONObject("actionData");
            if (null != actionObj) {
                mUuid = actionObj.optString("uuid");
                TuyaQRCodeActivatorBuilder builder = new TuyaQRCodeActivatorBuilder()
                        .setUuid(mUuid)
                        .setHomeId(homeId)
                        .setContext(this)
                        .setTimeOut(100)
                        .setListener(new ITuyaSmartActivatorListener() {
                            @Override
                            public void onError(String errorCode, String errorMsg) {

                            }

                            @Override
                            public void onActiveSuccess(DeviceBean devResp) {
                                Toast.makeText(DeviceConfigQrCodeDeviceActivity.this, "ActiveSuccess", Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onStep(String step, Object data) {

                            }
                        });
                ITuyaActivator iTuyaActivator = TuyaHomeSdk.getActivatorInstance().newQRCodeDevActivator(builder);
                iTuyaActivator.start();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}