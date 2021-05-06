package com.tuya.appsdk.sample.device.config.mesh.configByApp;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.tuya.appsdk.sample.device.config.R;
import com.tuya.smart.android.blemesh.api.ITuyaBlueMeshActivatorListener;
import com.tuya.smart.android.blemesh.api.ITuyaBlueMeshSearch;
import com.tuya.smart.android.blemesh.api.ITuyaBlueMeshSearchListener;
import com.tuya.smart.android.blemesh.bean.SearchDeviceBean;
import com.tuya.smart.android.blemesh.builder.SearchBuilder;
import com.tuya.smart.android.blemesh.builder.TuyaSigMeshActivatorBuilder;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.bluemesh.ITuyaBlueMeshActivator;
import com.tuya.smart.sdk.bean.DeviceBean;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author AoBing
 *
 * The activation of SIGMesh sub-devices through APP is realized through mobile phone Bluetooth.
 *
 * First, the device needs to enter the waiting state for activation.
 * And then, the APP scans the surrounding devices. After scanning the device,
 * you can request the name and icon of the device.
 * Finally, add the scanned device to the List and activate it.
 */

public class SubConfigAppActivity extends AppCompatActivity implements View.OnClickListener{
    private final String TAG = "SubConfigAppActivity";

    private CircularProgressIndicator cpiLoading;
    private Button mBtnStartScan, mBtnStopScan;
    private Button mBtnStartActivator, mBtnStopActivator;
    private TextView mTvCount;

    private int count;

    private final int START_SCAN = 0;
    private final int STOP_SCAN = 1;
    private final int START_ACTIVATOR = 2;
    private final int STOP_ACTIVATOR = 3;

