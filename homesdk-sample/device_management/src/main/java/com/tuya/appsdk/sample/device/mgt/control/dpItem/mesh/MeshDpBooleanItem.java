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

package com.tuya.appsdk.sample.device.mgt.control.dpItem.mesh;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.tuya.appsdk.sample.device.mgt.R;
import com.tuya.smart.android.blemesh.api.ITuyaBlueMeshDevice;
import com.tuya.smart.android.device.bean.SchemaBean;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.IResultCallback;

import java.util.HashMap;

/**
 * Data point(DP) Boolean type item
 *
 * @author chuanfeng <a href="mailto:developer@tuya.com"/>
 * @since 2021/2/21 3:06 PM
 *
 * <p>
 * The current class is used to issue dp (Boolean) directives to a mesh device or group.
 * </p>
 */

@SuppressLint("ViewConstructor")
public class MeshDpBooleanItem extends FrameLayout {

    private final String TAG = "MeshDpBooleanItem";

    /**
     * When the operating device is mesh, judge whether the device is a single device or a mesh group.
     * If it is a single device, then the value passed by isGroup is false and the value passed by localornodeid is nodeid.
     * If it is a mesh group, then the value passed by isGroup is true and the value passed by localornodeid is localid
     * @param context
     * @param attrs
     * @param defStyle
     * @param schemaBean
     * @param value
     * @param meshId
     * @param isGroup
     * @param localOrNodeId
     * @param pcc
     */
    @SuppressLint({"UseSwitchCompatOrMaterialCode", "ClickableViewAccessibility"})
    public MeshDpBooleanItem(Context context,
                             AttributeSet attrs,
                             int defStyle,
                             final SchemaBean schemaBean,
                             boolean value,
                             String meshId,
                             boolean isGroup,
                             String localOrNodeId,
                             String pcc) {
        super(context, attrs, defStyle);

        FrameLayout.inflate(context, R.layout.device_mgt_item_dp_boolean, this);
        TextView tvDpName = findViewById(R.id.tvDpName);
        tvDpName.setText(schemaBean.name);

        Switch swDp = findViewById(R.id.swDp);
        swDp.setChecked(value);

        if (schemaBean.mode.contains("w")) {
            // Data can be issued by the cloud.

            ITuyaBlueMeshDevice mTuyaSigMeshDevice= TuyaHomeSdk.newSigMeshDeviceInstance(meshId);

            swDp.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        HashMap map = new HashMap();
                        Boolean isChecked = !swDp.isChecked();
                        map.put(schemaBean.getId(), isChecked);
                        if (isGroup) {
                            mTuyaSigMeshDevice.multicastDps(localOrNodeId, pcc, JSONObject.toJSONString(map), new IResultCallback() {
                                @Override
                                public void onError(String code, String error) {
                                    Log.d(TAG, "send dps error:" + error);
                                }

                                @Override
                                public void onSuccess() {
                                    Log.d(TAG, "send dps success");
                                    swDp.setChecked(isChecked);
                                }
                            });
                        }else{
                            mTuyaSigMeshDevice.publishDps(localOrNodeId, pcc, JSONObject.toJSONString(map), new IResultCallback() {
                                @Override
                                public void onError(String s, String s1) {
                                    Log.d(TAG, "send dps error:" + s1);
                                }

                                @Override
                                public void onSuccess() {
                                    Log.d(TAG, "send dps success");
                                    swDp.setChecked(isChecked);
                                }
                            });
                        }

                    }
                    return true;
                }
            });
        }
    }
}
