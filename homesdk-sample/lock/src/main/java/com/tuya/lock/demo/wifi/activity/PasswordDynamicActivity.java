package com.tuya.lock.demo.wifi.activity;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.optimus.lock.api.IThingLockManager;
import com.thingclips.smart.optimus.lock.api.IThingWifiLock;
import com.thingclips.smart.optimus.sdk.ThingOptimusSdk;
import com.tuya.lock.demo.R;
import com.tuya.lock.demo.ble.constant.Constant;

/**
 * WIFI 门锁动态密码
 */
public class PasswordDynamicActivity extends AppCompatActivity {

    private IThingWifiLock wifiLock;
    private TextView dynamic_number;
    private ProgressBar progress_view;

    public static void startActivity(Context context, String devId) {
        Intent intent = new Intent(context, PasswordDynamicActivity.class);
        //设备id
        intent.putExtra(Constant.DEVICE_ID, devId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zigbee_password_dynamic);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        String deviceId = getIntent().getStringExtra(Constant.DEVICE_ID);
        IThingLockManager tuyaLockManager = ThingOptimusSdk.getManager(IThingLockManager.class);
        wifiLock = tuyaLockManager.getWifiLock(deviceId);

        dynamic_number = findViewById(R.id.dynamic_number);
        progress_view = findViewById(R.id.progress_view);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        countDownTimer.cancel();
        wifiLock.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getDynamicPasswordData();
    }

    private void getDynamicPasswordData() {
        wifiLock.getDynamicPassword(new IThingResultCallback<String>() {
            @Override
            public void onSuccess(String result) {
                dynamic_number.setText(result);
                countDownTimer.cancel();
                countDownTimer.start();
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                dynamic_number.setText(errorMessage);
                progress_view.setProgress(0);
            }
        });
    }

    private final CountDownTimer countDownTimer = new CountDownTimer(300000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            int value = (int) (millisUntilFinished / 1000);
            progress_view.post(() -> progress_view.setProgress(value));
        }

        @Override
        public void onFinish() {
            getDynamicPasswordData();
        }
    };
}