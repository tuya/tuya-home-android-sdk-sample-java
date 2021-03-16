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

package com.tuya.appsdk.sample.main

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.tuya.appsdk.sample.R
import com.tuya.appsdk.sample.device.config.main.DeviceConfigFuncWidget
import com.tuya.appsdk.sample.device.mgt.main.DeviceMgtFuncWidget
import com.tuya.appsdk.sample.home.main.HomeFuncWidget
import com.tuya.appsdk.sample.resource.HomeModel
import com.tuya.appsdk.sample.user.info.UserInfoActivity
import com.tuya.appsdk.sample.user.main.UserFuncActivity
import com.tuya.smart.android.user.api.ILogoutCallback
import com.tuya.smart.home.sdk.TuyaHomeSdk

/**
 * Sample Main List Page
 *
 * @author qianqi <a href="mailto:developer@tuya.com"/>
 * @since 2021/1/8 5:41 PM
 */
class MainSampleListActivity : AppCompatActivity() {
    lateinit var homeFuncWidget: HomeFuncWidget

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity_sample_list)

        findViewById<TextView>(R.id.tvUserInfo).setOnClickListener {
            // User Info
            startActivity(Intent(this, UserInfoActivity::class.java))
        }

        findViewById<TextView>(R.id.tvLogout).setOnClickListener {
            // Logout
            TuyaHomeSdk.getUserInstance().logout(object : ILogoutCallback {
                override fun onSuccess() {
                    // Clear cache
                    HomeModel.INSTANCE.clear(this@MainSampleListActivity)

                    // Navigate to User Func Navigation Page
                    val intent = Intent(this@MainSampleListActivity, UserFuncActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }

                override fun onError(code: String?, error: String?) {

                }

            })
        }

        val llFunc: LinearLayout = findViewById(R.id.llFunc)

        // Home Management
        homeFuncWidget = HomeFuncWidget()
        llFunc.addView(homeFuncWidget.render(this))

        // Device Configuration Management
        val deviceConfigFucWidget = DeviceConfigFuncWidget()
        llFunc.addView(deviceConfigFucWidget.render(this))

        // Device Management
        val deviceMgtFuncWidget = DeviceMgtFuncWidget()
        llFunc.addView(deviceMgtFuncWidget.render(this))
    }

    override fun onResume() {
        super.onResume()
        homeFuncWidget.refresh()
    }
}