package com.tuya.lock.demo.ble.activity.detail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.alibaba.fastjson.JSONObject;
import com.tuya.lock.demo.ble.activity.password.PasswordMainActivity;
import com.tuya.lock.demo.ble.activity.records.BleLockProRecordsActivity;
import com.tuya.lock.demo.ble.activity.setting.DpSettingActivity;
import com.tuya.lock.demo.ble.activity.setting.VoiceSettingActivity;
import com.tuya.lock.demo.ble.activity.unlock.BleSwitchLockActivity;
import com.tuya.lock.demo.R;
import com.tuya.lock.demo.ble.activity.member.MemberListActivity;
import com.tuya.lock.demo.ble.activity.opMode.OpModeListActivity;
import com.tuya.lock.demo.ble.activity.state.ConnectStateActivity;
import com.tuya.lock.demo.ble.constant.Constant;
import com.tuya.lock.demo.ble.activity.records.BleLockRecordsActivity;
import com.tuya.lock.demo.ble.activity.setting.LockSettingActivity;
import com.tuya.lock.demo.ble.utils.CopyLinkTextHelper;
import com.thingclips.smart.home.sdk.ThingHomeSdk;
import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.optimus.lock.api.IThingBleLockV2;
import com.thingclips.smart.optimus.lock.api.IThingLockManager;
import com.thingclips.smart.optimus.lock.api.callback.ConnectV2Listener;
import com.thingclips.smart.optimus.sdk.ThingOptimusSdk;
import com.thingclips.smart.sdk.api.IDevListener;
import com.thingclips.smart.sdk.api.IResultCallback;
import com.thingclips.smart.sdk.api.IThingDevice;
import com.thingclips.smart.sdk.bean.DeviceBean;
import com.thingclips.smart.sdk.optimus.lock.bean.ble.BLELockUser;
import com.thingclips.smart.sdk.optimus.lock.bean.ble.DataPoint;
import com.thingclips.smart.sdk.optimus.lock.utils.LockUtil;

public class BleLockDetailActivity extends AppCompatActivity {

    private IThingDevice IThingDevice;
    private TextView device_state_view;
    private IThingBleLockV2 tuyaLockDevice;
    private Button device_connect_btn;
    private boolean isPublishSync = false;
    private int deviceOnlineState = 0;
    private Button btn_unlock;

