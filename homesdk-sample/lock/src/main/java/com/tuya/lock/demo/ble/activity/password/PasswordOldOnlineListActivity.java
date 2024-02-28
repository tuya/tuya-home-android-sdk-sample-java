package com.tuya.lock.demo.ble.activity.password;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tuya.lock.demo.R;
import com.tuya.lock.demo.ble.adapter.PasswordOldOnlineListAdapter;
import com.tuya.lock.demo.ble.constant.Constant;
import com.tuya.lock.demo.ble.utils.DialogUtils;
import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.optimus.lock.api.IThingBleLockV2;
import com.thingclips.smart.optimus.lock.api.IThingLockManager;
import com.thingclips.smart.optimus.sdk.ThingOptimusSdk;
import com.thingclips.smart.sdk.optimus.lock.bean.ble.OnlinePasswordDeleteRequest;
import com.thingclips.smart.sdk.optimus.lock.bean.ble.TempPasswordBeanV3;

import java.util.ArrayList;

public class PasswordOldOnlineListActivity extends AppCompatActivity {

    private IThingBleLockV2 tuyaLockDevice;
    private PasswordOldOnlineListAdapter adapter;
    private int availTimes = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_old_online_list);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        String mDevId = getIntent().getStringExtra(Constant.DEVICE_ID);
        String type = getIntent().getStringExtra(Constant.PASSWORD_TYPE);
        if (type.equals(Constant.TYPE_SINGLE)) {
            availTimes = 1;
        } else if (type.equals(Constant.TYPE_MULTIPLE)) {
            availTimes = 0;
        }

        IThingLockManager tuyaLockManager = ThingOptimusSdk.getManager(IThingLockManager.class);
        tuyaLockDevice = tuyaLockManager.getBleLockV2(mDevId);

        RecyclerView rvList = findViewById(R.id.password_list);
        rvList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        adapter = new PasswordOldOnlineListAdapter();
        adapter.setDevId(mDevId);
        adapter.delete(new PasswordOldOnlineListAdapter.Callback() {
            @Override
            public void remove(TempPasswordBeanV3 bean, int position) {
                DialogUtils.showDelete(PasswordOldOnlineListActivity.this, (dialog, which) ->
                        deletePasscode(bean, position)
                );
            }
        });
        rvList.setAdapter(adapter);

        findViewById(R.id.password_add).setOnClickListener(v -> {
            PasswordOldOnlineDetailActivity.startActivity(v.getContext(), null, mDevId, 0, availTimes);
        });
    }

    private void deletePasscode(TempPasswordBeanV3 tempPasswordBeanV3, int position) {
        OnlinePasswordDeleteRequest deleteRequest = new OnlinePasswordDeleteRequest();
        deleteRequest.setPasswordId(String.valueOf(tempPasswordBeanV3.passwordId));
        deleteRequest.setSn(tempPasswordBeanV3.sn);
        tuyaLockDevice.deleteOnlinePassword(deleteRequest, new IThingResultCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Log.i(Constant.TAG, "deleteOnlineTempPassword onSuccess: " + result);
                Toast.makeText(getApplicationContext(), "delete success", Toast.LENGTH_SHORT).show();
                if (null != adapter) {
                    adapter.remove(position);
                }
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                Log.e(Constant.TAG, "deleteOnlineTempPassword failed: code = " + errorCode + "  message = " + errorMessage);
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
        tuyaLockDevice.getOnlinePasswordList(availTimes, new IThingResultCallback<ArrayList<TempPasswordBeanV3>>() {
            @Override
            public void onSuccess(ArrayList<TempPasswordBeanV3> result) {
                Log.i(Constant.TAG, "getOnlineTempPasswordList success: " + result);
                adapter.setData(result);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                Log.e(Constant.TAG, "getOnlineTempPasswordList failed: code = " + errorCode + "  message = " + errorMessage);
                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}