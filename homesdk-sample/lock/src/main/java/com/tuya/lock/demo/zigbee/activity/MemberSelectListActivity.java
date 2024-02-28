package com.tuya.lock.demo.zigbee.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.thingclips.smart.android.common.utils.L;
import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.optimus.lock.api.IThingLockManager;
import com.thingclips.smart.optimus.lock.api.IThingZigBeeLock;
import com.thingclips.smart.optimus.lock.api.zigbee.response.MemberInfoBean;
import com.thingclips.smart.optimus.sdk.ThingOptimusSdk;
import com.thingclips.smart.sdk.api.IThingDataCallback;
import com.tuya.lock.demo.R;
import com.tuya.lock.demo.zigbee.adapter.MemberSelectListAdapter;
import com.tuya.lock.demo.zigbee.utils.Constant;

import java.util.ArrayList;
import java.util.List;

public class MemberSelectListActivity extends AppCompatActivity {

    private IThingZigBeeLock iTuyaZigBeeLock;
    private MemberSelectListAdapter adapter;
    private List<String> unlockIds;
    private TextView errorView;
    private int from = 0;//0：未关联成员，1：记录列表进来

    public static void startActivity(Context context, String devId, List<String> unlockIds, int from) {
        Intent intent = new Intent(context, MemberSelectListActivity.class);
        //设备id
        intent.putExtra(Constant.DEVICE_ID, devId);
        intent.putExtra(Constant.UNLOCK_INFO_BEAN, JSONArray.toJSONString(unlockIds));
        intent.putExtra(Constant.FROM, from);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zigbee_member_select_list);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        from = getIntent().getIntExtra(Constant.FROM, 0);
        String mDevId = getIntent().getStringExtra(Constant.DEVICE_ID);
        String allocString = getIntent().getStringExtra(Constant.UNLOCK_INFO_BEAN);
        unlockIds = JSONArray.parseArray(allocString, String.class);


        IThingLockManager tuyaLockManager = ThingOptimusSdk.getManager(IThingLockManager.class);
        iTuyaZigBeeLock = tuyaLockManager.getZigBeeLock(mDevId);

        errorView = findViewById(R.id.error_view);

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
        iTuyaZigBeeLock.getMemberList(new IThingDataCallback<ArrayList<MemberInfoBean>>() {
            @Override
            public void onSuccess(ArrayList<MemberInfoBean> result) {
                adapter.setData(result);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String code, String message) {
                L.e(Constant.TAG, "getProLockMemberList failed: code = " + code + "  message = " + message);
                String errorString = message + "(" + code + ")";
                errorView.setVisibility(View.VISIBLE);
                errorView.setText(errorString);
            }
        });
    }

    private void allocProUnlockOpMode(MemberInfoBean memberInfoBean) {
        if (unlockIds.size() == 0) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_unlock_mode_selected), Toast.LENGTH_SHORT).show();
            return;
        }
        if (from == 1) {
            iTuyaZigBeeLock.bindOpModeToMember(memberInfoBean.getUserId(), unlockIds, new IThingResultCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    Toast.makeText(getApplicationContext(), "alloc onSuccess", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(String code, String message) {
                    L.e(Constant.TAG, "allocProUnlockOpMode failed: code = " + code + "  message = " + message);
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }
        iTuyaZigBeeLock.allocUnlockOpMode(memberInfoBean.getUserId(), unlockIds, new IThingResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Toast.makeText(getApplicationContext(), "alloc onSuccess", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String code, String message) {
                L.e(Constant.TAG, "allocProUnlockOpMode failed: code = " + code + "  message = " + message);
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}