package com.tuya.lock.demo.ble.activity.member;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.tuya.lock.demo.R;
import com.tuya.lock.demo.ble.adapter.MemberSelectListAdapter;
import com.tuya.lock.demo.ble.constant.Constant;
import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.optimus.lock.api.IThingBleLockV2;
import com.thingclips.smart.optimus.lock.api.IThingLockManager;
import com.thingclips.smart.optimus.sdk.ThingOptimusSdk;
import com.thingclips.smart.sdk.optimus.lock.bean.ble.MemberInfoBean;
import com.thingclips.smart.sdk.optimus.lock.bean.ble.UnlockInfoBean;

import java.util.ArrayList;
import java.util.List;

public class MemberSelectListActivity extends AppCompatActivity {

    private IThingBleLockV2 tuyaLockDevice;
    private MemberSelectListAdapter adapter;
    private List<UnlockInfoBean> unlockInfoBean;

    public static void startActivity(Context context, String devId, List<UnlockInfoBean> infoBean) {
        Intent intent = new Intent(context, MemberSelectListActivity.class);
        //设备id
        intent.putExtra(Constant.DEVICE_ID, devId);
        intent.putExtra(Constant.UNLOCK_INFO_BEAN, JSONArray.toJSONString(infoBean));
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_select_list);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        String mDevId = getIntent().getStringExtra(Constant.DEVICE_ID);
        String allocString = getIntent().getStringExtra(Constant.UNLOCK_INFO_BEAN);
        unlockInfoBean = JSONArray.parseArray(allocString, UnlockInfoBean.class);


        IThingLockManager tuyaLockManager = ThingOptimusSdk.getManager(IThingLockManager.class);
        tuyaLockDevice = tuyaLockManager.getBleLockV2(mDevId);

        RecyclerView rvList = findViewById(R.id.mode_list);
        rvList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        adapter = new MemberSelectListAdapter();
        adapter.setAlloc(new MemberSelectListAdapter.Callback() {
            @Override
            public void alloc(MemberInfoBean memberInfoBean, int position) {
                allocProUnlockOpMode(memberInfoBean);
            }
        });
        rvList.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getProLockMemberList();
    }

    private void getProLockMemberList() {
        tuyaLockDevice.getProLockMemberList(new IThingResultCallback<ArrayList<MemberInfoBean>>() {
            @Override
            public void onSuccess(ArrayList<MemberInfoBean> result) {
                adapter.setData(result);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String code, String message) {
                Log.e(Constant.TAG, "getProLockMemberList failed: code = " + code + "  message = " + message);
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void allocProUnlockOpMode(MemberInfoBean memberInfoBean) {
        List<String> unlockIds = new ArrayList<>();
        for (UnlockInfoBean infoBean : unlockInfoBean) {
            unlockIds.add(infoBean.getUnlockId());
        }
        if (unlockIds.size() == 0) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_unlock_mode_selected), Toast.LENGTH_SHORT).show();
            return;
        }
        tuyaLockDevice.allocProUnlockOpMode(memberInfoBean.getUserId(), unlockIds, new IThingResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Toast.makeText(getApplicationContext(), "alloc onSuccess", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String code, String message) {
                Log.e(Constant.TAG, "allocProUnlockOpMode failed: code = " + code + "  message = " + message);
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}