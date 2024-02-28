package com.tuya.lock.demo.ble.activity.password;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.alibaba.fastjson.JSONObject;
import com.tuya.lock.demo.R;
import com.tuya.lock.demo.ble.constant.Constant;
import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.optimus.lock.api.IThingBleLockV2;
import com.thingclips.smart.optimus.lock.api.IThingLockManager;
import com.thingclips.smart.optimus.sdk.ThingOptimusSdk;

public class PasswordOldOfflineAddRevokeActivity extends AppCompatActivity {


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
    protected void onResume() {
        super.onResume();
        getProSingleRevokeOfflinePassword();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tuyaLockDevice.onDestroy();
    }

    private void getProSingleRevokeOfflinePassword() {
        String pwdId = getIntent().getStringExtra("pwdId");
        tuyaLockDevice.getSingleRevokeOfflinePassword(pwdId, new IThingResultCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.i(Constant.TAG, "setOfflineTempPasswordName success :" + JSONObject.toJSONString(result));
                password_content.setText(result);
                Toast.makeText(getApplicationContext(), "success", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                Log.e(Constant.TAG, "setOfflineTempPasswordName failed: code = " + errorCode + "  message = " + errorMessage);
                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

}