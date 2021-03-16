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
package com.tuya.appsdk.sample.device.config.qrcode

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import com.tuya.appsdk.sample.device.config.R
import com.tuya.appsdk.sample.resource.HomeModel
import com.tuya.smart.home.sdk.TuyaHomeSdk
import com.tuya.smart.home.sdk.builder.TuyaQRCodeActivatorBuilder
import com.tuya.smart.sdk.api.ITuyaDataCallback
import com.tuya.smart.sdk.api.ITuyaSmartActivatorListener
import com.tuya.smart.sdk.bean.DeviceBean
import com.uuzuche.lib_zxing.activity.CaptureActivity
import com.uuzuche.lib_zxing.activity.CodeUtils
import java.util.*

/**
 * Qr Code
 *
 * @author yueguang [](mailto:developer@tuya.com)
 * @since 2021/3/11 3:13 PM
 */
class DeviceConfigQrCodeDeviceActivity : AppCompatActivity(), View.OnClickListener {
    private var topAppBar: MaterialToolbar? = null
    private var btSearch: Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.device_config_info_hint_activity)
        initView()
    }

    private fun initView() {
        topAppBar = findViewById<View>(R.id.topAppBar) as MaterialToolbar
        topAppBar!!.setNavigationOnClickListener { finish() }
        topAppBar!!.title = getString(R.string.device_qr_code_service_title)
        btSearch = findViewById<View>(R.id.bt_search) as Button
        btSearch!!.setOnClickListener(this)
        btSearch!!.setText(R.string.device_qr_code_service_title)
    }

    override fun onClick(v: View) {
        if (v.id == R.id.bt_search) {
            startQrCode()
        }
    }

    private fun startQrCode() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CODE_SCAN
            )
            return
        }
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_CODE_SCAN
            )
            return
        }
        val intent = Intent(this, CaptureActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_SCAN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SCAN) {
            if (null != data) {
                val bundle = data.extras ?: return
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    val result = bundle.getString(CodeUtils.RESULT_STRING)
                    Toast.makeText(this, "result:$result", Toast.LENGTH_LONG).show()
                    deviceQrCode(result)
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    Toast.makeText(this, "Failed to parse QR code", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun deviceQrCode(result: String?) {
        val postData = HashMap<String, Any?>()
        postData["code"] = result
        TuyaHomeSdk.getRequestInstance().requestWithApiNameWithoutSession(
            "tuya.m.qrcode.parse",
            "4.0",
            postData,
            String::class.java,
            object : ITuyaDataCallback<String> {
                override fun onSuccess(result: String) {
                    initQrCode(result)
                }

                override fun onError(errorCode: String, errorMessage: String) {}
            }
        )
    }

    private fun initQrCode(result: String) {
        val homeId = HomeModel.INSTANCE.getCurrentHome(this)
        val builder = TuyaQRCodeActivatorBuilder()
            .setUuid(result)
            .setHomeId(homeId)
            .setContext(this)
            .setTimeOut(100)
            .setListener(object : ITuyaSmartActivatorListener {
                override fun onError(errorCode: String, errorMsg: String) {}
                override fun onActiveSuccess(devResp: DeviceBean) {
                    Toast.makeText(
                        this@DeviceConfigQrCodeDeviceActivity,
                        "ActiveSuccess",
                        Toast.LENGTH_LONG
                    ).show()
                }

                override fun onStep(step: String, data: Any) {}
            })
        val iTuyaActivator = TuyaHomeSdk.getActivatorInstance().newQRCodeDevActivator(builder)
        iTuyaActivator.start()
    }

    companion object {
        private const val REQUEST_CODE_SCAN = 1
    }
}