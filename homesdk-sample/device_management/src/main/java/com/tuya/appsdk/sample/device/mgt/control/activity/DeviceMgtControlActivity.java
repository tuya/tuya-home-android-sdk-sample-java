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
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.alibaba.fastjson.JSONObject;
import com.tuya.appsdk.sample.device.mgt.R;
import com.tuya.appsdk.sample.device.mgt.control.dpItem.link.LinkManager;
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
import com.tuya.sdk.os.TuyaOSDevice;
import com.tuya.smart.android.device.bean.BitmapSchemaBean;
import com.tuya.smart.android.device.bean.BoolSchemaBean;
import com.tuya.smart.android.device.bean.EnumSchemaBean;
import com.tuya.smart.android.device.bean.SchemaBean;
import com.tuya.smart.android.device.bean.StringSchemaBean;
import com.tuya.smart.android.device.bean.ValueSchemaBean;
import com.tuya.smart.android.device.enums.DataTypeEnum;
import com.tuya.smart.android.device.enums.TuyaSmartThingMessageType;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.IDevListener;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.api.ITuyaDataCallback;
import com.tuya.smart.sdk.api.ITuyaDevice;
import com.tuya.smart.sdk.api.ITuyaLinkDeviceListener;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.bean.TuyaSmartThingAction;
import com.tuya.smart.sdk.bean.TuyaSmartThingEvent;
import com.tuya.smart.sdk.bean.TuyaSmartThingModel;
import com.tuya.smart.sdk.bean.TuyaSmartThingProperty;
import com.tuya.smart.sdk.bean.TuyaSmartThingServiceModel;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Device control sample
 *
 * @author chanfeng <a href="mailto:developer@tuya.com"/>
 * @since 2021/2/20 10:30 AM
 */

public class DeviceMgtControlActivity extends AppCompatActivity {

    private static final String TAG = "DeviceMgtControl";
    private String mDeviceId;
    private LinearLayout mContainer;
    private ITuyaDevice mDevice;

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


        mContainer = findViewById(R.id.llDp);
        mDeviceId = getIntent().getStringExtra("deviceId");

        mDevice = TuyaHomeSdk.newDeviceInstance(mDeviceId);
        DeviceBean deviceBean = TuyaHomeSdk.getDataInstance().getDeviceBean(mDeviceId);
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

        if (deviceBean != null && deviceBean.isSupportThingModelDevice()) {
            //TuyaLink设备
            requestThineModel(deviceBean);
        } else {
            Map<String, SchemaBean> map = TuyaHomeSdk.getDataInstance().getSchema(mDeviceId);
            Collection<SchemaBean> schemaBeans = map.values();

            for (SchemaBean bean : schemaBeans) {

                Object value = deviceBean.getDps().get(bean.getId());


                if (bean.type.equals(DataTypeEnum.OBJ.getType())) {
                    // obj
                    switch (bean.getSchemaType()) {
                        case BoolSchemaBean.type:
                            if (deviceBean.isSigMesh()) {
                                MeshDpBooleanItem dpBooleanItem = new MeshDpBooleanItem(
                                        this, null, 0, bean, (Boolean) value,
                                        deviceBean.getMeshId(),
                                        false,
                                        deviceBean.getNodeId(),
                                        deviceBean.getCategory());
                                mContainer.addView(dpBooleanItem);
                            } else {
                                DpBooleanItem dpBooleanItem = new DpBooleanItem(
                                        this,
                                        bean,
                                        (Boolean) value,
                                        mDevice);
                                mContainer.addView(dpBooleanItem);
                            }
                            break;

                        case EnumSchemaBean.type:
                            if (deviceBean.isSigMesh()) {
                                MeshDpEnumItem dpEnumItem = new MeshDpEnumItem(
                                        this, null, 0, bean, value.toString(),
                                        deviceBean.getMeshId(),
                                        false,
                                        deviceBean.getNodeId(),
                                        deviceBean.getCategory());
                                mContainer.addView(dpEnumItem);
                            } else {
                                DpEnumItem dpEnumItem = new DpEnumItem(
                                        this,
                                        bean,
                                        value.toString(),
                                        mDevice);
                                mContainer.addView(dpEnumItem);
                            }
                            break;

                        case StringSchemaBean.type:
                            if (deviceBean.isSigMesh()) {
                                MeshDpCharTypeItem dpCharTypeItem = new MeshDpCharTypeItem(
                                        this, null, 0, bean, (String) value,
                                        deviceBean.getMeshId(),
                                        false,
                                        deviceBean.getNodeId(),
                                        deviceBean.getCategory());
                                mContainer.addView(dpCharTypeItem);
                            } else {
                                DpCharTypeItem dpCharTypeItem = new DpCharTypeItem(
                                        this,
                                        bean,
                                        (String) value,
                                        mDevice);
                                mContainer.addView(dpCharTypeItem);
                            }
                            break;

                        case ValueSchemaBean.type:
                            if (deviceBean.isSigMesh()) {
                                MeshDpIntegerItem dpIntegerItem = new MeshDpIntegerItem(
                                        this, null, 0, bean, (int) value,
                                        deviceBean.getMeshId(),
                                        false,
                                        deviceBean.getNodeId(),
                                        deviceBean.getCategory());
                                mContainer.addView(dpIntegerItem);
                            } else {
                                DpIntegerItem dpIntegerItem = new DpIntegerItem(
                                        this,
                                        bean,
                                        (int) value,
                                        mDevice);
                                mContainer.addView(dpIntegerItem);
                            }

                            break;

                        case BitmapSchemaBean.type:
                            DpFaultItem dpFaultItem = new DpFaultItem(
                                    this,
                                    bean,
                                    value.toString());
                            mContainer.addView(dpFaultItem);
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
                    mContainer.addView(dpRawTypeItem);

                }
            }
        }

    }

