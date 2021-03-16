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

package com.tuya.appsdk.sample.resource;

import android.content.Context;
import android.content.SharedPreferences;

import org.jetbrains.annotations.NotNull;

import kotlin.jvm.internal.Intrinsics;

/**
 * Home Model Cache
 *
 * @author chuanfeng <a href="mailto:developer@tuya.com"/>
 * @since 2021/2/18 9:31 AM
 */
public enum HomeModel {
    INSTANCE;

    public static final String CURRENT_HOME_ID = "currentHomeId";

    /**
     * Set current home's homeId
     */
    public final void setCurrentHome(Context context, long homeId) {
        SharedPreferences sp = context.getSharedPreferences("HomeModel", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        editor.putLong(CURRENT_HOME_ID, homeId);
        editor.apply();
    }

    /**
     * Get current home's homeId
     */
    public static final long getCurrentHome(Context context) {
        SharedPreferences sp = context.getSharedPreferences("HomeModel", Context.MODE_PRIVATE);
        return sp.getLong(CURRENT_HOME_ID, 0);
    }

    /**
     * check if current home set
     */
    public final boolean checkHomeId(Context context) {
        return getCurrentHome(context) != 0L;
    }

    public final void clear(Context context) {
        SharedPreferences sp = context.getSharedPreferences("HomeModel", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(CURRENT_HOME_ID);
        editor.apply();
    }
}
