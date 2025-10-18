package com.tuya.lock.demo.wifi.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.optimus.lock.api.IThingLockManager;
import com.thingclips.smart.optimus.lock.api.IThingWifiLock;
import com.thingclips.smart.optimus.lock.api.bean.UnlockRelation;
import com.thingclips.smart.optimus.lock.api.bean.WifiLockUser;
import com.thingclips.smart.optimus.sdk.ThingOptimusSdk;
import com.tuya.lock.demo.R;
import com.tuya.lock.demo.ble.utils.DialogUtils;
import com.tuya.lock.demo.ble.utils.Utils;
import com.tuya.lock.demo.wifi.adapter.OpModeListAdapter;
import com.tuya.lock.demo.zigbee.bean.UnlockInfo;
import com.tuya.lock.demo.zigbee.utils.Constant;

import java.util.ArrayList;
import java.util.List;


/**
 * 添加成员
 */
public class MemberDetailActivity extends AppCompatActivity {

    private WifiLockUser userBean;
    private int mFrom;

    private boolean isUnlockEdit = false;

    private IThingWifiLock wifiLock;

    private final List<UnlockRelation> unlockRelations = new ArrayList<>();

    private OpModeListAdapter adapter;

    public static void startActivity(Context context, WifiLockUser memberInfoBean, String devId, int from) {
        Intent intent = new Intent(context, MemberDetailActivity.class);
        //设备id
        intent.putExtra(Constant.DEVICE_ID, devId);
        //0创建、1编辑
        intent.putExtra(Constant.FROM, from);
        //用户信息
        intent.putExtra(Constant.USER_DATA, JSONObject.toJSONString(memberInfoBean));
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_member_detail);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        String userData = getIntent().getStringExtra(Constant.USER_DATA);
        String mDevId = getIntent().getStringExtra(Constant.DEVICE_ID);
        mFrom = getIntent().getIntExtra(Constant.FROM, 0);
        try {
            userBean = JSONObject.parseObject(userData, WifiLockUser.class);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        if (null == userBean) {
            userBean = new WifiLockUser();
        }

        IThingLockManager tuyaLockManager = ThingOptimusSdk.getManager(IThingLockManager.class);
        wifiLock = tuyaLockManager.getWifiLock(mDevId);

        if (mFrom == 1) {
            toolbar.setTitle(getResources().getString(R.string.submit_edit));
        } else {
            toolbar.setTitle(getResources().getString(R.string.user_add));
        }

        if (userBean.unlockRelations != null) {
            unlockRelations.addAll(userBean.unlockRelations);
        }

        /**
         * 用户昵称
         */
        EditText nameView = findViewById(R.id.user_name);
        nameView.setText(userBean.userName);
        nameView.setEnabled(userBean.userType != 1);
        nameView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    userBean.userName = s.toString();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        /**
         * 头像
         */
        ImageView user_face = findViewById(R.id.user_face);
        if (!TextUtils.isEmpty(userBean.avatarUrl)) {
            Utils.showImageUrl(userBean.avatarUrl, user_face);
        }

        /**
         * 解锁方式列表
         */
        RecyclerView recyclerView = findViewById(R.id.unlock_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        adapter = new OpModeListAdapter();
        adapter.setDeviceId(mDevId);
        adapter.setData(unlockRelations);
        recyclerView.setAdapter(adapter);

        /**
         * 提交创建或更新
         */
        Button submitBtn = findViewById(R.id.edit_user_submit);
        submitBtn.setOnClickListener(v -> {
            if (mFrom == 0) {
                addLockUser();
            } else {
                upDateLockUser();
            }
        });
        if (mFrom == 1) {
            submitBtn.setText(getResources().getString(R.string.submit_edit));
        } else {
            submitBtn.setText(getResources().getString(R.string.submit_add));
        }

        adapter.addCallback(new OpModeListAdapter.Callback() {
            @Override
            public void edit(UnlockInfo info, int position) {
                OpModeDetailActivity.startActivity(MemberDetailActivity.this, Integer.parseInt(info.name), info.dpCode);
            }

            @Override
            public void delete(UnlockInfo info, int position) {
                DialogUtils.showDelete(MemberDetailActivity.this, (dialog, which) ->
                        deleteLockUser(Integer.parseInt(info.name))
                );
            }

            @Override
            public void add(UnlockInfo info, int position) {
                OpModeDetailActivity.startActivity(MemberDetailActivity.this, 0, info.dpCode);
            }
        });
    }

    private void deleteLockUser(int sn) {
        int index = -1;
        for (int i = 0; i < unlockRelations.size(); i++) {
            UnlockRelation item = unlockRelations.get(i);
            if (item.passwordNumber == sn) {
                index = i;
            }
        }
        unlockRelations.remove(index);
        adapter.setData(unlockRelations);
        adapter.notifyDataSetChanged();
        isUnlockEdit = true;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 用户更新信息
     */
    private void addLockUser() {
        wifiLock.addLockUser(userBean.userName, null, unlockRelations, new IThingResultCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.i(Constant.TAG, "add lock user success");
                Toast.makeText(getApplicationContext(), "add lock user success", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String code, String message) {
                runOnUiThread(() -> {
                    Log.e(Constant.TAG, "add lock user failed: code = " + code + "  message = " + message);
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void upDateLockUser() {
        if (isUnlockEdit) {
            wifiLock.updateFamilyUserUnlockMode(userBean.userId, unlockRelations, new IThingResultCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    Log.i(Constant.TAG, "update lock user success");
                    Toast.makeText(getApplicationContext(), "update lock user success", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(String code, String error) {
                    runOnUiThread(() -> {
                        Log.e(Constant.TAG, "add lock user failed: code = " + code + "  message = " + error);
                        Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                    });
                }
            });
            return;
        }
        wifiLock.updateLockUser(userBean.userId, userBean.userName, null, unlockRelations, new IThingResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Log.i(Constant.TAG, "update lock user success");
                Toast.makeText(getApplicationContext(), "update lock user success", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String code, String error) {
                runOnUiThread(() -> {
                    Log.e(Constant.TAG, "add lock user failed: code = " + code + "  message = " + error);
                    Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OpModeDetailActivity.REQUEST_CODE) {
            if (null != data) {
                String unlockData = data.getStringExtra(Constant.UNLOCK_INFO);
                Log.i(Constant.TAG, "onActivityResult====>" + unlockData);
                if (!TextUtils.isEmpty(unlockData)) {
                    UnlockRelation unlockInfo = JSONObject.parseObject(unlockData, UnlockRelation.class);
                    if (unlockInfo.passwordNumber == 0) {
                        return;
                    }
                    unlockRelations.add(unlockInfo);
                    adapter.setData(unlockRelations);
                    adapter.notifyDataSetChanged();
                    isUnlockEdit = true;
                }

            }
        }
    }
}