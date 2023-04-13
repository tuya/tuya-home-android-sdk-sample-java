package com.tuya.lock.demo.adapter;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.tuya.lock.demo.R;
import com.tuya.lock.demo.activity.code.ShowCodeActivity;
import com.tuya.lock.demo.constant.Constant;
import com.tuya.lock.demo.activity.password.PasswordOldOfflineAddRevokeActivity;
import com.thingclips.smart.optimus.lock.api.bean.OfflineTempPasswordItem;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class PasswordOldOfflineListAdapter extends RecyclerView.Adapter<PasswordOldOfflineListAdapter.ViewHolder> {

    public ArrayList<OfflineTempPasswordItem> data = new ArrayList<>();
    private String mDevId;

    @NotNull
    public final ArrayList<OfflineTempPasswordItem> getData() {
        return this.data;
    }

    public void setDevId(String devId) {
        mDevId = devId;
    }

    public final void setData(ArrayList<OfflineTempPasswordItem> list) {
        this.data = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.password_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OfflineTempPasswordItem bean = data.get(position);

        Log.i(Constant.TAG, JSONObject.toJSONString(bean));

        holder.tvDeviceName.setText(bean.getPwdName());
        holder.tvDeviceStatus.setText(String.valueOf(bean.getPwd()));


        holder.button_detail.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), PasswordOldOfflineAddRevokeActivity.class);
            intent.putExtra(Constant.DEVICE_ID, mDevId);
            intent.putExtra("pwdId", bean.getPwdId());
            v.getContext().startActivity(intent);
        });

        if (bean.getOpModeSubType() == 0 && bean.getOpModeType() == 3) {
            holder.button_detail.setVisibility(View.VISIBLE);
        } else {
            holder.button_detail.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            ShowCodeActivity.startActivity(v.getContext(), JSONObject.toJSONString(bean));
        });

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDeviceName;
        private final TextView tvDeviceStatus;
        private final Button button_detail;

        public ViewHolder(@NotNull View itemView) {
            super(itemView);
            tvDeviceName = itemView.findViewById(R.id.tvDeviceName);
            tvDeviceStatus = itemView.findViewById(R.id.tvDeviceStatus);
            button_detail = itemView.findViewById(R.id.button_detail);
        }
    }
}