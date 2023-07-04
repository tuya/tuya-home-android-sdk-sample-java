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
import com.thingclips.smart.activator.core.kit.ThingActivatorCoreKit;
import com.thingclips.smart.activator.core.kit.active.inter.IThingActiveManager;
import com.thingclips.smart.activator.core.kit.bean.ThingActivatorScanDeviceBean;
import com.thingclips.smart.activator.core.kit.bean.ThingActivatorScanFailureBean;
import com.thingclips.smart.activator.core.kit.bean.ThingDeviceActiveErrorBean;
import com.thingclips.smart.activator.core.kit.bean.ThingDeviceActiveLimitBean;
import com.thingclips.smart.activator.core.kit.builder.ThingDeviceActiveBuilder;
import com.thingclips.smart.activator.core.kit.callback.ThingActivatorScanCallback;
import com.thingclips.smart.activator.core.kit.constant.ThingDeviceActiveModeEnum;
import com.thingclips.smart.activator.core.kit.devicecore.ThingActivatorDeviceCoreKit;
import com.thingclips.smart.activator.core.kit.listener.IThingDeviceActiveListener;
import com.thingclips.smart.activator.core.kit.scan.ThingActivatorScanDeviceManager;
import com.tuya.appsdk.sample.device.config.R;
import com.tuya.appsdk.sample.resource.HomeModel;
import com.thingclips.smart.android.ble.api.BleConfigType;
import com.thingclips.smart.android.ble.api.LeScanSetting;
import com.thingclips.smart.android.ble.api.ScanDeviceBean;
import com.thingclips.smart.android.ble.api.ScanType;
import com.thingclips.smart.home.sdk.ThingHomeSdk;
import com.thingclips.smart.home.sdk.bean.ConfigProductInfoBean;
import com.thingclips.smart.sdk.api.IBleActivator;
import com.thingclips.smart.sdk.api.IBleActivatorListener;
import com.thingclips.smart.sdk.api.IMultiModeActivator;
import com.thingclips.smart.sdk.api.IMultiModeActivatorListener;
import com.thingclips.smart.sdk.api.IThingActivator;
import com.thingclips.smart.sdk.api.IThingActivatorGetToken;
import com.thingclips.smart.sdk.api.IThingDataCallback;
import com.thingclips.smart.sdk.bean.BleActivatorBean;
import com.thingclips.smart.sdk.bean.DeviceBean;
import com.thingclips.smart.sdk.bean.MultiModeActivatorBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author AoBing
 * <p>
 * BLE device activation, here you can activate Single and Dual devices.
 * <p>
 * First, you need to make the device enter the active state,
 * and then scan the surrounding devices through the mobile APP.
 * The scanned device can obtain the name and icon of the device by request.
 * <p>
 * Perform different activation methods according to the scanned device type:
 * <p>
 * If it is a single device, proceed directly to the activation step.
 * <p>
 * If it is a dual device, such as a gateway,
 * you need to obtain the Token from the cloud first,
 * and then pass in the Wi-Fi SSID and password to the gateway to perform activation.
 */

public class DeviceConfigBleAndDualActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "BLE_DEMO";
    private Button mBtnScan, mBtnStop;
    private CircularProgressIndicator cpiLoading;
    private final List<ThingActivatorScanDeviceBean> scanDeviceBeanList = new ArrayList<>();
    private BleDeviceListAdapter adapter;
    IThingActiveManager activeManager;

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
        scanDeviceBeanList.clear();

        List<ScanType> scanTypeList = new ArrayList<>();
        scanTypeList.add(ScanType.SINGLE);
        scanTypeList.add(ScanType.MESH);
        ThingActivatorScanDeviceManager.INSTANCE.startBlueToothDeviceSearch(
                60 * 1000,
                scanTypeList,
                new ThingActivatorScanCallback() {
                    @Override
                    public void deviceFound(@NonNull ThingActivatorScanDeviceBean thingActivatorScanDeviceBean) {
                        Log.d(TAG, "deviceFound : " + thingActivatorScanDeviceBean);
                        scanDeviceBeanList.add(thingActivatorScanDeviceBean);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void deviceUpdate(@NonNull ThingActivatorScanDeviceBean thingActivatorScanDeviceBean) {

                    }

                    @Override
                    public void deviceRepeat(@NonNull ThingActivatorScanDeviceBean thingActivatorScanDeviceBean) {

                    }

                    @Override
                    public void scanFinish() {

                    }

                    @Override
                    public void scanFailure(@NonNull ThingActivatorScanFailureBean thingActivatorScanFailureBean) {

                    }
                }
        );
    }

    private void stopScan() {
        ThingHomeSdk.getBleOperator().stopLeScan();
    }

    private void startActivator(ThingActivatorScanDeviceBean bean, String ssid, String pwd) {
        ThingDeviceActiveModeEnum thingDeviceActiveModeEnum = bean.getSupprotActivatorTypeList().get(0);
        activeManager = ThingActivatorCoreKit.INSTANCE.getActiveManager().newThingActiveManager();
        ThingDeviceActiveBuilder builder = new ThingDeviceActiveBuilder();
        builder.setActiveModel(bean.getSupprotActivatorTypeList().get(0));
        builder.setActivatorScanDeviceBean(bean);
        builder.setTimeOut(60);

        if (thingDeviceActiveModeEnum == ThingDeviceActiveModeEnum.BLE_WIFI || thingDeviceActiveModeEnum == ThingDeviceActiveModeEnum.MULT_MODE) {
            //need wifi info
            builder.setSsid(ssid);
            builder.setPassword(pwd);
        }

        builder.setRelationId(HomeModel.getCurrentHome(this));
        builder.setListener(new IThingDeviceActiveListener() {
            @Override
            public void onFind(@NonNull String s) {

            }

            @Override
            public void onBind(@NonNull String s) {

            }

            @Override
            public void onActiveSuccess(@NonNull DeviceBean deviceBean) {
                Log.i(TAG, "onActiveSuccess : " + deviceBean.getName());
                cpiLoading.setVisibility(View.GONE);
                Log.d(TAG, "activator success:" + deviceBean.getName());
                Toast.makeText(DeviceConfigBleAndDualActivity.this, "success:" + deviceBean.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onActiveError(@NonNull ThingDeviceActiveErrorBean thingDeviceActiveErrorBean) {
                cpiLoading.setVisibility(View.GONE);
                Log.d(TAG, "activator error:" + thingDeviceActiveErrorBean.getErrMsg());
                Toast.makeText(DeviceConfigBleAndDualActivity.this, "error:" + thingDeviceActiveErrorBean.getErrMsg(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onActiveLimited(@NonNull ThingDeviceActiveLimitBean thingDeviceActiveLimitBean) {
                Log.d(TAG, "onActiveLimited : " + thingDeviceActiveLimitBean.getErrorMsg());
            }
        });
        activeManager.startActive(builder);
    }

    private void stopActivator() {
        activeManager.stopActive();
    }

    public void dualActivatorDialog(ThingActivatorScanDeviceBean bean) {
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

                    cpiLoading.setVisibility(View.VISIBLE);
                    startActivator(bean, ssid, pwd);
                });
        builder.show();

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
            if (activity != null && activity.scanDeviceBeanList.size() > position) {
                holder.mTvDeviceName.setText(activity.scanDeviceBeanList.get(position).getName());
                holder.mBtnItemStartActivator.setOnClickListener(v -> {
                    Log.d(TAG, "点击：" + position);
                    activity.setViewVisible(false);
                    activity.stopScan();
                    ThingActivatorScanDeviceBean bean = activity.scanDeviceBeanList.get(position);
                    ThingDeviceActiveModeEnum thingDeviceActiveModeEnum = bean.getSupprotActivatorTypeList().get(0);
                    if (thingDeviceActiveModeEnum == ThingDeviceActiveModeEnum.BLE_WIFI || thingDeviceActiveModeEnum == ThingDeviceActiveModeEnum.MULT_MODE) {
                        //need wifi info
                        activity.dualActivatorDialog(bean);
                    } else {
                        activity.startActivator(bean, null, null);
                    }
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