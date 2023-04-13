package com.tuya.lock.demo.activity.setting;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.tuya.lock.demo.R;
import com.tuya.lock.demo.constant.Constant;
import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.optimus.lock.api.IThingBleLockV2;
import com.thingclips.smart.optimus.lock.api.IThingLockManager;
import com.thingclips.smart.optimus.sdk.ThingOptimusSdk;

/**
 * 门锁设置
 */
public class VoiceSettingActivity extends AppCompatActivity {

    private IThingBleLockV2 tuyaLockDevice;
    private TextView remote_unlock_available;
    private boolean isOpen = true;
    private RadioButton set_remote_open;
    private RadioButton set_remote_close;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_setting);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        String mDevId = getIntent().getStringExtra(Constant.DEVICE_ID);
        IThingLockManager tuyaLockManager = ThingOptimusSdk.getManager(IThingLockManager.class);
        tuyaLockDevice = tuyaLockManager.getBleLockV2(mDevId);

        remote_unlock_available = findViewById(R.id.remote_unlock_available);

        RadioGroup set_remote_group = findViewById(R.id.set_remote_group);
        set_remote_open = findViewById(R.id.set_remote_open);
        set_remote_close = findViewById(R.id.set_remote_close);

        set_remote_group.setOnCheckedChangeListener((group, checkedId) -> {
            isOpen = checkedId == R.id.set_remote_open;
        });

        EditText set_remote_password = findViewById(R.id.set_remote_password);
        set_remote_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    password = s.toString();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        findViewById(R.id.remote_button).setOnClickListener(v -> setVoicePassword());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tuyaLockDevice.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isRemoteUnlockAvailable();
    }

    private void isRemoteUnlockAvailable() {
        tuyaLockDevice.fetchRemoteVoiceUnlock(new IThingResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Log.i(Constant.TAG, "get remote unlock available success:" + result);
                remote_unlock_available.setText(String.valueOf(result));

                if (result) {
                    set_remote_open.setChecked(true);
                } else {
                    set_remote_close.setChecked(true);
                }
            }

            @Override
            public void onError(String code, String message) {
                Log.e(Constant.TAG, "get remote unlock available failed: code = " + code + "  message = " + message);
                remote_unlock_available.setText(message);
            }
        });
    }

    private void setVoicePassword() {
        tuyaLockDevice.setRemoteVoiceUnlock(isOpen, password, new IThingResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if (result) {
                    Toast.makeText(getApplicationContext(), "setting success", Toast.LENGTH_SHORT).show();
                    remote_unlock_available.setText(String.valueOf(isOpen));
                } else {
                    Toast.makeText(getApplicationContext(), "setting fail", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String code, String error) {
                Log.e(Constant.TAG, "setRemoteUnlockType failed: code = " + code + "  message = " + error);
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}