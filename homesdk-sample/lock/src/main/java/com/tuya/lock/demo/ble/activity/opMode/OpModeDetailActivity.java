package com.tuya.lock.demo.ble.activity.opMode;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.alibaba.fastjson.JSONObject;
import com.tuya.lock.demo.R;
import com.tuya.lock.demo.ble.activity.code.ShowCodeActivity;
import com.tuya.lock.demo.ble.constant.Constant;
import com.tuya.lock.demo.ble.utils.DialogUtils;
import com.tuya.lock.demo.ble.utils.OpModeUtils;
import com.tuya.lock.demo.ble.utils.PasscodeUtils;
import com.thingclips.smart.home.sdk.ThingHomeSdk;
import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.optimus.lock.api.BleLockConstant;
import com.thingclips.smart.optimus.lock.api.IThingBleLockV2;
import com.thingclips.smart.optimus.lock.api.IThingLockManager;
import com.thingclips.smart.optimus.lock.api.ThingUnlockType;
import com.thingclips.smart.optimus.lock.api.bean.UnlockModeResponse;
import com.thingclips.smart.optimus.sdk.ThingOptimusSdk;
import com.thingclips.smart.sdk.api.IDevListener;
import com.thingclips.smart.sdk.api.IResultCallback;
import com.thingclips.smart.sdk.api.IThingDevice;
import com.thingclips.smart.sdk.optimus.lock.bean.ble.AddOpmodeResult;
import com.thingclips.smart.sdk.optimus.lock.bean.ble.MemberInfoBean;
import com.thingclips.smart.sdk.optimus.lock.bean.ble.NotifyInfoBean;
import com.thingclips.smart.sdk.optimus.lock.bean.ble.OpModeDetailBean;
import com.thingclips.smart.sdk.optimus.lock.bean.ble.OpModeRequest;
import com.thingclips.smart.sdk.optimus.lock.utils.LockUtil;
import com.thingclips.smart.sdk.optimus.lock.utils.StandardDpConverter;

import java.util.ArrayList;
import java.util.Map;

public class OpModeDetailActivity extends AppCompatActivity {

    private IThingBleLockV2 tuyaLockDevice;
    private int mFrom = 0;
    private MemberInfoBean memberInfoBean;
    private final OpModeRequest request = new OpModeRequest();
    private TextView add_tips_view;
    private Button addView;
    private IThingDevice IThingDevice;
    private long opModeId;
    private Toolbar toolbar;
    private EditText add_name_view;
    private SwitchCompat hijack_switch;
    private EditText add_password;
    private Button show_code_view;
    private OpModeDetailBean detailBean;
    private String addString;
    private String mDevId;
    private String dpCode;
    private boolean isAddMode = false;

    public static void startActivity(Context context, MemberInfoBean memberInfoBean, Long opModeId,
                                     String devId, String dpCode) {

        Intent intent = new Intent(context, OpModeDetailActivity.class);
        //设备id
        intent.putExtra(Constant.DEVICE_ID, devId);
        //用户数据
        intent.putExtra(Constant.USER_DATA, JSONObject.toJSONString(memberInfoBean));
        //云端锁id
        intent.putExtra(Constant.OP_MODE_ID, opModeId);
        intent.putExtra(Constant.DP_CODE, dpCode);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock_mode_add);

        toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        add_tips_view = findViewById(R.id.add_tips);
        add_tips_view.setVisibility(View.GONE);

        String userData = getIntent().getStringExtra(Constant.USER_DATA);
        mDevId = getIntent().getStringExtra(Constant.DEVICE_ID);
        opModeId = getIntent().getLongExtra(Constant.OP_MODE_ID, -1);
        dpCode = getIntent().getStringExtra(Constant.DP_CODE);

        memberInfoBean = JSONObject.parseObject(userData, MemberInfoBean.class);

        if (opModeId > 0) {
            mFrom = 1;
        }

        IThingLockManager tuyaLockManager = ThingOptimusSdk.getManager(IThingLockManager.class);
        tuyaLockDevice = tuyaLockManager.getBleLockV2(mDevId);

