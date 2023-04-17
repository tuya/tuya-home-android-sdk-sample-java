package com.tuya.lock.demo.activity.records;

import android.os.Bundle;
import android.util.Log;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.tuya.lock.demo.R;
import com.tuya.lock.demo.adapter.RecordListAdapter;
import com.tuya.lock.demo.constant.Constant;
import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.optimus.lock.api.IThingBleLockV2;
import com.thingclips.smart.optimus.lock.api.IThingLockManager;
import com.thingclips.smart.optimus.lock.api.bean.Record;
import com.thingclips.smart.optimus.sdk.ThingOptimusSdk;


/**
 * 开锁记录和告警记录
 */
public class BleLockRecordsActivity extends AppCompatActivity {

    private IThingBleLockV2 tuyaLockDevice;
    private RecordListAdapter listAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_records);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        String deviceId = getIntent().getStringExtra(Constant.DEVICE_ID);
        IThingLockManager tuyaLockManager = ThingOptimusSdk.getManager(IThingLockManager.class);
        tuyaLockDevice = tuyaLockManager.getBleLockV2(deviceId);


        RecyclerView unlock_records_list = findViewById(R.id.unlock_records_list);

        unlock_records_list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        listAdapter = new RecordListAdapter();
        unlock_records_list.setAdapter(listAdapter);
        listAdapter.setDevice(deviceId);

        RadioGroup records_type = findViewById(R.id.records_type);
        records_type.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.unlock_records) {
                getUnlockRecords();
            } else if (checkedId == R.id.alarm_records) {
                getAlarmRecords();
            } else if (checkedId == R.id.hijack_records) {
                getHijackRecords();
            }
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

    private void getAlarmRecords() {
        tuyaLockDevice.getAlarmRecordList(0, 10, new IThingResultCallback<Record>() {
            @Override
            public void onSuccess(Record result) {
                Log.i(Constant.TAG, "get alarm records success: recordBean = " + JSONObject.toJSONString(result));
                listAdapter.setData(result.datas);
                listAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                Log.e(Constant.TAG, "get alarm records failed: code = " + errorCode + "  message = " + errorMessage);
                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getHijackRecords() {
        tuyaLockDevice.getHijackRecords(0, 10, new IThingResultCallback<Record>() {
            @Override
            public void onSuccess(Record result) {
                Log.i(Constant.TAG, "getHijackRecords success: recordBean = " + JSONObject.toJSONString(result));
                listAdapter.setData(result.datas);
                listAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                Log.e(Constant.TAG, "getHijackRecords failed: code = " + errorCode + "  message = " + errorMessage);
                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getUnlockRecords() {
        tuyaLockDevice.getUnlockRecordList(0, 10, new IThingResultCallback<Record>() {
            @Override
            public void onSuccess(Record result) {
                Log.i(Constant.TAG, "get alarm records success: recordBean = " + JSONObject.toJSONString(result));
                listAdapter.setData(result.datas);
                listAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                Log.e(Constant.TAG, "get alarm records failed: code = " + errorCode + "  message = " + errorMessage);
                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}