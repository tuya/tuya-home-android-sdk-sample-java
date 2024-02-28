package com.tuya.lock.demo.zigbee.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.thingclips.smart.android.common.utils.L;
import com.thingclips.smart.optimus.lock.api.IThingLockManager;
import com.thingclips.smart.optimus.lock.api.IThingZigBeeLock;
import com.thingclips.smart.optimus.lock.api.zigbee.response.OpModeBean;
import com.thingclips.smart.optimus.lock.api.zigbee.response.UnAllocLockBean;
import com.thingclips.smart.optimus.lock.api.zigbee.response.UnAllocOpModeBean;
import com.thingclips.smart.optimus.sdk.ThingOptimusSdk;
import com.thingclips.smart.sdk.api.IThingDataCallback;
import com.thingclips.smart.sdk.optimus.lock.bean.ZigBeeDatePoint;
import com.thingclips.smart.sdk.optimus.lock.utils.LockUtil;
import com.tuya.lock.demo.R;
import com.tuya.lock.demo.ble.activity.code.ShowCodeActivity;
import com.tuya.lock.demo.ble.utils.OpModeUtils;
import com.tuya.lock.demo.zigbee.adapter.OpModeSelectListAdapter;
import com.tuya.lock.demo.zigbee.bean.UnlockInfo;
import com.tuya.lock.demo.zigbee.utils.Constant;

import java.util.ArrayList;
import java.util.List;


public class OpModeUnboundListActivity extends AppCompatActivity {

    private IThingZigBeeLock zigBeeLock;
    private OpModeSelectListAdapter adapter;
    private String mDevId;

    private final ArrayList<UnAllocOpModeBean> unAllocOpModeArrayList = new ArrayList<>();

    public static void startActivity(Context context, String devId) {
        Intent intent = new Intent(context, OpModeUnboundListActivity.class);
        //设备id
        intent.putExtra(Constant.DEVICE_ID, devId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zigbee_opmode_unbound_list);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        mDevId = getIntent().getStringExtra(Constant.DEVICE_ID);
        IThingLockManager tuyaLockManager = ThingOptimusSdk.getManager(IThingLockManager.class);
        zigBeeLock = tuyaLockManager.getZigBeeLock(mDevId);

        RecyclerView rvList = findViewById(R.id.mode_list);
        rvList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        adapter = new OpModeSelectListAdapter();

        rvList.setAdapter(adapter);

        Button select_btn = findViewById(R.id.select_btn);
        select_btn.setOnClickListener(v -> {
            MemberSelectListActivity.startActivity(v.getContext(), mDevId, adapter.getSelectList(), 0);
        });

        findViewById(R.id.code_btn).setOnClickListener(v ->
                ShowCodeActivity.startActivity(OpModeUnboundListActivity.this, JSONObject.toJSONString(unAllocOpModeArrayList))
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        getUnboundList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unAllocOpModeArrayList.clear();
    }

    private void getUnboundList() {

        zigBeeLock.getUnAllocOpMode(new IThingDataCallback<ArrayList<UnAllocOpModeBean>>() {
            @Override
            public void onSuccess(ArrayList<UnAllocOpModeBean> result) {
                L.i(Constant.TAG, "getProUnboundUnlockOpModeList:" + JSONArray.toJSONString(result));
                unAllocOpModeArrayList.clear();
                unAllocOpModeArrayList.addAll(result);

                List<UnlockInfo> unlockInfoList = new ArrayList<>();
                List<UnlockInfo> fingerList = new ArrayList<>();
                List<UnlockInfo> passwordList = new ArrayList<>();
                List<UnlockInfo> cradList = new ArrayList<>();

                //指纹标题
                UnlockInfo fingerInfo = new UnlockInfo();
                fingerInfo.dpCode = ZigBeeDatePoint.UNLOCK_FINGERPRINT;
                fingerInfo.name = OpModeUtils.getTypeName(OpModeUnboundListActivity.this, ZigBeeDatePoint.UNLOCK_FINGERPRINT);
                fingerInfo.type = 0;
                fingerList.add(fingerInfo);
                //密码标题
                UnlockInfo passwordInfo = new UnlockInfo();
                passwordInfo.dpCode = ZigBeeDatePoint.UNLOCK_PASSWORD;
                passwordInfo.name = OpModeUtils.getTypeName(OpModeUnboundListActivity.this, ZigBeeDatePoint.UNLOCK_PASSWORD);
                passwordInfo.type = 0;
                passwordList.add(passwordInfo);
                //卡片标题
                UnlockInfo cardInfo = new UnlockInfo();
                cardInfo.dpCode = ZigBeeDatePoint.UNLOCK_CARD;
                cardInfo.name = OpModeUtils.getTypeName(OpModeUnboundListActivity.this, ZigBeeDatePoint.UNLOCK_CARD);
                cardInfo.type = 0;
                cradList.add(cardInfo);


                for (UnAllocOpModeBean itemDetail : result) {
                    for (UnAllocLockBean unlockInfo : itemDetail.getUnlockInfo()) {
                        OpModeBean opModeBean = new OpModeBean();
                        opModeBean.setOpmodeId(unlockInfo.getOpmodeId());
                        opModeBean.setUnlockName(unlockInfo.getUnlockName());
                        opModeBean.setUnlockId(unlockInfo.getUnlockId());

                        String dpCode = LockUtil.convertId2Code(mDevId, itemDetail.getOpmode());
                        UnlockInfo infoItem = new UnlockInfo();
                        infoItem.type = 1;
                        infoItem.dpCode = dpCode;
                        infoItem.infoBean = opModeBean;

                        if (TextUtils.equals(dpCode, ZigBeeDatePoint.UNLOCK_FINGERPRINT)) {
                            fingerList.add(infoItem);
                        } else if (TextUtils.equals(dpCode, ZigBeeDatePoint.UNLOCK_PASSWORD)) {
                            passwordList.add(infoItem);
                        } else if (TextUtils.equals(dpCode, ZigBeeDatePoint.UNLOCK_CARD)) {
                            cradList.add(infoItem);
                        }
                    }
                }
                unlockInfoList.addAll(fingerList);
                unlockInfoList.addAll(passwordList);
                unlockInfoList.addAll(cradList);
                adapter.setData(unlockInfoList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String code, String message) {
                runOnUiThread(() -> {
                    L.e(Constant.TAG, "getProUnboundUnlockOpModeList failed: code = " + code + "  message = " + message);
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}