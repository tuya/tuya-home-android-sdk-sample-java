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

package com.tuya.appsdk.sample.device.mgt.list.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.thingclips.smart.sdk.api.IThingDevice;
import com.tuya.appsdk.sample.device.mgt.R;
import com.tuya.appsdk.sample.device.mgt.list.adapter.DeviceMgtAdapter;
import com.tuya.appsdk.sample.device.mgt.list.tag.DeviceListTypePage;
import com.tuya.appsdk.sample.resource.HomeModel;
import com.thingclips.smart.android.ble.builder.BleConnectBuilder;
import com.thingclips.smart.android.blemesh.api.IThingBlueMeshDevice;
import com.thingclips.smart.home.sdk.ThingHomeSdk;
import com.thingclips.smart.home.sdk.bean.HomeBean;
import com.thingclips.smart.home.sdk.callback.IThingHomeResultCallback;
import com.thingclips.smart.sdk.api.IDevListener;
import com.thingclips.smart.sdk.api.bluemesh.IMeshDevListener;
import com.thingclips.smart.sdk.bean.DeviceBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Device Management initial device data sample
 *
 * @author chuanfeng <a href="mailto:developer@tuya.com"/>
 * @since 2021/2/21 9:58 AM
 */
public class DeviceMgtListActivity extends AppCompatActivity {

    public DeviceMgtAdapter adapter;
    int type;
    private final HashMap<String, IThingDevice> iThingDeviceHashMap = new HashMap<>();
    private final HashMap<String, IThingBlueMeshDevice> iThingBlueMeshDeviceHashMap = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_mgt_activity_list);

        // Get Device List Type
        type = getIntent().getIntExtra("type", 0);


        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setTitle(type == 1 ? getString(R.string.device_mgt_list) :
                getString(R.string.device_zb_gateway_list));

        RecyclerView rvList = findViewById(R.id.rvList);
        rvList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        adapter = new DeviceMgtAdapter();
        rvList.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (Map.Entry<String, IThingDevice> entry : iThingDeviceHashMap.entrySet()) {
            entry.getValue().unRegisterDevListener();
            entry.getValue().onDestroy();
        }
        iThingDeviceHashMap.clear();

        for (Map.Entry<String, IThingBlueMeshDevice> entry : iThingBlueMeshDeviceHashMap.entrySet()) {
            entry.getValue().unRegisterMeshDevListener();
            entry.getValue().onDestroy();
        }
        iThingBlueMeshDeviceHashMap.clear();
    }

    IMeshDevListener iMeshDevListener = new IMeshDevListener() {

        @Override
        public void onDpUpdate(String nodeId, String dps, boolean isFromLocal) {
        }

        @Override
        public void onStatusChanged(List<String> online, List<String> offline, String gwId) {
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onNetworkStatusChanged(String devId, boolean status) {

        }

        @Override
        public void onRawDataUpdate(byte[] bytes) {

        }

        @Override
        public void onDevInfoUpdate(String devId) {
        }

        @Override
        public void onRemoved(String devId) {
        }
    };

    IDevListener iDevListener = new IDevListener() {
        @Override
        public void onDpUpdate(String devId, String dpStr) {

        }

        @Override
        public void onRemoved(String devId) {

        }

        @Override
        public void onStatusChanged(String devId, boolean online) {
            if (adapter != null && adapter.data != null && adapter.data.size() > 0) {
                for (DeviceBean item : adapter.data) {
                    if (item.getIsOnline() != online) {
                        item.setIsOnline(online);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        }

        @Override
        public void onNetworkStatusChanged(String devId, boolean status) {

        }

        @Override
        public void onDevInfoUpdate(String devId) {

        }
    };

    @Override
    public void onResume() {
        super.onResume();
        long homeId = HomeModel.getCurrentHome(this);
        /**
         * The device control must first initialize the data,
         * and call the following method to get the device information in the home.
         * initialization only need when the begin of app lifecycle and switch home.
         */
        ThingHomeSdk.newHomeInstance(homeId).getHomeDetail(new IThingHomeResultCallback() {
            @Override
            public void onSuccess(HomeBean homeBean) {

                if (type == DeviceListTypePage.NORMAL_DEVICE_LIST) {
                    ArrayList<DeviceBean> deviceList = (ArrayList) homeBean.getDeviceList();
                    if (deviceList != null && deviceList.size() > 0) {
                        List<BleConnectBuilder> builderList = new ArrayList<>();
                        for (DeviceBean deviceBean : deviceList) {
                            if (null == iThingDeviceHashMap.get(deviceBean.devId)) {
                                IThingDevice iThingDevice = ThingHomeSdk.newDeviceInstance(deviceBean.devId);
                                iThingDevice.registerDevListener(iDevListener);
                                iThingDeviceHashMap.put(deviceBean.devId, iThingDevice);
                            }
                            if (deviceBean.isBluetooth()) {
                                BleConnectBuilder builder = new BleConnectBuilder();
                                builder.setDevId(deviceBean.devId);
                                builderList.add(builder);
                            }
                        }
                        if (builderList.size() > 0) {
                            ThingHomeSdk.getBleManager().connectBleDevice(builderList);
                        }
                    }
                    if (homeBean.getSigMeshList() != null && homeBean.getSigMeshList().size() > 0) {
                        // Control sigmesh equipment,
                        // we need to call {@link ThingHomeSdk.GetThingSigMeshClient().#startClient(SigMeshBean SigMeshBean)}
                        // This method of correction is in {@link com.thingclips.Smart.SDK.API.Bluemesh.IMeshDevListener} inside,
                        // we need to go at the time of this callback to refresh the equipment status.
                        String meshId = homeBean.getSigMeshList().get(0).getMeshId();
                        if (iThingBlueMeshDeviceHashMap.get(meshId) == null) {
                            IThingBlueMeshDevice mThingSigMeshDevice = ThingHomeSdk.newSigMeshDeviceInstance(meshId);
                            mThingSigMeshDevice.registerMeshDevListener(iMeshDevListener);
                            iThingBlueMeshDeviceHashMap.put(meshId, mThingSigMeshDevice);
                            ThingHomeSdk.getThingSigMeshClient().startClient(ThingHomeSdk.getSigMeshInstance().getSigMeshList().get(0));
                        }
                    }
                    adapter.setData(deviceList, type);
                    adapter.notifyDataSetChanged();
                } else {

                    List<DeviceBean> deviceBeans = homeBean != null ? homeBean.getDeviceList() : null;
                    ArrayList deviceList = (ArrayList) deviceBeans;
                    Iterable iterable = (Iterable) deviceList;
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

                    adapter.setData((ArrayList<DeviceBean>) zigBeeGatewayList, type);

                    adapter.notifyDataSetChanged();

                }


            }

            @Override
            public void onError(String errorCode, String errorMsg) {

                Toast.makeText(DeviceMgtListActivity.this,
                        "Activate error-->" + errorMsg,
                        Toast.LENGTH_LONG
                ).show();

            }
        });
    }


}