package com.tuya.lock.demo.ble.activity.password;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.alibaba.fastjson.JSONObject;
import com.tuya.lock.demo.R;
import com.tuya.lock.demo.ble.constant.Constant;
import com.tuya.lock.demo.ble.utils.Utils;
import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.optimus.lock.api.IThingBleLockV2;
import com.thingclips.smart.optimus.lock.api.IThingLockManager;
import com.thingclips.smart.optimus.lock.api.bean.ProTempPasswordItem;
import com.thingclips.smart.optimus.sdk.ThingOptimusSdk;
import com.thingclips.smart.sdk.optimus.lock.bean.ble.PasswordRequest;
import com.thingclips.smart.sdk.optimus.lock.bean.ble.ScheduleBean;
import com.thingclips.smart.sdk.optimus.lock.utils.LockUtil;

public class PasswordProOnlineDetailActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private IThingBleLockV2 tuyaLockDevice;

    private ScheduleBean scheduleBean = new ScheduleBean();

    private int schedule_effective_time_hour = 0;
    private int schedule_effective_time_minute = 0;
    private int schedule_invalid_time_hour = 0;
    private int schedule_invalid_time_minute = 0;

    private int mFrom = 0;//0是创建、1是编辑
    private ProTempPasswordItem mPasswordData;

    private String passwordValue;

    public static void startActivity(Context context, ProTempPasswordItem passwordItem,
                                     String devId, int from) {
        Intent intent = new Intent(context, PasswordProOnlineDetailActivity.class);
        //设备id
        intent.putExtra(Constant.DEVICE_ID, devId);
        //创建还是编辑
        intent.putExtra(Constant.FROM, from);
        //编辑的密码数据
        intent.putExtra(Constant.PASSWORD_DATA, JSONObject.toJSONString(passwordItem));
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_temp_add);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        String mDevId = getIntent().getStringExtra(Constant.DEVICE_ID);
        mFrom = getIntent().getIntExtra(Constant.FROM, 0);
        mPasswordData = JSONObject.parseObject(getIntent().getStringExtra(Constant.PASSWORD_DATA), ProTempPasswordItem.class);

        if (null == mPasswordData) {
            mPasswordData = new ProTempPasswordItem();
        } else {
            if (null != mPasswordData.getScheduleDetails() && null != mPasswordData.getScheduleDetails().get(0)) {
                scheduleBean = mPasswordData.getScheduleDetails().get(0);
            }
        }

        IThingLockManager tuyaLockManager = ThingOptimusSdk.getManager(IThingLockManager.class);
        tuyaLockDevice = tuyaLockManager.getBleLockV2(mDevId);


        showScheduleMain();

        CheckBox password_day_weeks_1 = findViewById(R.id.password_day_weeks_1);
        CheckBox password_day_weeks_2 = findViewById(R.id.password_day_weeks_2);
        CheckBox password_day_weeks_3 = findViewById(R.id.password_day_weeks_3);
        CheckBox password_day_weeks_4 = findViewById(R.id.password_day_weeks_4);
        CheckBox password_day_weeks_5 = findViewById(R.id.password_day_weeks_5);
        CheckBox password_day_weeks_6 = findViewById(R.id.password_day_weeks_6);
        CheckBox password_day_weeks_7 = findViewById(R.id.password_day_weeks_7);
        password_day_weeks_1.setOnCheckedChangeListener(this);
        password_day_weeks_2.setOnCheckedChangeListener(this);
        password_day_weeks_3.setOnCheckedChangeListener(this);
        password_day_weeks_4.setOnCheckedChangeListener(this);
        password_day_weeks_5.setOnCheckedChangeListener(this);
        password_day_weeks_6.setOnCheckedChangeListener(this);
        password_day_weeks_7.setOnCheckedChangeListener(this);

        if (mFrom == 0) {
            scheduleBean.dayOfWeeks.add(ScheduleBean.DayOfWeek.FRIDAY);
            scheduleBean.dayOfWeeks.add(ScheduleBean.DayOfWeek.MONDAY);
            scheduleBean.dayOfWeeks.add(ScheduleBean.DayOfWeek.SATURDAY);
            scheduleBean.dayOfWeeks.add(ScheduleBean.DayOfWeek.SUNDAY);
            scheduleBean.dayOfWeeks.add(ScheduleBean.DayOfWeek.THURSDAY);
            scheduleBean.dayOfWeeks.add(ScheduleBean.DayOfWeek.TUESDAY);
            scheduleBean.dayOfWeeks.add(ScheduleBean.DayOfWeek.WEDNESDAY);

            password_day_weeks_1.setChecked(true);
            password_day_weeks_2.setChecked(true);
            password_day_weeks_3.setChecked(true);
            password_day_weeks_4.setChecked(true);
            password_day_weeks_5.setChecked(true);
            password_day_weeks_6.setChecked(true);
            password_day_weeks_7.setChecked(true);
        } else {
            scheduleBean.dayOfWeeks = LockUtil.parseWorkingDay(scheduleBean.workingDay);
            for (ScheduleBean.DayOfWeek dayOfWeek : scheduleBean.dayOfWeeks) {
                if (dayOfWeek == ScheduleBean.DayOfWeek.MONDAY) {
                    password_day_weeks_1.setChecked(true);
                } else if (dayOfWeek == ScheduleBean.DayOfWeek.TUESDAY) {
                    password_day_weeks_2.setChecked(true);
                } else if (dayOfWeek == ScheduleBean.DayOfWeek.WEDNESDAY) {
                    password_day_weeks_3.setChecked(true);
                } else if (dayOfWeek == ScheduleBean.DayOfWeek.THURSDAY) {
                    password_day_weeks_4.setChecked(true);
                } else if (dayOfWeek == ScheduleBean.DayOfWeek.FRIDAY) {
                    password_day_weeks_5.setChecked(true);
                } else if (dayOfWeek == ScheduleBean.DayOfWeek.SATURDAY) {
                    password_day_weeks_6.setChecked(true);
                } else if (dayOfWeek == ScheduleBean.DayOfWeek.SUNDAY) {
                    password_day_weeks_7.setChecked(true);
                }
            }
        }

        RadioGroup password_all_day_wrap = findViewById(R.id.password_all_day_wrap);
        password_all_day_wrap.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.password_all_day_yes) {
                scheduleBean.allDay = false;
            } else if (checkedId == R.id.password_all_day_no) {
                scheduleBean.allDay = true;
            }
            showScheduleTimeMain();
        });
        if (scheduleBean.allDay) {
            password_all_day_wrap.check(R.id.password_all_day_no);
        } else {
            password_all_day_wrap.check(R.id.password_all_day_yes);
        }

        showScheduleTimeMain();

        setScheduleEffectiveTime();
        setScheduleInvalidTime();


        EditText password_name = findViewById(R.id.password_name);
        password_name.setText(mPasswordData.getName());
        password_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    mPasswordData.setName(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        EditText password_content = findViewById(R.id.password_content);
        password_content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    passwordValue = s.toString();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        if (mFrom == 1) {
            findViewById(R.id.password_content_wrap).setVisibility(View.GONE);
            findViewById(R.id.password_content_line).setVisibility(View.GONE);
        } else {
            findViewById(R.id.password_content_wrap).setVisibility(View.VISIBLE);
            findViewById(R.id.password_content_line).setVisibility(View.VISIBLE);
        }

        EditText password_effective_time = findViewById(R.id.password_effective_time);
        if (mPasswordData.getEffectiveTime() == 0) {
            mPasswordData.setEffectiveTime(System.currentTimeMillis());
        }
        long effectiveTime = mPasswordData.getEffectiveTime();
        if (String.valueOf(effectiveTime).length() == 10) {
            effectiveTime = effectiveTime * 1000;
        }
        password_effective_time.setText(Utils.getDateDay(effectiveTime));
        password_effective_time.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    mPasswordData.setEffectiveTime(Utils.getStampTime(s.toString()));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        EditText password_invalid_time = findViewById(R.id.password_invalid_time);
        if (mPasswordData.getInvalidTime() == 0) {
            mPasswordData.setInvalidTime(System.currentTimeMillis() + 7 * 86400000L);
        }
        long invalidTime = mPasswordData.getInvalidTime();
        if (String.valueOf(invalidTime).length() == 10) {
            invalidTime = invalidTime * 1000;
        }
        password_invalid_time.setText(Utils.getDateDay(invalidTime));
        password_invalid_time.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    mPasswordData.setInvalidTime(Utils.getStampTime(s.toString()));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

//        if (mFrom == 1) {
//            findViewById(R.id.password_content_wrap).setVisibility(View.GONE);
//            findViewById(R.id.password_content_line).setVisibility(View.GONE);
//        } else {
//            findViewById(R.id.password_content_wrap).setVisibility(View.VISIBLE);
//            findViewById(R.id.password_content_line).setVisibility(View.VISIBLE);
//        }

        findViewById(R.id.password_add).setOnClickListener(v -> {
            createPassword();
        });
    }

    private void setScheduleEffectiveTime() {
        EditText password_schedule_effective_time_hour = findViewById(R.id.password_schedule_effective_time_hour);
        if (scheduleBean.effectiveTime == 0) {
            schedule_effective_time_hour = 0;
            password_schedule_effective_time_hour.setText("0");
        } else {
            password_schedule_effective_time_hour.setText(String.valueOf(scheduleBean.effectiveTime / 60));
            schedule_effective_time_hour = scheduleBean.effectiveTime / 60;
        }
        password_schedule_effective_time_hour.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    schedule_effective_time_hour = Integer.parseInt(s.toString());
                } else {
                    schedule_effective_time_hour = 0;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        EditText password_schedule_effective_time_minute = findViewById(R.id.password_schedule_effective_time_minute);
        if (scheduleBean.effectiveTime == 0) {
            schedule_effective_time_minute = 0;
            password_schedule_effective_time_minute.setText("0");
        } else {
            password_schedule_effective_time_minute.setText(String.valueOf(scheduleBean.effectiveTime % 60));
            schedule_effective_time_minute = scheduleBean.effectiveTime % 60;
        }
        password_schedule_effective_time_minute.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    schedule_effective_time_minute = Integer.parseInt(s.toString());
                } else {
                    schedule_effective_time_minute = 0;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setScheduleInvalidTime() {
        EditText password_schedule_invalid_time_hour = findViewById(R.id.password_schedule_invalid_time_hour);
        if (scheduleBean.invalidTime == 0) {
            schedule_invalid_time_hour = 23;
            password_schedule_invalid_time_hour.setText("23");
        } else {
            password_schedule_invalid_time_hour.setText(String.valueOf(scheduleBean.invalidTime / 60));
            schedule_invalid_time_hour = scheduleBean.invalidTime / 60;
        }
        password_schedule_invalid_time_hour.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    schedule_invalid_time_hour = Integer.parseInt(s.toString());
                } else {
                    schedule_invalid_time_hour = 0;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        EditText password_schedule_invalid_time_minute = findViewById(R.id.password_schedule_invalid_time_minute);
        if (scheduleBean.invalidTime == 0) {
            schedule_invalid_time_minute = 59;
            password_schedule_invalid_time_minute.setText("59");
        } else {
            password_schedule_invalid_time_minute.setText(String.valueOf(scheduleBean.invalidTime % 60));
            schedule_invalid_time_minute = scheduleBean.invalidTime % 60;
        }
        password_schedule_invalid_time_minute.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    schedule_invalid_time_minute = Integer.parseInt(s.toString());
                } else {
                    schedule_invalid_time_minute = 0;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void showScheduleMain() {
        View password_schedule_main = findViewById(R.id.password_schedule_main);
        password_schedule_main.setVisibility(View.VISIBLE);
    }

    private void showScheduleTimeMain() {
        View password_schedule_time_main = findViewById(R.id.password_schedule_time_main);
        View password_day_weeks_main = findViewById(R.id.password_day_weeks_main);
        if (scheduleBean.allDay) {
            password_schedule_time_main.setVisibility(View.GONE);
            password_day_weeks_main.setVisibility(View.GONE);
        } else {
            password_schedule_time_main.setVisibility(View.VISIBLE);
            password_day_weeks_main.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void createPassword() {
        if (!scheduleBean.allDay) {
            scheduleBean.effectiveTime = schedule_effective_time_hour * 60 + schedule_effective_time_minute;
            scheduleBean.invalidTime = schedule_invalid_time_hour * 60 + schedule_invalid_time_minute;
            scheduleBean.workingDay = Integer.parseInt(LockUtil.convertWorkingDay(scheduleBean.dayOfWeeks), 16);
        } else {
            scheduleBean.effectiveTime = 0;
            scheduleBean.invalidTime = 0;
        }

        PasswordRequest passwordRequest = new PasswordRequest();
        passwordRequest.setPassword(passwordValue);
        passwordRequest.setAvailTime(0);
        passwordRequest.setSn(mPasswordData.getSn());
        passwordRequest.setName(mPasswordData.getName());
        passwordRequest.setSchedule(scheduleBean);
        passwordRequest.setEffectiveTime(mPasswordData.getEffectiveTime());
        passwordRequest.setInvalidTime(mPasswordData.getInvalidTime());
        if (mFrom == 1) {
            passwordRequest.setId(mPasswordData.getUnlockBindingId());
        }

        Log.i(Constant.TAG, "request:" + passwordRequest.toString());


        if (mFrom == 0) {
            tuyaLockDevice.getProCustomOnlinePassword(passwordRequest, new IThingResultCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    Toast.makeText(getApplicationContext(), "onSuccess", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(String errorCode, String errorMessage) {
                    Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            tuyaLockDevice.updateProOnlinePassword(passwordRequest, new IThingResultCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    Toast.makeText(getApplicationContext(), "onSuccess", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(String errorCode, String errorMessage) {
                    Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (null == scheduleBean) {
            scheduleBean = new ScheduleBean();
        }
        int id = buttonView.getId();
        if (id == R.id.password_day_weeks_1) {
            if (isChecked) {
                scheduleBean.dayOfWeeks.add(ScheduleBean.DayOfWeek.MONDAY);
            } else {
                scheduleBean.dayOfWeeks.remove(ScheduleBean.DayOfWeek.MONDAY);
            }
        } else if (id == R.id.password_day_weeks_2) {
            if (isChecked) {
                scheduleBean.dayOfWeeks.add(ScheduleBean.DayOfWeek.TUESDAY);
            } else {
                scheduleBean.dayOfWeeks.remove(ScheduleBean.DayOfWeek.TUESDAY);
            }
        } else if (id == R.id.password_day_weeks_3) {
            if (isChecked) {
                scheduleBean.dayOfWeeks.add(ScheduleBean.DayOfWeek.WEDNESDAY);
            } else {
                scheduleBean.dayOfWeeks.remove(ScheduleBean.DayOfWeek.WEDNESDAY);
            }
        } else if (id == R.id.password_day_weeks_4) {
            if (isChecked) {
                scheduleBean.dayOfWeeks.add(ScheduleBean.DayOfWeek.THURSDAY);
            } else {
                scheduleBean.dayOfWeeks.remove(ScheduleBean.DayOfWeek.THURSDAY);
            }
        } else if (id == R.id.password_day_weeks_5) {
            if (isChecked) {
                scheduleBean.dayOfWeeks.add(ScheduleBean.DayOfWeek.FRIDAY);
            } else {
                scheduleBean.dayOfWeeks.remove(ScheduleBean.DayOfWeek.FRIDAY);
            }
        } else if (id == R.id.password_day_weeks_6) {
            if (isChecked) {
                scheduleBean.dayOfWeeks.add(ScheduleBean.DayOfWeek.SATURDAY);
            } else {
                scheduleBean.dayOfWeeks.remove(ScheduleBean.DayOfWeek.SATURDAY);
            }
        } else if (id == R.id.password_day_weeks_7) {
            if (isChecked) {
                scheduleBean.dayOfWeeks.add(ScheduleBean.DayOfWeek.SUNDAY);
            } else {
                scheduleBean.dayOfWeeks.remove(ScheduleBean.DayOfWeek.SUNDAY);
            }
        }
    }
}