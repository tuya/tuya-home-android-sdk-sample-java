package com.tuya.lock.demo.activity.password;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.tuya.lock.demo.R;
import com.tuya.lock.demo.constant.Constant;
import com.thingclips.smart.optimus.lock.api.IThingLockManager;
import com.thingclips.smart.optimus.sdk.ThingOptimusSdk;

/**
 * 密码列表
 */
public class PasswordMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_main);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        String deviceId = getIntent().getStringExtra(Constant.DEVICE_ID);

        IThingLockManager tuyaLockManager = ThingOptimusSdk.getManager(IThingLockManager.class);
        boolean isProDevice = tuyaLockManager.getBleLockV2(deviceId).isProDevice();

        findViewById(R.id.password_dynamic).setOnClickListener(v -> {
            //动态密码
            Intent intent = new Intent(v.getContext(), PasswordDynamicActivity.class);
            intent.putExtra(Constant.DEVICE_ID, deviceId);
            v.getContext().startActivity(intent);
        });

        findViewById(R.id.unassigned_list).setOnClickListener(v -> {
            //获取可分配的离线不限次数密码列表
            Intent intent = new Intent(v.getContext(), PasswordSingleRevokeListActivity.class);
            intent.putExtra(Constant.DEVICE_ID, deviceId);
            v.getContext().startActivity(intent);
        });

        findViewById(R.id.lock_device_config).setOnClickListener(v -> {
            //设备信息
            Intent intent = new Intent(v.getContext(), PasswordLockDeviceConfigActivity.class);
            intent.putExtra(Constant.DEVICE_ID, deviceId);
            v.getContext().startActivity(intent);
        });

        View lock_pro_password_list = findViewById(R.id.lock_pro_password_list);
        View online_list_single_wrap = findViewById(R.id.online_list_single_wrap);
        //单次在线密码
        online_list_single_wrap.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), PasswordOldOnlineListActivity.class);
            intent.putExtra(Constant.DEVICE_ID, deviceId);
            intent.putExtra(Constant.PASSWORD_TYPE, Constant.TYPE_SINGLE);
            v.getContext().startActivity(intent);
        });
        if (isProDevice) {
            online_list_single_wrap.setVisibility(View.GONE);
            lock_pro_password_list.setVisibility(View.VISIBLE);
        } else {
            online_list_single_wrap.setVisibility(View.VISIBLE);
            lock_pro_password_list.setVisibility(View.GONE);
        }

        lock_pro_password_list.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), PasswordProListActivity.class);
            intent.putExtra(Constant.DEVICE_ID, deviceId);
            v.getContext().startActivity(intent);
        });

        //多次在线密码
        TextView online_list_multiple = findViewById(R.id.online_list_multiple);
        findViewById(R.id.online_list_multiple_wrap).setOnClickListener(v -> {
            if (isProDevice) {
                PasswordProOnlineDetailActivity.startActivity(v.getContext(), null, deviceId, 0);
            } else {
                Intent intent = new Intent(v.getContext(), PasswordOldOnlineListActivity.class);
                intent.putExtra(Constant.DEVICE_ID, deviceId);
                intent.putExtra(Constant.PASSWORD_TYPE, Constant.TYPE_MULTIPLE);
                v.getContext().startActivity(intent);
            }
        });
        if (isProDevice) {
            online_list_multiple.setText(getResources().getString(R.string.password_custom));
        } else {
            online_list_multiple.setText(getResources().getString(R.string.password_periodic));
        }

        //离线单次密码
        findViewById(R.id.offline_list_single_wrap).setOnClickListener(v -> {
            Intent intent;
            if (isProDevice) {
                intent = new Intent(v.getContext(), PasswordProOfflineAddActivity.class);
            } else {
                intent = new Intent(v.getContext(), PasswordOldOfflineListActivity.class);
            }
            intent.putExtra(Constant.DEVICE_ID, deviceId);
            intent.putExtra(Constant.PASSWORD_TYPE, Constant.TYPE_SINGLE);
            v.getContext().startActivity(intent);
        });

        //离线不限次数密码
        findViewById(R.id.offline_list_multiple_wrap).setOnClickListener(v -> {
            Intent intent;
            if (isProDevice) {
                intent = new Intent(v.getContext(), PasswordProOfflineAddActivity.class);
            } else {
                intent = new Intent(v.getContext(), PasswordOldOfflineListActivity.class);
            }
            intent.putExtra(Constant.DEVICE_ID, deviceId);
            intent.putExtra(Constant.PASSWORD_TYPE, Constant.TYPE_MULTIPLE);
            v.getContext().startActivity(intent);
        });

        //全部清空码
        findViewById(R.id.offline_list_clear_wrap).setOnClickListener(v -> {
            Intent intent;
            if (isProDevice) {
                intent = new Intent(v.getContext(), PasswordProOfflineAddActivity.class);
            } else {
                intent = new Intent(v.getContext(), PasswordOldOfflineListActivity.class);
            }
            intent.putExtra(Constant.DEVICE_ID, deviceId);
            intent.putExtra(Constant.PASSWORD_TYPE, Constant.TYPE_CLEAR_ALL);
            v.getContext().startActivity(intent);
        });
    }

}