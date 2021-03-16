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
 *
 */

package com.tuya.appsdk.sample.home.edit;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.tuya.appsdk.sample.user.R;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;
import com.tuya.smart.sdk.api.IResultCallback;

import java.util.ArrayList;

/**
 * Home Edit Sample
 *
 * @author aiwen <a href="mailto:developer@tuya.com"/>
 * @since 2/19/21 10:01 AM
 */
public class HomeEditActivity extends AppCompatActivity implements View.OnClickListener {

    private Toolbar mToolbar;
    private Button mBtDone;
    private long mHomeId;
    private TextInputEditText mEtHomeName;
    private TextInputEditText mEtCity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity_new_home);

        initView();
        initData();
    }


    private void initView() {

        mToolbar = findViewById(R.id.topAppBar);
        mBtDone = findViewById(R.id.btnDone);

        mEtHomeName = findViewById(R.id.etHomeName);
        mEtCity = findViewById(R.id.etCity);

        mBtDone.setOnClickListener(this);

    }


    private void initData() {

        mHomeId = getIntent().getLongExtra("homeId", 0);
        mToolbar.setTitle(getString(R.string.home_edit_title));
        mToolbar.setNavigationOnClickListener(v -> finish());

        mBtDone.setText(getString(R.string.home_done));

        TuyaHomeSdk.newHomeInstance(mHomeId).getHomeDetail(new ITuyaHomeResultCallback() {
            @Override
            public void onSuccess(HomeBean bean) {
                mEtHomeName.setText(bean.getName());
                mEtCity.setText(bean.getGeoName());
            }

            @Override
            public void onError(String errorCode, String errorMsg) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnDone) {
            done();
        }
    }


    public void done() {
        String strHomeName = mEtHomeName.getText().toString();
        String strCity = mEtCity.getText().toString();
        TuyaHomeSdk.newHomeInstance(mHomeId).updateHome(
                strHomeName,
                // Get location by yourself, here just sample as Shanghai's location
                120.52,
                30.40,
                strCity,
                new ArrayList<>(),
                false,
                new IResultCallback() {
                    @Override
                    public void onError(String code, String error) {

                    }

                    @Override
                    public void onSuccess() {
                        Toast.makeText(
                                HomeEditActivity.this,
                                "Update success",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}
