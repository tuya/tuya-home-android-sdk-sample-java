package com.tuya.lock.demo.wifi.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.alibaba.fastjson.JSONObject;
import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.optimus.lock.api.IThingLockManager;
import com.thingclips.smart.optimus.lock.api.IThingWifiLock;
import com.thingclips.smart.optimus.lock.api.TempPasswordBuilder;
import com.thingclips.smart.optimus.lock.api.bean.TempPassword;
import com.thingclips.smart.optimus.sdk.ThingOptimusSdk;
import com.tuya.lock.demo.R;
import com.tuya.lock.demo.ble.constant.Constant;
import com.tuya.lock.demo.ble.utils.Utils;

/**
 * WIFI 密码添加
 */
public class PasswordDetailActivity extends AppCompatActivity {

    private IThingWifiLock wifiLock;


    private TempPassword dataBean;

    private String passwordValue;
    private int mFrom = 0;


    public static void startActivity(Context context, String devId) {
        Intent intent = new Intent(context, PasswordDetailActivity.class);
        //设备id
        intent.putExtra(Constant.DEVICE_ID, devId);
        context.startActivity(intent);
    }

    public static void startEditActivity(Context context, String devId, TempPassword bean) {
        Intent intent = new Intent(context, PasswordDetailActivity.class);
        //设备id
        intent.putExtra(Constant.DEVICE_ID, devId);
        intent.putExtra(Constant.PASSWORD_DATA, JSONObject.toJSONString(bean));
        intent.putExtra(Constant.FROM, 1);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_password_temp_add);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        String mDevId = getIntent().getStringExtra(Constant.DEVICE_ID);
        mFrom = getIntent().getIntExtra(Constant.FROM, 0);
        dataBean = JSONObject.parseObject(getIntent().getStringExtra(Constant.PASSWORD_DATA), TempPassword.class);
        toolbar.setTitle(getResources().getString(R.string.zigbee_temp_pwd));

        if (null == dataBean) {
            dataBean = new TempPassword();
        }

        IThingLockManager tuyaLockManager = ThingOptimusSdk.getManager(IThingLockManager.class);
        wifiLock = tuyaLockManager.getWifiLock(mDevId);

        EditText password_name = findViewById(R.id.password_name);
        password_name.setText(dataBean.name);
        password_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    dataBean.name = s.toString();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        EditText password_content = findViewById(R.id.password_content);
        password_content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    passwordValue = s.toString();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        EditText password_effective_time = findViewById(R.id.password_effective_time);
        if (dataBean.effectiveTime == 0) {
            dataBean.effectiveTime = System.currentTimeMillis();
        }
        long effectiveTime = dataBean.effectiveTime;
        if (String.valueOf(effectiveTime).length() == 10) {
            effectiveTime = effectiveTime * 1000;
        }
        password_effective_time.setText(Utils.getDateDay(effectiveTime));
        password_effective_time.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    dataBean.effectiveTime = Utils.getStampTime(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        EditText password_invalid_time = findViewById(R.id.password_invalid_time);
        if (dataBean.invalidTime == 0) {
            dataBean.invalidTime = System.currentTimeMillis() + 7 * 86400000L;
        }
        long invalidTime = dataBean.invalidTime;
        if (String.valueOf(invalidTime).length() == 10) {
            invalidTime = invalidTime * 1000;
        }
        password_invalid_time.setText(Utils.getDateDay(invalidTime));
        password_invalid_time.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    dataBean.invalidTime = Utils.getStampTime(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        if (mFrom == 1) {
            findViewById(R.id.password_content_wrap).setVisibility(View.GONE);
            findViewById(R.id.password_content_line).setVisibility(View.GONE);
        } else {
            findViewById(R.id.password_content_wrap).setVisibility(View.VISIBLE);
            findViewById(R.id.password_content_line).setVisibility(View.VISIBLE);
        }

        findViewById(R.id.password_effective_time_main).setVisibility(View.VISIBLE);
        findViewById(R.id.password_effective_time_line).setVisibility(View.VISIBLE);
        findViewById(R.id.password_invalid_time_main).setVisibility(View.VISIBLE);
        findViewById(R.id.password_invalid_time_line).setVisibility(View.VISIBLE);

        findViewById(R.id.password_add).setOnClickListener(v -> createPassword());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wifiLock.onDestroy();
    }

    private void createPassword() {
        TempPasswordBuilder builder = new TempPasswordBuilder();
        builder.password(passwordValue);
        builder.name(dataBean.name);
        builder.invalidTime(dataBean.invalidTime);
        builder.effectiveTime(dataBean.effectiveTime);
        Log.i(Constant.TAG, "request:" + builder);
        wifiLock.createTempPassword(builder, new IThingResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Toast.makeText(getApplicationContext(), "onSuccess", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}