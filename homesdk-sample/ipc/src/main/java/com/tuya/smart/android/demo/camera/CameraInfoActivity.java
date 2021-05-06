package com.tuya.smart.android.demo.camera;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tuya.smart.android.camera.sdk.TuyaIPCSdk;
import com.tuya.smart.android.camera.sdk.api.ICameraConfigInfo;
import com.tuya.smart.android.camera.sdk.api.ITuyaIPCCore;
import com.tuya.smart.android.camera.sdk.constant.TuyaIPCConstant;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.camera.adapter.CameraInfoAdapter;
import com.tuya.smart.android.demo.camera.utils.Constants;
import com.tuya.smart.camera.ipccamerasdk.p2p.ICameraP2P;

import java.util.ArrayList;
import java.util.List;

public class CameraInfoActivity extends AppCompatActivity {

    private String mDevId;
    private List<String> mData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_info);
        mDevId = getIntent().getStringExtra(Constants.INTENT_DEV_ID);
        initData();
        initView();
    }

    private void initData() {
        ITuyaIPCCore cameraInstance = TuyaIPCSdk.getCameraInstance();
        if (cameraInstance != null) {
            mData = new ArrayList<>();
            mData.add(getString(R.string.low_power) + cameraInstance.isLowPowerDevice(mDevId));
            ICameraConfigInfo cameraConfig = cameraInstance.getCameraConfig(mDevId);
            if (cameraConfig != null) {
                mData.add(getString(R.string.video_num) + cameraConfig.getVideoNum());
                mData.add(getString(R.string.default_definition) + parseClarity(cameraConfig.getDefaultDefinition()));
                mData.add(getString(R.string.is_support_speaker) + cameraConfig.isSupportSpeaker());
                mData.add(getString(R.string.is_support_picK_up) + cameraConfig.isSupportPickup());
                mData.add(getString(R.string.is_support_talk) + cameraConfig.isSupportChangeTalkBackMode());
                mData.add(getString(R.string.default_talk_mode) + cameraConfig.getDefaultTalkBackMode());
                mData.add(getString(R.string.support_speed) + list2String(cameraConfig.getSupportPlaySpeedList()));
                mData.add(getString(R.string.raw_data) + cameraConfig.getRawDataJsonStr());
            }
        }
    }

    private String parseClarity(int clarityMode) {
        String info = getString(R.string.other);
        if (clarityMode == 4) {
            info = getString(R.string.hd);
        } else if (clarityMode == 2) {
            info = getString(R.string.sd);
        }
        return info;
    }

    private String list2String(List<Integer> list) {
        if (list != null && !list.isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < list.size();i ++) {
                stringBuilder.append(list.get(i).toString());
                if (i < list.size() - 1) {
                    stringBuilder.append(", ");
                }
            }
            return stringBuilder.toString();
        } else {
            return "";
        }
    }

    private void initView() {
        if (mData != null) {
            RecyclerView ry = findViewById(R.id.camera_info_ry);
            ry.setLayoutManager(new LinearLayoutManager(this));
            CameraInfoAdapter cameraInfoAdapter = new CameraInfoAdapter(mData);
            ry.setAdapter(cameraInfoAdapter);
            cameraInfoAdapter.notifyDataSetChanged();
        }
    }
}