package com.tuya.lock.demo.ble.activity.state;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.tuya.lock.demo.R;
import com.tuya.lock.demo.ble.constant.Constant;
import com.thingclips.smart.optimus.lock.api.IThingBleLockV2;
import com.thingclips.smart.optimus.lock.api.IThingLockManager;
import com.thingclips.smart.optimus.sdk.ThingOptimusSdk;

/**
 * 展示蓝牙连接状态
 */
public class ConnectStateActivity extends AppCompatActivity {

    IThingBleLockV2 tuyaLockDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_state);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        String mDevId = getIntent().getStringExtra(Constant.DEVICE_ID);
        IThingLockManager tuyaLockManager = ThingOptimusSdk.getManager(IThingLockManager.class);
        tuyaLockDevice = tuyaLockManager.getBleLockV2(mDevId);

        TextView local_ble_connect = findViewById(R.id.local_ble_connect);
        TextView ble_online = findViewById(R.id.ble_online);

        boolean isBLEConnected = false;
        if (null != tuyaLockDevice) {
            isBLEConnected = tuyaLockDevice.isBLEConnected();
        }

        String isBLEConnectedStr = "isBLEConnected: " + isBLEConnected;
        local_ble_connect.setText(isBLEConnectedStr);

        boolean isOnline = false;
        if (null != tuyaLockDevice) {
            isOnline = tuyaLockDevice.isOnline();
//            tuyaLockDevice.autoConnect(new ConnectV2Listener() {
//                @Override
//                public void onStatusChanged(boolean online) {
//                    Log.i(Constant.TAG, "tuyaLockDevice connect online:" + online);
//                    String isOnlineStr = "isOnline: " + online;
//                    ble_online.setText(isOnlineStr);
//                }
//
//                @Override
//                public void onError(String code, String error) {
//                    Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
//                }
//            });
        }
        String isOnlineStr = "isOnline: " + isOnline;
        ble_online.setText(isOnlineStr);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tuyaLockDevice.onDestroy();
    }
}