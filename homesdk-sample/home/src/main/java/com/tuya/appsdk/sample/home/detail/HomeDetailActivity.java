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

package com.tuya.appsdk.sample.home.detail;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.tuya.appsdk.sample.home.edit.HomeEditActivity;
import com.tuya.appsdk.sample.resource.HomeModel;
import com.tuya.appsdk.sample.user.R;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.bean.WeatherBean;
import com.tuya.smart.home.sdk.callback.IIGetHomeWetherSketchCallBack;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;
import com.tuya.smart.sdk.api.IResultCallback;

/**
 * Home Detail Sample
 *
 * @author aiwen <a href="mailto:developer@tuya.com"/>
 * @since 2/19/21 10:01 AM
 */
public class HomeDetailActivity extends AppCompatActivity implements View.OnClickListener {

    private Toolbar mToolbar;
    private TextView mTvHomeId;
    private TextView mTvHomeName;
    private TextView mTvHomeCity;
    private TextView mTvWeather;
    private TextView mTvHomeTemperature;
    private Button mBtEdit;
    private Button mBtDismiss;
    private long mHomeId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_acitivty_detail);

        initView();
        initData();
    }


    private void initView() {
        mToolbar = findViewById(R.id.topAppBar);


        mTvHomeId = findViewById(R.id.tvHomeId);
        mTvHomeName = findViewById(R.id.tvHomeName);
        mTvHomeCity = findViewById(R.id.tvHomeCity);

        mBtEdit = findViewById(R.id.btnEdit);
        mBtDismiss = findViewById(R.id.btnDismiss);

        mTvWeather = findViewById(R.id.tvWeather);
        mTvHomeTemperature = findViewById(R.id.tvHomeTemperature);

        mBtDismiss.setOnClickListener(this);
        mBtEdit.setOnClickListener(this);
    }

    private void initData() {
        mToolbar.setNavigationOnClickListener(v -> finish());

        mHomeId = getIntent().getLongExtra("homeId", 0);

        // Get home info
        TuyaHomeSdk.newHomeInstance(mHomeId).getHomeDetail(new ITuyaHomeResultCallback() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess(HomeBean bean) {
                mTvHomeId.setText(bean.getHomeId() + "");
                mTvHomeName.setText(bean.getName());
                mTvHomeCity.setText(bean.getGeoName());

                // Get home weather info
                TuyaHomeSdk.newHomeInstance(mHomeId).getHomeWeatherSketch(bean.getLon(),
                        bean.getLat(),
                        new IIGetHomeWetherSketchCallBack() {
                            @Override
                            public void onSuccess(WeatherBean result) {
                                mTvWeather.setText(result.getCondition());
                                mTvHomeTemperature.setText(result.getTemp());
                            }

                            @Override
                            public void onFailure(String errorCode, String errorMsg) {
                                Toast.makeText(
                                        HomeDetailActivity.this,
                                        "get home weather error->$errorMsg",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        });
            }

            @Override
            public void onError(String errorCode, String errorMsg) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnDismiss) {
            TuyaHomeSdk.newHomeInstance(mHomeId).dismissHome(new IResultCallback() {
                @Override
                public void onError(String code, String error) {

                }

                @Override
                public void onSuccess() {
                    HomeModel.INSTANCE.clear(v.getContext());
                    finish();
                }
            });

        } else if (id == R.id.btnEdit) {
            Intent intent = new Intent(this, HomeEditActivity.class);
            intent.putExtra("homeId", mHomeId);
            startActivity(intent);
        }
    }
}