    private void requestThineModel(DeviceBean deviceBean) {
        TuyaOSDevice.getDeviceOperator().getThingModelWithProductId(deviceBean.productId,
                new ITuyaDataCallback<TuyaSmartThingModel>() {
                    @Override
                    public void onSuccess(TuyaSmartThingModel result) {
                        Log.i(TAG, "requestThineModel onSuccess");
                        renderTuyaLinkView(result);
                    }

                    @Override
                    public void onError(String errorCode, String errorMessage) {
                        Log.i(TAG, "requestThineModel onError errorCode -> " + errorCode + " / errorMessage -> " + errorMessage);
                    }
                });
    }

    private void renderTuyaLinkView(TuyaSmartThingModel model) {
//        DeviceBean deviceBean = TuyaHomeSdk.getDataInstance().getDeviceBean(mDeviceId);
//        TuyaSmartThingModel thingModel = deviceBean.getThingModel();
        if (model == null) {
            return;
        }
        for(TuyaSmartThingServiceModel service: model.getServices()){
            renderProperties(service.getProperties());
            renderActions(service.getActions());
            //事件不支持执行，动作只支持设备上报
            //renderEvents(service.getEvents());
        }

        //注册tuyalink监听
        registerTuyaLinkListener();
    }

    //渲染属性到界面上，动作、事件同理
    private void renderProperties(List<TuyaSmartThingProperty> properties){
        if(properties == null || properties.isEmpty()){
            Log.i(TAG,"properties is null");
            return;
        }
        DeviceBean deviceBean = TuyaHomeSdk.getDataInstance().getDeviceBean(mDeviceId);
        for(TuyaSmartThingProperty property : properties) {
            //属性才能从DeviceBean模型中找到对应的dp值
            Object value = deviceBean.getDps().get(String.valueOf(property.getAbilityId()));

            View item = LinkManager.getInstance().createPropertyView(this, value,property, mDevice);
            if(item == null){
                continue;
            }
            mContainer.addView(item);
        }
    }

    private void renderActions(List<TuyaSmartThingAction> actions){
        if(actions == null || actions.isEmpty()){
            Log.i(TAG,"actions is null");
            return;
        }
        for(TuyaSmartThingAction action : actions) {
            View item = LinkManager.getInstance().createActionView(this, action, mDevice);
            if(item == null){
                continue;
            }
            mContainer.addView(item);
        }
    }

    private void renderEvents(List<TuyaSmartThingEvent> events){
        if(events == null || events.isEmpty()){
            Log.i(TAG,"events is null");
            return;
        }
        for(TuyaSmartThingEvent event : events) {
            View item = LinkManager.getInstance().createEventView(this, event, mDevice);
            if(item == null){
                continue;
            }
            mContainer.addView(item);
        }
    }

    private void registerTuyaLinkListener(){
        mDevice.registerTuyaLinkMessageListener(new ITuyaLinkDeviceListener() {
            @Override
            public void onReceiveThingMessage(TuyaSmartThingMessageType messageType, Map<String, Object> payload) {
                Log.i(TAG,"messageType = " + messageType.name() );
                Log.i(TAG,"message = " + JSONObject.toJSONString(payload));
            }
        });
        mDevice.registerDevListener(new IDevListener() {
            @Override
            public void onDpUpdate(String devId, String dpStr) {
                //只有属性才会转换成dp回调
                Log.i(TAG,"devId = " + devId);
                Log.i(TAG,"dpStr = " + dpStr);
            }

            @Override
            public void onRemoved(String devId) {

            }

            @Override
            public void onStatusChanged(String devId, boolean online) {

            }

            @Override
            public void onNetworkStatusChanged(String devId, boolean status) {

            }

            @Override
            public void onDevInfoUpdate(String devId) {

            }
        });
    }
}

