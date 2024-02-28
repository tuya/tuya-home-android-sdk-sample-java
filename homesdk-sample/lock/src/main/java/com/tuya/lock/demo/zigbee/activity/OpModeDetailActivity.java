package com.tuya.lock.demo.zigbee.activity;

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
import com.thingclips.smart.android.common.utils.L;
import com.thingclips.smart.home.sdk.ThingHomeSdk;
import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.optimus.lock.api.IThingLockManager;
import com.thingclips.smart.optimus.lock.api.IThingZigBeeLock;
import com.thingclips.smart.optimus.lock.api.ThingUnlockType;
import com.thingclips.smart.optimus.lock.api.bean.UnlockModeResponse;
import com.thingclips.smart.optimus.lock.api.zigbee.request.OpModeAddRequest;
import com.thingclips.smart.optimus.lock.api.zigbee.response.MemberInfoBean;
import com.thingclips.smart.optimus.lock.api.zigbee.response.OpModeAddBean;
import com.thingclips.smart.optimus.lock.api.zigbee.response.OpModeBean;
import com.thingclips.smart.optimus.lock.api.zigbee.status.ZigbeeOpModeStage;
import com.thingclips.smart.optimus.sdk.ThingOptimusSdk;
import com.thingclips.smart.sdk.api.IDevListener;
import com.thingclips.smart.sdk.api.IThingDevice;
import com.thingclips.smart.sdk.optimus.lock.utils.StandardDpConverter;
import com.tuya.lock.demo.R;
import com.tuya.lock.demo.ble.activity.code.ShowCodeActivity;
import com.tuya.lock.demo.ble.utils.DialogUtils;
import com.tuya.lock.demo.ble.utils.OpModeUtils;
import com.tuya.lock.demo.ble.utils.PasscodeUtils;
import com.tuya.lock.demo.zigbee.utils.Constant;
import com.thingclips.smart.sdk.api.IResultCallback;

import java.util.Map;

public class OpModeDetailActivity extends AppCompatActivity {

    private IThingZigBeeLock zigBeeLock;
    private int mFrom = 0;
    private MemberInfoBean memberInfoBean;
    private final OpModeAddRequest request = new OpModeAddRequest();
    private TextView add_tips_view;
    private Button addView;
    private IThingDevice ITuyaDevice;
    private Toolbar toolbar;
    private EditText add_name_view;
    private SwitchCompat hijack_switch;
    private EditText add_password;
    private Button show_code_view;
    private String addString;
    private String mDevId;
    private boolean isAddMode = false;
    private OpModeBean opModeBean;
    private int total = 0;
    private String dpCode;
    private boolean tyabitmqxx = false;

    public static void startEditActivity(Context context, MemberInfoBean memberInfoBean, String devId, OpModeBean opModeBean, String dpCode) {

        Intent intent = new Intent(context, OpModeDetailActivity.class);
        //设备id
        intent.putExtra(Constant.DEVICE_ID, devId);
        //用户数据
        intent.putExtra(Constant.USER_DATA, JSONObject.toJSONString(memberInfoBean));
        //解锁方式详情
        intent.putExtra(Constant.UNLOCK_INFO, JSONObject.toJSONString(opModeBean));
        intent.putExtra(Constant.DP_CODE, dpCode);
        context.startActivity(intent);
    }

    public static void startAddActivity(Context context, MemberInfoBean memberInfoBean, String devId, String dpCode) {

        Intent intent = new Intent(context, OpModeDetailActivity.class);
        //设备id
        intent.putExtra(Constant.DEVICE_ID, devId);
        //用户数据
        intent.putExtra(Constant.USER_DATA, JSONObject.toJSONString(memberInfoBean));
        //锁类型
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
        String unlockData = getIntent().getStringExtra(Constant.UNLOCK_INFO);
        mDevId = getIntent().getStringExtra(Constant.DEVICE_ID);
        dpCode = getIntent().getStringExtra(Constant.DP_CODE);

        memberInfoBean = JSONObject.parseObject(userData, MemberInfoBean.class);

        if (!TextUtils.isEmpty(unlockData)) {
            mFrom = 1;
        }
        opModeBean = JSONObject.parseObject(unlockData, OpModeBean.class);

        IThingLockManager tuyaLockManager = ThingOptimusSdk.getManager(IThingLockManager.class);
        zigBeeLock = tuyaLockManager.getZigBeeLock(mDevId);

        ITuyaDevice = ThingHomeSdk.newDeviceInstance(mDevId);
        ITuyaDevice.registerDevListener(new IDevListener() {
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
        if (mFrom == 1) {
            request.setUnlockName(opModeBean.getUnlockName());
            request.setUnlockAttr(opModeBean.getUnlockAttr());
        }

        initView();
        //面板云能力
        zigBeeLock.getLockDeviceConfig(new IThingResultCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject result) {
                JSONObject powerCode = result.getJSONObject("powerCode");
                if (powerCode != null) {
                    if (powerCode.containsKey("tyabitmqxx")) {
                        tyabitmqxx = powerCode.getBooleanValue("tyabitmqxx");
                    } else {
                        tyabitmqxx = true;
                    }
                }
                initData();
            }

            @Override
            public void onError(String errorCode, String errorMessage) {

            }
        });
    }

