package com.tuya.lock.demo.activity.setting;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.alibaba.fastjson.JSONObject;
import com.tuya.lock.demo.R;
import com.tuya.lock.demo.constant.Constant;
import com.tuya.lock.demo.utils.DialogUtils;
import com.thingclips.smart.home.sdk.ThingHomeSdk;
import com.thingclips.smart.sdk.api.IDevListener;
import com.thingclips.smart.sdk.api.IResultCallback;
import com.thingclips.smart.sdk.api.IThingDevice;
import com.thingclips.smart.sdk.bean.DeviceBean;
import com.thingclips.smart.sdk.optimus.lock.utils.LockUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 门锁设置
 */
public class DpSettingActivity extends AppCompatActivity {


    private IThingDevice IThingDevice;
    private SwitchCompat set_time_delay_switch;
    private TextView set_time_delay_select;
    private String mDevId;
    private int timeInt = 0;
    private RadioGroup language_select_group;
    private RadioButton language_china;
    private RadioButton language_english;

    public static void startActivity(Context context, String devId) {
        Intent intent = new Intent(context, DpSettingActivity.class);
        //设备id
        intent.putExtra(Constant.DEVICE_ID, devId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dp_setting);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        mDevId = getIntent().getStringExtra(Constant.DEVICE_ID);

        IThingDevice = ThingHomeSdk.newDeviceInstance(mDevId);
        IThingDevice.registerDevListener(listener);

        language_select_group = findViewById(R.id.language_select_group);
        language_china = findViewById(R.id.language_china);
        language_english = findViewById(R.id.language_english);

        set_time_delay_switch = findViewById(R.id.set_time_delay_switch);
        set_time_delay_select = findViewById(R.id.set_time_delay_select);

        LinearLayout time_delay_select_wrap = findViewById(R.id.time_delay_select_wrap);
        time_delay_select_wrap.setOnClickListener(v -> {
            if (!getIsOnline()) {
                Toast.makeText(v.getContext(), "device offline", Toast.LENGTH_SHORT).show();
                return;
            }
            DialogUtils.showNumberEdit(v.getContext(), timeInt, number -> {
                timeInt = number;
                String timeValue = number + "s";
                set_time_delay_select.setText(timeValue);
                //下发dp
                String dpId = LockUtil.convertCode2Id(mDevId, "auto_lock_time");
                Map<String, Object> dpMap = new HashMap<>();
                dpMap.put(dpId, timeInt);
                publishDps(dpMap);
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        IThingDevice.unRegisterDevListener();
        IThingDevice.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initUi();
    }

    private boolean getIsOnline() {
        DeviceBean deviceBean = ThingHomeSdk.getDataInstance().getDeviceBean(mDevId);
        if (null == deviceBean) {
            return false;
        }
        return deviceBean.getIsOnline();
    }

    private void initUi() {
        DeviceBean deviceBean = ThingHomeSdk.getDataInstance().getDeviceBean(mDevId);
        if (null == deviceBean) {
            return;
        }
        boolean isOnline = deviceBean.getIsOnline();
        String automatic_lock_dp_id = LockUtil.convertCode2Id(mDevId, "automatic_lock");
        String auto_lock_time_dp_id = LockUtil.convertCode2Id(mDevId, "auto_lock_time");

        if (null != automatic_lock_dp_id) {
            String automatic_lock_dp_value = String.valueOf(deviceBean.getDps().get(automatic_lock_dp_id));
            set_time_delay_switch.setEnabled(isOnline);
            set_time_delay_switch.setChecked(automatic_lock_dp_value.equals("true"));
        } else {
            set_time_delay_switch.setEnabled(false);
        }

        set_time_delay_switch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Map<String, Object> data = new HashMap<>();
            data.put(automatic_lock_dp_id, isChecked);
            publishDps(data);
        });

        if (null != auto_lock_time_dp_id) {
            if (null != deviceBean.getDps().get(auto_lock_time_dp_id)) {
                timeInt = Integer.parseInt(String.valueOf(deviceBean.getDps().get(auto_lock_time_dp_id)));
            }
            String timeValue = timeInt + "s";
            set_time_delay_select.setText(timeValue);
        } else {
            set_time_delay_select.setText("不支持");
        }

        /**
         * chinese_simplified, english, japanese, german, spanish, latin, french, russian, italian, chinese_traditional, korean
         */
        String language_id = LockUtil.convertCode2Id(mDevId, "language");
        if (TextUtils.isEmpty(language_id)) {
            findViewById(R.id.language_select_wrap).setVisibility(View.GONE);
            findViewById(R.id.language_select_line).setVisibility(View.GONE);
        } else {
            findViewById(R.id.language_select_wrap).setVisibility(View.VISIBLE);
            findViewById(R.id.language_select_line).setVisibility(View.VISIBLE);


            String language_value = (String) deviceBean.getDps().get(language_id);
            if (TextUtils.equals(language_value, "chinese_simplified")) {
                language_select_group.check(R.id.language_china);
            } else {
                language_select_group.check(R.id.language_english);
            }
            language_select_group.setOnCheckedChangeListener((group, checkedId) -> {
                String languageSelect = "english";
                if (checkedId == R.id.language_china) {
                    languageSelect = "chinese_simplified";
                } else if (checkedId == R.id.language_english) {
                    languageSelect = "english";
                }
                Map<String, Object> data = new HashMap<>();
                data.put(language_id, languageSelect);
                publishDps(data);
            });
        }
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
            set_time_delay_switch.setEnabled(online);
            language_select_group.setEnabled(online);
        }

        @Override
        public void onNetworkStatusChanged(String devId, boolean status) {

        }

        @Override
        public void onDevInfoUpdate(String devId) {

        }
    };


    private void publishDps(Map<String, Object> data) {
        IThingDevice.publishDps(JSONObject.toJSONString(data), new IResultCallback() {
            @Override
            public void onError(String code, String message) {
                Log.e(Constant.TAG, "publishDps code:" + code + ", message:" + message);
            }

            @Override
            public void onSuccess() {
                Log.i(Constant.TAG, "publishDps onSuccess");
            }
        });
    }
}