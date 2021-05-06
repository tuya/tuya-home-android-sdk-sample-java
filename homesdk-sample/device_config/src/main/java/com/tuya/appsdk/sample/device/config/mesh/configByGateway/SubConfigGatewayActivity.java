package com.tuya.appsdk.sample.device.config.mesh.configByGateway;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.builder.TuyaGwSubDevActivatorBuilder;
import com.tuya.smart.sdk.api.ITuyaActivator;
import com.tuya.smart.sdk.api.ITuyaSmartActivatorListener;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.bean.SigMeshBean;

import java.util.List;

/**
 * @author AoBing
 *
 * To activate the sub-device through the gateway,
 * you first need to make the device enter the waiting state for activation,
 * and then select a Bluetooth device that has been activated in the current home.
 * After the activation operation is performed,
 * the gateway will automatically search for nearby devices to be activated and automatically activate and connect it to the gateway.
 */

public class SubConfigGatewayActivity extends AppCompatActivity implements View.OnClickListener{

    private final String TAG = "SubConfigGateway";
    private CircularProgressIndicator cpiLoading;
    private Button mBtnStartConfig;
    private Button mBtnStopConfig;
    private ITuyaActivator mTuyaGWSubActivator;
    private TextView mTvShowGatewayName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_config_mesh_sub_gateway);
        checkPermission();
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTuyaGWSubActivator != null) {
            mTuyaGWSubActivator.onDestroy();
        }
    }

    private void initView() {
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setNavigationOnClickListener(v -> finish());
        topAppBar.setTitle(R.string.device_config_sub_by_app_title);

        cpiLoading = findViewById(R.id.cpiLoading);

        mBtnStartConfig = findViewById(R.id.bt_mesh_start_config_sub_by_gateway);
        mBtnStopConfig = findViewById(R.id.bt_mesh_stop_config_sub_by_gateway);

        mTvShowGatewayName = findViewById(R.id.tv_config_sub_gateway_name);

        mBtnStartConfig.setOnClickListener(this);
        mBtnStopConfig.setOnClickListener(this);

        mBtnStopConfig.setEnabled(false);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_mesh_start_config_sub_by_gateway) {
            addSubDevice();
        }
        if (v.getId() == R.id.bt_mesh_stop_config_sub_by_gateway) {
            setPbViewVisible(false);
            if (mTuyaGWSubActivator != null) {
                mTuyaGWSubActivator.stop();
            }
        }
    }

    // activator by gateway
    public void addSubDevice() {
        Log.d(TAG, "start activator by gateway");
        setPbViewVisible(true);
        List<SigMeshBean> meshList = TuyaHomeSdk.getSigMeshInstance().getSigMeshList();
        // get mesh. Because there can only be one MESH in a family, here get(0)
        SigMeshBean mSigMeshBean = meshList.get(0);
        // get device list from mesh
        List<DeviceBean> deviceBeanList = TuyaHomeSdk.newSigMeshDeviceInstance(mSigMeshBean.getMeshId()).getMeshSubDevList();

        if (deviceBeanList != null && deviceBeanList.size() > 0) {
            for (DeviceBean deviceBean : deviceBeanList) {
                // Find the gateway in the device list, of course you can also specify one when you develop your own
                // Here we forEach the List, and only get the first gateway device
                if (deviceBean.isSigMeshWifi()) {
                    if (!TextUtils.isEmpty(deviceBean.getName()) && deviceBean.getName().length() > 0) {
                        mTvShowGatewayName.setText(deviceBean.getName());
                    }
                    TuyaGwSubDevActivatorBuilder builder = new TuyaGwSubDevActivatorBuilder()
                            .setDevId(deviceBean.getDevId())  // gateway DevId
                            .setTimeOut(100) // Timeout: s
                            .setListener(mTuyaSmartActivatorListener);

                    mTuyaGWSubActivator = TuyaHomeSdk.getActivatorInstance().newGwSubDevActivator(builder);
                    // start add sub device
                    mTuyaGWSubActivator.start();
                    break;
                }
            }
        } else {
            Toast.makeText(this, "please add gateway first", Toast.LENGTH_SHORT).show();
        }
    }

    private final ITuyaSmartActivatorListener mTuyaSmartActivatorListener = new ITuyaSmartActivatorListener() {
        @Override
        public void onError(String errorCode, String errorMsg) {
            Log.d(TAG, "activator error:" + errorMsg);
            Toast.makeText(SubConfigGatewayActivity.this, "activator error:" + errorMsg, Toast.LENGTH_SHORT).show();
            setPbViewVisible(false);
        }

        @Override
        public void onActiveSuccess(DeviceBean devResp) {
            setPbViewVisible(false);
            Log.d(TAG, "activator success");
            Toast.makeText(SubConfigGatewayActivity.this, "activator success", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStep(String step, Object data) {

        }
    };

    private void setPbViewVisible(boolean isShow) {
        cpiLoading.setVisibility(isShow ? View.VISIBLE : View.GONE);
        mBtnStartConfig.setEnabled(!isShow);
        mBtnStopConfig.setEnabled(isShow);
    }

    // You need to check permissions before using Bluetooth devices
    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_COARSE_LOCATION") != 0 || ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") != 0) {
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