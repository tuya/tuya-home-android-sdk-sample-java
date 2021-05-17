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

package com.tuya.appsdk.sample.user.register;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.tuya.appsdk.sample.R;
import com.tuya.smart.android.user.api.IRegisterCallback;
import com.tuya.smart.android.user.api.IValidateCallback;
import com.tuya.smart.android.user.bean.User;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.IResultCallback;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User Register Example
 *
 * @author chuanfeng <a href="mailto:developer@tuya.com"/>
 * @since 2021/2/9 3:05 PM
 */
public class UserRegisterActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "UserRegisterActivity";
    private final String check = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
    private final Pattern regex = Pattern.compile(check);
    //mType equals 1 is for register
    private final int mRegisterType = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_activity_register);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button btnRegister = findViewById(R.id.btnRegister);
        Button btnCode = findViewById(R.id.btnCode);
        btnRegister.setOnClickListener(this);
        btnCode.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        EditText etAccount = findViewById(R.id.etAccount);
        String strAccount = etAccount.getText().toString();
        EditText etCountryCode = findViewById(R.id.etCountryCode);
        String strCountryCode = etCountryCode.getText().toString();
        EditText etPassword = findViewById(R.id.etPassword);
        String strPassword = etPassword.getText().toString();
        EditText etCode = findViewById(R.id.etCode);
        String strCode = etCode.getText().toString();

        Matcher matcher = regex.matcher(strAccount);
        boolean isEmail = matcher.matches();

        if (v.getId() == R.id.btnRegister) {
            IRegisterCallback callback = new IRegisterCallback() {
                @Override
                public void onSuccess(User user) {
                    Toast.makeText(
                            UserRegisterActivity.this,
                            "Register success",
                            Toast.LENGTH_LONG
                    ).show();
                }

                @Override
                public void onError(String code, String error) {
                    Toast.makeText(
                            UserRegisterActivity.this,
                            "Register error:" + error,
                            Toast.LENGTH_LONG
                    ).show();
                }
            };

            if (isEmail) {
                // Register by email
                TuyaHomeSdk.getUserInstance().registerAccountWithEmail(
                        strCountryCode,
                        strAccount,
                        strPassword,
                        strCode,
                        callback
                );
            } else {
                TuyaHomeSdk.getUserInstance().registerAccountWithPhone(
                        strCountryCode,
                        strAccount,
                        strPassword,
                        strCode,
                        callback
                );
            }
        } else if (v.getId() == R.id.btnCode) {
                // Get verification code code by phone or Email
                TuyaHomeSdk.getUserInstance().sendVerifyCodeWithUserName(
                        strAccount,
                        "",
                        strCountryCode,
                        mRegisterType,
                        new IResultCallback() {
                            @Override
                            public void onError(String code, String error) {
                                Toast.makeText(
                                        UserRegisterActivity.this,
                                        "getValidateCode error:" + error,
                                        Toast.LENGTH_LONG
                                ).show();

                            }

                            @Override
                            public void onSuccess() {
                                Toast.makeText(
                                        UserRegisterActivity.this,
                                        "Got validateCode",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        });

        }
    }
}