        IThingDevice = ThingHomeSdk.newDeviceInstance(mDevId);
        IThingDevice.registerDevListener(new IDevListener() {
            @Override
            public void onDpUpdate(String devId, String dpStr) {
                Map<String, Object> dpCode = StandardDpConverter.convertIdToCodeMap(dpStr, StandardDpConverter.getSchemaMap(mDevId));
                Log.i(Constant.TAG, "onDpUpdate dpCode = " + dpCode);
                dealAddUnlockMode(dpCode);
            }

            @Override
            public void onRemoved(String devId) {

            }

            @Override
            public void onStatusChanged(String devId, boolean online) {

            }

            @Override
            public void onNetworkStatusChanged(String devId, boolean status) {

            }

            @Override
            public void onDevInfoUpdate(String devId) {

            }
        });
        request.setUserType(memberInfoBean.getUserType());
        request.setUserId(memberInfoBean.getUserId());
        request.setLockUserId(memberInfoBean.getLockUserId());
        request.setUnlockType(dpCode);

        initData();
        initUi();
    }

    public void initData() {
        add_name_view = findViewById(R.id.add_name);
        hijack_switch = findViewById(R.id.hijack_switch);
        addView = findViewById(R.id.unlock_mode_add);
        add_password = findViewById(R.id.add_password);
        show_code_view = findViewById(R.id.show_code_view);
    }

    public void initUi() {
        toolbar.setTitle(OpModeUtils.getTypeName(this, dpCode));

        add_name_view.setText(request.getUnlockName());
        add_name_view.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    request.setUnlockName(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        if (mFrom == 1) {
            hijack_switch.setChecked(request.getUnlockAttr() == 1);
        } else {
            hijack_switch.setChecked(false);
            request.setUnlockAttr(0);
        }
        hijack_switch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            request.setUnlockAttr(isChecked ? 1 : 0);
        });

        if (dpCode.equals(ThingUnlockType.PASSWORD)) {
            findViewById(R.id.password_wrap).setVisibility(View.VISIBLE);
            findViewById(R.id.random_password).setOnClickListener(v -> add_password.setText(PasscodeUtils.getRandom(6)));
            add_password.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!TextUtils.isEmpty(s)) {
                        request.setPassword(s.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        } else {
            findViewById(R.id.password_wrap).setVisibility(View.GONE);
        }

        if (mFrom == 1) {
            addString = getResources().getString(R.string.submit_edit);
        } else {
            addString = getResources().getString(R.string.submit_add);
        }
        addView.setText(addString);
        addView.setOnClickListener(v -> {
            //老版本校验解锁方式名称
            if (!tuyaLockDevice.isProDevice() && TextUtils.isEmpty(add_name_view.getText())) {
                showTips(getResources().getString(R.string.enter_unlock_mode_name), false);
                return;
            }
            String loadingStr = addString + "...";
            addView.setText(loadingStr);
            addView.setEnabled(false);
            validateOpModePassword();
        });

        show_code_view.setOnClickListener(v -> {
            ShowCodeActivity.startActivity(v.getContext(), JSONObject.toJSONString(detailBean));
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (opModeId > 0) {
            tuyaLockDevice.getProUnlockOpModeDetail(opModeId, new IThingResultCallback<OpModeDetailBean>() {
                @Override
                public void onSuccess(OpModeDetailBean result) {
                    Log.i(Constant.TAG, JSONObject.toJSONString(result));
                    detailBean = result;

                    request.setUnlockId(result.getUnlockId());
                    request.setOpModeId(result.getOpModeId());
                    request.setUnlockAttr(result.getUnlockAttr());
                    request.setUnlockName(result.getUnlockName());
                    request.setNotifyInfo(result.getNotifyInfo());
                    request.setUserType(result.getUserType());
                    request.setUserId(result.getUserId());
                    request.setLockUserId(result.getLockUserId());

                    show_code_view.setVisibility(View.VISIBLE);

                    initUi();
                }

                @Override
                public void onError(String errorCode, String errorMessage) {
                    showTips(errorMessage, false);
                }
            });
        }
    }

    private void validateOpModePassword() {
        if (TextUtils.equals(dpCode, ThingUnlockType.PASSWORD)) {
            tuyaLockDevice.validateOpModePassword(request.getPassword(), new IThingResultCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    JSONObject responseJSON = JSONObject.parseObject(result);
                    if (responseJSON.getBooleanValue("valid")) {
                        if (mFrom == 0) {
                            addUnlockMode();
                        } else {
                            upDataUnlockMode();
                        }
                    } else {
                        String errorCode = responseJSON.getString("errorCode");
                        Log.e(Constant.TAG, "validatePassword onSuccess is:" + false + ", errorCode:" + errorCode);
                        showTips(errorCode, false);
                        addView.setText(addString);
                        addView.setEnabled(true);
                        isAddMode = false;
                    }
                }

                @Override
                public void onError(String errorCode, String errorMessage) {
                    Log.e(Constant.TAG, "validatePassword onError errorCode:" + errorCode + ", errorMessage:" + errorMessage);
                    showTips(errorCode, false);
                    addView.setText(addString);
                    addView.setEnabled(true);
                    isAddMode = false;
                }
            });
        } else {
            if (mFrom == 0) {
                addUnlockMode();
            } else {
                upDataUnlockMode();
            }
        }
    }

    private void addUnlockMode() {
        isAddMode = true;
        tuyaLockDevice.addProUnlockOpModeForMember(request, new IThingResultCallback<AddOpmodeResult>() {
            @Override
            public void onSuccess(AddOpmodeResult result) {
                String tips = "add onSuccess";
                showTips(tips, true);
                Log.i(Constant.TAG, tips + JSONObject.toJSONString(result));

                isAddMode = false;

                //同步解锁方式
                String typeDpId = LockUtil.convertCode2Id(mDevId, dpCode);
                ArrayList<String> dpIds = new ArrayList<>();
                dpIds.add(typeDpId);
                tuyaLockDevice.syncData(dpIds, null);

                //退出页面
                finish();
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                Log.e(Constant.TAG, "addProUnlockOpModeForMember:" + errorMessage);
                showTips(errorMessage, false);
                addView.setText(addString);
                addView.setEnabled(true);
                isAddMode = false;
            }
        });
    }

    private void upDataUnlockMode() {
        NotifyInfoBean notifyInfoBean = new NotifyInfoBean();
        notifyInfoBean.setAppSend(true);
        request.setUnlockId(detailBean.getUnlockId());
        request.setNotifyInfo(notifyInfoBean);
        tuyaLockDevice.modifyProUnlockOpModeForMember(request, new IThingResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                String tips = "update onSuccess";
                showTips(tips, true);
                Log.i(Constant.TAG, tips + JSONObject.toJSONString(result));
                finish();
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                showTips(errorMessage, false);
                addView.setText(addString);
                addView.setEnabled(true);
                Log.i(Constant.TAG, "upDataUnlockMode" + errorMessage);
            }
        });
    }

    private void dealAddUnlockMode(Map<String, Object> dpCode) {
        for (String key : dpCode.keySet()) {
            Object o = dpCode.get(key);
            if (TextUtils.equals(key, UnlockModeResponse.UNLOCK_METHOD_CREATE)) {
                if (o instanceof String) {
                    String lockResponse = (String) o;
                    dealLockResponse(lockResponse);
                }
            }
        }
    }

    private void dealLockResponse(String lockResponse) {
        int stage = Integer.parseInt(lockResponse.substring(2, 4), 16);
        int times = Integer.parseInt(lockResponse.substring(10, 12), 16);
        if (stage == BleLockConstant.STAGE_ENTERING) {
            if (times >= 0) {
                Log.i(Constant.TAG, "times:" + times);
                String tipsAdd = "times: " + times + "/5";
                showTips(tipsAdd, true);
            }
        }
    }

    private void showTips(String tips, boolean isSteps) {
        if (isSteps) {
            add_tips_view.setBackgroundColor(getResources().getColor(R.color.green));
        } else {
            add_tips_view.setBackgroundColor(getResources().getColor(R.color.red));
        }
        add_tips_view.setVisibility(View.VISIBLE);
        add_tips_view.post(() -> add_tips_view.setText(tips));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        IThingDevice.unRegisterDevListener();
        IThingDevice.onDestroy();
        memberInfoBean = null;
    }

    private void cancelUnlock() {
        tuyaLockDevice.cancelUnlockOpModeForFinger(request.getLockUserId(), request.getUserType(), new IResultCallback() {
            @Override
            public void onError(String code, String error) {
                showTips(error, false);
            }

            @Override
            public void onSuccess() {
            }
        });
    }


    @Override
    public void onBackPressed() {
        //展示指纹录入的提示
        if (dpCode.equals(ThingUnlockType.FINGERPRINT) && isAddMode) {
            DialogUtils.showDelete(this, getResources().getString(R.string.whether_to_cancel), (dialog, which) -> {
                cancelUnlock();
                OpModeDetailActivity.super.onBackPressed();
            });
        } else {
            super.onBackPressed();
        }
    }
}