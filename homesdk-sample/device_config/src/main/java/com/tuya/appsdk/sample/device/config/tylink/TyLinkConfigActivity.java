package com.tuya.appsdk.sample.device.config.tylink;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.tuya.appsdk.sample.device.config.R;
import com.tuya.appsdk.sample.device.config.scan.DeviceConfigQrCodeDeviceActivity;
import com.tuya.appsdk.sample.resource.HomeModel;
import com.tuya.smart.android.common.utils.L;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.builder.TuyaQRCodeActivatorBuilder;
import com.tuya.smart.sdk.api.ITuyaActivator;
import com.tuya.smart.sdk.api.ITuyaDataCallback;
import com.tuya.smart.sdk.api.ITuyaDevActivatorListener;
import com.tuya.smart.sdk.api.ITuyaSmartActivatorListener;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * @author axiong
 * @date 2022/8/8
 * @description :
 */
public class TyLinkConfigActivity extends AppCompatActivity implements View.OnClickListener{
    private static final int REQUEST_CODE_SCAN = 1;

    private MaterialToolbar topAppBar;
    private Button bt_search;
    private String mUuid;
    private static final String TAG = "TyLinkConfigActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_config_info_hint_activity);
        initView();

    }

    private void initView() {
        topAppBar = (MaterialToolbar) findViewById(R.id.topAppBar);
        topAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        topAppBar.setTitle(getString(R.string.device_qr_code_service_title));
        bt_search = (Button) findViewById(R.id.bt_search);


        bt_search.setOnClickListener(this);
        bt_search.setText(R.string.device_qr_code_service_title);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_search) {
            startQrCode();
        }
    }

    private void startQrCode() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_SCAN);
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_SCAN);
            return;
        }

        Intent intent = new Intent(this, CaptureActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SCAN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SCAN) {

            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    String result = bundle.getString(CodeUtils.RESULT_STRING);
                    Toast.makeText(this, "result:" + result, Toast.LENGTH_LONG).show();
                    deviceQrCode(result);
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    Toast.makeText(this, "Failed to parse QR code", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void deviceQrCode(String result) {
        Log.d(TAG,"result -> " + result);
        HashMap<String, Object> postData = new HashMap<>();
        postData.put("code", result);
        TuyaHomeSdk.getRequestInstance().requestWithApiNameWithoutSession(
                "tuya.m.qrcode.parse", "4.0", postData, String.class, new ITuyaDataCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        initQrCode(result);
                    }

                    @Override
                    public void onError(String errorCode, String errorMessage) {

                    }
                }
        );
    }

    private void initQrCode(String result) {
        long homeId = HomeModel.getCurrentHome(this);
        try {
            JSONObject obj = new JSONObject(result);
            JSONObject actionObj = obj.optJSONObject("actionData");
            if (null != actionObj) {
                mUuid = actionObj.optString("uuid");
                TuyaHomeSdk.getActivatorInstance().bindTuyaLinkDeviceWithQRCode(homeId, mUuid, new ITuyaDevActivatorListener() {
                    @Override
                    public void onError(String errorCode, String errorMsg) {
                        Log.d(TAG,"errorMsg = " + errorMsg + " / errorCode = " + errorCode);
                    }

                    @Override
                    public void onActiveSuccess(DeviceBean devResp) {
                        Toast.makeText(TyLinkConfigActivity.this, "绑定成功!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG,"onActiveSuccess --->>" );
                    }
                });

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
