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

package com.tuya.appsdk.sample.device.mgt.control.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.tuya.appsdk.sample.device.mgt.R;
import com.tuya.appsdk.sample.device.mgt.control.dpItem.mesh.MeshDpBooleanItem;
import com.tuya.appsdk.sample.device.mgt.control.dpItem.mesh.MeshDpCharTypeItem;
import com.tuya.appsdk.sample.device.mgt.control.dpItem.mesh.MeshDpEnumItem;
import com.tuya.appsdk.sample.device.mgt.control.dpItem.mesh.MeshDpIntegerItem;
import com.tuya.appsdk.sample.device.mgt.control.dpItem.normal.DpBooleanItem;
import com.tuya.appsdk.sample.device.mgt.control.dpItem.normal.DpCharTypeItem;
import com.tuya.appsdk.sample.device.mgt.control.dpItem.normal.DpEnumItem;
import com.tuya.appsdk.sample.device.mgt.control.dpItem.DpFaultItem;
import com.tuya.appsdk.sample.device.mgt.control.dpItem.normal.DpIntegerItem;
import com.tuya.appsdk.sample.device.mgt.control.dpItem.normal.DpRawTypeItem;
import com.tuya.smart.android.device.bean.BitmapSchemaBean;
import com.tuya.smart.android.device.bean.BoolSchemaBean;
import com.tuya.smart.android.device.bean.EnumSchemaBean;
import com.tuya.smart.android.device.bean.SchemaBean;
import com.tuya.smart.android.device.bean.StringSchemaBean;
import com.tuya.smart.android.device.bean.ValueSchemaBean;
import com.tuya.smart.android.device.enums.DataTypeEnum;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.api.ITuyaDevice;
import com.tuya.smart.sdk.bean.DeviceBean;

import java.util.Collection;
import java.util.Map;

/**
 * Device control sample
 *
 * @author chanfeng <a href="mailto:developer@tuya.com"/>
 * @since 2021/2/20 10:30 AM
 */

public class DeviceMgtControlActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_mgt_activity_control);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.group_control);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        LinearLayout llDp = findViewById(R.id.llDp);
        String deviceId = getIntent().getStringExtra("deviceId");

        ITuyaDevice mDevice = TuyaHomeSdk.newDeviceInstance(deviceId);
        DeviceBean deviceBean = TuyaHomeSdk.getDataInstance().getDeviceBean(deviceId);
        findViewById(R.id.btnReset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // device reset factory
                mDevice.resetFactory(new IResultCallback() {
                    @Override
                    public void onError(String errorCode, String errorMsg) {
                        Toast.makeText(DeviceMgtControlActivity.this,
                                "Activate error-->" + errorMsg,
                                Toast.LENGTH_LONG
                        ).show();
                    }

                    @Override
                    public void onSuccess() {
                        finish();
                    }
                });

            }
        });

        findViewById(R.id.btnRemove).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDevice.removeDevice(new IResultCallback() {
                    @Override
                    public void onError(String errorCode, String errorMsg) {
                        Toast.makeText(DeviceMgtControlActivity.this,
                                "Activate error-->" + errorMsg,
                                Toast.LENGTH_LONG
                        ).show();
                    }

                    @Override
                    public void onSuccess() {
                        finish();
                    }
                });
            }

        });

        TextView tvDeviceName = findViewById(R.id.tvDeviceName);
        tvDeviceName.setText(deviceBean.getName());


        Map<String, SchemaBean> map = TuyaHomeSdk.getDataInstance().getSchema(deviceId);
        Collection<SchemaBean> schemaBeans = map.values();

        for (SchemaBean bean : schemaBeans) {

            Object value = deviceBean.getDps().get(bean.getId());


            if (bean.type.equals(DataTypeEnum.OBJ.getType())) {
                // obj
                switch (bean.getSchemaType()) {
                    case BoolSchemaBean.type:
                        if (deviceBean.isSigMesh()){
                            MeshDpBooleanItem dpBooleanItem = new MeshDpBooleanItem(
                                    this, null, 0, bean, (Boolean) value,
                                    deviceBean.getMeshId(),
                                    false,
                                    deviceBean.getNodeId(),
                                    deviceBean.getCategory());
                            llDp.addView(dpBooleanItem);
                        }else {
                            DpBooleanItem dpBooleanItem = new DpBooleanItem(
                                    this,
                                    bean,
                                    (Boolean) value,
                                    mDevice);
                            llDp.addView(dpBooleanItem);
                        }
                        break;

                    case EnumSchemaBean.type:
                        if (deviceBean.isSigMesh()){
                            MeshDpEnumItem dpEnumItem = new MeshDpEnumItem(
                                    this, null, 0, bean, value.toString(),
                                    deviceBean.getMeshId(),
                                    false,
                                    deviceBean.getNodeId(),
                                    deviceBean.getCategory());
                            llDp.addView(dpEnumItem);
                        }else {
                            DpEnumItem dpEnumItem = new DpEnumItem(
                                    this,
                                    bean,
                                    value.toString(),
                                    mDevice);
                            llDp.addView(dpEnumItem);
                        }
                        break;

                    case StringSchemaBean.type:
                        if (deviceBean.isSigMesh()){
                            MeshDpCharTypeItem dpCharTypeItem = new MeshDpCharTypeItem(
                                    this, null, 0, bean,  (String)value,
                                    deviceBean.getMeshId(),
                                    false,
                                    deviceBean.getNodeId(),
                                    deviceBean.getCategory());
                            llDp.addView(dpCharTypeItem);
                        }else {
                            DpCharTypeItem dpCharTypeItem = new DpCharTypeItem(
                                    this,
                                    bean,
                                    (String) value,
                                    mDevice);
                            llDp.addView(dpCharTypeItem);
                        }
                        break;

                    case ValueSchemaBean.type:
                        if (deviceBean.isSigMesh()){
                            MeshDpIntegerItem dpIntegerItem = new MeshDpIntegerItem(
                                    this, null, 0, bean,  (int)value,
                                    deviceBean.getMeshId(),
                                    false,
                                    deviceBean.getNodeId(),
                                    deviceBean.getCategory());
                            llDp.addView(dpIntegerItem);
                        }else{
                            DpIntegerItem dpIntegerItem = new DpIntegerItem(
                                    this,
                                    bean,
                                    (int) value,
                                    mDevice);
                            llDp.addView(dpIntegerItem);
                        }

                        break;

                    case BitmapSchemaBean.type:
                        DpFaultItem dpFaultItem = new DpFaultItem(
                                this,
                                bean,
                                value.toString());
                        llDp.addView(dpFaultItem);
                }

            } else if (bean.type.equals(DataTypeEnum.RAW.getType())) {
                // raw | file
                if (value == null) {
                    value = "null";
                }
                DpRawTypeItem dpRawTypeItem = new DpRawTypeItem(
                        this,
                        bean,
                        value.toString(),
                        mDevice);
                llDp.addView(dpRawTypeItem);

            }
        }

    }
}

