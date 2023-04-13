package com.tuya.lock.demo.activity.member;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.tuya.lock.demo.R;
import com.tuya.lock.demo.activity.opMode.OpModeUnboundListActivity;
import com.tuya.lock.demo.adapter.MemberListAdapter;
import com.tuya.lock.demo.constant.Constant;
import com.tuya.lock.demo.utils.DialogUtils;
import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.optimus.lock.api.IThingBleLockV2;
import com.thingclips.smart.optimus.lock.api.IThingLockManager;
import com.thingclips.smart.optimus.sdk.ThingOptimusSdk;
import com.thingclips.smart.sdk.api.IResultCallback;
import com.thingclips.smart.sdk.optimus.lock.bean.ble.MemberInfoBean;

import java.util.ArrayList;

/**
 * 门锁用户成员管理
 */
public class MemberListActivity extends AppCompatActivity {

    private IThingBleLockV2 tuyaLockDevice;
    private MemberListAdapter adapter;
    private TextView is_need_alloc_unlock_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_list);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        String mDevId = getIntent().getStringExtra(Constant.DEVICE_ID);
        IThingLockManager tuyaLockManager = ThingOptimusSdk.getManager(IThingLockManager.class);
        tuyaLockDevice = tuyaLockManager.getBleLockV2(mDevId);

        findViewById(R.id.user_add).setOnClickListener(v -> {
            MemberDetailActivity.startActivity(v.getContext(), null, mDevId, 0);
        });

        RecyclerView rvList = findViewById(R.id.user_list);
        rvList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        is_need_alloc_unlock_view = findViewById(R.id.is_need_alloc_unlock);
        is_need_alloc_unlock_view.setOnClickListener(v -> {
            OpModeUnboundListActivity.startActivity(v.getContext(), mDevId);
        });

        adapter = new MemberListAdapter();
        adapter.setDevId(mDevId);
        adapter.setProDevice(tuyaLockDevice.isProDevice());
        adapter.deleteUser(new MemberListAdapter.Callback() {
            @Override
            public void remove(MemberInfoBean infoBean, int position) {
                DialogUtils.showDelete(MemberListActivity.this, (dialog, which) -> deleteLockUser(infoBean, position));
            }
        });
        rvList.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLockUser();
        isNeedAllocUnlock();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tuyaLockDevice.onDestroy();
    }

    /**
     * 获取是否有未分配的解锁方式
     */
    private void isNeedAllocUnlock() {
        tuyaLockDevice.isProNeedAllocUnlockOpMode(new IThingResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                String isNeedAllocTitle = getResources().getString(R.string.lock_opMode_unassigned) + ": " + result;
                is_need_alloc_unlock_view.setText(isNeedAllocTitle);
            }

            @Override
            public void onError(String code, String message) {
                Log.e(Constant.TAG, "isProNeedAllocUnlockOpMode failed: code = " + code + "  message = " + message);
//                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 获取门锁成员
     */
    private void getLockUser() {
        tuyaLockDevice.getProLockMemberList(new IThingResultCallback<ArrayList<MemberInfoBean>>() {
            @Override
            public void onSuccess(ArrayList<MemberInfoBean> result) {
                Log.i(Constant.TAG, "getProLockMemberList success: lockUserBean = " + JSONArray.toJSONString(result));
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

    /**
     * 删除成员
     */
    private void deleteLockUser(MemberInfoBean infoBean, int position) {
        tuyaLockDevice.removeProLockMember(infoBean, new IResultCallback() {
            @Override
            public void onError(String code, String error) {
                Log.e(Constant.TAG, "delete lock user failed: code = " + code + "  message = " + error);
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess() {
                Log.i(Constant.TAG, "delete lock user success");
                Toast.makeText(getApplicationContext(), "delete lock user success", Toast.LENGTH_SHORT).show();
                adapter.remove(position);
            }
        });
    }
}