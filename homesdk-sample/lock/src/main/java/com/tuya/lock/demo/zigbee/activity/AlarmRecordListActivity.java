package com.tuya.lock.demo.zigbee.activity;

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
import com.thingclips.smart.optimus.lock.api.IThingZigBeeLock;
import com.thingclips.smart.optimus.lock.api.zigbee.response.RecordBean;
import com.thingclips.smart.optimus.sdk.ThingOptimusSdk;
import com.thingclips.smart.sdk.optimus.lock.bean.ZigBeeDatePoint;
import com.tuya.lock.demo.R;
import com.tuya.lock.demo.zigbee.adapter.RecordListAdapter;
import com.tuya.lock.demo.zigbee.utils.Constant;

import java.util.ArrayList;
import java.util.List;

public class AlarmRecordListActivity extends AppCompatActivity {

    private IThingZigBeeLock zigBeeLock;
    private RecordListAdapter adapter;
    private RecyclerView recyclerView;
    private TextView error_view;

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

        String mDevId = getIntent().getStringExtra(Constant.DEVICE_ID);
        IThingLockManager tuyaLockManager = ThingOptimusSdk.getManager(IThingLockManager.class);
        zigBeeLock = tuyaLockManager.getZigBeeLock(mDevId);

        error_view = findViewById(R.id.error_view);
        recyclerView = findViewById(R.id.record_list_view);
        adapter = new RecordListAdapter();
        adapter.setDevice(mDevId);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        List<String> dpIds = new ArrayList<>();
        dpIds.add(zigBeeLock.convertCode2Id(ZigBeeDatePoint.HI_JACK));
        dpIds.add(zigBeeLock.convertCode2Id(ZigBeeDatePoint.ALARM_LOCK));
        dpIds.add(zigBeeLock.convertCode2Id(ZigBeeDatePoint.DOORBELL));
        zigBeeLock.getAlarmRecordList(dpIds, 0, 30, new IThingResultCallback<RecordBean>() {
            @Override
            public void onSuccess(RecordBean result) {
                adapter.setData(result.getDatas());
                adapter.notifyDataSetChanged();
                if (result.getDatas().size() == 0) {
                    showError(getString(R.string.zigbee_no_content));
                } else {
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
