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
package com.tuya.appsdk.sample.home.edit

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.appbar.MaterialToolbar
import com.tuya.appsdk.sample.user.R
import com.tuya.smart.home.sdk.TuyaHomeSdk
import com.tuya.smart.home.sdk.bean.HomeBean
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback
import com.tuya.smart.sdk.api.IResultCallback

/**
 * Home Edit Sample
 *
 * @author yueguang [](mailto:developer@tuya.com)
 * @since 2021/1/18 6:13 PM
 */
class HomeEditActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity_new_home)

        val toolbar: Toolbar = findViewById<View>(R.id.topAppBar) as Toolbar
        toolbar.setNavigationOnClickListener {
            finish()
        }

        findViewById<MaterialToolbar>(R.id.topAppBar).title = getString(R.string.home_edit_title)
        findViewById<Button>(R.id.btnDone).text = getString(R.string.home_done)

        val homeId = intent.getLongExtra("homeId", 0)

        // Get home info
        TuyaHomeSdk.newHomeInstance(homeId).getHomeDetail(object : ITuyaHomeResultCallback {
            override fun onSuccess(bean: HomeBean?) {
                bean?.let {
                    findViewById<EditText>(R.id.etHomeName).setText(bean.name)
                    findViewById<EditText>(R.id.etCity).setText(bean.geoName)
                }

            }

            override fun onError(errorCode: String?, errorMsg: String?) {

            }

        })

        // Update home info
        findViewById<Button>(R.id.btnDone).setOnClickListener {

            val strHomeName = findViewById<EditText>(R.id.etHomeName).text.toString()
            val strCity = findViewById<EditText>(R.id.etCity).text.toString()
            TuyaHomeSdk.newHomeInstance(homeId).updateHome(
                    strHomeName,
                    // Get location by yourself, here just sample as Shanghai's location
                    120.52,
                    30.40,
                    strCity,
                    arrayListOf(),
                    false,
                    object : IResultCallback {
                        override fun onSuccess() {
                            Toast.makeText(
                                    this@HomeEditActivity,
                                    "Update success",
                                    Toast.LENGTH_LONG
                            ).show()
                        }

                        override fun onError(code: String?, error: String?) {

                        }
                    }
            )
        }
    }
}