    private final List<SearchDeviceBean> searchDeviceBeanList = new ArrayList<>();
    private ITuyaBlueMeshSearch mMeshSearch;
    private ITuyaBlueMeshActivator iTuyaBlueMeshActivator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_config_mesh_sub_app);
        checkPermission();
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopScan();
        stopActivator();
    }

    private void initView() {
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setNavigationOnClickListener(v -> finish());
        topAppBar.setTitle(R.string.mesh_sub_activator_title);

        cpiLoading = findViewById(R.id.cpiLoading);
        mBtnStartScan = findViewById(R.id.bt_mesh_config_sub_by_app_scan);
        mBtnStopScan = findViewById(R.id.bt_mesh_config_sub_by_app_scan_stop);
        mBtnStartActivator = findViewById(R.id.bt_mesh_config_sub_by_app_start_activator);
        mBtnStopActivator = findViewById(R.id.bt_mesh_config_sub_by_app_stop_activator);
        mTvCount = findViewById(R.id.tv_count);

        mBtnStartScan.setOnClickListener(this);
        mBtnStopScan.setOnClickListener(this);
        mBtnStartActivator.setOnClickListener(this);
        mBtnStopActivator.setOnClickListener(this);

        setViewVisible(STOP_ACTIVATOR);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_mesh_config_sub_by_app_scan) {
            setViewVisible(START_SCAN);
            startScan();
        } else if (v.getId() == R.id.bt_mesh_config_sub_by_app_scan_stop) {
            stopScan();
            if (searchDeviceBeanList.size() == 0) {
                setViewVisible(STOP_ACTIVATOR);
                return;
            }
            setViewVisible(STOP_SCAN);
        } else if (v.getId() == R.id.bt_mesh_config_sub_by_app_start_activator) {
            setViewVisible(START_ACTIVATOR);
            startActivator();
        } else {
            setViewVisible(STOP_ACTIVATOR);
            stopActivator();
        }
    }

    private void startScan() {
        searchDeviceBeanList.clear();
        count = 0;
        // The UUID of the SigMesh device to be configured is fixed
        UUID[] MESH_PROVISIONING_UUID = {UUID.fromString("00001827-0000-1000-8000-00805f9b34fb")};
        SearchBuilder searchBuilder = new SearchBuilder()
                .setServiceUUIDs(MESH_PROVISIONING_UUID)
                .setTimeOut(100)        // timeout: s
                .setTuyaBlueMeshSearchListener(new ITuyaBlueMeshSearchListener() {
                    @Override
                    public void onSearched(SearchDeviceBean bean) {
//                        getSubDeviceInfo(bean);
                        searchDeviceBeanList.add(bean);
                        count++;
                        String strCount = getString(R.string.device_amount) + count;
                        mTvCount.setText(strCount);
                    }

                    @Override
                    public void onSearchFinish() {
                        Toast.makeText(SubConfigAppActivity.this, "search finish", Toast.LENGTH_SHORT).show();
                        setViewVisible(STOP_SCAN);
                    }
                }).build();

        mMeshSearch = TuyaHomeSdk.getTuyaBlueMeshConfig().newTuyaBlueMeshSearch(searchBuilder);
        // start scan
        mMeshSearch.startSearch();
    }

    private void stopScan() {
        if (mMeshSearch != null) {
            mMeshSearch.stopSearch();
        }
    }

    private void startActivator() {

        TuyaSigMeshActivatorBuilder tuyaSigMeshActivatorBuilder = new TuyaSigMeshActivatorBuilder()
                .setSearchDeviceBeans(searchDeviceBeanList)
                .setSigMeshBean(TuyaHomeSdk.getSigMeshInstance().getSigMeshList().get(0))
                .setTimeOut(100)  // timeout: s
                .setTuyaBlueMeshActivatorListener(new ITuyaBlueMeshActivatorListener() {
                    @Override
                    public void onSuccess(String mac, DeviceBean deviceBean) {
                        Log.d(TAG, "activator success:" + deviceBean.getName());
                        Toast.makeText(SubConfigAppActivity.this,
                                "activator success:" + deviceBean.getName(),
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String mac, String errorCode, String errorMsg) {
                        Log.d(TAG, "activator error:" + errorMsg);
                        Toast.makeText(SubConfigAppActivity.this,
                                "activator error:" + errorMsg,
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFinish() {
                        Log.d(TAG, "activator finish");
                        setViewVisible(STOP_ACTIVATOR);
                    }
                });

        iTuyaBlueMeshActivator = TuyaHomeSdk.getTuyaBlueMeshConfig().newSigActivator(tuyaSigMeshActivatorBuilder);
        iTuyaBlueMeshActivator.startActivator();
    }

    private void stopActivator() {
        if (iTuyaBlueMeshActivator != null) {
            iTuyaBlueMeshActivator.stopActivator();
        }
    }

    // Set the loading view and button are disabled
    private void setViewVisible(int states) {
        if (states == START_SCAN) {
            cpiLoading.setVisibility(View.VISIBLE);
            mBtnStartScan.setEnabled(false);
            mBtnStopScan.setEnabled(true);
            mBtnStartActivator.setEnabled(false);
            mBtnStopActivator.setEnabled(false);
        } else if(states == STOP_SCAN) {
            cpiLoading.setVisibility(View.GONE);
            mBtnStartScan.setEnabled(true);
            mBtnStopScan.setEnabled(false);
            mBtnStartActivator.setEnabled(true);
            mBtnStopActivator.setEnabled(false);
        } else if(states == START_ACTIVATOR) {
            cpiLoading.setVisibility(View.VISIBLE);
            mBtnStartScan.setEnabled(false);
            mBtnStopScan.setEnabled(false);
            mBtnStartActivator.setEnabled(false);
            mBtnStopActivator.setEnabled(true);
        } else {
            cpiLoading.setVisibility(View.GONE);
            mBtnStartScan.setEnabled(true);
            mBtnStopScan.setEnabled(false);
            mBtnStartActivator.setEnabled(false);
            mBtnStopActivator.setEnabled(false);
        }
    }

    // You need to check permissions before using Bluetooth devices
    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_COARSE_LOCATION") != 0 ||
                ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") != 0) {

            ActivityCompat.requestPermissions(this, new String[]{"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"}, 1001);
        }
        BluetoothAdapter bluetoothAdapter= BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Does not support Bluetooth", Toast.LENGTH_SHORT).show();
        }
        if (!bluetoothAdapter.isEnabled()) {
            AlertDialog builder = new AlertDialog.Builder(this)
                    .setMessage(R.string.ble_open_dialog)
                    .setPositiveButton("OK", (dialog, which) -> finish())
                    .create();
            builder.show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            if (grantResults.length != 0 && grantResults[0] == 0) {
                Log.i(TAG, "onRequestPermissionsResult: agree");
            } else {
                this.finish();
                Log.i(TAG, "onRequestPermissionsResult: denied");
            }
        } else {
            throw new IllegalStateException("Unexpected value: " + requestCode);
        }
    }

}