package com.tuya.lock.demo.wifi.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.alibaba.fastjson.JSONObject;
import com.thingclips.smart.optimus.lock.api.bean.UnlockRelation;
import com.tuya.lock.demo.R;
import com.tuya.lock.demo.zigbee.utils.Constant;


public class OpModeDetailActivity extends AppCompatActivity {

    private String dpCode;
    private int sn;
    private int mFrom;
    private EditText add_password_sn;
    private Button addView;

    private int inputSn = 0;

    public static final int REQUEST_CODE = 9999;

    public static void startActivity(Activity activity, int sn, String dpCode) {

        Intent intent = new Intent(activity, OpModeDetailActivity.class);
        intent.putExtra(Constant.DP_CODE, dpCode);
        intent.putExtra("sn", sn);
        activity.startActivityForResult(intent, REQUEST_CODE);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_unlock_add);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        dpCode = getIntent().getStringExtra(Constant.DP_CODE);
        sn = getIntent().getIntExtra("sn", 0);

        if (sn > 0) {
            mFrom = 1;
        }

        initView();
        initData();
    }

    public void initView() {
        add_password_sn = findViewById(R.id.add_password_sn);
        addView = findViewById(R.id.unlock_mode_add);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void initData() {
        add_password_sn.setText(String.valueOf(sn));
        add_password_sn.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    inputSn = Integer.parseInt(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        String addString;
        if (mFrom == 1) {
            addString = getResources().getString(R.string.submit_edit);
        } else {
            addString = getResources().getString(R.string.submit_add);
        }
        addView.setText(addString);
        addView.setOnClickListener(v -> {
            if (inputSn != sn && inputSn > 0) {
                setResult(0, getOneIntent());
            }
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        if (inputSn != sn && inputSn > 0) {
            setResult(0, getOneIntent());
        }
        finish();
        super.onBackPressed();
    }

    private Intent getOneIntent() {
        UnlockRelation relation = new UnlockRelation();
        relation.unlockType = dpCode;
        relation.passwordNumber = inputSn;
        Intent intent = new Intent();
        intent.putExtra(Constant.UNLOCK_INFO, JSONObject.toJSONString(relation));
        return intent;
    }

}