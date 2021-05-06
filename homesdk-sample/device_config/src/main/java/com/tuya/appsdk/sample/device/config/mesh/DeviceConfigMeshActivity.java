package com.tuya.appsdk.sample.device.config.mesh;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.tuya.appsdk.sample.device.config.R;
import com.tuya.appsdk.sample.device.config.mesh.configByApp.SubConfigAppActivity;
import com.tuya.appsdk.sample.device.config.mesh.configByGateway.SubConfigGatewayActivity;
import com.tuya.appsdk.sample.resource.HomeModel;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.callback.ITuyaResultCallback;
import com.tuya.smart.sdk.bean.SigMeshBean;

import java.util.List;

/**
 * @author AoBing
 * A family can only have one MESH,
 * so it is necessary to determine whether the current family has created a MESH,
 * if the MESH List is empty, create a new MESH.
 *
 * There are two types of sub-device activation:
 *      1. Via APP;
 *      2. Via gateway.
 *
 * Activation through APP is similar to BLE,
 * you need to scan first,
 * you can get device information after scanning, and then activate;
 *
 * Activation through the gateway requires that the current family has an activated Bluetooth gateway.
 * After executing the sub-device activation instruction,
 * the gateway will automatically scan the surrounding devices waiting to be activated and activate them.
 */
public class DeviceConfigMeshActivity extends AppCompatActivity implements View.OnClickListener{

    private final String TAG = "ConfigMeshActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_config_mesh_activity);
        initView();
        checkMesh();
    }

    // list is null, start create Mesh
    private void checkMesh() {
        List<SigMeshBean> meshList = TuyaHomeSdk.getSigMeshInstance().getSigMeshList();
        if (meshList.isEmpty() || meshList.size() == 0) {
            createMesh();
        }
    }

    public void createMesh() {
        TuyaHomeSdk.newHomeInstance(HomeModel.getCurrentHome(this)).createSigMesh(new ITuyaResultCallback<SigMeshBean>() {
            @Override
            public void onSuccess(SigMeshBean result) {
                Log.d(TAG, "create success:" + result.getMeshId());
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                Log.d(TAG, "create error");
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_mesh_sub_app) {
            // activator by app
            startActivity(new Intent(this, SubConfigAppActivity.class));
        }
        if (v.getId() == R.id.bt_mesh_sub_gateway) {
            // activator by gateway
            startActivity(new Intent(this, SubConfigGatewayActivity.class));
        }
    }

    private void initView() {
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setNavigationOnClickListener(v -> finish());
        topAppBar.setTitle(R.string.device_config_mesh_title);

        Button mBtnGotoConfigByApp = findViewById(R.id.bt_mesh_sub_app);
        Button mBtnGotoConfigByGateway = findViewById(R.id.bt_mesh_sub_gateway);

        mBtnGotoConfigByApp.setOnClickListener(this);
        mBtnGotoConfigByGateway.setOnClickListener(this);
    }
}