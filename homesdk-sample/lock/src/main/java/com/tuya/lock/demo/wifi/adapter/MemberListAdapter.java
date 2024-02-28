package com.tuya.lock.demo.wifi.adapter;

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
import com.thingclips.smart.optimus.lock.api.bean.WifiLockUser;
import com.tuya.lock.demo.R;
import com.tuya.lock.demo.ble.activity.code.ShowCodeActivity;
import com.tuya.lock.demo.ble.utils.Utils;
import com.tuya.lock.demo.wifi.activity.MemberDetailActivity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MemberListAdapter extends RecyclerView.Adapter<MemberListAdapter.ViewHolder> {

    public ArrayList<WifiLockUser> data = new ArrayList<>();
    private Callback callback;
    private String mDevId;

    @NotNull
    public final ArrayList<WifiLockUser> getData() {
        return this.data;
    }

    public void setDevId(String devId) {
        mDevId = devId;
    }

    public final void setData(List<WifiLockUser> list) {
        this.data.clear();
        for (WifiLockUser item : list) {
            if (null != item.userId && item.userId.length() > 2) {
                this.data.add(item);
            }
        }
    }

    public void deleteUser(Callback callback) {
        this.callback = callback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_zigbee_member_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WifiLockUser bean = data.get(position);
        String userName = bean.userName;
        if (bean.userType == 1) {
            userName = "[Family]" + bean.userName;
        }
        holder.tvDeviceName.setText(userName);
        holder.tvStatus.setText(bean.contact);

        holder.itemView.setOnClickListener(v -> {
            ShowCodeActivity.startActivity(v.getContext(), JSONObject.toJSONString(bean));
        });

        holder.user_update.setOnClickListener(v -> {
            MemberDetailActivity.startActivity(v.getContext(), bean, mDevId, 1);
        });

        holder.user_unlock.setVisibility(View.GONE);

        holder.itemView.setOnLongClickListener(v -> {
            if (null != callback) {
                callback.remove(bean, position);
            }
            return false;
        });

        if (!TextUtils.isEmpty(bean.avatarUrl)) {
            Utils.showImageUrl(bean.avatarUrl, holder.user_face);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDeviceName;
        private final TextView tvStatus;
        private final ImageView user_face;
        private final Button user_update;
        private final Button user_unlock;

        public ViewHolder(@NotNull View itemView) {
            super(itemView);
            user_face = itemView.findViewById(R.id.user_face);
            tvDeviceName = itemView.findViewById(R.id.tvDeviceName);
            tvStatus = itemView.findViewById(R.id.tvDeviceStatus);
            user_update = itemView.findViewById(R.id.user_update);
            user_unlock = itemView.findViewById(R.id.user_unlock);
        }
    }

    public interface Callback {
        void remove(WifiLockUser infoBean, int position);
    }

    public void remove(int position) {
        data.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, data.size() - position);
    }
}