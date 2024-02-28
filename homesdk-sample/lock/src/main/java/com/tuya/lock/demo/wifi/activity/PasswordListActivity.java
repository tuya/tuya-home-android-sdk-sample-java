package com.tuya.lock.demo.wifi.activity;


import static com.tuya.lock.demo.zigbee.utils.Constant.DEVICE_ID;

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

import com.thingclips.smart.android.common.utils.L;
import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.optimus.lock.api.IThingLockManager;
import com.thingclips.smart.optimus.lock.api.IThingWifiLock;
import com.thingclips.smart.optimus.lock.api.bean.TempPassword;
import com.thingclips.smart.optimus.sdk.ThingOptimusSdk;
import com.tuya.lock.demo.R;
import com.tuya.lock.demo.ble.constant.Constant;
import com.tuya.lock.demo.ble.utils.DialogUtils;
import com.tuya.lock.demo.wifi.adapter.PasswordListAdapter;

import java.util.List;

/**
 * WIFI 临时密码列表
 */
public class PasswordListActivity extends AppCompatActivity {

    private IThingWifiLock wifiLock;
    private PasswordListAdapter adapter;
    private RecyclerView password_list;
    private TextView error_view;

    public static void startActivity(Context context, String devId) {
        Intent intent = new Intent(context, PasswordListActivity.class);
        //设备id
        intent.putExtra(DEVICE_ID, devId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_password_list);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        String mDevId = getIntent().getStringExtra(Constant.DEVICE_ID);

        IThingLockManager tuyaLockManager = ThingOptimusSdk.getManager(IThingLockManager.class);
        wifiLock = tuyaLockManager.getWifiLock(mDevId);

        error_view = findViewById(R.id.error_view);
        password_list = findViewById(R.id.password_list);
        password_list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        adapter = new PasswordListAdapter();
        adapter.addCallback((bean, position) ->
                DialogUtils.showDelete(PasswordListActivity.this, (dialog, which) ->
                        deletePasscode(bean.id, position)
                ));
        password_list.setAdapter(adapter);

        findViewById(R.id.password_add).setOnClickListener(v -> {
            PasswordDetailActivity.startActivity(v.getContext(), mDevId);
        });
    }

    private void deletePasscode(int passwordId, int position) {
        wifiLock.deleteTempPassword(passwordId, new IThingResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Log.i(Constant.TAG, "deleteOnlineTempPassword onSuccess: " + result);
                showToast("delete success");
                if (null != adapter) {
                    adapter.remove(position);
                }
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                Log.e(Constant.TAG, "deleteOnlineTempPassword failed: code = " + errorCode + "  message = " + errorMessage);
                showToast(errorMessage);
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
        wifiLock.onDestroy();
    }

    /**
     * 密码列表
     */
    private void getOfflineTempPasswordList() {
        wifiLock.getTempPasswords(new IThingResultCallback<List<TempPassword>>() {
            @Override
            public void onSuccess(List<TempPassword> result) {
                L.i(Constant.TAG, "getOnlineTempPasswordList success: " + result);
                if (result.size() == 0) {
                    showError("No content");
                } else {
                    adapter.setData(result);
                    adapter.notifyDataSetChanged();
                    password_list.setVisibility(View.VISIBLE);
                    error_view.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                L.e(Constant.TAG, "getTempPasswords failed: code = " + errorCode + "  message = " + errorMessage);
                showError(errorMessage);
            }
        });
    }

    private void showError(String msg) {
        password_list.setVisibility(View.GONE);
        error_view.setVisibility(View.VISIBLE);
        error_view.setText(msg);
    }

    private void showToast(String msg) {
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show());
    }

}