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

package com.tuya.appsdk.sample.user.main;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.tuya.appsdk.sample.R;
import com.tuya.appsdk.sample.main.MainSampleListActivity;
import com.tuya.appsdk.sample.user.login.UserLoginActivity;
import com.tuya.appsdk.sample.user.register.UserRegisterActivity;
import com.tuya.smart.home.sdk.TuyaHomeSdk;

/**
 * User Func Navigation Page
 *
 * @author chuanfeng <a href="mailto:developer@tuya.com"/>
 * @since 2021/2/9 2:41 PM
 */
public class UserFuncActivity extends AppCompatActivity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (TuyaHomeSdk.getUserInstance().isLogin()) {
            startActivity(new Intent(this, MainSampleListActivity.class));
            finish();
        }
        setContentView(R.layout.user_activity_func);

        Button btnRegister = findViewById(R.id.btnRegister);
        Button btnLogin = findViewById(R.id.btnLogin);
        btnRegister.setOnClickListener(this);
        btnLogin.setOnClickListener(this);


        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);

            TextView tvAppVersion = findViewById(R.id.tvAppVersion);
            tvAppVersion.setText(String.format(getString(R.string.app_version_tips), pInfo.versionName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnRegister) {
            startActivity(new Intent(this, UserRegisterActivity.class));
        } else if (v.getId() == R.id.btnLogin) {
            // Login
            startActivity(new Intent(this, UserLoginActivity.class));
        }
    }
}
