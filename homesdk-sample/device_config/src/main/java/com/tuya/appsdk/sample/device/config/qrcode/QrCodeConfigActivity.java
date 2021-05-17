package com.tuya.appsdk.sample.device.config.qrcode;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.WriterException;
import com.tuya.appsdk.sample.device.config.R;
import com.tuya.appsdk.sample.device.config.util.qrcode.QRCodeUtil;
import com.tuya.appsdk.sample.resource.HomeModel;
import com.tuya.smart.android.common.utils.L;
import com.tuya.smart.android.common.utils.WiFiUtil;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.builder.TuyaCameraActivatorBuilder;
import com.tuya.smart.sdk.api.ITuyaActivatorGetToken;
import com.tuya.smart.sdk.api.ITuyaCameraDevActivator;
import com.tuya.smart.sdk.api.ITuyaSmartCameraActivatorListener;
import com.tuya.smart.sdk.bean.DeviceBean;

/**
 * QR code device config, generally used for ipc device.
 */
public class QrCodeConfigActivity extends AppCompatActivity implements ITuyaSmartCameraActivatorListener {

    private String wifiSSId = "";
    private String token = "";
    private String wifiPwd = "";
    private ImageView mIvQr;
    private LinearLayout mLlInputWifi;
    private EditText mEtInputWifiSSid;
    private EditText mEtInputWifiPwd;
    private Button mBtnSave;
    private ITuyaCameraDevActivator mTuyaActivator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_config_qr_code);
        Toolbar toolbar = findViewById(R.id.toolbar_view);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mLlInputWifi = findViewById(R.id.ll_input_wifi);
        mEtInputWifiSSid = findViewById(R.id.et_wifi_ssid);
        mEtInputWifiPwd = findViewById(R.id.et_wifi_pwd);
        mBtnSave = findViewById(R.id.btn_save);
        mBtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createQrcode();
                hideKeyboard(v);
            }
        });
        mIvQr = findViewById(R.id.iv_qrcode);
        init();
    }

    private void hideKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    private void init() {
        getCurrentSSID();
        TuyaHomeSdk.getActivatorInstance().getActivatorToken(HomeModel.getCurrentHome(this), new ITuyaActivatorGetToken() {
            @Override
            public void onSuccess(String s) {
                token = s;
            }

            @Override
            public void onFailure(String s, String s1) {
                L.e("QrCodeConfigActivity", s);
            }
        });
    }

    private void getCurrentSSID() {
        //WifiManager#getConnectionInfo need ACCESS_FINE_LOCATION permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1001);
            return;
        }
        wifiSSId = WiFiUtil.getCurrentSSID(this);
        mEtInputWifiSSid.setText(wifiSSId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getCurrentSSID();
    }

    private void createQrcode() {
        if (TextUtils.isEmpty(token)) {
            Toast.makeText(this, "token is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        wifiPwd = mEtInputWifiPwd.getText().toString();
        TuyaCameraActivatorBuilder builder = new TuyaCameraActivatorBuilder()
                .setToken(token).setPassword(wifiPwd).setSsid(wifiSSId).setListener(this);
        mTuyaActivator = TuyaHomeSdk.getActivatorInstance().newCameraDevActivator(builder);
        mTuyaActivator.createQRCode();
        mTuyaActivator.start();
    }

    @Override
    public void onQRCodeSuccess(String s) {
        final Bitmap bitmap;
        try {
            bitmap = QRCodeUtil.createQRCode(s, 300);
            QrCodeConfigActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mIvQr.setImageBitmap(bitmap);
                    mIvQr.setVisibility(View.VISIBLE);
                    mLlInputWifi.setVisibility(View.GONE);
                }
            });
        } catch (WriterException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onError(String s, String s1) {

    }

    @Override
    public void onActiveSuccess(DeviceBean deviceBean) {
        Toast.makeText(this, "config success!", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mTuyaActivator) {
            mTuyaActivator.stop();
            mTuyaActivator.onDestroy();
        }
    }
}
