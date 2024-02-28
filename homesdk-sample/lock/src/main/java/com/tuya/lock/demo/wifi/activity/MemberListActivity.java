package com.tuya.lock.demo.wifi.activity;

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
import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.optimus.lock.api.IThingLockManager;
import com.thingclips.smart.optimus.lock.api.IThingWifiLock;
import com.thingclips.smart.optimus.lock.api.bean.WifiLockUser;
import com.thingclips.smart.optimus.sdk.ThingOptimusSdk;
import com.tuya.lock.demo.R;
import com.tuya.lock.demo.ble.constant.Constant;
import com.tuya.lock.demo.ble.utils.DialogUtils;
import com.tuya.lock.demo.wifi.adapter.MemberListAdapter;

import java.util.List;

/**
 * 门锁用户成员管理
 */
public class MemberListActivity extends AppCompatActivity {

    private IThingWifiLock wifiLock;
    private MemberListAdapter adapter;

    public static void startActivity(Context context, String devId) {

        Intent intent = new Intent(context, MemberListActivity.class);
        //设备id
        intent.putExtra(Constant.DEVICE_ID, devId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_member_list);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        String mDevId = getIntent().getStringExtra(Constant.DEVICE_ID);
        IThingLockManager tuyaLockManager = ThingOptimusSdk.getManager(IThingLockManager.class);
        wifiLock = tuyaLockManager.getWifiLock(mDevId);

        findViewById(R.id.user_add).setOnClickListener(v -> {
            //添加成员
            MemberDetailActivity.startActivity(v.getContext(), null, mDevId, 0);
        });

        RecyclerView rvList = findViewById(R.id.user_list);
        rvList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        adapter = new MemberListAdapter();
        adapter.setDevId(mDevId);
        adapter.deleteUser((infoBean, position) -> {
            DialogUtils.showDelete(MemberListActivity.this, (dialog, which) ->
                    deleteLockUser(infoBean.userId, position)
            );
        });
        rvList.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLockUser();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wifiLock.onDestroy();
    }

    /**
     * 获取门锁成员
     */
    private void getLockUser() {
        wifiLock.getLockUsers(new IThingResultCallback<List<WifiLockUser>>() {
            @Override
            public void onSuccess(List<WifiLockUser> result) {
                Log.i(Constant.TAG, "getMemberList success: lockUserBean = " + JSONArray.toJSONString(result));
                adapter.setData(result);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String code, String message) {
                Log.e(Constant.TAG, "getMemberList failed: code = " + code + "  message = " + message);
                showToast(message);
            }
        });
    }

    /**
     * 删除成员
     */
    private void deleteLockUser(String userId, int position) {
        wifiLock.deleteLockUser(userId, new IThingResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Log.i(Constant.TAG, "delete lock user success");
                showToast("delete lock user success");
                adapter.remove(position);
            }

            @Override
            public void onError(String code, String error) {
                Log.e(Constant.TAG, "delete lock user failed: code = " + code + "  message = " + error);
                showToast(error);
            }
        });
    }

    private void showToast(String msg) {
        runOnUiThread(() -> {
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        });
    }
}