    public static void startActivity(Context context, String devId) {
        Intent intent = new Intent(context, BleLockDetailActivity.class);
        //设备id
        intent.putExtra(Constant.DEVICE_ID, devId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_ble_detail);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        String deviceId = getIntent().getStringExtra(Constant.DEVICE_ID);

        IThingLockManager tuyaLockManager = ThingOptimusSdk.getManager(IThingLockManager.class);
        tuyaLockDevice = tuyaLockManager.getBleLockV2(deviceId);
        tuyaLockDevice.publishSyncBatchData();
        boolean isProDevice = tuyaLockDevice.isProDevice();


        IThingDevice = ThingHomeSdk.newDeviceInstance(deviceId);
        IThingDevice.registerDevListener(listener);

        TextView device_info_view = findViewById(R.id.device_info_view);
        String deviceIdName = "device ID: " + deviceId;
        device_info_view.setText(deviceIdName);

        DeviceBean deviceBean = ThingHomeSdk.getDataInstance().getDeviceBean(deviceId);
        if (null != deviceBean) {
            toolbar.setTitle(deviceBean.getName());
        }

        btn_unlock = findViewById(R.id.btn_unlock);

        device_state_view = findViewById(R.id.device_state_view);
        device_connect_btn = findViewById(R.id.device_connect_btn);
        device_connect_btn.setOnClickListener(v -> {
            String connectIng = getResources().getString(R.string.submit_connect) + "...";
            device_connect_btn.setText(connectIng);
            device_connect_btn.setEnabled(false);
            tuyaLockDevice.autoConnect(new ConnectV2Listener() {
                @Override
                public void onStatusChanged(boolean online) {
                    device_connect_btn.setVisibility(View.GONE);
                    device_connect_btn.setEnabled(true);
                    Toast.makeText(v.getContext(), "connect success", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String code, String error) {
                    device_connect_btn.setEnabled(true);
                    device_connect_btn.setText(getResources().getString(R.string.submit_connect));
                    Toast.makeText(v.getContext(), error, Toast.LENGTH_SHORT).show();
                }
            });
        });
        showState();
        findViewById(R.id.device_state_layout).setOnClickListener(v -> {
            //蓝牙连接
            Intent intent = new Intent(v.getContext(), ConnectStateActivity.class);
            intent.putExtra(Constant.DEVICE_ID, deviceId);
            v.getContext().startActivity(intent);
        });

        findViewById(R.id.btn_get_device_info).setOnClickListener(v -> {
            CopyLinkTextHelper.getInstance(v.getContext()).CopyText(deviceId);
            Toast.makeText(v.getContext(), "copy success", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.door_lock_member).setOnClickListener(v -> {
            //成员管理
            Intent intent = new Intent(v.getContext(), MemberListActivity.class);
            intent.putExtra(Constant.DEVICE_ID, deviceId);
            v.getContext().startActivity(intent);
        });

        findViewById(R.id.ble_unlock_and_lock).setOnClickListener(v -> {
            //蓝牙解锁和落锁
            Intent intent = new Intent(v.getContext(), BleSwitchLockActivity.class);
            intent.putExtra(Constant.DEVICE_ID, deviceId);
            v.getContext().startActivity(intent);
        });

        findViewById(R.id.lock_record_list).setOnClickListener(v -> {
            Intent intent;
            if (isProDevice) {
                //门锁记录 pro
                intent = new Intent(v.getContext(), BleLockProRecordsActivity.class);
            } else {
                //门锁记录 老版本
                intent = new Intent(v.getContext(), BleLockRecordsActivity.class);
            }
            intent.putExtra(Constant.DEVICE_ID, deviceId);
            v.getContext().startActivity(intent);
        });


        findViewById(R.id.unlock_mode_management).setOnClickListener(v -> {
            //解锁方式管理
            Intent intent = new Intent(v.getContext(), OpModeListActivity.class);
            intent.putExtra(Constant.DEVICE_ID, deviceId);
            v.getContext().startActivity(intent);
        });

        findViewById(R.id.password_management).setOnClickListener(v -> {
            //临时密码
            Intent intent = new Intent(v.getContext(), PasswordMainActivity.class);
            intent.putExtra(Constant.DEVICE_ID, deviceId);
            v.getContext().startActivity(intent);
        });


        //校验远程语音是否有对应dp
        TextView voice_settings_view = findViewById(R.id.voice_settings);
        String dpId = LockUtil.convertCode2Id(deviceId, DataPoint.UNLOCK_VOICE_REMOTE);
        if (TextUtils.isEmpty(dpId)) {
            voice_settings_view.setText(getResources().getString(R.string.voice_set_title) + "(not support)");
            voice_settings_view.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
        } else {
            voice_settings_view.setText(getResources().getString(R.string.voice_set_title));
            voice_settings_view.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_next, 0);
            voice_settings_view.setOnClickListener(v -> {
                //远程语音设置
                Intent intent = new Intent(v.getContext(), VoiceSettingActivity.class);
                intent.putExtra(Constant.DEVICE_ID, deviceId);
                v.getContext().startActivity(intent);
            });
        }

        //校验远程开关锁dp是否存在
        TextView door_lock_settings = findViewById(R.id.door_lock_settings);
        String remote_dpId = LockUtil.convertCode2Id(deviceId, DataPoint.REMOTE_NO_DP_KEY);
        if (TextUtils.isEmpty(remote_dpId)) {
            door_lock_settings.setText(getResources().getString(R.string.lock_remote_set) + "(not support)");
            door_lock_settings.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
        } else {
            door_lock_settings.setText(getResources().getString(R.string.lock_remote_set));
            door_lock_settings.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_next, 0);
            door_lock_settings.setOnClickListener(v -> {
                //远程开锁设置
                Intent intent = new Intent(v.getContext(), LockSettingActivity.class);
                intent.putExtra(Constant.DEVICE_ID, deviceId);
                v.getContext().startActivity(intent);
            });
        }

        findViewById(R.id.device_delete).setOnClickListener(v -> {
            DeleteDeviceActivity.startActivity(v.getContext(), deviceId);
        });

        findViewById(R.id.open_dp_demo).setOnClickListener(v -> {
            DpSettingActivity.startActivity(v.getContext(), deviceId);
        });

        if (null != deviceBean && deviceBean.getIsOnline()) {
            publishSyncBatchData();
        }

        btn_unlock.setOnClickListener(v -> {
            btn_unlock.setEnabled(false);
            if (deviceOnlineState == 1) {
                bleUnlock();
            } else if (deviceOnlineState == 2) {
                farUnlock();
            }
        });
    }

    private final IDevListener listener = new IDevListener() {
        @Override
        public void onDpUpdate(String devId, String dpStr) {

        }

        @Override
        public void onRemoved(String devId) {

        }

        @Override
        public void onStatusChanged(String devId, boolean online) {
            if (online) {
                publishSyncBatchData();
            }
            showState();

        }

        @Override
        public void onNetworkStatusChanged(String devId, boolean status) {

        }

        @Override
        public void onDevInfoUpdate(String devId) {

        }
    };

    private void publishSyncBatchData() {
        if (isPublishSync) {
            return;
        }
        isPublishSync = true;
        tuyaLockDevice.publishSyncBatchData();
    }

    private void showState() {
        boolean isBLEConnected = tuyaLockDevice.isBLEConnected();
        boolean isOnline = tuyaLockDevice.isOnline();
        btn_unlock.setEnabled(isOnline);
        if (!isBLEConnected && isOnline) {
            device_state_view.setText(getResources().getString(R.string.connected_gateway));
            device_connect_btn.setVisibility(View.GONE);
            deviceOnlineState = 2;
        } else if (isBLEConnected && isOnline) {
            device_state_view.setText(getResources().getString(R.string.connected_bluetooth));
            device_connect_btn.setVisibility(View.GONE);
            deviceOnlineState = 1;
        } else {
            device_state_view.setText(getResources().getString(R.string.device_offline));
            device_connect_btn.setVisibility(View.VISIBLE);
            deviceOnlineState = 0;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        device_connect_btn.setText(getResources().getString(R.string.submit_connect));
        device_connect_btn.setEnabled(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        IThingDevice.unRegisterDevListener();
        IThingDevice.onDestroy();
    }

    private void bleUnlock() {
        tuyaLockDevice.getCurrentMemberDetail(new IThingResultCallback<BLELockUser>() {
            @Override
            public void onSuccess(BLELockUser result) {
                Log.i(Constant.TAG, "getCurrentUser:" + JSONObject.toJSONString(result));

                tuyaLockDevice.bleUnlock(result.lockUserId, new IResultCallback() {
                    @Override
                    public void onError(String code, String error) {
                        Log.i(Constant.TAG, "bleUnlock onError code:" + code + ", error:" + error);
                        Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                        btn_unlock.setEnabled(true);
                    }

                    @Override
                    public void onSuccess() {
                        Toast.makeText(getApplicationContext(), "unlock success", Toast.LENGTH_SHORT).show();
                        btn_unlock.setEnabled(true);
                    }
                });
            }

            @Override
            public void onError(String code, String error) {
                Log.e(Constant.TAG, "getCurrentUser onError code:" + code + ", error:" + error);
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                btn_unlock.setEnabled(true);
            }
        });
    }

    private void farUnlock() {
        Log.i(Constant.TAG, "remoteSwitchLock");
        tuyaLockDevice.remoteSwitchLock(true, new IResultCallback() {
            @Override
            public void onError(String code, String error) {
                Log.e(Constant.TAG, "remoteSwitchLock unlock onError code:" + code + ", error:" + error);
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                btn_unlock.setEnabled(true);
            }

            @Override
            public void onSuccess() {
                Toast.makeText(getApplicationContext(), "remote unlock success", Toast.LENGTH_SHORT).show();
                btn_unlock.setEnabled(true);
            }
        });
    }
}