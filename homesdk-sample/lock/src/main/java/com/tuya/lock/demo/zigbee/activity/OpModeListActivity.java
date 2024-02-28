package com.tuya.lock.demo.zigbee.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.thingclips.sdk.os.ThingOSDevice;
import com.thingclips.smart.android.device.bean.SchemaBean;
import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.optimus.lock.api.IThingLockManager;
import com.thingclips.smart.optimus.lock.api.IThingZigBeeLock;
import com.thingclips.smart.optimus.lock.api.zigbee.response.MemberInfoBean;
import com.thingclips.smart.optimus.lock.api.zigbee.response.OpModeBean;
import com.thingclips.smart.optimus.sdk.ThingOptimusSdk;
import com.thingclips.smart.sdk.bean.DeviceBean;
import com.thingclips.smart.sdk.optimus.lock.bean.ble.OpModeRemoveRequest;
import com.thingclips.smart.sdk.optimus.lock.bean.ble.UnlockDetail;
import com.thingclips.smart.sdk.optimus.lock.utils.LockUtil;
import com.tuya.lock.demo.R;
import com.tuya.lock.demo.ble.activity.code.ShowCodeActivity;
import com.tuya.lock.demo.ble.utils.DialogUtils;
import com.tuya.lock.demo.zigbee.adapter.OpModeListAdapter;
import com.tuya.lock.demo.zigbee.bean.UnlockInfo;
import com.tuya.lock.demo.zigbee.utils.Constant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class OpModeListActivity extends AppCompatActivity {

    private IThingZigBeeLock zigBeeLock;
    private MemberInfoBean memberInfo;
    private OpModeListAdapter adapter;
    private String deviceId;
    private final ArrayList<OpModeBean> opModeBeanArrayList = new ArrayList<>();

    public static void startActivity(Context context, String devId, MemberInfoBean bean) {
        Intent intent = new Intent(context, OpModeListActivity.class);
        intent.putExtra(Constant.DEVICE_ID, devId);
        intent.putExtra(Constant.USER_DATA, JSONObject.toJSONString(bean));
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock_mode_list);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        deviceId = getIntent().getStringExtra(Constant.DEVICE_ID);
        memberInfo = JSONObject.parseObject(getIntent().getStringExtra(Constant.USER_DATA), MemberInfoBean.class);

        IThingLockManager tuyaLockManager = ThingOptimusSdk.getManager(IThingLockManager.class);
        zigBeeLock = tuyaLockManager.getZigBeeLock(deviceId);

        findViewById(R.id.show_code).setOnClickListener(v -> {
            ShowCodeActivity.startActivity(v.getContext(), JSONObject.toJSONString(opModeBeanArrayList));
        });
    }

    private void initData() {
        List<UnlockInfo> unlockInfoList = new ArrayList<>();
        DeviceBean deviceBean = ThingOSDevice.getDeviceBean(deviceId);

        for (UnlockDetail itemDetail : memberInfo.getUnlockDetail()) {
            String dpCode = "";
            String opModeName = "";
            for (Map.Entry<String, SchemaBean> schemaBean : deviceBean.getSchemaMap().entrySet()) {
                if (TextUtils.equals(schemaBean.getKey(), String.valueOf(itemDetail.getDpId()))) {
                    SchemaBean schemaItem = schemaBean.getValue();
                    opModeName = schemaItem.name;
                    dpCode = schemaItem.code;
                    break;
                }
            }
            UnlockInfo unlockInfo = new UnlockInfo();
            unlockInfo.type = 0;
            unlockInfo.count = itemDetail.getUnlockList().size();
            unlockInfo.name = opModeName + "(" + itemDetail.getUnlockList().size() + ")";
            unlockInfo.dpCode = dpCode;
            unlockInfoList.add(unlockInfo);

            for (OpModeBean opModeBean : opModeBeanArrayList) {
                if (TextUtils.equals(String.valueOf(itemDetail.getDpId()), opModeBean.getOpmode())) {
                    String beanDpCode = LockUtil.convertId2Code(deviceId, opModeBean.getOpmode());
                    UnlockInfo infoItem = new UnlockInfo();
                    infoItem.type = 1;
                    infoItem.dpCode = beanDpCode;
                    infoItem.infoBean = opModeBean;
                    unlockInfoList.add(infoItem);
                }
            }
        }

        RecyclerView recyclerView = findViewById(R.id.unlock_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        adapter = new OpModeListAdapter();
        recyclerView.setAdapter(adapter);
        adapter.setData(unlockInfoList);
        adapter.notifyDataSetChanged();
        adapter.addCallback(new OpModeListAdapter.Callback() {
            @Override
            public void edit(UnlockInfo info, int position) {
                OpModeDetailActivity.startEditActivity(OpModeListActivity.this, memberInfo, deviceId, info.infoBean, info.dpCode);
            }

            @Override
            public void delete(UnlockInfo info, int position) {
                DialogUtils.showDelete(OpModeListActivity.this, (dialog, which) -> removeOpMode(info.infoBean, position));
            }

            @Override
            public void add(UnlockInfo info, int position) {
                OpModeDetailActivity.startAddActivity(OpModeListActivity.this, memberInfo, deviceId, info.dpCode);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getListData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void getListData() {
        zigBeeLock.getMemberOpmodeList(memberInfo.getUserId(), new IThingResultCallback<ArrayList<OpModeBean>>() {
            @Override
            public void onSuccess(ArrayList<OpModeBean> result) {
                Log.i(Constant.TAG, "getProBoundUnlockOpModeList:" + JSONObject.toJSONString(result));
                opModeBeanArrayList.clear();
                opModeBeanArrayList.addAll(result);
                initData();
            }

            @Override
            public void onError(String code, String message) {
                runOnUiThread(() -> {
                    Log.i(Constant.TAG, "getProBoundUnlockOpModeList onError code:" + code + ", message:" + message);
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    public void removeOpMode(OpModeBean infoBean, int position) {
        OpModeRemoveRequest removeRequest = new OpModeRemoveRequest();
        removeRequest.setUserId(infoBean.getUserId());
        removeRequest.setLockUserId(infoBean.getLockUserId());
        removeRequest.setUnlockId(infoBean.getUnlockId());
        removeRequest.setOpModeId(infoBean.getOpmodeId());
        removeRequest.setUserType(infoBean.getUserType());
        zigBeeLock.removeUnlockOpmodeForMember(removeRequest, new IThingResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Toast.makeText(getApplicationContext(), "onSuccess", Toast.LENGTH_SHORT).show();
                adapter.remove(position);
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show());
            }
        });
    }
}