    public void initView() {
        add_name_view = findViewById(R.id.add_name);
        hijack_switch = findViewById(R.id.hijack_switch);
        addView = findViewById(R.id.unlock_mode_add);
        add_password = findViewById(R.id.add_password);
        show_code_view = findViewById(R.id.show_code_view);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void initData() {
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
            if (!tyabitmqxx) {
                findViewById(R.id.password_wrap).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.password_wrap).setVisibility(View.GONE);
            }
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
            if (TextUtils.isEmpty(add_name_view.getText())) {
                showTips(getResources().getString(R.string.enter_unlock_mode_name), false);
                return;
            }
            String loadingStr = addString + "...";
            addView.setText(loadingStr);
            addView.setEnabled(false);
            if (mFrom == 0) {
                addUnlockMode();
            } else {
                upDataUnlockMode();
            }
        });

        show_code_view.setOnClickListener(v -> {
            ShowCodeActivity.startActivity(v.getContext(), JSONObject.toJSONString(opModeBean));
        });
    }

    private void addUnlockMode() {
        isAddMode = true;
        if (!tyabitmqxx && dpCode.equals(ThingUnlockType.PASSWORD)) {
            zigBeeLock.addPasswordOpmodeForMember(request, new IThingResultCallback<OpModeAddBean>() {
                @Override
                public void onSuccess(OpModeAddBean result) {
                    String tips = "add onSuccess";
                    showTips(tips, true);
                    L.i(Constant.TAG, tips + JSONObject.toJSONString(result));

                    isAddMode = false;

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
        } else {
            zigBeeLock.addUnlockOpmodeForMember(request, new IThingResultCallback<OpModeAddBean>() {
                @Override
                public void onSuccess(OpModeAddBean result) {
                    String tips = "add onSuccess";
                    showTips(tips, true);
                    L.i(Constant.TAG, tips + JSONObject.toJSONString(result));

                    isAddMode = false;

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
    }

    private void upDataUnlockMode() {
        zigBeeLock.modifyUnlockOpmodeForMember(
                request.getUnlockName(),
                opModeBean.getOpmodeId(),
                request.getUnlockAttr(),
                opModeBean.getUnlockId(),
                new IThingResultCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        String tips = "update onSuccess";
                        showTips(tips, true);
                        L.i(Constant.TAG, tips + JSONObject.toJSONString(result));
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
        int count = Integer.parseInt(lockResponse.substring(14, 16), 16);
        if (stage == ZigbeeOpModeStage.STAGE_START) {
            Log.i(Constant.TAG, "count:" + count);
            String tipsAdd = "count: " + 0 + "/" + count;
            showTips(tipsAdd, true);
            total = count;
        } else if (stage == ZigbeeOpModeStage.STAGE_ENTERING) {
            Log.i(Constant.TAG, "count:" + count);
            String tipsAdd = "count: " + count + "/" + total;
            showTips(tipsAdd, true);
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
        ITuyaDevice.unRegisterDevListener();
        ITuyaDevice.onDestroy();
        memberInfoBean = null;
    }

    private void cancelUnlock() {
        zigBeeLock.cancelUnlockOpMode(request.getUnlockType(), request.getLockUserId(), request.getUserType(), new IResultCallback() {
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
        if (isAddMode) {
            DialogUtils.showDelete(this, getResources().getString(R.string.whether_to_cancel), (dialog, which) -> {
                cancelUnlock();
            });
            OpModeDetailActivity.super.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }
}