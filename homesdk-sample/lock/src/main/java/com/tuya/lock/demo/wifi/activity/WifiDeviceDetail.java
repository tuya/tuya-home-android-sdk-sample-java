package com.tuya.lock.demo.wifi.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.ims.ImsReasonInfo;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.thingclips.sdk.os.ThingOSDevice;
import com.thingclips.smart.home.sdk.ThingHomeSdk;
import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.optimus.lock.api.IThingLockManager;
import com.thingclips.smart.optimus.lock.api.IThingWifiLock;
import com.thingclips.smart.optimus.sdk.ThingOptimusSdk;
import com.thingclips.smart.sdk.api.IDevListener;
import com.thingclips.smart.sdk.api.IThingDevice;
import com.thingclips.smart.sdk.bean.DeviceBean;
import com.thingclips.smart.sdk.optimus.lock.utils.StandardDpConverter;
import com.tuya.lock.demo.R;
import com.tuya.lock.demo.ble.activity.detail.DeleteDeviceActivity;
import com.tuya.lock.demo.ble.constant.Constant;
import com.tuya.lock.demo.zigbee.view.LockButtonProgressView;

import java.util.Locale;
import java.util.Map;

/**
 * Created by HuiYao on 2024/1/16
 */
public class WifiDeviceDetail extends AppCompatActivity {

    private LockButtonProgressView unlock_btn;
    private TextView closed_door_view;
    private TextView anti_lock_view;
    private TextView child_lock_view;
    private TextView power_view;
    private TextView alarm_record_view;
    private TextView door_record_view;
    private TextView temporary_password_view;
    private TextView member_list_view;
    private TextView dynamic_password_view;

    private IThingWifiLock wifiLock;

    private IThingDevice ITuyaDevice;

    private String mDevId;

    private boolean dialogShowing = false;

    public static void startActivity(Context context, String devId) {
        Intent intent = new Intent(context, WifiDeviceDetail.class);
        //设备id
        intent.putExtra(Constant.DEVICE_ID, devId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_device_detail);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        mDevId = getIntent().getStringExtra(Constant.DEVICE_ID);

        IThingLockManager tuyaLockManager = ThingOptimusSdk.getManager(IThingLockManager.class);
        wifiLock = tuyaLockManager.getWifiLock(mDevId);

        wifiLock.setRemoteUnlockListener((devId, second) -> {
            if (second != 0 && !dialogShowing) {
                dialogShowing = true;
                Log.i(Constant.TAG, "remote unlock request onReceive");
                onCreateDialog();
            }
        });

        ITuyaDevice = ThingHomeSdk.newDeviceInstance(mDevId);
        ITuyaDevice.registerDevListener(deviceListener);

        initView();
        deviceOnline();
    }

