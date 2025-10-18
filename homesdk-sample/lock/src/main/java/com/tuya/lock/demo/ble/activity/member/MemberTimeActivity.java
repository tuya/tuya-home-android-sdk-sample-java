package com.tuya.lock.demo.ble.activity.member;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.alibaba.fastjson.JSONObject;
import com.tuya.lock.demo.R;
import com.tuya.lock.demo.ble.constant.Constant;
import com.tuya.lock.demo.ble.utils.Utils;
import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.optimus.lock.api.IThingBleLockV2;
import com.thingclips.smart.optimus.lock.api.IThingLockManager;
import com.thingclips.smart.optimus.sdk.ThingOptimusSdk;
import com.thingclips.smart.sdk.optimus.lock.bean.ble.MemberInfoBean;
import com.thingclips.smart.sdk.optimus.lock.bean.ble.ScheduleBean;
import com.thingclips.smart.sdk.optimus.lock.utils.LockUtil;


/**
 * 添加成员
 */
public class MemberTimeActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private IThingBleLockV2 tuyaLockDevice;
    private MemberInfoBean userBean;
    private ScheduleBean scheduleBean;

    private int schedule_effective_time_hour = 0;
    private int schedule_effective_time_minute = 0;
    private int schedule_invalid_time_hour = 0;
    private int schedule_invalid_time_minute = 0;

    public static void startActivity(Context context, MemberInfoBean memberInfoBean,
                                     String devId) {
        Intent intent = new Intent(context, MemberTimeActivity.class);
        //设备id
        intent.putExtra(Constant.DEVICE_ID, devId);
        //编辑的密码数据
        intent.putExtra(Constant.USER_DATA, JSONObject.toJSONString(memberInfoBean));
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_time);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        String userData = getIntent().getStringExtra(Constant.USER_DATA);
        String mDevId = getIntent().getStringExtra(Constant.DEVICE_ID);
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

        if (null != userBean.getTimeScheduleInfo().getScheduleDetails() && userBean.getTimeScheduleInfo().getScheduleDetails().size() > 0) {
            scheduleBean = userBean.getTimeScheduleInfo().getScheduleDetails().get(0);
        } else {
            scheduleBean = new ScheduleBean();
            userBean.getTimeScheduleInfo().getScheduleDetails().add(scheduleBean);
        }

        RadioButton user_unlock_permanent_yes = findViewById(R.id.user_unlock_permanent_yes);
        RadioButton user_unlock_permanent_no = findViewById(R.id.user_unlock_permanent_no);
        if (userBean.getTimeScheduleInfo().isPermanent()) {
            user_unlock_permanent_yes.setChecked(true);
        } else {
            user_unlock_permanent_no.setChecked(true);
        }

        RadioGroup user_unlock_permanent = findViewById(R.id.user_unlock_permanent);
        user_unlock_permanent.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.user_unlock_permanent_yes) {
                userBean.getTimeScheduleInfo().setPermanent(true);
            } else if (checkedId == R.id.user_unlock_permanent_no) {
                userBean.getTimeScheduleInfo().setPermanent(false);
            }
            showTimeMain(userBean.getTimeScheduleInfo().isPermanent());
        });
        showTimeMain(userBean.getTimeScheduleInfo().isPermanent());

        EditText user_effective_timestamp_content = findViewById(R.id.user_effective_timestamp_content);
        if (userBean.getTimeScheduleInfo().getEffectiveTime() > 0) {
            user_effective_timestamp_content.setText(Utils.getDateDay(userBean.getTimeScheduleInfo().getEffectiveTime() * 1000, "yyyy-MM-dd HH:mm:ss"));
        } else {
            user_effective_timestamp_content.setText(Utils.getDateDay(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss"));
            userBean.getTimeScheduleInfo().setEffectiveTime(System.currentTimeMillis());
        }
        user_effective_timestamp_content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    userBean.getTimeScheduleInfo().setEffectiveTime(Utils.getStampTime(s.toString(), "yyyy-MM-dd HH:mm:ss"));
                    Log.i(Constant.TAG, "effectiveTimestamp select:" + userBean.getTimeScheduleInfo().getEffectiveTime());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        EditText user_invalid_timestamp_content = findViewById(R.id.user_invalid_timestamp_content);
        if (userBean.getTimeScheduleInfo().getExpiredTime() > 0) {
            user_invalid_timestamp_content.setText(Utils.getDateDay(userBean.getTimeScheduleInfo().getExpiredTime() * 1000, "yyyy-MM-dd HH:mm:ss"));
        } else {
            user_invalid_timestamp_content.setText(Utils.getDateDay(System.currentTimeMillis() + 31104000000L, "yyyy-MM-dd HH:mm:ss"));
            userBean.getTimeScheduleInfo().setExpiredTime(System.currentTimeMillis() + 31104000000L);
        }
        user_invalid_timestamp_content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    userBean.getTimeScheduleInfo().setExpiredTime(Utils.getStampTime(s.toString(), "yyyy-MM-dd HH:mm:ss"));
                    Log.i(Constant.TAG, "invalidTimestamp select:" + userBean.getTimeScheduleInfo().getExpiredTime());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        RadioGroup password_all_day_wrap = findViewById(R.id.password_all_day_wrap);
        password_all_day_wrap.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.password_all_day_yes) {
                scheduleBean.allDay = false;
            } else if (checkedId == R.id.password_all_day_no) {
                scheduleBean.allDay = true;
            }
            showScheduleTimeMain();
        });
        password_all_day_wrap.check(scheduleBean.allDay ? R.id.password_all_day_no : R.id.password_all_day_yes);
        showScheduleTimeMain();
        setScheduleEffectiveTime();
        setScheduleInvalidTime();

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

        SwitchCompat user_offline_unlock = findViewById(R.id.user_offline_unlock);
        user_offline_unlock.setChecked(userBean.isOfflineUnlock());
        user_offline_unlock.setOnCheckedChangeListener((buttonView, isChecked) -> {
            userBean.setOfflineUnlock(isChecked);
        });

        Button submitBtn = findViewById(R.id.edit_user_submit);
        submitBtn.setOnClickListener(v -> {
            updateLockUser();
        });
        submitBtn.setText("更新时效");
    }

    private void showTimeMain(boolean hide) {
        View user_time_main = findViewById(R.id.user_time_main);
        if (hide) {
            user_time_main.setVisibility(View.GONE);
        } else {
            user_time_main.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tuyaLockDevice.onDestroy();
    }

    private void showScheduleTimeMain() {
        View password_schedule_time_main = findViewById(R.id.password_schedule_time_main);
        if (scheduleBean.allDay) {
            password_schedule_time_main.setVisibility(View.GONE);
        } else {
            password_schedule_time_main.setVisibility(View.VISIBLE);
        }
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


    /**
     * 用户更新信息
     */
    private void updateLockUser() {
        if (!scheduleBean.allDay) {
            scheduleBean.effectiveTime = schedule_effective_time_hour * 60 + schedule_effective_time_minute;
            scheduleBean.invalidTime = schedule_invalid_time_hour * 60 + schedule_invalid_time_minute;
            scheduleBean.workingDay = Integer.parseInt(LockUtil.convertWorkingDay(scheduleBean.dayOfWeeks), 16);
        } else {
            scheduleBean.effectiveTime = 0;
            scheduleBean.invalidTime = 0;
        }

        tuyaLockDevice.updateProLockMemberTime(userBean, new IThingResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Log.i(Constant.TAG, "update lock user time success");
                Toast.makeText(getApplicationContext(), "add lock user success", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String code, String message) {
                Log.e(Constant.TAG, "update lock user time failed: code = " + code + "  message = " + message);
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
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