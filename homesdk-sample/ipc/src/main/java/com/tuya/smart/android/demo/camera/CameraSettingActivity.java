package com.tuya.smart.android.demo.camera;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.alibaba.fastjson.JSONObject;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.camera.utils.Constants;
import com.tuya.smart.android.demo.camera.utils.DPConstants;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.IDevListener;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.api.ITuyaDevice;
import com.tuya.smart.sdk.bean.DeviceBean;

import java.util.Map;

public class CameraSettingActivity extends AppCompatActivity {

    private static final String TAG = CameraSettingActivity.class.getSimpleName();
    private String devId;
    private ITuyaDevice iTuyaDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_setting);
        Toolbar toolbar = findViewById(R.id.toolbar_view);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        devId = getIntent().getStringExtra(Constants.INTENT_DEV_ID);

        sdStatus();
        sdCardFormat();
        watermark();
        record();
    }


    private void sdStatus() {
        String dpId = DPConstants.SD_STATUS;
        TextView tv = findViewById(R.id.tv_sd_status);
        Object value = queryValueByDPID(dpId);
        if (value != null) {
            tv.setText(String.valueOf(value));
            listenDPUpdate(dpId, new DPCallback() {
                @Override
                public void callback(Object obj) {
                    tv.setText(String.valueOf(value));
                }
            });
        } else {
            tv.setText(getString(R.string.not_support));
        }
    }

    private void sdCardFormat() {
        TextView tv = findViewById(R.id.tv_sd_format);
        Object value = queryValueByDPID(DPConstants.SD_STORAGE);
        if (value != null) {
            tv.setText(String.valueOf(value));
            Button btn = findViewById(R.id.btn_sd_format);
            Object formatValue = queryValueByDPID(DPConstants.SD_FORMAT);
            if (formatValue != null) {
                btn.setVisibility(View.VISIBLE);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        publishDps(DPConstants.SD_FORMAT, true);
                        listenDPUpdate(DPConstants.SD_FORMAT_STATUS, new DPCallback() {
                            @Override
                            public void callback(Object obj) {
                                tv.setText(getString(R.string.format_status) + obj);
                                if ("100".equals(String.valueOf(obj))) {
                                    tv.setText(String.valueOf(queryValueByDPID(DPConstants.SD_STORAGE)));
                                }
                            }
                        });
                    }
                });
            }
        } else {
            tv.setText(getString(R.string.not_support));
        }
    }

    private void watermark() {
        String dpId = DPConstants.WATERMARK;
        TextView tv = findViewById(R.id.tv_watermark);
        Object value = queryValueByDPID(dpId);
        if (value != null) {
            tv.setText(String.valueOf(value));
            Button btn = findViewById(R.id.btn_watermark);
            btn.setVisibility(View.VISIBLE);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    publishDps(DPConstants.WATERMARK, !Boolean.parseBoolean(tv.getText().toString()));
                }
            });
            listenDPUpdate(dpId, new DPCallback() {
                @Override
                public void callback(Object obj) {
                    tv.setText(String.valueOf(obj));
                }
            });
        } else {
            tv.setText(getString(R.string.not_support));
        }
    }

    private void record() {
        String dpId = DPConstants.SD_CARD_RECORD_SWITCH;
        TextView tv = findViewById(R.id.tv_record);
        Object value = queryValueByDPID(dpId);
        if (value != null) {
            tv.setText(String.valueOf(value));
            Button btn = findViewById(R.id.btn_record);
            btn.setVisibility(View.VISIBLE);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    publishDps(DPConstants.SD_CARD_RECORD_SWITCH, !Boolean.parseBoolean(tv.getText().toString()));
                }
            });
            listenDPUpdate(dpId, new DPCallback() {
                @Override
                public void callback(Object obj) {
                    tv.setText(String.valueOf(obj));
                }
            });
        } else {
            tv.setText(getString(R.string.not_support));
        }
    }

    private Object queryValueByDPID(String dpId) {
        DeviceBean deviceBean = TuyaHomeSdk.getDataInstance().getDeviceBean(devId);
        if (deviceBean != null) {
            Map<String, Object> dps = deviceBean.getDps();
            if (dps != null) {
                return dps.get(dpId);
            }
        }
        return null;
    }

    private void publishDps(String dpId, Object value) {
        if (iTuyaDevice == null) {
            iTuyaDevice = TuyaHomeSdk.newDeviceInstance(devId);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(dpId, value);
        String dps = jsonObject.toString();
        iTuyaDevice.publishDps(dps, new IResultCallback() {
            @Override
            public void onError(String code, String error) {
                Log.e(TAG, "publishDps err " + dps);
            }

            @Override
            public void onSuccess() {
                Log.i(TAG, "publishDps suc " + dps);
            }
        });
    }

    private void listenDPUpdate(String dpId, DPCallback callback) {
        TuyaHomeSdk.newDeviceInstance(devId).registerDevListener(new IDevListener() {
            @Override
            public void onDpUpdate(String devId, String dpStr) {
                if (callback != null) {
                    Map<String, Object> dps = (Map) JSONObject.parseObject(dpStr, Map.class);
                    if (dps.containsKey(dpId)) {
                        callback.callback(dps.get(dpId));
                    }
                }
            }

            @Override
            public void onRemoved(String devId) {

            }

            @Override
            public void onStatusChanged(String devId, boolean online) {

            }

            @Override
            public void onNetworkStatusChanged(String devId, boolean status) {

            }

            @Override
            public void onDevInfoUpdate(String devId) {

            }
        });
    }

    private interface DPCallback {
        void callback(Object obj);
    }
}
