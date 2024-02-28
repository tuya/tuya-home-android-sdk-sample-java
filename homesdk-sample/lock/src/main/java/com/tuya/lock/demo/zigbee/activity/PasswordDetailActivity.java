package com.tuya.lock.demo.zigbee.activity;

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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.alibaba.fastjson.JSONObject;
import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.optimus.lock.api.IThingLockManager;
import com.thingclips.smart.optimus.lock.api.IThingZigBeeLock;
import com.thingclips.smart.optimus.lock.api.zigbee.request.PasswordRequest;
import com.thingclips.smart.optimus.lock.api.zigbee.request.ScheduleBean;
import com.thingclips.smart.optimus.lock.api.zigbee.response.PasswordBean;
import com.thingclips.smart.optimus.sdk.ThingOptimusSdk;
import com.thingclips.smart.sdk.optimus.lock.utils.ZigbeeLockUtils;
import com.tuya.lock.demo.R;
import com.tuya.lock.demo.ble.utils.Utils;
import com.tuya.lock.demo.zigbee.utils.Constant;

public class PasswordDetailActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private IThingZigBeeLock zigBeeLock;

    private ScheduleBean scheduleBean = new ScheduleBean();

    private String schedule_effective_time_hour = "00";
    private String schedule_effective_time_minute = "00";
    private String schedule_invalid_time_hour = "00";
    private String schedule_invalid_time_minute = "00";

    private PasswordBean.DataBean dataBean;

    private String passwordValue;
    private int oneTime;
    private int mFrom = 0;


    public static void startActivity(Context context, String devId, int times) {
        Intent intent = new Intent(context, PasswordDetailActivity.class);
        //设备id
        intent.putExtra(Constant.DEVICE_ID, devId);
        intent.putExtra(Constant.AVAIL_TIMES, times);
        context.startActivity(intent);
    }

    public static void startEditActivity(Context context, String devId, PasswordBean.DataBean bean) {
        Intent intent = new Intent(context, PasswordDetailActivity.class);
        //设备id
        intent.putExtra(Constant.DEVICE_ID, devId);
        intent.putExtra(Constant.PASSWORD_DATA, JSONObject.toJSONString(bean));
        intent.putExtra(Constant.FROM, 1);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zigbee_password_temp_add);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        String mDevId = getIntent().getStringExtra(Constant.DEVICE_ID);
        mFrom = getIntent().getIntExtra(Constant.FROM, 0);
        dataBean = JSONObject.parseObject(getIntent().getStringExtra(Constant.PASSWORD_DATA), PasswordBean.DataBean.class);
        oneTime = getIntent().getIntExtra(Constant.AVAIL_TIMES, 0);
        if (oneTime == 1) {
            toolbar.setTitle(getResources().getString(R.string.password_one_time));
        } else {
            toolbar.setTitle(getResources().getString(R.string.password_periodic));
        }

        if (null == dataBean) {
            dataBean = new PasswordBean.DataBean();
        } else {
            if (null != dataBean.getModifyData().getScheduleList() &&
                    dataBean.getModifyData().getScheduleList().size() > 0 &&
                    null != dataBean.getModifyData().getScheduleList().get(0)) {
                scheduleBean = dataBean.getModifyData().getScheduleList().get(0);
            }
        }

        IThingLockManager tuyaLockManager = ThingOptimusSdk.getManager(IThingLockManager.class);
        zigBeeLock = tuyaLockManager.getZigBeeLock(mDevId);


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
            scheduleBean.setAllDay(true);

            password_day_weeks_1.setChecked(true);
            password_day_weeks_2.setChecked(true);
            password_day_weeks_3.setChecked(true);
            password_day_weeks_4.setChecked(true);
            password_day_weeks_5.setChecked(true);
            password_day_weeks_6.setChecked(true);
            password_day_weeks_7.setChecked(true);
        } else {
            scheduleBean.dayOfWeeks = ZigbeeLockUtils.parseWorkingDay(scheduleBean.getWorkingDay());
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
            showScheduleTimeMain();
        });
        if (scheduleBean.isAllDay() || scheduleBean.getInvalidTime() == 0) {
            password_all_day_wrap.check(R.id.password_all_day_no);
        } else {
            password_all_day_wrap.check(R.id.password_all_day_yes);
        }
        showScheduleTimeMain();

        setScheduleEffectiveTime();
        setScheduleInvalidTime();


        EditText password_name = findViewById(R.id.password_name);
        password_name.setText(dataBean.getName());
        password_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    dataBean.setName(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        EditText password_content = findViewById(R.id.password_content);
        password_content.setText(dataBean.getPassword());
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

        EditText password_effective_time = findViewById(R.id.password_effective_time);
        if (dataBean.getEffectiveTime() == 0) {
            dataBean.setEffectiveTime(System.currentTimeMillis());
        }
        long effectiveTime = dataBean.getEffectiveTime();
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
                    dataBean.setEffectiveTime(Utils.getStampTime(s.toString()));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        EditText password_invalid_time = findViewById(R.id.password_invalid_time);
        if (dataBean.getInvalidTime() == 0) {
            dataBean.setInvalidTime(System.currentTimeMillis() + 7 * 86400000L);
        }
        long invalidTime = dataBean.getInvalidTime();
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
                    dataBean.setInvalidTime(Utils.getStampTime(s.toString()));
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

        if (oneTime == 1) {
            findViewById(R.id.password_effective_time_main).setVisibility(View.GONE);
            findViewById(R.id.password_effective_time_line).setVisibility(View.GONE);
            findViewById(R.id.password_invalid_time_main).setVisibility(View.GONE);
            findViewById(R.id.password_invalid_time_line).setVisibility(View.GONE);
        } else {
            findViewById(R.id.password_effective_time_main).setVisibility(View.VISIBLE);
            findViewById(R.id.password_effective_time_line).setVisibility(View.VISIBLE);
            findViewById(R.id.password_invalid_time_main).setVisibility(View.VISIBLE);
            findViewById(R.id.password_invalid_time_line).setVisibility(View.VISIBLE);
        }

        findViewById(R.id.password_add).setOnClickListener(v -> {
            createPassword();
        });
    }

    private void setScheduleEffectiveTime() {
        EditText password_schedule_effective_time_hour = findViewById(R.id.password_schedule_effective_time_hour);
        String effectiveTime = String.valueOf(scheduleBean.getEffectiveTime());
        if (effectiveTime.length() == 3) {
            effectiveTime = "0" + effectiveTime;
        }
        if (scheduleBean.getEffectiveTime() == 0) {
            schedule_effective_time_hour = "00";
            password_schedule_effective_time_hour.setText(schedule_effective_time_hour);
        } else {
            password_schedule_effective_time_hour.setText(effectiveTime.substring(0, 2));
            schedule_effective_time_hour = effectiveTime.substring(0, 2);
        }
        password_schedule_effective_time_hour.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    String endStr = s.toString();
                    if (endStr.length() == 1) {
                        endStr = "0" + endStr;
                    }
                    schedule_effective_time_hour = endStr;
                } else {
                    schedule_effective_time_hour = "00";
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        EditText password_schedule_effective_time_minute = findViewById(R.id.password_schedule_effective_time_minute);
        if (scheduleBean.getEffectiveTime() == 0) {
            schedule_effective_time_minute = "00";
            password_schedule_effective_time_minute.setText(schedule_effective_time_minute);
        } else {
            password_schedule_effective_time_minute.setText(effectiveTime.substring(2, 4));
            schedule_effective_time_minute = effectiveTime.substring(2, 4);
        }
        password_schedule_effective_time_minute.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    String endStr = s.toString();
                    if (endStr.length() == 1) {
                        endStr = "0" + endStr;
                    }
                    schedule_effective_time_minute = endStr;
                } else {
                    schedule_effective_time_minute = "00";
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setScheduleInvalidTime() {
        EditText password_schedule_invalid_time_hour = findViewById(R.id.password_schedule_invalid_time_hour);
        String invalidTime = String.valueOf(scheduleBean.getInvalidTime());
        if (invalidTime.length() == 3) {
            invalidTime = "0" + invalidTime;
        }
        if (scheduleBean.getInvalidTime() == 0) {
            if (mFrom == 0) {
                schedule_invalid_time_hour = "23";
                password_schedule_invalid_time_hour.setText(schedule_invalid_time_hour);
            } else {
                schedule_invalid_time_hour = "00";
                password_schedule_invalid_time_hour.setText(schedule_invalid_time_hour);
            }
        } else {
            password_schedule_invalid_time_hour.setText(invalidTime.substring(0, 2));
            schedule_invalid_time_hour = invalidTime.substring(0, 2);
        }
        password_schedule_invalid_time_hour.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    String endStr = s.toString();
                    if (endStr.length() == 1) {
                        endStr = "0" + endStr;
                    }
                    schedule_invalid_time_hour = endStr;
                } else {
                    schedule_invalid_time_hour = "00";
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        EditText password_schedule_invalid_time_minute = findViewById(R.id.password_schedule_invalid_time_minute);
        if (scheduleBean.getInvalidTime() == 0) {
            if (mFrom == 0) {
                schedule_invalid_time_minute = "59";
                password_schedule_invalid_time_minute.setText(schedule_invalid_time_minute);
            } else {
                schedule_invalid_time_minute = "00";
                password_schedule_invalid_time_minute.setText(schedule_invalid_time_minute);
            }
        } else {
            password_schedule_invalid_time_minute.setText(invalidTime.substring(2, 4));
            schedule_invalid_time_minute = invalidTime.substring(2, 4);
        }
        password_schedule_invalid_time_minute.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    String endStr = s.toString();
                    if (endStr.length() == 1) {
                        endStr = "0" + endStr;
                    }
                    schedule_invalid_time_minute = endStr;
                } else {
                    schedule_invalid_time_minute = "00";
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void showScheduleMain() {
        View password_schedule_main = findViewById(R.id.password_schedule_main);
        if (oneTime == 1) {
            password_schedule_main.setVisibility(View.GONE);
        } else {
            password_schedule_main.setVisibility(View.VISIBLE);
        }

    }

    private void showScheduleTimeMain() {
        View password_schedule_time_main = findViewById(R.id.password_schedule_time_main);
        View password_day_weeks_main = findViewById(R.id.password_day_weeks_main);
        RadioButton password_all_no = findViewById(R.id.password_all_day_no);
        if (password_all_no.isChecked()) {
            scheduleBean.setAllDay(true);
            password_schedule_time_main.setVisibility(View.GONE);
            password_day_weeks_main.setVisibility(View.GONE);
        } else {
            scheduleBean.setAllDay(false);
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
        if (!scheduleBean.isAllDay()) {
            scheduleBean.setEffectiveTime(Integer.parseInt(schedule_effective_time_hour + schedule_effective_time_minute));
            scheduleBean.setInvalidTime(Integer.parseInt(schedule_invalid_time_hour + schedule_invalid_time_minute));
            scheduleBean.setWorkingDay(Integer.parseInt(ZigbeeLockUtils.convertWorkingDay(scheduleBean.dayOfWeeks), 16));
        } else {
            scheduleBean.setEffectiveTime(0);
            scheduleBean.setInvalidTime(0);
        }

        PasswordRequest passwordRequest = new PasswordRequest();
        passwordRequest.setName(dataBean.getName());
        if (!TextUtils.isEmpty(passwordValue)) {
            passwordRequest.setPassword(passwordValue);
        }
        if (oneTime == 0) {
            passwordRequest.setSchedule(scheduleBean);
            passwordRequest.setEffectiveTime(dataBean.getEffectiveTime());
            passwordRequest.setInvalidTime(dataBean.getInvalidTime());
        }
        passwordRequest.setOneTime(oneTime);
        if (mFrom == 1) {
            passwordRequest.setId(dataBean.getId());
        }

        Log.i(Constant.TAG, "request:" + passwordRequest);
        if (mFrom == 1) {
            //编辑临时密码
            zigBeeLock.modifyTemporaryPassword(passwordRequest, new IThingResultCallback<String>() {
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
            return;
        }
        zigBeeLock.addTemporaryPassword(passwordRequest, new IThingResultCallback<String>() {
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