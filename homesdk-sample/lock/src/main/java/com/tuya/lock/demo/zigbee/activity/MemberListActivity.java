package com.tuya.lock.demo.zigbee.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.thingclips.smart.optimus.lock.api.IThingLockManager;
import com.thingclips.smart.optimus.lock.api.IThingZigBeeLock;
import com.thingclips.smart.optimus.lock.api.zigbee.response.MemberInfoBean;
import com.thingclips.smart.optimus.lock.api.zigbee.response.UnAllocOpModeBean;
import com.thingclips.smart.optimus.sdk.ThingOptimusSdk;
import com.thingclips.smart.sdk.api.IResultCallback;
import com.thingclips.smart.sdk.api.IThingDataCallback;
import com.tuya.lock.demo.R;
import com.tuya.lock.demo.ble.utils.DialogUtils;
import com.tuya.lock.demo.zigbee.adapter.MemberListAdapter;
import com.tuya.lock.demo.zigbee.utils.Constant;

import java.util.ArrayList;

/**
 * 门锁用户成员管理
 */
public class MemberListActivity extends AppCompatActivity {

    private IThingZigBeeLock zigBeeLock;
    private MemberListAdapter adapter;
    private MemberInfoBean mCurrentBean;
    private TextView is_need_alloc_unlock_view;

    public static void startActivity(Context context, String devId) {

        Intent intent = new Intent(context, MemberListActivity.class);
        //设备id
        intent.putExtra(Constant.DEVICE_ID, devId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zigbee_member_list);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        String mDevId = getIntent().getStringExtra(Constant.DEVICE_ID);
        IThingLockManager tuyaLockManager = ThingOptimusSdk.getManager(IThingLockManager.class);
        zigBeeLock = tuyaLockManager.getZigBeeLock(mDevId);

        findViewById(R.id.user_add).setOnClickListener(v -> {
            //添加成员
            MemberDetailActivity.startActivity(v.getContext(), null, mDevId, 0);
        });

        RecyclerView rvList = findViewById(R.id.user_list);
        rvList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        is_need_alloc_unlock_view = findViewById(R.id.is_need_alloc_unlock);
        is_need_alloc_unlock_view.setOnClickListener(v -> {
            //未分配解锁方式列表
            OpModeUnboundListActivity.startActivity(v.getContext(), mDevId);
        });

        adapter = new MemberListAdapter();
        adapter.setDevId(mDevId);
        adapter.deleteUser((infoBean, position) -> {
            if (mCurrentBean.getUserType() == 10 || mCurrentBean.getUserType() == 50) {
                DialogUtils.showDelete(MemberListActivity.this, (dialog, which) ->
                        deleteLockUser(infoBean, position)
                );
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
    }

    /**
     * 获取是否有未分配的解锁方式
     */
    private void isNeedAllocUnlock() {
        zigBeeLock.getUnAllocOpMode(new IThingDataCallback<ArrayList<UnAllocOpModeBean>>() {
            @Override
            public void onSuccess(ArrayList<UnAllocOpModeBean> result) {
                if (null != result && result.size() > 0) {
                    String isNeedAllocTitle = getResources().getString(R.string.lock_opMode_unassigned);
                    is_need_alloc_unlock_view.setText(isNeedAllocTitle);
                    is_need_alloc_unlock_view.setVisibility(View.VISIBLE);
                    findViewById(R.id.is_need_alloc_unlock_line).setVisibility(View.VISIBLE);
                } else {
                    is_need_alloc_unlock_view.setVisibility(View.GONE);
                    findViewById(R.id.is_need_alloc_unlock_line).setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String code, String message) {
                is_need_alloc_unlock_view.setVisibility(View.GONE);
                findViewById(R.id.is_need_alloc_unlock_line).setVisibility(View.GONE);
                Log.e(Constant.TAG, "isProNeedAllocUnlockOpMode failed: code = " + code + "  message = " + message);
            }
        });
    }

    /**
     * 获取门锁成员
     */
    private void getLockUser() {
        zigBeeLock.getMemberList(new IThingDataCallback<ArrayList<MemberInfoBean>>() {
            @Override
            public void onSuccess(ArrayList<MemberInfoBean> result) {
                Log.i(Constant.TAG, "getMemberList success: lockUserBean = " + JSONArray.toJSONString(result));
                adapter.setData(result);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String code, String message) {
                runOnUiThread(() -> {
                    Log.e(Constant.TAG, "getMemberList failed: code = " + code + "  message = " + message);
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                });
            }
        });

        zigBeeLock.getMemberInfo(new IThingDataCallback<MemberInfoBean>() {
            @Override
            public void onSuccess(MemberInfoBean currentBean) {
                mCurrentBean = currentBean;
            }

            @Override
            public void onError(String code, String message) {
                runOnUiThread(() -> {
                    Log.e(Constant.TAG, "getMemberList failed: code = " + code + "  message = " + message);
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /**
     * 删除成员
     */
    private void deleteLockUser(MemberInfoBean infoBean, int position) {
        zigBeeLock.removeMember(infoBean, new IResultCallback() {
            @Override
            public void onError(String code, String error) {
                runOnUiThread(() -> {
                    Log.e(Constant.TAG, "delete lock user failed: code = " + code + "  message = " + error);
                    Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                });
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