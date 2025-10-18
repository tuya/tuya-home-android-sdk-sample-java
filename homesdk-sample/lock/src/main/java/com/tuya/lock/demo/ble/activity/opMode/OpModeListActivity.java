package com.tuya.lock.demo.ble.activity.opMode;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.tuya.lock.demo.R;
import com.tuya.lock.demo.ble.activity.code.ShowCodeActivity;
import com.tuya.lock.demo.ble.adapter.OpModeListAdapter;
import com.tuya.lock.demo.ble.constant.Constant;
import com.tuya.lock.demo.ble.bean.UnlockInfo;
import com.tuya.lock.demo.ble.utils.DialogUtils;
import com.tuya.lock.demo.ble.utils.OpModeUtils;
import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.optimus.lock.api.IThingBleLockV2;
import com.thingclips.smart.optimus.lock.api.IThingLockManager;
import com.thingclips.smart.optimus.sdk.ThingOptimusSdk;
import com.thingclips.smart.sdk.optimus.lock.bean.ble.MemberInfoBean;
import com.thingclips.smart.sdk.optimus.lock.bean.ble.OpModeRemoveRequest;
import com.thingclips.smart.sdk.optimus.lock.bean.ble.UnlockDetail;
import com.thingclips.smart.sdk.optimus.lock.bean.ble.UnlockInfoBean;
import com.thingclips.smart.sdk.optimus.lock.utils.LockUtil;


import java.util.ArrayList;
import java.util.List;


public class OpModeListActivity extends AppCompatActivity {

    private IThingBleLockV2 tuyaLockDevice;
    private final List<UnlockInfo> unlockInfoList = new ArrayList<>();
    private MemberInfoBean memberInfo;
    private OpModeListAdapter adapter;
    private String deviceId;
    private String userId;
    private int lockUserId;

    public static void startActivity(Context context, String devId, String userId, int lockUserId) {
        Intent intent = new Intent(context, OpModeListActivity.class);
        intent.putExtra(Constant.DEVICE_ID, devId);
        intent.putExtra(Constant.USER_ID, userId);
        intent.putExtra(Constant.LOCK_USER_ID, lockUserId);
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
        userId = getIntent().getStringExtra(Constant.USER_ID);
        lockUserId = getIntent().getIntExtra(Constant.LOCK_USER_ID, 0);

        IThingLockManager tuyaLockManager = ThingOptimusSdk.getManager(IThingLockManager.class);
        tuyaLockDevice = tuyaLockManager.getBleLockV2(deviceId);

        findViewById(R.id.show_code).setOnClickListener(v -> {
            ShowCodeActivity.startActivity(v.getContext(), JSONObject.toJSONString(memberInfo));
        });
    }

    private void initData() {
        memberInfo.setLockUserId(lockUserId);
        unlockInfoList.clear();
        for (UnlockDetail itemDetail : memberInfo.getUnlockDetail()) {
            String dpCode = LockUtil.convertId2Code(deviceId, String.valueOf(itemDetail.getDpId()));

            UnlockInfo unlockInfo = new UnlockInfo();
            unlockInfo.type = 0;
            unlockInfo.count = itemDetail.getUnlockList().size();
            unlockInfo.name = OpModeUtils.getTypeName(OpModeListActivity.this, dpCode);
            unlockInfo.dpCode = dpCode;
            unlockInfoList.add(unlockInfo);

            for (UnlockInfoBean infoBean : itemDetail.getUnlockList()) {
                UnlockInfo infoItem = new UnlockInfo();
                infoItem.type = 1;
                infoItem.dpCode = dpCode;
                infoItem.name = infoBean.getUnlockName();
                infoItem.infoBean = infoBean;
                unlockInfoList.add(infoItem);
            }
        }

        RecyclerView recyclerView = findViewById(R.id.unlock_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        adapter = new OpModeListAdapter();
        recyclerView.setAdapter(adapter);
        adapter.setData(unlockInfoList);


        adapter.addCallback(new OpModeListAdapter.Callback() {
            @Override
            public void edit(UnlockInfo info, int position) {
                OpModeDetailActivity.startActivity(OpModeListActivity.this, memberInfo, info.infoBean.getOpModeId(), deviceId, info.dpCode);
            }

            @Override
            public void delete(UnlockInfo info, int position) {
                DialogUtils.showDelete(OpModeListActivity.this, (dialog, which) -> removeOpMode(info, position));
            }

            @Override
            public void add(UnlockInfo info, int position) {
                OpModeDetailActivity.startActivity(OpModeListActivity.this, memberInfo, null, deviceId, info.dpCode);
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
        unlockInfoList.clear();
    }

    private void getListData() {
        tuyaLockDevice.getProBoundUnlockOpModeList(userId, new IThingResultCallback<MemberInfoBean>() {
            @Override
            public void onSuccess(MemberInfoBean result) {
                Log.i(Constant.TAG, "getProBoundUnlockOpModeList:" + JSONObject.toJSONString(result));
                memberInfo = result;
                initData();
            }

            @Override
            public void onError(String code, String message) {
                Log.i(Constant.TAG, "getProBoundUnlockOpModeList onError code:" + code + ", message:" + message);
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void removeOpMode(UnlockInfo info, int position) {
        OpModeRemoveRequest removeRequest = new OpModeRemoveRequest();
        removeRequest.setUserId(memberInfo.getUserId());
        removeRequest.setLockUserId(memberInfo.getLockUserId());
        removeRequest.setUnlockId(info.infoBean.getUnlockId());
        removeRequest.setOpModeId(info.infoBean.getOpModeId());
        removeRequest.setUserType(memberInfo.getUserType());
        tuyaLockDevice.removeProUnlockOpModeForMember(removeRequest, new IThingResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Toast.makeText(getApplicationContext(), "onSuccess", Toast.LENGTH_SHORT).show();
                adapter.remove(position);
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}