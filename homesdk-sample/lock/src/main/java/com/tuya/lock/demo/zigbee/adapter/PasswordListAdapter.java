package com.tuya.lock.demo.zigbee.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.thingclips.smart.android.common.utils.L;
import com.thingclips.smart.optimus.lock.api.zigbee.request.ScheduleBean;
import com.thingclips.smart.optimus.lock.api.zigbee.response.PasswordBean;
import com.tuya.lock.demo.R;
import com.tuya.lock.demo.ble.activity.code.ShowCodeActivity;
import com.tuya.lock.demo.ble.utils.Utils;
import com.tuya.lock.demo.zigbee.activity.PasswordUpdateActivity;
import com.tuya.lock.demo.zigbee.utils.Constant;
import com.tuya.lock.demo.zigbee.utils.DialogUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PasswordListAdapter extends RecyclerView.Adapter<PasswordListAdapter.ViewHolder> {

    public List<PasswordBean.DataBean> data = new ArrayList<>();
    private Callback callback;
    private String mDevId;
    private boolean isShowDelete = true;

    @NotNull
    public final List<PasswordBean.DataBean> getData() {
        return this.data;
    }

    public void setDevId(String devId) {
        mDevId = devId;
    }

    public final void setData(List<PasswordBean.DataBean> list) {
        this.data = list;
    }

    public void addCallback(Callback callback) {
        this.callback = callback;
    }

    public void hideDelete() {
        isShowDelete = false;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_zigbee_password_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PasswordBean.DataBean bean = data.get(position);

        Log.i(Constant.TAG, JSONObject.toJSONString(bean));

        String operate = "";
        int color = holder.itemView.getResources().getColor(R.color.black);
        if (bean.getPhase() == 2) {
            if (bean.getOperate() == 125) {
                if (TextUtils.equals(bean.getDeliveryStatus(), "1")) {
                    operate = holder.itemView.getResources().getString(R.string.zigbee_deleting);
                    color = holder.itemView.getResources().getColor(R.color.ty_theme_color_m1);
                }
            } else {
                if (TextUtils.equals(bean.getDeliveryStatus(), "1")) {
                    operate = holder.itemView.getResources().getString(R.string.zigbee_editing);
                    color = holder.itemView.getResources().getColor(R.color.ty_theme_color_m1);
                } else if (TextUtils.equals(bean.getDeliveryStatus(), "2") && bean.isIfEffective()) {
                    operate = holder.itemView.getResources().getString(R.string.zigbee_in_force);
                    color = holder.itemView.getResources().getColor(R.color.green);
                } else if (TextUtils.equals(bean.getDeliveryStatus(), "2") && !bean.isIfEffective()) {
                    operate = holder.itemView.getResources().getString(R.string.zigbee_not_active);
                    color = holder.itemView.getResources().getColor(R.color.gray);
                }
            }
        } else if (bean.getPhase() == 3) {
            if (TextUtils.equals(bean.getDeliveryStatus(), "1")) {
                operate = holder.itemView.getResources().getString(R.string.zigbee_editing);
                color = holder.itemView.getResources().getColor(R.color.ty_theme_color_m1);
            } else if (TextUtils.equals(bean.getDeliveryStatus(), "2")) {
                operate = holder.itemView.getResources().getString(R.string.zigbee_frozen);
                color = holder.itemView.getResources().getColor(R.color.red);
            }
        }
        String count = bean.getOneTime() == 1 ? "[" + holder.itemView.getResources().getString(R.string.zigbee_disposable) + "] " : "[" + holder.itemView.getResources().getString(R.string.zigbee_cycle) + "] ";
        String name = count + bean.getName();
        holder.tvDeviceName.setText(name);
        holder.tvDeviceStatus.setText(operate);
        holder.tvDeviceStatus.setTextColor(color);
        String passwordStr = "password:" + bean.getPassword();
        holder.password_view.setText(passwordStr);
        String timeStr = Utils.getDateOneDay(bean.getEffectiveTime()) + " ~ " + Utils.getDateOneDay(bean.getInvalidTime());

        StringBuilder stringBuilder = new StringBuilder();
        if (bean.getModifyData().getScheduleList().size() > 0) {
            ScheduleBean scheduleBean = bean.getModifyData().getScheduleList().get(0);
            stringBuilder.append(getDayName(scheduleBean.getWorkingDay()));
            stringBuilder.append(" [");
            String effectiveTimeStr = String.valueOf(scheduleBean.getEffectiveTime());
            if (effectiveTimeStr.length() == 3) {
                String effectiveTimeEnd = effectiveTimeStr.charAt(0) + ":" + effectiveTimeStr.substring(1, 3);
                stringBuilder.append(effectiveTimeEnd);
            } else if (String.valueOf(scheduleBean.getEffectiveTime()).length() == 4) {
                String effectiveTimeEnd = effectiveTimeStr.substring(0, 2) + ":" + effectiveTimeStr.substring(2, 4);
                stringBuilder.append(effectiveTimeEnd);
            } else {
                stringBuilder.append(scheduleBean.getEffectiveTime());
            }
            stringBuilder.append("-");

            String invalidTimeStr = String.valueOf(scheduleBean.getInvalidTime());
            if (invalidTimeStr.length() == 3) {
                String invalidTimeEnd = invalidTimeStr.charAt(0) + ":" + invalidTimeStr.substring(1, 3);
                stringBuilder.append(invalidTimeEnd);
            } else if (invalidTimeStr.length() == 4) {
                String invalidTimeEnd = invalidTimeStr.substring(0, 2) + ":" + invalidTimeStr.substring(2, 4);
                stringBuilder.append(invalidTimeEnd);
            } else {
                stringBuilder.append(scheduleBean.getInvalidTime());
            }
            stringBuilder.append("]");

            if (scheduleBean.isAllDay() || scheduleBean.getInvalidTime() == 0 && scheduleBean.getEffectiveTime() == 0) {
                holder.schedule_view.setVisibility(View.GONE);
            } else {
                holder.schedule_view.setText(stringBuilder.toString());
                holder.schedule_view.setVisibility(View.VISIBLE);
            }
        } else {
            holder.schedule_view.setVisibility(View.GONE);
        }

        holder.time_view.setText(timeStr);

        holder.itemView.setOnClickListener(v -> {
            if (isShowDelete) {
                showDialog(v.getContext(), bean, position);
            } else {
                ShowCodeActivity.startActivity(v.getContext(), JSONObject.toJSONString(bean));
            }
        });

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private void showDialog(Context context, PasswordBean.DataBean bean, int position) {
        DialogUtils.showPassword(context, bean, new DialogUtils.Callback() {
            @Override
            public void edit(PasswordBean.DataBean bean) {
                callback.edit(bean, position);
            }

            @Override
            public void delete(PasswordBean.DataBean bean) {
                callback.remove(bean, position);
            }

            @Override
            public void rename(PasswordBean.DataBean bean) {
                PasswordUpdateActivity.startActivity(context, bean, mDevId);
            }

            @Override
            public void freeze(PasswordBean.DataBean bean, boolean isFreeze) {
                callback.freeze(bean, position, isFreeze);
            }

            @Override
            public void showCode(PasswordBean.DataBean bean) {
                ShowCodeActivity.startActivity(context, JSONObject.toJSONString(bean));
            }
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDeviceName;
        private final TextView tvDeviceStatus;
        private final TextView password_view;
        private final TextView time_view;
        private final TextView schedule_view;

        public ViewHolder(@NotNull View itemView) {
            super(itemView);
            tvDeviceName = itemView.findViewById(R.id.tvDeviceName);
            tvDeviceStatus = itemView.findViewById(R.id.tvDeviceStatus);
            password_view = itemView.findViewById(R.id.password_view);
            time_view = itemView.findViewById(R.id.time_view);
            schedule_view = itemView.findViewById(R.id.schedule_view);
        }
    }

    public interface Callback {
        void remove(PasswordBean.DataBean bean, int position);

        void freeze(PasswordBean.DataBean bean, int position, boolean isFreeze);

        void edit(PasswordBean.DataBean bean, int position);
    }

    public void remove(int position) {
        data.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, data.size() - position);
    }

    private String getDayName(int workingDay) {
        String workDay = String.format(Locale.CHINA, "%07d", Integer.parseInt(Integer.toBinaryString(workingDay)));
        L.i(Constant.TAG, "workDay:" + workDay);
        List<String> dayName = new ArrayList<>();
        if (workDay.charAt(0) == '1') {
            dayName.add("7");
        }
        if (workDay.charAt(1) == '1') {
            dayName.add("1");
        }
        if (workDay.charAt(2) == '1') {
            dayName.add("2");
        }
        if (workDay.charAt(3) == '1') {
            dayName.add("3");
        }
        if (workDay.charAt(4) == '1') {
            dayName.add("4");
        }
        if (workDay.charAt(5) == '1') {
            dayName.add("5");
        }
        if (workDay.charAt(6) == '1') {
            dayName.add("6");
        }
        return String.join("、", dayName);
    }
}