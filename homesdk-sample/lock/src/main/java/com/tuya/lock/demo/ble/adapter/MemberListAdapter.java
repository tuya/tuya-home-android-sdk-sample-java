package com.tuya.lock.demo.ble.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.tuya.lock.demo.R;
import com.tuya.lock.demo.ble.activity.opMode.OpModeListActivity;
import com.tuya.lock.demo.ble.activity.member.MemberDetailActivity;
import com.tuya.lock.demo.ble.activity.code.ShowCodeActivity;
import com.tuya.lock.demo.ble.activity.member.MemberTimeActivity;
import com.tuya.lock.demo.ble.utils.Utils;
import com.thingclips.smart.sdk.optimus.lock.bean.ble.MemberInfoBean;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class MemberListAdapter extends RecyclerView.Adapter<MemberListAdapter.ViewHolder> {

    public ArrayList<MemberInfoBean> data = new ArrayList<>();
    private Callback callback;
    private String mDevId;
    private boolean isProDevice = false;

    @NotNull
    public final ArrayList<MemberInfoBean> getData() {
        return this.data;
    }

    public void setDevId(String devId) {
        mDevId = devId;
    }

    public void setProDevice(boolean proDevice) {
        isProDevice = proDevice;
    }

    public final void setData(ArrayList<MemberInfoBean> list) {
        this.data = list;
    }

    public void deleteUser(Callback callback) {
        this.callback = callback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.member_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MemberInfoBean bean = data.get(position);
        holder.tvDeviceName.setText(bean.getNickName());

        long expiredTime = bean.getTimeScheduleInfo().getExpiredTime();
        if (String.valueOf(expiredTime).length() == 10) {
            expiredTime = expiredTime * 1000;
        }
        String endTime = "过期时间：" + Utils.getDateDay(expiredTime, "yyyy-MM-dd");

        String statusStr = bean.getTimeScheduleInfo().isPermanent() ? holder.itemView.getContext().getString(R.string.user_in_permanent) : endTime;

        holder.tvStatus.setText(statusStr);

        holder.user_delete.setOnClickListener(v -> {
            if (null != callback) {
                callback.remove(bean, position);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            ShowCodeActivity.startActivity(v.getContext(), JSONObject.toJSONString(bean));
        });

        holder.user_update.setOnClickListener(v -> {
            MemberDetailActivity.startActivity(v.getContext(), bean, mDevId, 1);
        });

        holder.user_time.setOnClickListener(v -> {
            MemberTimeActivity.startActivity(v.getContext(), bean, mDevId);
        });

        holder.user_unlock.setOnClickListener(v -> {
            OpModeListActivity.startActivity(v.getContext(), mDevId, bean.getUserId(), bean.getLockUserId());
        });

        if (bean.getUserType() == 50) {
            holder.user_delete.setVisibility(View.GONE);
        } else {
            holder.user_delete.setVisibility(View.VISIBLE);
        }

        if (isProDevice) {
            holder.user_time.setVisibility(View.VISIBLE);
        } else {
            holder.user_time.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(bean.getAvatarUrl())) {
            Utils.showImageUrl(bean.getAvatarUrl(), holder.user_face);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDeviceName;
        private final TextView tvStatus;
        private final Button user_delete;
        private final ImageView user_face;
        private final Button user_time;
        private final Button user_update;
        private final Button user_unlock;

        public ViewHolder(@NotNull View itemView) {
            super(itemView);
            user_face = itemView.findViewById(R.id.user_face);
            tvDeviceName = itemView.findViewById(R.id.tvDeviceName);
            tvStatus = itemView.findViewById(R.id.tvDeviceStatus);
            user_delete = itemView.findViewById(R.id.user_delete);
            user_time = itemView.findViewById(R.id.user_time);
            user_update = itemView.findViewById(R.id.user_update);
            user_unlock = itemView.findViewById(R.id.user_unlock);
        }
    }

    public interface Callback {
        void remove(MemberInfoBean infoBean, int position);
    }

    public void remove(int position) {
        data.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, data.size() - position);
    }
}