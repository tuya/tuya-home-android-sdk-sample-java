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

import com.tuya.appsdk.sample.device.mgt.R;
import com.tuya.smart.android.device.bean.SchemaBean;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Data point(DP) Fault type item
 *
 * @author chanfeng <a href="mailto:developer@tuya.com"/>
 * @since 2021/2/21 3:06 PM
 */

public class DpFaultItem extends FrameLayout {
    public DpFaultItem(Context context,
                       AttributeSet attrs,
                       int defStyle,
                       SchemaBean schemaBean,
                       String value) {
        super(context, attrs, defStyle);

        inflate(context, R.layout.device_mgt_item_dp_fault, this);


        TextView tvDpName = findViewById(R.id.tvDpName);
        tvDpName.setText(schemaBean.name);

        TextView tvFault = findViewById(R.id.tvFault);
        tvFault.setText(value);

    }

    // $FF: synthetic method
    public DpFaultItem(Context context, AttributeSet attrs, int defStyle, SchemaBean schemaBean, String value, int var6) {


        this(context, attrs, defStyle, schemaBean, value);
    }


    public DpFaultItem(@NotNull Context context, @Nullable AttributeSet attrs, @NotNull SchemaBean schemaBean, @NotNull String value) {
        this(context, attrs, 0, schemaBean, value, 4);
    }


    public DpFaultItem(@NotNull Context context, @NotNull SchemaBean schemaBean, @NotNull String value) {
        this(context, null, 0, schemaBean, value);
    }
}
