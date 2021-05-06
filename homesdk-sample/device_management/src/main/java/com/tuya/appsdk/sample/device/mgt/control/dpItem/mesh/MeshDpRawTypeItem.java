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

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.tuya.appsdk.sample.device.mgt.R;
import com.tuya.smart.android.blemesh.api.ITuyaBlueMeshDevice;
import com.tuya.smart.android.common.utils.HexUtil;
import com.tuya.smart.android.device.bean.SchemaBean;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.IResultCallback;

import java.util.HashMap;

/**
 * Data point(DP) Raw Type type item
 *
 * @author chuanfeng <a href="mailto:developer@tuya.com"/>
 * @since 2021/2/21 3:06 PM
 */
public class MeshDpRawTypeItem extends FrameLayout {
    private final String TAG = "MeshDpRawTypeItem";

    public MeshDpRawTypeItem(Context context,
                             AttributeSet attrs,
                             int defStyle,
                             final SchemaBean schemaBean,
                             String value,
                             String meshId,
                             boolean isGroup,
                             String localId,
                             String pcc) {
        super(context, attrs, defStyle);

        inflate(context, R.layout.device_mgt_item_dp_raw_type, this);

        TextView tvDpName = findViewById(R.id.tvDpName);
        tvDpName.setText(schemaBean.name);

        EditText etDp = findViewById(R.id.etDp);
        etDp.setText(value);

        if (schemaBean.mode.contains("w")) {
            // Data can be issued by the cloud.
            ITuyaBlueMeshDevice mTuyaSigMeshDevice= TuyaHomeSdk.newSigMeshDeviceInstance(meshId);
            etDp.setOnEditorActionListener((v, actionId, event) -> {

                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String rawValue = etDp.getText().toString();

                    if (checkRawValue(rawValue)) { //raw | file
                        HashMap map = new HashMap();
                        map.put(schemaBean.id, rawValue);
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
                        }else{
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
                    }
                    return true;
                }
                return false;
            });
        }
    }

    private Boolean checkRawValue(String rawValue) {
        return HexUtil.checkHexString(rawValue) && rawValue.length() % 2 == 0;
    }
}
