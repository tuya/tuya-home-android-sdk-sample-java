package com.tuya.appsdk.sample.device.config.qrcode;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
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
import com.tuya.appsdk.sample.device.config.ap.DeviceConfigAPActivity;
import com.tuya.appsdk.sample.device.config.util.qrcode.QRCodeUtil;
import com.tuya.appsdk.sample.resource.HomeModel;
import com.thingclips.smart.android.common.utils.L;
import com.thingclips.smart.android.common.utils.WiFiUtil;
import com.thingclips.smart.home.sdk.ThingHomeSdk;
import com.thingclips.smart.home.sdk.builder.ActivatorBuilder;
import com.thingclips.smart.home.sdk.builder.ThingCameraActivatorBuilder;
import com.thingclips.smart.sdk.api.IThingActivatorGetToken;
import com.thingclips.smart.sdk.api.IThingCameraDevActivator;
import com.thingclips.smart.sdk.api.IThingSmartActivatorListener;
import com.thingclips.smart.sdk.api.IThingSmartCameraActivatorListener;
import com.thingclips.smart.sdk.bean.DeviceBean;
import com.thingclips.smart.sdk.enums.ActivatorModelEnum;

/**
 * QR code device config, generally used for ipc device.
 */
public class QrCodeConfigActivity extends AppCompatActivity implements View.OnClickListener {

    private String wifiSSId = "";
    private String wifiPwd = "";
    private String mtoken = "";
    private ImageView mIvQr;
    private LinearLayout mLlInputWifi;
    private EditText mEtInputWifiSSid;
    private EditText mEtInputWifiPwd;
    private Button mBtnSave;
    private IThingCameraDevActivator mTuyaActivator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_config_qr_code);
        Toolbar toolbar = findViewById(R.id.toolbar_view);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mLlInputWifi = findViewById(R.id.ll_input_wifi);
        mEtInputWifiSSid = findViewById(R.id.et_wifi_ssid);
        mEtInputWifiPwd = findViewById(R.id.et_wifi_pwd);
        mBtnSave = findViewById(R.id.btn_save);
        mBtnSave.setOnClickListener(this);
        mIvQr = findViewById(R.id.iv_qrcode);
    }

    private void hideKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }


    public void onClick(View v) {
        if (v.getId() == R.id.btn_save) {
            wifiSSId = mEtInputWifiSSid.getText().toString();
            wifiPwd = mEtInputWifiPwd.getText().toString();
            long homeId = HomeModel.getCurrentHome(this);

            // Get Network Configuration Token
            ThingHomeSdk.getActivatorInstance().getActivatorToken(homeId,
                    new IThingActivatorGetToken() {
                        @Override
                        public void onSuccess(String token) {
                            //Create and show qrCode
                            ThingCameraActivatorBuilder builder = new ThingCameraActivatorBuilder()
                                    .setToken(token)
                                    .setPassword(wifiPwd)
                                    .setTimeOut(100)
                                    .setContext(QrCodeConfigActivity.this)
                                    .setSsid(wifiSSId)
                                    .setListener(new IThingSmartCameraActivatorListener() {
                                        @Override
                                        public void onQRCodeSuccess(String qrcodeUrl) {
                                            final Bitmap bitmap;
                                            try {
                                                bitmap = QRCodeUtil.createQRCode(qrcodeUrl, 300);
                                                QrCodeConfigActivity.this.runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mIvQr.setImageBitmap(bitmap);
                                                        mLlInputWifi.setVisibility(View.GONE);
                                                        mIvQr.setVisibility(View.VISIBLE);
                                                    }
                                                });
                                            } catch (WriterException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onError(String errorCode, String errorMsg) {

                                        }

                                        @Override
                                        public void onActiveSuccess(DeviceBean devResp) {
                                            Toast.makeText(QrCodeConfigActivity.this,"config success!",Toast.LENGTH_LONG).show();
                                        }
                                    });
                            mTuyaActivator = ThingHomeSdk.getActivatorInstance().newCameraDevActivator(builder);
                            mTuyaActivator.createQRCode();
                            mTuyaActivator.start();
                        }


                        @Override
                        public void onFailure(String errorCode, String errorMsg) {

                        }
                    });
            hideKeyboard(v);
        }
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