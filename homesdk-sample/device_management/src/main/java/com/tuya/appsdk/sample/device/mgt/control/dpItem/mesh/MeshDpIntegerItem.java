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
import android.widget.FrameLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.google.android.material.slider.Slider;
import com.tuya.appsdk.sample.device.mgt.R;
import com.tuya.smart.android.blemesh.api.ITuyaBlueMeshDevice;
import com.tuya.smart.android.device.bean.SchemaBean;
import com.tuya.smart.android.device.bean.ValueSchemaBean;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.utils.SchemaMapper;
import com.tuya.smart.sdk.api.IResultCallback;

import java.util.HashMap;

/**
 * Data point(DP) Integer type item
 *
 * @author chuanfeng <a href="mailto:developer@tuya.com"/>
 * @since 2021/2/21 3:06 PM
 *
 * <p>
 * The current class is used to issue dp (Boolean) directives to a mesh device or group.
 * </p>
 */
@SuppressLint("ViewConstructor")
public class MeshDpIntegerItem extends FrameLayout {
    private final String TAG = "MeshDpIntegerItem";

    public MeshDpIntegerItem(Context context,
                             AttributeSet attrs,
                             int defStyle,
                             final SchemaBean schemaBean,
                             int value,
                             String meshId,
                             boolean isGroup,
                             String localId,
                             String pcc) {
        super(context, attrs, defStyle);

        inflate(context, R.layout.device_mgt_item_dp_integer, this);

        Slider slDp = findViewById(R.id.slDp);

        ValueSchemaBean valueSchemaBean = SchemaMapper.toValueSchema(schemaBean.property);
        double offset = 0;
        if (valueSchemaBean.min < 0){
            offset = 0 - valueSchemaBean.min;
        }
        double scale = Math.pow(10.0,valueSchemaBean.getScale());
        if (value > valueSchemaBean.max) {
            value = valueSchemaBean.max;
        }
        double curValue = ( value + offset ) / scale;
        double min = (valueSchemaBean.min + offset) / scale;
        double max = (valueSchemaBean.max + offset) / scale;
        slDp.setValue((float) curValue);

        slDp.setStepSize((float) ((double) valueSchemaBean.step / scale));
        slDp.setValueFrom((float) min);
        slDp.setValueTo((float) max);

        TextView tvDpName = findViewById(R.id.tvDpName);
        tvDpName.setText(schemaBean.getName());

        Log.i("zongwu.lin", ">>>>>>"
                + "\nvalueSchemaBean.getScale():" + valueSchemaBean.getScale()
                + "\nvalueSchemaBean.step:" + valueSchemaBean.step
                + "\nvalueSchemaBean.min:" + valueSchemaBean.min
                + "\nvalueSchemaBean.max:" + valueSchemaBean.max
                + "\nvalue:" + value
                + "\ncurValue:" + curValue
                + "\nmin:" + min
                + "\nmax:" + max
                + "\nscale:" + scale
        );
        if (schemaBean.mode.contains("w")) {
            // Data can be issued by the cloud.
            ITuyaBlueMeshDevice mTuyaSigMeshDevice= TuyaHomeSdk.newSigMeshDeviceInstance(meshId);
            double finalOffset = offset;
            slDp.addOnChangeListener((slider, sValue, fromUser) -> {
                HashMap map = new HashMap();
                map.put(schemaBean.id, (int) (((sValue * scale) - finalOffset) / valueSchemaBean.step));
                if (isGroup) {
                    mTuyaSigMeshDevice.multicastDps(localId, pcc, JSONObject.toJSONString(map), new IResultCallback() {
                        @Override
                        public void onError(String code, String error) {
                            Log.d(TAG, "send dps error:" + error);
                        }

                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "send dps success");
                        }
                    });
                }else {
                    mTuyaSigMeshDevice.publishDps(localId, pcc, JSONObject.toJSONString(map), new IResultCallback() {
                        @Override
                        public void onError(String code, String error) {
                            Log.d(TAG, "send dps error:" + error);
                        }

                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "send dps success");
                        }
                    });
                }
            });
        }
    }
}