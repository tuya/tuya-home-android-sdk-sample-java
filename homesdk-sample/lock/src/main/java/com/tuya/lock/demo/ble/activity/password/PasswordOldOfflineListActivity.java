package com.tuya.lock.demo.ble.activity.password;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tuya.lock.demo.R;
import com.tuya.lock.demo.ble.adapter.PasswordOldOfflineListAdapter;
import com.tuya.lock.demo.ble.constant.Constant;
import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.optimus.lock.api.IThingBleLockV2;
import com.thingclips.smart.optimus.lock.api.IThingLockManager;
import com.thingclips.smart.optimus.lock.api.bean.OfflineTempPasswordItem;
import com.thingclips.smart.optimus.lock.api.enums.OfflineTempPasswordStatus;
import com.thingclips.smart.optimus.lock.api.enums.OfflineTempPasswordType;
import com.thingclips.smart.optimus.sdk.ThingOptimusSdk;


import java.util.ArrayList;

public class PasswordOldOfflineListActivity extends AppCompatActivity {

    private IThingBleLockV2 tuyaLockDevice;
    private PasswordOldOfflineListAdapter adapter;
    private OfflineTempPasswordType selectType = OfflineTempPasswordType.MULTIPLE;
    private OfflineTempPasswordStatus selectStatus = OfflineTempPasswordStatus.TO_BE_USED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_old_offline_list);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        String mDevId = getIntent().getStringExtra(Constant.DEVICE_ID);
        String type = getIntent().getStringExtra(Constant.PASSWORD_TYPE);
        switch (type) {
            case Constant.TYPE_SINGLE:
                selectType = OfflineTempPasswordType.SINGLE;
                break;
            case Constant.TYPE_MULTIPLE:
                selectType = OfflineTempPasswordType.MULTIPLE;
                break;
            case Constant.TYPE_CLEAR_ALL:
                selectType = OfflineTempPasswordType.CLEAR_ALL;
                break;
        }


        IThingLockManager tuyaLockManager = ThingOptimusSdk.getManager(IThingLockManager.class);
        tuyaLockDevice = tuyaLockManager.getBleLockV2(mDevId);

        findViewById(R.id.password_offline_add).setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), PasswordOldOfflineAddActivity.class);
            intent.putExtra(Constant.DEVICE_ID, mDevId);
            intent.putExtra(Constant.PASSWORD_TYPE, type);
            v.getContext().startActivity(intent);
        });

        RecyclerView rvList = findViewById(R.id.password_list);
        rvList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        RadioGroup password_offline_state_main = findViewById(R.id.password_offline_state_main);
        password_offline_state_main.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.password_offline_state_to_be_used) {
                selectStatus = OfflineTempPasswordStatus.TO_BE_USED;
            } else if (checkedId == R.id.password_offline_state_used) {
                selectStatus = OfflineTempPasswordStatus.USED;
            } else if (checkedId == R.id.password_offline_state_expired) {
                selectStatus = OfflineTempPasswordStatus.EXPIRED;
            }
            getOfflineTempPasswordList();
        });


        adapter = new PasswordOldOfflineListAdapter();
        adapter.setDevId(mDevId);
        rvList.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getOfflineTempPasswordList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tuyaLockDevice.onDestroy();
    }

    /**
     * 获取门锁成员
     */
    private void getOfflineTempPasswordList() {
        tuyaLockDevice.getOfflinePasswordList(selectType, 0, 10, selectStatus, new IThingResultCallback<ArrayList<OfflineTempPasswordItem>>() {
            @Override
            public void onSuccess(ArrayList<OfflineTempPasswordItem> result) {
                Log.i(Constant.TAG, "getOfflineTempPasswordList success: " + result);
                adapter.setData(result);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                Log.e(Constant.TAG, "getOfflineTempPasswordList failed: code = " + errorCode + "  message = " + errorMessage);
                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

}