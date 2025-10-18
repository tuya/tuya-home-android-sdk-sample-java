package com.tuya.lock.demo.ble.activity.password;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.alibaba.fastjson.JSONObject;
import com.tuya.lock.demo.R;
import com.tuya.lock.demo.ble.constant.Constant;
import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.optimus.lock.api.IThingBleLockV2;
import com.thingclips.smart.optimus.lock.api.IThingLockManager;
import com.thingclips.smart.optimus.sdk.ThingOptimusSdk;

public class PasswordLockDeviceConfigActivity extends AppCompatActivity {


    private IThingBleLockV2 tuyaLockDevice;
    private TextView password_content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_offline_add_2);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        String mDevId = getIntent().getStringExtra(Constant.DEVICE_ID);
        IThingLockManager tuyaLockManager = ThingOptimusSdk.getManager(IThingLockManager.class);
        tuyaLockDevice = tuyaLockManager.getBleLockV2(mDevId);

        password_content = findViewById(R.id.password_content);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tuyaLockDevice.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLockDeviceConfig();
    }

    private void getLockDeviceConfig() {
        tuyaLockDevice.getLockDeviceConfig(new IThingResultCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.i(Constant.TAG, "getLockDeviceConfig success :" + JSONObject.toJSONString(result));
                password_content.setText(result);
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                Log.e(Constant.TAG, "getLockDeviceConfig failed: code = " + errorCode + "  message = " + errorMessage);
                password_content.setText(errorMessage);
            }
        });
    }

}