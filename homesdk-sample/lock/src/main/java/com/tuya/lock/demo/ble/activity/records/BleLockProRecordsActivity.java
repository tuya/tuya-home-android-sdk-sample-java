package com.tuya.lock.demo.ble.activity.records;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.tuya.lock.demo.R;
import com.tuya.lock.demo.ble.adapter.RecordProListAdapter;
import com.tuya.lock.demo.ble.constant.Constant;
import com.tuya.lock.demo.ble.view.FlowRadioGroup;
import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.optimus.lock.api.IThingBleLockV2;
import com.thingclips.smart.optimus.lock.api.IThingLockManager;
import com.thingclips.smart.optimus.lock.api.bean.ProRecord;
import com.thingclips.smart.optimus.sdk.ThingOptimusSdk;
import com.thingclips.smart.sdk.optimus.lock.bean.ble.RecordRequest;

import java.util.ArrayList;


/**
 * 开锁记录和告警记录
 */
public class BleLockProRecordsActivity extends AppCompatActivity {

    private IThingBleLockV2 tuyaLockDevice;
    private RecordProListAdapter listAdapter;
    private final RecordRequest recordRequest = new RecordRequest();
    private final ArrayList<RecordRequest.LogRecord> logRecords = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_pro_records);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        String deviceId = getIntent().getStringExtra(Constant.DEVICE_ID);
        IThingLockManager tuyaLockManager = ThingOptimusSdk.getManager(IThingLockManager.class);
        tuyaLockDevice = tuyaLockManager.getBleLockV2(deviceId);


        RecyclerView unlock_records_list = findViewById(R.id.unlock_records_list);

        unlock_records_list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        listAdapter = new RecordProListAdapter();
        listAdapter.setDevice(deviceId);
        unlock_records_list.setAdapter(listAdapter);

        logRecords.add(RecordRequest.LogRecord.UNLOCK_RECORD);
        logRecords.add(RecordRequest.LogRecord.CLOSE_RECORD);
        logRecords.add(RecordRequest.LogRecord.ALARM_RECORD);
        logRecords.add(RecordRequest.LogRecord.OPERATION);

        FlowRadioGroup records_type = findViewById(R.id.records_type);
        records_type.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.all_records) {
                logRecords.clear();
                logRecords.add(RecordRequest.LogRecord.UNLOCK_RECORD);
                logRecords.add(RecordRequest.LogRecord.CLOSE_RECORD);
                logRecords.add(RecordRequest.LogRecord.ALARM_RECORD);
                logRecords.add(RecordRequest.LogRecord.OPERATION);
            } else if (checkedId == R.id.unlock_records) {
                logRecords.clear();
                logRecords.add(RecordRequest.LogRecord.UNLOCK_RECORD);
            } else if (checkedId == R.id.close_records) {
                logRecords.clear();
                logRecords.add(RecordRequest.LogRecord.CLOSE_RECORD);
            } else if (checkedId == R.id.alarm_records) {
                logRecords.clear();
                logRecords.add(RecordRequest.LogRecord.ALARM_RECORD);
            } else if (checkedId == R.id.operation_records) {
                logRecords.clear();
                logRecords.add(RecordRequest.LogRecord.OPERATION);
            }
            getUnlockRecords();
        });

        getUnlockRecords();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tuyaLockDevice.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void getUnlockRecords() {
        recordRequest.setLogCategories(logRecords);
        recordRequest.setLimit(10);
        tuyaLockDevice.getProUnlockRecordList(recordRequest, new IThingResultCallback<ProRecord>() {
            @Override
            public void onSuccess(ProRecord result) {
                Log.i(Constant.TAG, "get ProUnlock RecordList success: recordBean = " + JSONObject.toJSONString(result));
                listAdapter.setData(result.records);
                listAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                Log.e(Constant.TAG, "get ProUnlock RecordList failed: code = " + errorCode + "  message = " + errorMessage);
                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

}