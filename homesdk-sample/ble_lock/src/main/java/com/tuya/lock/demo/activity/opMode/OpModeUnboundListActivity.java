package com.tuya.lock.demo.activity.opMode;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.tuya.lock.demo.R;
import com.tuya.lock.demo.activity.member.MemberSelectListActivity;
import com.tuya.lock.demo.adapter.OpModeSelectListAdapter;
import com.tuya.lock.demo.bean.UnlockInfo;
import com.tuya.lock.demo.constant.Constant;
import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.optimus.lock.api.IThingBleLockV2;
import com.thingclips.smart.optimus.lock.api.IThingLockManager;
import com.thingclips.smart.optimus.sdk.ThingOptimusSdk;
import com.thingclips.smart.sdk.optimus.lock.bean.ble.AllocOpModeBean;
import com.thingclips.smart.sdk.optimus.lock.bean.ble.UnlockInfoBean;

import java.util.ArrayList;
import java.util.List;

public class OpModeUnboundListActivity extends AppCompatActivity {

    private IThingBleLockV2 tuyaLockDevice;
    private OpModeSelectListAdapter adapter;
    private final List<UnlockInfo> unlockInfoList = new ArrayList<>();

    public static void startActivity(Context context, String devId) {
        Intent intent = new Intent(context, OpModeUnboundListActivity.class);
        //设备id
        intent.putExtra(Constant.DEVICE_ID, devId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opmode_unbound_list);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        String mDevId = getIntent().getStringExtra(Constant.DEVICE_ID);
        IThingLockManager tuyaLockManager = ThingOptimusSdk.getManager(IThingLockManager.class);
        tuyaLockDevice = tuyaLockManager.getBleLockV2(mDevId);

        RecyclerView rvList = findViewById(R.id.mode_list);
        rvList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        adapter = new OpModeSelectListAdapter();

        rvList.setAdapter(adapter);

        Button select_btn = findViewById(R.id.select_btn);
        select_btn.setOnClickListener(v -> {
            MemberSelectListActivity.startActivity(v.getContext(), mDevId, adapter.getSelectList());
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getUnboundList();
    }

    private void getUnboundList() {
        tuyaLockDevice.getProUnboundUnlockOpModeList(new IThingResultCallback<ArrayList<AllocOpModeBean>>() {
            @Override
            public void onSuccess(ArrayList<AllocOpModeBean> result) {
                Log.i(Constant.TAG, "getProUnboundUnlockOpModeList:" + JSONArray.toJSONString(result));
                unlockInfoList.clear();
                for (AllocOpModeBean itemDetail : result) {
                    String type = "";
                    switch (itemDetail.getOpMode()) {
                        case "1":
                            type = getResources().getString(R.string.mode_fingerprint);
                            break;
                        case "2":
                            type = getResources().getString(R.string.mode_card);
                            break;
                        case "3":
                            type = getResources().getString(R.string.mode_password);
                            break;
                    }

                    UnlockInfo unlockInfo = new UnlockInfo();
                    unlockInfo.count = itemDetail.getUnlockList().size();
                    unlockInfo.type = 0;
                    unlockInfo.name = type;
                    unlockInfoList.add(unlockInfo);

                    for (UnlockInfoBean infoBean : itemDetail.getUnlockList()) {
                        UnlockInfo infoItem = new UnlockInfo();
                        infoItem.type = 1;
                        infoItem.name = infoBean.getUnlockName();
                        infoItem.infoBean = infoBean;
                        unlockInfoList.add(infoItem);
                    }
                }
                adapter.setData(unlockInfoList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String code, String message) {
                Log.e(Constant.TAG, "getProUnboundUnlockOpModeList failed: code = " + code + "  message = " + message);
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}