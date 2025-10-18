package com.tuya.lock.demo.video.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.thingclips.sdk.os.ThingOSDevice;
import com.thingclips.smart.home.sdk.ThingHomeSdk;
import com.thingclips.smart.optimus.lock.api.IThingLockManager;
import com.thingclips.smart.optimus.sdk.ThingOptimusSdk;
import com.thingclips.smart.sdk.api.IDevListener;
import com.thingclips.smart.sdk.api.IThingDevice;
import com.thingclips.smart.sdk.bean.DeviceBean;
import com.thingclips.smart.sdk.optimus.lock.utils.StandardDpConverter;
import com.thingclips.thinglock.videolock.api.IVideoLockManager;
import com.tuya.lock.demo.R;
import com.tuya.lock.demo.ble.activity.detail.DeleteDeviceActivity;
import com.tuya.lock.demo.ble.constant.Constant;
import com.tuya.lock.demo.wifi.activity.MemberListActivity;
import com.tuya.lock.demo.zigbee.view.LockButtonProgressView;

import java.util.Locale;
import java.util.Map;

/**
 * Created by HuiYao on 2024/1/16
 */
public class VideoDeviceDetail extends AppCompatActivity {

    private LockButtonProgressView unlock_btn;
    private TextView closed_door_view;
    private TextView anti_lock_view;
    private TextView child_lock_view;
    private TextView power_view;
    private TextView door_record_view;
    private IThingDevice ITuyaDevice;
    private TextView member_list_view;
    private String mDevId;

    public static void startActivity(Context context, String devId) {
        Intent intent = new Intent(context, VideoDeviceDetail.class);
        //设备id
        intent.putExtra(Constant.DEVICE_ID, devId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_device_detail);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        mDevId = getIntent().getStringExtra(Constant.DEVICE_ID);

        ITuyaDevice = ThingHomeSdk.newDeviceInstance(mDevId);
        ITuyaDevice.registerDevListener(deviceListener);

        initView();
        deviceOnline();
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
        ITuyaDevice.onDestroy();
    }

    private void initView() {
        unlock_btn = findViewById(R.id.unlock_btn);
        closed_door_view = findViewById(R.id.closed_door_view);
        anti_lock_view = findViewById(R.id.anti_lock_view);
        child_lock_view = findViewById(R.id.child_lock_view);
        power_view = findViewById(R.id.power_view);
        door_record_view = findViewById(R.id.door_record_view);
        member_list_view = findViewById(R.id.member_list_view);
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
         * 开门记录
         */
        door_record_view.setOnClickListener(v ->
                LogRecordListActivity.startActivity(this, mDevId)
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

    private void showToast(String msg) {
        runOnUiThread(() -> Toast.makeText(VideoDeviceDetail.this, msg, Toast.LENGTH_SHORT).show());
    }

}
