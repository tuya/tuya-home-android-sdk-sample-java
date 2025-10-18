package com.tuya.lock.demo.wifi.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.optimus.lock.api.IThingLockManager;
import com.thingclips.smart.optimus.lock.api.IThingWifiLock;
import com.thingclips.smart.optimus.lock.api.bean.Record;
import com.thingclips.smart.optimus.sdk.ThingOptimusSdk;
import com.thingclips.smart.sdk.optimus.lock.utils.LockUtil;
import com.tuya.lock.demo.R;
import com.tuya.lock.demo.ble.constant.Constant;
import com.tuya.lock.demo.wifi.adapter.RecordListAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * WIFI告警记录
 */
public class AlarmRecordListActivity extends AppCompatActivity {

    private IThingWifiLock wifiLock;
    private RecordListAdapter adapter;
    private RecyclerView recyclerView;
    private TextView error_view;

    private String mDevId;

    public static void startActivity(Context context, String devId) {
        Intent intent = new Intent(context, AlarmRecordListActivity.class);
        intent.putExtra(Constant.DEVICE_ID, devId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zigbee_record_list);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        mDevId = getIntent().getStringExtra(Constant.DEVICE_ID);
        IThingLockManager tuyaLockManager = ThingOptimusSdk.getManager(IThingLockManager.class);
        wifiLock = tuyaLockManager.getWifiLock(mDevId);

        error_view = findViewById(R.id.error_view);
        recyclerView = findViewById(R.id.record_list_view);
        adapter = new RecordListAdapter();
        adapter.setDevice(mDevId);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wifiLock.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        List<String> dpIds = new ArrayList<>();
        dpIds.add(LockUtil.convertCode2Id(mDevId, "hijack"));
        dpIds.add(LockUtil.convertCode2Id(mDevId, "alarm_lock"));
        dpIds.add(LockUtil.convertCode2Id(mDevId, "doorbell"));
        wifiLock.getRecords(dpIds, 0, 30, new IThingResultCallback<Record>() {
            @Override
            public void onSuccess(Record result) {
                if (result.datas.size() == 0) {
                    showError(getString(R.string.zigbee_no_content));
                } else {
                    adapter.setData(result.datas);
                    adapter.notifyDataSetChanged();
                    recyclerView.setVisibility(View.VISIBLE);
                    error_view.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                showError(errorMessage);
            }
        });
    }

    private void showError(String msg) {
        recyclerView.post(() -> {
            recyclerView.setVisibility(View.GONE);
            error_view.setVisibility(View.VISIBLE);
            error_view.setText(msg);
        });
    }
}
