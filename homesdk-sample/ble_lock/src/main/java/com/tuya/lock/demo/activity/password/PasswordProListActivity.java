package com.tuya.lock.demo.activity.password;

import android.os.Bundle;
import android.util.Log;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tuya.lock.demo.R;
import com.tuya.lock.demo.adapter.PasswordProListAdapter;
import com.tuya.lock.demo.constant.Constant;
import com.tuya.lock.demo.utils.DialogUtils;
import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.optimus.lock.api.IThingBleLockV2;
import com.thingclips.smart.optimus.lock.api.IThingLockManager;
import com.thingclips.smart.optimus.lock.api.bean.ProTempPasswordItem;
import com.thingclips.smart.optimus.lock.api.enums.ProPasswordListTypeEnum;
import com.thingclips.smart.optimus.sdk.ThingOptimusSdk;
import com.thingclips.smart.sdk.optimus.lock.bean.ble.OnlinePasswordDeleteRequest;

import java.util.ArrayList;
import java.util.List;

public class PasswordProListActivity extends AppCompatActivity {

    private IThingBleLockV2 tuyaLockDevice;
    private PasswordProListAdapter adapter;
    private final List<ProPasswordListTypeEnum> authTypes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_pro_offline_list);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        String mDevId = getIntent().getStringExtra(Constant.DEVICE_ID);
        IThingLockManager tuyaLockManager = ThingOptimusSdk.getManager(IThingLockManager.class);
        tuyaLockDevice = tuyaLockManager.getBleLockV2(mDevId);

        RecyclerView rvList = findViewById(R.id.password_list);
        rvList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        authTypes.add(ProPasswordListTypeEnum.LOCK_BLUE_PASSWORD);
        authTypes.add(ProPasswordListTypeEnum.LOCK_OFFLINE_TEMP_PWD);
        authTypes.add(ProPasswordListTypeEnum.LOCK_TEMP_PWD);

        RadioGroup list_type_main = findViewById(R.id.list_type_main);
        list_type_main.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.list_type_all) {
                authTypes.clear();
                authTypes.add(ProPasswordListTypeEnum.LOCK_BLUE_PASSWORD);
                authTypes.add(ProPasswordListTypeEnum.LOCK_OFFLINE_TEMP_PWD);
                authTypes.add(ProPasswordListTypeEnum.LOCK_TEMP_PWD);
            } else if (checkedId == R.id.list_type_online) {
                authTypes.clear();
                authTypes.add(ProPasswordListTypeEnum.LOCK_TEMP_PWD);
            } else if (checkedId == R.id.list_type_offline) {
                authTypes.clear();
                authTypes.add(ProPasswordListTypeEnum.LOCK_OFFLINE_TEMP_PWD);
            }
            getOfflineTempPasswordList();
        });

        adapter = new PasswordProListAdapter();
        adapter.setDevId(mDevId);
        adapter.delete(new PasswordProListAdapter.Callback() {
            @Override
            public void remove(ProTempPasswordItem passwordItem, int position) {
                DialogUtils.showDelete(PasswordProListActivity.this, (dialog, which) -> deletePasscode(passwordItem, position));
            }
        });
        rvList.setAdapter(adapter);
    }

    private void deletePasscode(ProTempPasswordItem passwordItem, int position) {
        OnlinePasswordDeleteRequest deleteRequest = new OnlinePasswordDeleteRequest();
        deleteRequest.setSn(passwordItem.getSn());
        deleteRequest.setPasswordId(passwordItem.getUnlockBindingId());
        tuyaLockDevice.deleteProOnlinePassword(deleteRequest, new IThingResultCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.i(Constant.TAG, "deleteProOnlineTempPassword success: " + result);
                Toast.makeText(getApplicationContext(), "delete success", Toast.LENGTH_SHORT).show();
                adapter.remove(position);
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                Log.e(Constant.TAG, "deleteProOnlineTempPassword failed: code = " + errorCode + "  message = " + errorMessage);
                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
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
        tuyaLockDevice.getProPasswordList(authTypes, new IThingResultCallback<ArrayList<ProTempPasswordItem>>() {
            @Override
            public void onSuccess(ArrayList<ProTempPasswordItem> result) {
                adapter.setData(result);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                Log.e(Constant.TAG, "getProTempPasswordList failed: code = " + errorCode + "  message = " + errorMessage);
                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}