package com.tuya.appsdk.sample.device.config.ble;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.tuya.appsdk.sample.device.config.R;
import com.tuya.appsdk.sample.resource.HomeModel;
import com.tuya.smart.android.ble.api.BleConfigType;
import com.tuya.smart.android.ble.api.LeScanSetting;
import com.tuya.smart.android.ble.api.ScanDeviceBean;
import com.tuya.smart.android.ble.api.ScanType;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.ConfigProductInfoBean;
import com.tuya.smart.sdk.api.IBleActivator;
import com.tuya.smart.sdk.api.IBleActivatorListener;
import com.tuya.smart.sdk.api.IMultiModeActivator;
import com.tuya.smart.sdk.api.IMultiModeActivatorListener;
import com.tuya.smart.sdk.api.ITuyaActivator;
import com.tuya.smart.sdk.api.ITuyaActivatorGetToken;
import com.tuya.smart.sdk.api.ITuyaDataCallback;
import com.tuya.smart.sdk.bean.BleActivatorBean;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.bean.MultiModeActivatorBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author AoBing
 *
 * BLE device activation, here you can activate Single and Dual devices.
 *
 * First, you need to make the device enter the active state,
 * and then scan the surrounding devices through the mobile APP.
 * The scanned device can obtain the name and icon of the device by request.
 *
 * Perform different activation methods according to the scanned device type:
 *
 * If it is a single device, proceed directly to the activation step.
 *
 * If it is a dual device, such as a gateway,
 * you need to obtain the Token from the cloud first,
 * and then pass in the Wi-Fi SSID and password to the gateway to perform activation.
 */

public class DeviceConfigBleAndDualActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "BLE";
    private Button mBtnScan, mBtnStop;
    private CircularProgressIndicator cpiLoading;
    private static IBleActivator mBleActivator = TuyaHomeSdk.getActivator().newBleActivator();
    private static IMultiModeActivator mMultiModeActivator = TuyaHomeSdk.getActivator().newMultiModeActivator();

    private final List<ScanDeviceBean> scanDeviceBeanList = new ArrayList<>();
    private final List<ConfigProductInfoBean> infoBeanList = new ArrayList<>();
    private BleDeviceListAdapter adapter;
    BleActivatorBean bleActivatorBean;
    MultiModeActivatorBean multiModeActivatorBean;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_config_ble_and_dual_activity);
        checkPermission();
        initView();
    }

    private void initView() {
        MaterialToolbar topAppBar;
        topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setNavigationOnClickListener(v -> finish());
        topAppBar.setTitle(R.string.ble_activator_title);

        mBtnScan = findViewById(R.id.bt_search);
        mBtnStop = findViewById(R.id.bt_stop);
        RecyclerView mRvl = findViewById(R.id.rvList);
        cpiLoading = findViewById(R.id.cpiLoading);

        mBtnScan.setOnClickListener(this);
        mBtnStop.setOnClickListener(this);

        adapter = new BleDeviceListAdapter(this);
        mRvl.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        mRvl.setAdapter(adapter);
        setViewVisible(false);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_search) {
            setViewVisible(true);
            startScan();
        } else if (v.getId() == R.id.bt_stop) {
            setViewVisible(false);
            stopScan();
        }
    }

    private void startScan() {
        infoBeanList.clear();
        scanDeviceBeanList.clear();

        // Scan Single Ble Device
        LeScanSetting scanSetting = new LeScanSetting.Builder()
                .setTimeout(60 * 1000) // Timeout：ms
                .addScanType(ScanType.SINGLE) // If you need to scan for BLE devices, you only need to add ScanType.SINGLE
                .build();

        // start scan
        TuyaHomeSdk.getBleOperator().startLeScan(scanSetting, bean -> {
            Log.d(TAG, "扫描结果:" + bean.getUuid());
            scanDeviceBeanList.add(bean);
            getDeviceInfo(bean);
        });
    }

    private void stopScan() {
        TuyaHomeSdk.getBleOperator().stopLeScan();
    }

    private void getDeviceInfo(ScanDeviceBean scanDeviceBean) {
        TuyaHomeSdk.getActivatorInstance().getActivatorDeviceInfo(scanDeviceBean.getProductId(),
                scanDeviceBean.getUuid(),
                scanDeviceBean.getMac(),
                new ITuyaDataCallback<ConfigProductInfoBean>() {
                    @Override
                    public void onSuccess(ConfigProductInfoBean result) {
                        infoBeanList.add(result);
                        adapter.notifyDataSetChanged();
                        Log.d(TAG, "getDeviceInfo:" + result.getName());
                    }

                    @Override
                    public void onError(String errorCode, String errorMessage) {
                        Log.d(TAG, "getDeviceInfoError:" + errorMessage);
                        Toast.makeText(DeviceConfigBleAndDualActivity.this,
                                "getDeviceInfoError:" + errorMessage,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startActivator(int pos) {
        String type = scanDeviceBeanList.get(pos).getConfigType();
        if (BleConfigType.CONFIG_TYPE_SINGLE.getType().equals(type)) {
            singleActivator(pos);
        } else if (BleConfigType.CONFIG_TYPE_WIFI.getType().equals(type)) {
            dualActivatorDialog(pos);
        } else {
            Toast.makeText(DeviceConfigBleAndDualActivity.this, "Device Type not support", Toast.LENGTH_SHORT).show();
        }
    }

    private void singleActivator(int pos) {
        cpiLoading.setVisibility(View.VISIBLE);

        bleActivatorBean = new BleActivatorBean();

        bleActivatorBean.homeId = HomeModel.getCurrentHome(this); // homeId
        bleActivatorBean.address = scanDeviceBeanList.get(pos).getAddress();
        bleActivatorBean.deviceType = scanDeviceBeanList.get(pos).getDeviceType();
        bleActivatorBean.uuid = scanDeviceBeanList.get(pos).getUuid(); // UUID
        bleActivatorBean.productId = scanDeviceBeanList.get(pos).getProductId();

        mBleActivator.startActivator(bleActivatorBean, new IBleActivatorListener() {
            @Override
            public void onSuccess(DeviceBean deviceBean) {
                cpiLoading.setVisibility(View.GONE);
                bleActivatorBean = null;
                Log.d(TAG, "activator success:" + deviceBean.getName());
                Toast.makeText(DeviceConfigBleAndDualActivity.this, "success:" + deviceBean.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int code, String msg, Object handle) {
                cpiLoading.setVisibility(View.GONE);
                bleActivatorBean = null;
                Log.d(TAG, "activator error:" + msg);
                Toast.makeText(DeviceConfigBleAndDualActivity.this, "error:" + msg, Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void stopActivator(){
        if (bleActivatorBean != null) {
            mBleActivator.stopActivator(bleActivatorBean.uuid);
        }
        if (multiModeActivatorBean != null) {
            mMultiModeActivator.stopActivator(multiModeActivatorBean.uuid);
        }
    }

    private void dualActivatorDialog(int pos) {
        final View v = LayoutInflater.from(this).inflate(R.layout.ble_dual_activator_dialog, null);
        TextInputEditText etSSID = v.findViewById(R.id.et_ssid);
        TextInputEditText etPwd = v.findViewById(R.id.et_pwd);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Dual Activator")
                .setView(v)
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Ok", (dialog, which) -> {
                    String ssid = Objects.requireNonNull(etSSID.getText()).toString();
                    if (TextUtils.isEmpty(ssid)) {
                        Toast.makeText(DeviceConfigBleAndDualActivity.this, "SSID is Null", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "SSID is Null");
                    }
                    // Wi-Fi password can be null
                    String pwd = Objects.requireNonNull(etPwd.getText()).toString();

                    startDualActivator(pos, ssid, pwd);
                });
        builder.show();

    }

    private void startDualActivator(int pos, String ssid, String pwd) {
        cpiLoading.setVisibility(View.VISIBLE);
        long homeId = HomeModel.getCurrentHome(this);
        TuyaHomeSdk.getActivatorInstance().getActivatorToken(homeId,
                new ITuyaActivatorGetToken() {

                    // get Token
                    @Override
                    public void onSuccess(String token) {
                        Log.d(TAG, "getToken success, token :" + token);
                        multiModeActivatorBean = new MultiModeActivatorBean();
                        multiModeActivatorBean.deviceType = scanDeviceBeanList.get(pos).getDeviceType();
                        multiModeActivatorBean.uuid = scanDeviceBeanList.get(pos).getUuid();
                        multiModeActivatorBean.address = scanDeviceBeanList.get(pos).getAddress();
                        multiModeActivatorBean.mac = scanDeviceBeanList.get(pos).getMac();
                        multiModeActivatorBean.ssid = ssid;
                        multiModeActivatorBean.pwd = pwd;
                        multiModeActivatorBean.token = token;
                        multiModeActivatorBean.homeId = homeId;
                        multiModeActivatorBean.timeout = 120 * 1000;

                        // start activator
                        mMultiModeActivator.startActivator(multiModeActivatorBean, new IMultiModeActivatorListener() {
                            @Override
                            public void onSuccess(DeviceBean deviceBean) {
                                if (deviceBean != null) {
                                    Toast.makeText(DeviceConfigBleAndDualActivity.this, "config success", Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "Success:" + deviceBean.getName());
                                    cpiLoading.setVisibility(View.GONE);

                                }
                                multiModeActivatorBean = null;
                            }

                            @Override
                            public void onFailure(int code, String msg, Object handle) {
                                Log.d(TAG, "error:" + msg);
                                Toast.makeText(DeviceConfigBleAndDualActivity.this, "error:" + msg, Toast.LENGTH_SHORT).show();
                                cpiLoading.setVisibility(View.GONE);
                                multiModeActivatorBean = null;
                            }
                        });
                    }

                    @Override
                    public void onFailure(String code, String msg) {
                        Log.e(TAG, "getToken failed:" + msg);
                    }
                });


    }

    private static class VH extends RecyclerView.ViewHolder {
        TextView mTvDeviceName;
        LinearLayout mLlItemRoot;
        Button mBtnItemStartActivator;
        public VH(@NonNull View itemView) {
            super(itemView);
            mTvDeviceName = itemView.findViewById(R.id.tv_ble_device_item_name);
            mLlItemRoot = itemView.findViewById(R.id.ll_ble_device_root);
            mBtnItemStartActivator = itemView.findViewById(R.id.bt_item_start_activator);
        }
    }


    private static class BleDeviceListAdapter extends RecyclerView.Adapter<DeviceConfigBleAndDualActivity.VH> {
        private final DeviceConfigBleAndDualActivity activity;
        public BleDeviceListAdapter(DeviceConfigBleAndDualActivity activity) {
            this.activity = activity;
        }

        @NonNull
        @Override
        public DeviceConfigBleAndDualActivity.VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new DeviceConfigBleAndDualActivity.VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.device_ble_device_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull DeviceConfigBleAndDualActivity.VH holder, int position) {
            if (activity != null && activity.infoBeanList.size() > position){
                holder.mTvDeviceName.setText(activity.infoBeanList.get(position).getName());
                holder.mBtnItemStartActivator.setOnClickListener(v -> {
                    Log.d(TAG, "点击：" + position);
                    activity.setViewVisible(false);
                    activity.stopScan();
                    activity.startActivator(position);
                });
            }
        }

        @Override
        public int getItemCount() {
            return activity.scanDeviceBeanList.size();
        }
    }

    // You need to check permissions before using Bluetooth devices
    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_COARSE_LOCATION") != 0 || ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") != 0) {
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"}, 1001);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            if (grantResults.length != 0 && grantResults[0] == 0) {
                Log.i("DeviceConfigBleActivity", "onRequestPermissionsResult: agree");
            } else {
                this.finish();
                Log.e("DeviceConfigBleActivity", "onRequestPermissionsResult: denied");
            }
        } else {
            throw new IllegalStateException("Unexpected value: " + requestCode);
        }
    }

    private void setViewVisible(boolean visible) {
        cpiLoading.setVisibility(visible ? View.VISIBLE : View.GONE);
        mBtnScan.setEnabled(!visible);
        mBtnStop.setEnabled(visible);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopScan();
        stopActivator();
    }
}