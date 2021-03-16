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

package com.tuya.appsdk.sample.user.info

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.ListPopupWindow
import androidx.appcompat.widget.Toolbar
import com.tuya.appsdk.sample.R
import com.tuya.appsdk.sample.resource.HomeModel
import com.tuya.appsdk.sample.user.main.UserFuncActivity
import com.tuya.smart.home.sdk.TuyaHomeSdk
import com.tuya.smart.sdk.TuyaSdk
import com.tuya.smart.sdk.api.IResultCallback
import com.tuya.smart.sdk.enums.TempUnitEnum
import java.time.ZoneId


/**
 * User Info Example
 *
 * @author qianqi <a href="mailto:developer@tuya.com"/>
 * @since 2021/1/5 5:13 PM
 */
class UserInfoActivity : AppCompatActivity() {
    lateinit var lat: String
    lateinit var lon: String
    lateinit var items: List<String>

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_activity_info)

        val toolbar: Toolbar = findViewById<View>(R.id.topAppBar) as Toolbar
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val user = TuyaHomeSdk.getUserInstance().user

        findViewById<TextView>(R.id.tvName).text = user?.nickName
        findViewById<TextView>(R.id.tvPhone).text = user?.mobile
        findViewById<TextView>(R.id.tvEmail).text = user?.email
        findViewById<TextView>(R.id.tvCountryCode).text = user?.phoneCode

        val tempUnit = findViewById<Button>(R.id.Temperature)
        tempUnit.text = if (user?.tempUnit == 1) "°C" else "°F"

        tempUnit.setOnClickListener {
            val listPopupWindow = ListPopupWindow(
                    this,
                    null,
                    com.tuya.appsdk.sample.device.mgt.R.attr.listPopupWindowStyle
            )
            listPopupWindow.anchorView = tempUnit


            val items = listOf("°C", "°F")
            val adapter = ArrayAdapter(this, R.layout.device_mgt_item_dp_enum_popup_item, items)
            listPopupWindow.setAdapter(adapter)

            listPopupWindow.setOnItemClickListener { parent, view, position, id ->
                TuyaHomeSdk.getUserInstance().setTempUnit(
                        if (items[position] == "°C") TempUnitEnum.Celsius else TempUnitEnum.Fahrenheit, object : IResultCallback {

                    override fun onSuccess() {
                        tempUnit.text = items[position]
                    }

                    override fun onError(code: String?, error: String?) {
                        Toast.makeText(
                                this@UserInfoActivity,
                                " error->$error",
                                Toast.LENGTH_LONG
                        ).show()
                    }
                })
                listPopupWindow.dismiss()
            }
            listPopupWindow.show()
        }

        findViewById<Button>(R.id.Updata).setOnClickListener {


            val country = arrayOf(
                    this.getString(R.string.user_country_China),
                    this.getString(R.string.user_country_America),
                    this.getString(R.string.user_country_English),
                    this.getString(R.string.user_country_Australia),
                    this.getString(R.string.user_country_Japan),
                    this.getString(R.string.user_country_Egypt)
            )


            val builder = AlertDialog.Builder(this)
            builder.setItems(
                    country,
                    DialogInterface.OnClickListener { dialog, which ->
                        when (which) {
                            1 -> {
                                lat = "116.20"
                                lon = "39.55"
                            }
                            2 -> {
                                lat = "-77.02"
                                lon = "39.91"
                            }
                            3 -> {
                                lat = "-0.05"
                                lon = "51.36"
                            }
                            4 -> {
                                lat = "139.46"
                                lon = "35.42"
                            }
                            5 -> {
                                lat = "31.14"
                                lon = "30.01"
                            }
                        }

                        TuyaSdk.setLatAndLong(lat, lon)

                        Toast.makeText(
                                this@UserInfoActivity,
                                "success",
                                Toast.LENGTH_LONG
                        ).show()
                    })
            builder.create().show()

        }

        findViewById<Button>(R.id.deactive).setOnClickListener {
            TuyaHomeSdk.getUserInstance().cancelAccount(
                    object : IResultCallback {
                        override fun onSuccess() {
                            // Clear cache
                            HomeModel.INSTANCE.clear(this@UserInfoActivity)

                            // Navigate to User Func Navigation Page
                            val intent = Intent(this@UserInfoActivity, UserFuncActivity::class.java)
                            intent.flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)

                        }

                        override fun onError(code: String?, error: String?) {
                            Toast.makeText(
                                    this@UserInfoActivity,
                                    " error->$error",
                                    Toast.LENGTH_LONG
                            ).show()
                        }
                    })
        }

        val btTimeZone = findViewById<Button>(R.id.btTimeZone)
        btTimeZone.text = user?.timezoneId

        // Data can be issued by the cloud.
        val listPopupWindow = ListPopupWindow(this, null, R.attr.listPopupWindowStyle)
        listPopupWindow.anchorView = btTimeZone
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val availableZoneIds = ZoneId.getAvailableZoneIds()
            items = availableZoneIds.toList()
        } else {
            items = arrayOf(
                    this.getString(R.string.user_time_America),
                    this.getString(R.string.user_time_Asia), this.getString(R.string.user_time_Etc)

            ).toList()
        }

        val arrayAdapter: ArrayAdapter<*> =
                ArrayAdapter<Any?>(this, R.layout.user_activity_item_time_item, items)
        listPopupWindow.setAdapter(arrayAdapter)
        listPopupWindow.setOnItemClickListener { parent, view, position, id ->
            val timezoneId = items[position]
            TuyaHomeSdk.getUserInstance().updateTimeZone(
                    timezoneId,
                    object : IResultCallback {
                        override fun onSuccess() {
                            Toast.makeText(
                                    this@UserInfoActivity,
                                    "success",
                                    Toast.LENGTH_SHORT
                            ).show()
                            btTimeZone.text = items[position]
                        }

                        override fun onError(code: String, error: String) {
                            Toast.makeText(
                                    this@UserInfoActivity,
                                    "error$error$timezoneId",
                                    Toast.LENGTH_LONG
                            ).show()
                        }
                    })
            listPopupWindow.dismiss()
        }
        btTimeZone.setOnClickListener { listPopupWindow.show() }

    }
}