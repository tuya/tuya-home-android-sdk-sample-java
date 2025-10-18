package com.tuya.lock.demo.ble.activity.member;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.alibaba.fastjson.JSONObject;
import com.thingclips.smart.home.sdk.anntation.MemberRole;
import com.tuya.lock.demo.R;
import com.tuya.lock.demo.ble.constant.Constant;
import com.thingclips.smart.home.sdk.bean.MemberBean;
import com.thingclips.smart.home.sdk.bean.MemberWrapperBean;
import com.thingclips.smart.optimus.lock.api.IThingBleLockV2;
import com.thingclips.smart.optimus.lock.api.IThingLockManager;
import com.thingclips.smart.optimus.sdk.ThingOptimusSdk;
import com.thingclips.smart.sdk.api.IResultCallback;
import com.thingclips.smart.sdk.api.IThingDataCallback;
import com.thingclips.smart.sdk.optimus.lock.bean.ble.MemberInfoBean;
import com.tuya.appsdk.sample.resource.HomeModel;


/**
 * 添加成员
 */
public class MemberDetailActivity extends AppCompatActivity {

    private IThingBleLockV2 tuyaLockDevice;
    private MemberInfoBean userBean;
    private int mFrom;

    private EditText nameView;
    private EditText account_View;
    private EditText role_View;
    private EditText countryCode_view;

    private MemberWrapperBean.Builder memberWrapperBean;

    public static void startActivity(Context context, MemberInfoBean memberInfoBean, String devId, int from) {
        Intent intent = new Intent(context, MemberDetailActivity.class);
        //设备id
        intent.putExtra(Constant.DEVICE_ID, devId);
        //0创建、1编辑
        intent.putExtra(Constant.FROM, from);
        //用户信息
        intent.putExtra(Constant.USER_DATA, JSONObject.toJSONString(memberInfoBean));
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_add);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        String userData = getIntent().getStringExtra(Constant.USER_DATA);
        String mDevId = getIntent().getStringExtra(Constant.DEVICE_ID);
        mFrom = getIntent().getIntExtra(Constant.FROM, 0);
        try {
            userBean = JSONObject.parseObject(userData, MemberInfoBean.class);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        if (null == userBean) {
            userBean = new MemberInfoBean();
        }

        IThingLockManager tuyaLockManager = ThingOptimusSdk.getManager(IThingLockManager.class);
        tuyaLockDevice = tuyaLockManager.getBleLockV2(mDevId);

        memberWrapperBean = new MemberWrapperBean.Builder();

        if (mFrom == 1) {
            toolbar.setTitle(getResources().getString(R.string.submit_edit));
        } else {
            toolbar.setTitle(getResources().getString(R.string.user_add));
        }

        /* *
         * 用户昵称
         */
        nameView = findViewById(R.id.nameView);
        nameView.setText(userBean.getNickName());

        /**
         * 受邀账号
         */
        account_View = findViewById(R.id.account_View);
        if (mFrom == 1) {
            account_View.setEnabled(false);
            findViewById(R.id.account_wrap).setVisibility(View.GONE);
            findViewById(R.id.account_line).setVisibility(View.GONE);
            findViewById(R.id.countryCode_wrap).setVisibility(View.GONE);
            findViewById(R.id.countryCode_line).setVisibility(View.GONE);
        } else {
            findViewById(R.id.account_wrap).setVisibility(View.VISIBLE);
            findViewById(R.id.account_line).setVisibility(View.VISIBLE);
            findViewById(R.id.countryCode_wrap).setVisibility(View.VISIBLE);
            findViewById(R.id.countryCode_line).setVisibility(View.VISIBLE);
            account_View.setEnabled(true);
        }

        /**
         * 账号ID
         * 10. 管理员 20.普通成员. 30. 没有名字的成员. 50. 家庭拥有者
         */
        role_View = findViewById(R.id.role_View);
        int userType = MemberRole.INVALID_ROLE;
        if (userBean.getUserType() == 50) {
            userType = MemberRole.ROLE_OWNER;
        } else if (userBean.getUserType() == 10) {
            userType = MemberRole.ROLE_ADMIN;
        } else if (userBean.getUserType() == 20) {
            userType = MemberRole.ROLE_MEMBER;
        } else if (userBean.getUserType() == 30) {
            userType = MemberRole.ROLE_CUSTOM;
        }
        if (userBean.getUserType() != 0) {
            role_View.setText(String.valueOf(userType));
            memberWrapperBean.setRole(userType);
        }

        if (!TextUtils.isEmpty(userBean.getUserId())) {
            try {
                memberWrapperBean.setMemberId(Long.parseLong(userBean.getUserId()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * 是否需要受邀请者同意接受加入家庭邀请
         */
        memberWrapperBean.setAutoAccept(true);
        RadioGroup autoAccept_wrap = findViewById(R.id.autoAccept_wrap);
        autoAccept_wrap.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.autoAccept_yes) {
                memberWrapperBean.setAutoAccept(true);
            } else {
                memberWrapperBean.setAutoAccept(false);
            }
        });
        if (mFrom == 1) {
            findViewById(R.id.autoAccept_main).setVisibility(View.GONE);
        } else {
            findViewById(R.id.autoAccept_main).setVisibility(View.VISIBLE);
        }

        /**
         * 提交创建或更新
         */
        Button submitBtn = findViewById(R.id.edit_user_submit);
        submitBtn.setOnClickListener(v -> {
            if (mFrom == 0) {
                addLockUser();
            } else {
                upDateLockUser();
            }
        });
        if (mFrom == 1) {
            submitBtn.setText(getResources().getString(R.string.submit_edit));
        } else {
            submitBtn.setText(getResources().getString(R.string.submit_add));
        }

        countryCode_view = findViewById(R.id.countryCode_view);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        tuyaLockDevice.onDestroy();
    }

    /**
     * 用户更新信息
     */
    private void addLockUser() {
        if (!TextUtils.isEmpty(nameView.getText())) {
            memberWrapperBean.setNickName(nameView.getText().toString().trim());
        }
        if (!TextUtils.isEmpty(account_View.getText()) && !TextUtils.isEmpty(countryCode_view.getText())) {
            memberWrapperBean.setCountryCode(countryCode_view.getText().toString().trim());
            memberWrapperBean.setAccount(account_View.getText().toString().trim());
        }
        if (!TextUtils.isEmpty(role_View.getText())) {
            memberWrapperBean.setRole(Integer.parseInt(role_View.getText().toString().trim()));
        }
        memberWrapperBean.setHomeId(HomeModel.getCurrentHome(this));

        tuyaLockDevice.createProLockMember(memberWrapperBean.build(), new IThingDataCallback<MemberBean>() {
            @Override
            public void onSuccess(MemberBean result) {
                Log.i(Constant.TAG, "add lock user success");
                Toast.makeText(getApplicationContext(), "add lock user success", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String code, String message) {
                Log.e(Constant.TAG, "add lock user failed: code = " + code + "  message = " + message);
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void upDateLockUser() {
        memberWrapperBean.setNickName(nameView.getText().toString().trim());
        memberWrapperBean.setRole(Integer.parseInt(role_View.getText().toString().trim()));
        tuyaLockDevice.updateProLockMemberInfo(memberWrapperBean.build(), new IResultCallback() {
            @Override
            public void onError(String code, String error) {
                Log.e(Constant.TAG, "add lock user failed: code = " + code + "  message = " + error);
                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess() {
                Log.i(Constant.TAG, "update lock user success");
                Toast.makeText(getApplicationContext(), "update lock user success", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}