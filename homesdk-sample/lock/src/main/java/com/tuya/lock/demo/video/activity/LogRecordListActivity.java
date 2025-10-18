package com.tuya.lock.demo.video.activity;

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
import com.thingclips.smart.optimus.sdk.ThingOptimusSdk;
import com.thingclips.thinglock.videolock.api.IVideoLockManager;
import com.thingclips.thinglock.videolock.bean.LogsListBean;
import com.tuya.lock.demo.R;
import com.tuya.lock.demo.ble.constant.Constant;
import com.tuya.lock.demo.video.adapter.RecordListAdapter;

/**
 * 视频锁告警记录
 */
public class LogRecordListActivity extends AppCompatActivity {

    private IVideoLockManager lockManager;
    private RecordListAdapter adapter;
    private RecyclerView recyclerView;
    private TextView error_view;

    public static void startActivity(Context context, String devId) {
        Intent intent = new Intent(context, LogRecordListActivity.class);
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
        toolbar.setTitle(getString(R.string.lock_log_list));

        String mDevId = getIntent().getStringExtra(Constant.DEVICE_ID);
        IThingLockManager tuyaLockManager = ThingOptimusSdk.getManager(IThingLockManager.class);
        lockManager = tuyaLockManager.newVideoLockManagerInstance(mDevId);

        error_view = findViewById(R.id.error_view);
        recyclerView = findViewById(R.id.record_list_view);
        adapter = new RecordListAdapter();
        adapter.setDevice(mDevId);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        lockManager.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        lockManager.getBaseAbilityManager().getLogList(
                "",
                "",
                false,
                null,
                System.currentTimeMillis(),
                "",
                20,
                0,
                "",
                new IThingResultCallback<LogsListBean>() {
                    @Override
                    public void onSuccess(LogsListBean result) {
                        if (result.getRecords().size() == 0) {
                            showError(getString(R.string.zigbee_no_content));
                        } else {
                            adapter.setData(result.getRecords());
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
