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

package com.tuya.appsdk.sample.user.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.tuya.appsdk.sample.R
import com.tuya.appsdk.sample.main.MainSampleListActivity
import com.tuya.appsdk.sample.user.resetPassword.UserResetPasswordActivity
import com.tuya.smart.android.user.api.ILoginCallback
import com.tuya.smart.android.user.bean.User
import com.tuya.smart.home.sdk.TuyaHomeSdk

/**
 * User Login Example
 *
 * @author qianqi <a href="mailto:developer@tuya.com">Contact me.</a>
 * @since 2021/1/5 5:13 PM
 */
class UserLoginActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_activity_login)

        val toolbar: Toolbar = findViewById<View>(R.id.topAppBar) as Toolbar
        toolbar.setNavigationOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.btnLogin).setOnClickListener(this)
        findViewById<Button>(R.id.btnForget).setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        val strAccount = findViewById<EditText>(R.id.etAccount).text.toString()
        val strCountryCode = findViewById<EditText>(R.id.etCountryCode).text.toString()
        val strPassword = findViewById<EditText>(R.id.etPassword).text.toString()

        v?.id?.let {
            if (it == R.id.btnLogin) {
                // Login with phone
                TuyaHomeSdk.getUserInstance().loginWithPhonePassword(strCountryCode,
                        strAccount,
                        strPassword,
                        object : ILoginCallback {
                            override fun onSuccess(user: User?) {
                                Toast.makeText(
                                        this@UserLoginActivity,
                                        "Login success",
                                        Toast.LENGTH_LONG
                                ).show()

                                startActivity(
                                        Intent(
                                                this@UserLoginActivity,
                                                MainSampleListActivity::class.java
                                        )
                                )
                            }

                            override fun onError(code: String?, error: String?) {
                                Toast.makeText(
                                        this@UserLoginActivity,
                                        "login error->$error",
                                        Toast.LENGTH_LONG
                                ).show()
                            }
                        })
            } else if (it == R.id.btnForget) {
                startActivity(Intent(this, UserResetPasswordActivity::class.java))
            }
        }
    }
}