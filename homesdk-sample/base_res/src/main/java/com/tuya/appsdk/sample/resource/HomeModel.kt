/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2021 Tuya Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tuya.appsdk.sample.resource

import android.content.Context

/**
 * Home Model Cache
 *
 * @author qianqi <a href="mailto:developer@tuya.com"/>
 * @since 2021/1/20 3:01 PM
 */
enum class HomeModel {
    INSTANCE;

    companion object {
        const val CURRENT_HOME_ID = "currentHomeId"
    }

    /**
     * Set current home's homeId
     */
    fun setCurrentHome(context: Context, homeId: Long) {
        val sp = context.getSharedPreferences("HomeModel", Context.MODE_PRIVATE)
        val editor = sp.edit()

        editor.putLong(CURRENT_HOME_ID, homeId)
        editor.apply()
    }

    /**
     * Get current home's homeId
     */
    fun getCurrentHome(context: Context): Long {
        val sp = context.getSharedPreferences("HomeModel", Context.MODE_PRIVATE)
        return sp.getLong(CURRENT_HOME_ID, 0)
    }

    /**
     * check if current home set
     */
    fun checkHomeId(context: Context): Boolean {
        return HomeModel.INSTANCE.getCurrentHome(context) != 0L
    }

    fun clear(context: Context) {
        val sp = context.getSharedPreferences("HomeModel", Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.clear()
        editor.apply()
    }
}