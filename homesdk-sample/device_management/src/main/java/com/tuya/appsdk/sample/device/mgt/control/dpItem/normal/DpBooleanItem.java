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

package com.tuya.appsdk.sample.device.mgt.control.dpItem.normal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.tuya.appsdk.sample.device.mgt.R;
import com.tuya.smart.android.device.bean.SchemaBean;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.api.ITuyaDevice;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

/**
 * Data point(DP) Boolean type item
 *
 * @author chuanfeng <a href="mailto:developer@tuya.com"/>
 * @since 2021/2/21 3:06 PM
 *
 * <p>
 * The current class is used to issue dp (Boolean) directives to a single device.
 * Different constructors correspond to different types of action objects.
 * </p>
 */

@SuppressLint("ViewConstructor")
public class DpBooleanItem extends FrameLayout {

    private final String TAG = "MeshDpBooleanItem";

    /**
     * 当操作的是
     * @param context
     * @param attrs
     * @param defStyle
     * @param schemaBean
     * @param value
     * @param device
     */
    @SuppressLint({"UseSwitchCompatOrMaterialCode", "ClickableViewAccessibility"})
    public DpBooleanItem(Context context,
                         AttributeSet attrs,
                         int defStyle,
                         final SchemaBean schemaBean,
                         boolean value, final ITuyaDevice device) {
        super(context, attrs, defStyle);

        FrameLayout.inflate(context, R.layout.device_mgt_item_dp_boolean, this);

        TextView tvDpName = findViewById(R.id.tvDpName);
        tvDpName.setText(schemaBean.name);

        Switch swDp = findViewById(R.id.swDp);
        swDp.setChecked(value);

        if (schemaBean.mode.contains("w")) {
            // Data can be issued by the cloud.
            swDp.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    HashMap map = new HashMap();
                    Boolean isChecked = !swDp.isChecked();
                    map.put(schemaBean.getId(), isChecked);

                    device.publishDps(JSONObject.toJSONString(map), new IResultCallback() {
                        @Override
                        public void onError(String code, String error) {
                            Toast.makeText(context,
                                    "Activate error-->" + error,
                                    Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onSuccess() {
                            swDp.setChecked(isChecked);
                        }
                    });
                }
                return true;
            });
        }
    }

    // $FF: synthetic method
    public DpBooleanItem(Context context,
                         AttributeSet attrs,
                         int defStyle,
                         SchemaBean schemaBean,
                         boolean value,
                         ITuyaDevice device,
                         int var7) {
        this(context, attrs, defStyle, schemaBean, value, device);
    }


    public DpBooleanItem(@NotNull Context context, @Nullable AttributeSet attrs, @NotNull SchemaBean schemaBean, boolean value, @NotNull ITuyaDevice device) {
        this(context, attrs, 0, schemaBean, value, device, 4);
    }

    /**
     * This constructor is used when the operating device is a single point Bluetooth device
     * @param context
     * @param schemaBean
     * @param value
     * @param device
     */
    public DpBooleanItem(Context context,
                         SchemaBean schemaBean,
                         boolean value,
                         ITuyaDevice device) {
        this(context, null, 0, schemaBean, value, device);
    }
}
