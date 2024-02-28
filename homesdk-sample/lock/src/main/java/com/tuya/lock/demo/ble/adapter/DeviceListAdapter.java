package com.tuya.lock.demo.ble.adapter;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tuya.lock.demo.ble.activity.detail.BleLockDetailActivity;
import com.tuya.lock.demo.R;
import com.tuya.lock.demo.ble.constant.Constant;
import com.thingclips.smart.sdk.bean.DeviceBean;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;


public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.ViewHolder> {

    public ArrayList<DeviceBean> data = new ArrayList<>();

    @NotNull
    public final ArrayList<DeviceBean> getData() {
        return this.data;
    }

    public final void setData(ArrayList<DeviceBean> list) {
        this.data = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.lock_device_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DeviceBean bean = data.get(position);
        String title = bean.name;
        if (null != bean.getProductBean()) {
            title = bean.name + " (" + bean.getProductBean().getCategory() + ")";
        }
        holder.tvDeviceName.setText(title);
        boolean isOnline = bean.getIsOnline();
        String onlineStr = holder.itemView.getContext().getString(R.string.device_offline);
        if (isOnline) {
            onlineStr = holder.itemView.getContext().getString(R.string.device_online);
        }
        Log.i(Constant.TAG, "list adapter position:" + position + ", isOnline:" + isOnline);
        holder.tvStatus.setText(onlineStr);

        holder.itemView.setOnClickListener(v -> {
            String devId = bean.getDevId();
            Intent intent = new Intent(v.getContext(), BleLockDetailActivity.class);
            intent.putExtra(Constant.DEVICE_ID, devId);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDeviceName;
        private final TextView tvStatus;

        public ViewHolder(@NotNull View itemView) {
            super(itemView);
            tvDeviceName = itemView.findViewById(R.id.tvDeviceName);
            tvStatus = itemView.findViewById(R.id.tvDeviceStatus);
        }
    }
}