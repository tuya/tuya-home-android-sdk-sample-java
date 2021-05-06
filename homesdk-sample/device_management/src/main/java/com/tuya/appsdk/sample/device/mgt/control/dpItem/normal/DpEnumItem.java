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

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListPopupWindow;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.tuya.appsdk.sample.device.mgt.R;
import com.tuya.smart.android.device.bean.EnumSchemaBean;
import com.tuya.smart.android.device.bean.SchemaBean;
import com.tuya.smart.home.sdk.utils.SchemaMapper;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.api.ITuyaDevice;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kotlin.collections.CollectionsKt;

/**
 * Data point(DP) Enum type item
 *
 * @author chaunfeng <a href="mailto:developer@tuya.com"/>
 * @since 2021/2/21 3:06 PM
 * <p>
 * The current class is used to issue dp (Boolean) directives to a single device .
 * </p>
 */
public class DpEnumItem extends FrameLayout {

    private final String TAG = "MeshDpBooleanItem";


    public DpEnumItem(Context context,
                      AttributeSet attrs,
                      int defStyle,
                      SchemaBean schemaBean,
                      String value,
                      ITuyaDevice device) {
        super(context, attrs, defStyle);
        inflate(context, R.layout.device_mgt_item_dp_enum, this);

        TextView tvDpName = findViewById(R.id.tvDpName);
        tvDpName.setText(schemaBean.name);

        Button btnDp = findViewById(R.id.btnDp);
        btnDp.setText(value);

        if (schemaBean.mode.contains("w")) {
            // Data can be issued by the cloud.
            ListPopupWindow listPopupWindow = new ListPopupWindow(context, null, R.attr.listPopupWindowStyle);
            listPopupWindow.setAnchorView(btnDp);

            EnumSchemaBean enumSchemaBean = SchemaMapper.toEnumSchema(schemaBean.property);
            Set set = enumSchemaBean.range;
            List items = CollectionsKt.toList(set);
            ArrayAdapter adapter = new ArrayAdapter(context, R.layout.device_mgt_item_dp_enum_popup_item, items);
            listPopupWindow.setAdapter(adapter);
            listPopupWindow.setOnItemClickListener((parent, view, position, id) -> {

                Map map = new HashMap();

                map.put(schemaBean.id, items.get(position));

                device.publishDps(JSONObject.toJSONString(map), new IResultCallback() {

                    @Override
                    public void onError(String code, String error) {
                        Log.e("MeshDpEnumItem", "$code --> $error");
                    }

                    @Override
                    public void onSuccess() {
                        btnDp.setText(
                                (CharSequence) items.get(position));
                    }
                });
                listPopupWindow.dismiss();


            });
            btnDp.setOnClickListener(v -> {
                listPopupWindow.show();
            });
        }
    }
    // $FF: synthetic method
    public DpEnumItem(Context context, AttributeSet attrs, int defStyle, SchemaBean schemaBean, String value, ITuyaDevice device, int var7) {

        this(context, attrs, defStyle, schemaBean, value, device);
    }


    public DpEnumItem(@NotNull Context context, @Nullable AttributeSet attrs, @NotNull SchemaBean schemaBean, @NotNull String value, @NotNull ITuyaDevice device) {
        this(context, attrs, 0, schemaBean, value, device, 4);
    }


    public DpEnumItem(@NotNull Context context, @NotNull SchemaBean schemaBean, @NotNull String value, @NotNull ITuyaDevice device) {
        this(context, null, 0, schemaBean, value, device);
    }
}