    /**
     * 创建远程开锁确认弹框
     */
    public void onCreateDialog() {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Whether to allow remote unlocking?")
                .setPositiveButton("YES", (dialog, id) -> {
                    remoteUnlock(true);
                    Log.i(Constant.TAG, "remote unlock request access");
                    dialog.dismiss();
                    dialogShowing = false;
                })
                .setNegativeButton("NO", (dialog, id) -> {
                    remoteUnlock(false);
                    dialog.dismiss();
                    Log.i(Constant.TAG, "remote unlock request deny");
                    dialogShowing = false;
                }).setCancelable(false);
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    private final IDevListener deviceListener = new IDevListener() {
        @Override
        public void onDpUpdate(String devId, String dpStr) {
            Map<String, Object> dpData = StandardDpConverter.convertIdToCodeMap(dpStr, StandardDpConverter.getSchemaMap(mDevId));
            dealAddUnlockMode(dpData);
        }

        @Override
        public void onRemoved(String devId) {

        }

        @Override
        public void onStatusChanged(String devId, boolean online) {
            deviceOnline();
        }

        @Override
        public void onNetworkStatusChanged(String devId, boolean status) {

        }

        @Override
        public void onDevInfoUpdate(String devId) {

        }
    };

    private void dealAddUnlockMode(Map<String, Object> dpData) {
        for (String key : dpData.keySet()) {
            String lockResponse = String.valueOf(dpData.get(key));
            switch (key) {
                case "closed_opened":
                    checkDoorOpen(lockResponse);
                    break;
                case "reverse_lock":
                    checkReverseOpen(Boolean.parseBoolean(lockResponse));
                    break;
                case "child_lock":
                    checkChildOpen(Boolean.parseBoolean(lockResponse));
                    break;
                case "residual_electricity":
                case "battery_state":
                    checkResidual(lockResponse);
                    break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wifiLock.unRegisterDevListener();
        wifiLock.onDestroy();
        ITuyaDevice.onDestroy();
    }

    private void initView() {
        unlock_btn = findViewById(R.id.unlock_btn);
        closed_door_view = findViewById(R.id.closed_door_view);
        anti_lock_view = findViewById(R.id.anti_lock_view);
        child_lock_view = findViewById(R.id.child_lock_view);
        power_view = findViewById(R.id.power_view);
        alarm_record_view = findViewById(R.id.alarm_record_view);
        door_record_view = findViewById(R.id.door_record_view);
        temporary_password_view = findViewById(R.id.temporary_password_view);
        member_list_view = findViewById(R.id.member_list_view);
        dynamic_password_view = findViewById(R.id.dynamic_password_view);

        String numStr = String.format(Locale.CHINA, getString(R.string.alarm_records), "");
        alarm_record_view.setText(numStr);
    }

    private void initData() {
        DeviceBean deviceBean = ThingOSDevice.getDeviceBean(mDevId);

        //门是否关闭
        if (null != deviceBean && deviceBean.getDpCodes().containsKey("closed_opened")) {
            String closedCode = (String) deviceBean.getDpCodes().get("closed_opened");
            checkDoorOpen(closedCode);
            closed_door_view.setVisibility(View.VISIBLE);
        } else {
            closed_door_view.setVisibility(View.GONE);
        }

        //是否反锁
        if (null != deviceBean && deviceBean.getDpCodes().containsKey("reverse_lock")) {
            Boolean reverseCode = (Boolean) deviceBean.getDpCodes().get("reverse_lock");
            checkReverseOpen(reverseCode);
            anti_lock_view.setVisibility(View.VISIBLE);
        } else {
            anti_lock_view.setVisibility(View.GONE);
        }

        //是否童锁
        if (null != deviceBean && deviceBean.getDpCodes().containsKey("child_lock")) {
            Boolean childCode = (Boolean) deviceBean.getDpCodes().get("child_lock");
            checkChildOpen(childCode);
            child_lock_view.setVisibility(View.VISIBLE);
        } else {
            child_lock_view.setVisibility(View.GONE);
        }

        //电量
        if (null != deviceBean) {
            if (deviceBean.getDpCodes().containsKey("battery_state")) {
                String state = String.valueOf(deviceBean.getDpCodes().get("battery_state"));
                checkResidual(state);
            } else if (deviceBean.getDpCodes().containsKey("residual_electricity")) {
                String residualCode = String.valueOf(deviceBean.getDpCodes().get("residual_electricity"));
                checkResidual(residualCode);
            }
        } else {
            power_view.setText(getString(R.string.zigbee_battery_not_support));
        }

        /**
         * 告警记录
         */
        alarm_record_view.setOnClickListener(v -> {
                    AlarmRecordListActivity.startActivity(this, mDevId);
                }
        );

        /**
         * 开门记录
         */
        door_record_view.setOnClickListener(v ->
                DoorRecordListActivity.startActivity(this, mDevId)
        );

        /**
         * 临时密码
         */
        temporary_password_view.setOnClickListener(v -> PasswordListActivity.startActivity(this, mDevId)

        );
        /**
         * 动态密码
         */
        dynamic_password_view.setOnClickListener(v ->
                PasswordDynamicActivity.startActivity(this, mDevId)
        );
        /**
         * 家庭成员入口
         */
        member_list_view.setOnClickListener(v -> MemberListActivity.startActivity(this, mDevId)
        );
        /**
         * 解绑
         */
        findViewById(R.id.device_delete).setOnClickListener(v -> {
            DeleteDeviceActivity.startActivity(v.getContext(), mDevId);
        });
    }

    private void deviceOnline() {
        DeviceBean deviceBean = ThingOSDevice.getDeviceBean(mDevId);
        //设备是否在线
        if (deviceBean.getIsOnline()) {
            unlock_btn.setTitle(getString(R.string.device_online));
        } else {
            unlock_btn.setTitle(getString(R.string.zigbee_device_offline));
        }
        unlock_btn.setEnabled(false);
    }

    private void remoteUnlock(boolean unlock) {
        wifiLock.replyRemoteUnlock(unlock, new IThingResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                doorOpenSuccess();
            }

            @Override
            public void onError(String code, String message) {
                Log.e(Constant.TAG, "reply remote unlock failed: code = " + code + "  message = " + message);
            }
        });
    }

    private void checkDoorOpen(String closedOpened) {
        //门是否关闭
        if (TextUtils.equals(closedOpened, "open")) {
            closed_door_view.setText(getString(R.string.zigbee_door_not_closed));
        } else {
            closed_door_view.setText(getString(R.string.zigbee_door_is_closed));
        }
    }

    private void checkReverseOpen(Boolean reverseCode) {
        if (reverseCode) {
            anti_lock_view.setText(getString(R.string.zigbee_door_locked));
        } else {
            anti_lock_view.setText(getString(R.string.zigbee_door_not_locked));
        }
    }

    private void checkChildOpen(Boolean childCode) {
        if (childCode) {
            child_lock_view.setText(getString(R.string.zigbee_child_lock_open));
        } else {
            child_lock_view.setText(getString(R.string.zigbee_child_lock_off));
        }
    }

    private void checkResidual(String residualCode) {
        String residual = String.format(Locale.CHINA, getString(R.string.zigbee_power), residualCode);
        power_view.setText(residual);
    }

    /**
     * 开门成功提示
     */
    private void doorOpenSuccess() {
        showToast(getString(R.string.zigbee_operation_suc));
    }

    private void showToast(String msg) {
        runOnUiThread(() -> Toast.makeText(WifiDeviceDetail.this, msg, Toast.LENGTH_SHORT).show());
    }

}
