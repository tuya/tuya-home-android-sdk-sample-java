package com.tuya.smart.android.demo.camera;

import static com.tuya.smart.android.demo.camera.utils.Constants.INTENT_MSGID;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.thingclips.smart.android.camera.sdk.ThingIPCSdk;
import com.thingclips.smart.android.camera.sdk.api.IThingIPCDoorBellManager;
import com.thingclips.smart.android.camera.sdk.bean.ThingDoorBellCallModel;
import com.thingclips.smart.android.camera.sdk.callback.ThingSmartDoorBellObserver;
import com.tuya.smart.android.demo.R;
import com.thingclips.smart.home.sdk.ThingHomeSdk;
import com.thingclips.smart.sdk.bean.DeviceBean;

/**
 * Created by HuangXin on 2/20/21.
 */
public class CameraDoorBellActivity extends AppCompatActivity {

    private String mMessageId;
    private final IThingIPCDoorBellManager mDoorBellInstance = ThingIPCSdk.getDoorbell().getIPCDoorBellManagerInstance();
    private TextView tvState;
    private Toolbar toolbar;

    private final ThingSmartDoorBellObserver mObserver = new ThingSmartDoorBellObserver() {
        @Override
        public void doorBellCallDidCanceled(ThingDoorBellCallModel callModel, boolean isTimeOut) {
            if (isTimeOut) {
                Toast.makeText(CameraDoorBellActivity.this, "Automatically hang up when the doorbell expires", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(CameraDoorBellActivity.this, "The doorbell was cancelled by the device", Toast.LENGTH_LONG).show();
            }
            finish();
        }

        @Override
        public void doorBellCallDidHangUp(ThingDoorBellCallModel callModel) {
            Toast.makeText(CameraDoorBellActivity.this, "Hung up", Toast.LENGTH_LONG).show();
            finish();
        }

        @Override
        public void doorBellCallDidAnsweredByOther(ThingDoorBellCallModel callModel) {
            Toast.makeText(CameraDoorBellActivity.this, "The doorbell is answered by another user", Toast.LENGTH_LONG).show();
            mDoorBellInstance.refuseDoorBellCall(callModel.getMessageId());
            finish();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_doorbell_calling);
        mMessageId = getIntent().getStringExtra(INTENT_MSGID);
        initData();
        initView();
    }

    private void initData() {
        if (TextUtils.isEmpty(mMessageId)) {
            finish();
            return;
        }
        mDoorBellInstance.addObserver(mObserver);
    }

    @SuppressLint("SetTextI18n")
    private void initView() {
        toolbar = findViewById(R.id.toolbar_view);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        tvState = findViewById(R.id.tv_state);
        Button btnRefuse = findViewById(R.id.btn_refuse);
        Button btnAccept = findViewById(R.id.btn_accept);
        ThingDoorBellCallModel model = mDoorBellInstance.getCallModelByMessageId(mMessageId);
        DeviceBean deviceBean = ThingHomeSdk.getDataInstance().getDeviceBean(model.getDevId());
        tvState.setText(deviceBean.getName() + " call, \nwaiting to be answered..");
        btnRefuse.setOnClickListener(v -> {
            if (isAnsweredBySelf()) {
                mDoorBellInstance.hangupDoorBellCall(mMessageId);
            } else {
                mDoorBellInstance.refuseDoorBellCall(mMessageId);
            }
            finish();
        });
        btnAccept.setOnClickListener(v -> {
            mDoorBellInstance.answerDoorBellCall(mMessageId);
            tvState.setText("The doorbell has been answered.");
            v.setVisibility(View.GONE);
            btnRefuse.setText(R.string.ipc_doorbell_hangup);
        });
    }

    private boolean isAnsweredBySelf() {
        ThingDoorBellCallModel callModel = mDoorBellInstance.getCallModelByMessageId(mMessageId);
        if (callModel == null) {
            return false;
        }
        return callModel.isAnsweredBySelf();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDoorBellInstance.removeObserver(mObserver);
    }
}