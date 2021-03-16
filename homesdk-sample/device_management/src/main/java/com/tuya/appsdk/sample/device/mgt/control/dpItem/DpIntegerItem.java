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

package com.tuya.appsdk.sample.device.mgt.control.dpItem;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.google.android.material.slider.Slider;
import com.tuya.appsdk.sample.device.mgt.R;
import com.tuya.smart.android.device.bean.SchemaBean;
import com.tuya.smart.android.device.bean.ValueSchemaBean;
import com.tuya.smart.home.sdk.utils.SchemaMapper;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.api.ITuyaDevice;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

import kotlin.jvm.JvmOverloads;

/**
 * Data point(DP) Integer type item
 *
 * @author chuanfeng <a href="mailto:developer@tuya.com"/>
 * @since 2021/2/21 3:06 PM
 */
public class DpIntegerItem extends FrameLayout {
    public DpIntegerItem(Context context,
                         AttributeSet attrs,
                         int defStyle,
                         final SchemaBean schemaBean,
                         int value,
                         final ITuyaDevice device) {
        super(context, attrs, defStyle);

        inflate(context, R.layout.device_mgt_item_dp_integer, this);

        Slider slDp = findViewById(R.id.slDp);

        ValueSchemaBean valueSchemaBean = SchemaMapper.toValueSchema(schemaBean.property);

        Double scale = Math.pow(10.0, valueSchemaBean.getScale());

        Double curValue = (float) (value * valueSchemaBean.step + valueSchemaBean.min) / scale;

        if (curValue > valueSchemaBean.max) {
            curValue = (double) valueSchemaBean.max;
        }

        slDp.setValue((float) (double) curValue);

        slDp.setStepSize((float) ((double) valueSchemaBean.step / scale));
        slDp.setValueFrom((float) valueSchemaBean.min);
        slDp.setValueTo((float) valueSchemaBean.max);

        TextView tvDpName = findViewById(R.id.tvDpName);
        tvDpName.setText(schemaBean.getName());

        if (schemaBean.mode.contains("w")) {
            // Data can be issued by the cloud.
            slDp.addOnChangeListener((slider, sValue, fromUser) -> {
                HashMap map = new HashMap();
                map.put(schemaBean.id, (int) (((sValue * scale) - valueSchemaBean.min) / valueSchemaBean.step));

                device.publishDps(JSONObject.toJSONString(map), new IResultCallback() {

                    @Override
                    public void onError(String code, String error) {

                    }

                    @Override
                    public void onSuccess() {

                    }
                });
            });
        }
    }

    // $FF: synthetic method
    public DpIntegerItem(Context context, AttributeSet attrs, int defStyle, SchemaBean schemaBean, int value, ITuyaDevice device, int var7) {

        this(context, attrs, defStyle, schemaBean, value, device);
    }

    @JvmOverloads
    public DpIntegerItem(@NotNull Context context, @Nullable AttributeSet attrs, @NotNull SchemaBean schemaBean, int value, @NotNull ITuyaDevice device) {
        this(context, attrs, 0, schemaBean, value, device, 4);
    }

    @JvmOverloads
    public DpIntegerItem(@NotNull Context context, @NotNull SchemaBean schemaBean, int value, @NotNull ITuyaDevice device) {
        this(context, null, 0, schemaBean, value, device);
    }

}