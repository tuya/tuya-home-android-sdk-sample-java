package com.tuya.lock.demo.activity.password;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.alibaba.fastjson.JSONObject;
import com.tuya.lock.demo.R;
import com.tuya.lock.demo.constant.Constant;
import com.tuya.lock.demo.utils.Utils;
import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.optimus.lock.api.IThingBleLockV2;
import com.thingclips.smart.optimus.lock.api.IThingLockManager;
import com.thingclips.smart.optimus.lock.api.bean.OfflineTempPassword;
import com.thingclips.smart.optimus.lock.api.enums.OfflineTempPasswordType;
import com.thingclips.smart.optimus.sdk.ThingOptimusSdk;

public class PasswordOldOfflineAddActivity extends AppCompatActivity {


    private IThingBleLockV2 tuyaLockDevice;
    private OfflineTempPasswordType pwdType;
    private long gmtStart;
    private long gmtExpired;
    private TextView password_content;
    private String name = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_offline_add);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        String mDevId = getIntent().getStringExtra(Constant.DEVICE_ID);
        String type = getIntent().getStringExtra(Constant.PASSWORD_TYPE);
        switch (type) {
            case Constant.TYPE_SINGLE:
                pwdType = OfflineTempPasswordType.SINGLE;
                break;
            case Constant.TYPE_MULTIPLE:
                pwdType = OfflineTempPasswordType.MULTIPLE;
                break;
            case Constant.TYPE_CLEAR_ALL:
                pwdType = OfflineTempPasswordType.CLEAR_ALL;
                break;
        }

        IThingLockManager tuyaLockManager = ThingOptimusSdk.getManager(IThingLockManager.class);
        tuyaLockDevice = tuyaLockManager.getBleLockV2(mDevId);

        EditText password_offline_add_start = findViewById(R.id.password_offline_add_start);
        gmtStart = System.currentTimeMillis();
        password_offline_add_start.setText(Utils.getDateDay(gmtStart));
        password_offline_add_start.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String selectTime = s.toString();
                gmtStart = Utils.getStampTime(selectTime);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        EditText password_offline_add_name = findViewById(R.id.password_offline_add_name);
        password_offline_add_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    name = s.toString();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        EditText password_offline_add_expired = findViewById(R.id.password_offline_add_expired);
        gmtExpired = System.currentTimeMillis() + 7 * 86400000L;
        password_offline_add_expired.setText(Utils.getDateDay(gmtExpired));
        password_offline_add_expired.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String selectTime = s.toString();
                gmtExpired = Utils.getStampTime(selectTime);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        password_content = findViewById(R.id.password_content);

        findViewById(R.id.password_offline_add).setOnClickListener(v -> {
            getOfflinePassword();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tuyaLockDevice.onDestroy();
    }

    private void getOfflinePassword() {
        tuyaLockDevice.getOfflinePassword(pwdType, gmtStart, gmtExpired, name, new IThingResultCallback<OfflineTempPassword>() {
            @Override
            public void onSuccess(OfflineTempPassword result) {
                Log.i(Constant.TAG, "setOfflineTempPasswordName success :" + JSONObject.toJSONString(result));
                password_content.setText(JSONObject.toJSONString(result));
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