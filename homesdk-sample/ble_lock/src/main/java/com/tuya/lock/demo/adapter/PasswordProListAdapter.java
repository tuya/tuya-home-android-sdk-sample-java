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
import com.tuya.lock.demo.activity.password.PasswordProOfflineRevokeActivity;
import com.tuya.lock.demo.activity.password.PasswordProOnlineDetailActivity;
import com.thingclips.smart.optimus.lock.api.bean.ProTempPasswordItem;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class PasswordProListAdapter extends RecyclerView.Adapter<PasswordProListAdapter.ViewHolder> {

    public ArrayList<ProTempPasswordItem> data = new ArrayList<>();
    private Callback callback;
    private String mDevId;

    @NotNull
    public final ArrayList<ProTempPasswordItem> getData() {
        return this.data;
    }

    public void setDevId(String devId) {
        mDevId = devId;
    }

    public final void setData(ArrayList<ProTempPasswordItem> list) {
        this.data = list;
    }

    public void delete(Callback callback) {
        this.callback = callback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.password_pro_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProTempPasswordItem bean = data.get(position);

        Log.i(Constant.TAG, JSONObject.toJSONString(bean));

        holder.tvDeviceName.setText(bean.getName());

        holder.itemView.setOnClickListener(v -> {
            ShowCodeActivity.startActivity(v.getContext(), JSONObject.toJSONString(bean));
        });

        holder.button_clear.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), PasswordProOfflineRevokeActivity.class);
            intent.putExtra(Constant.DEVICE_ID, mDevId);
            intent.putExtra("unlockBindingId", bean.getUnlockBindingId());
            intent.putExtra("name", bean.getName());
            v.getContext().startActivity(intent);
        });

        if (bean.getOpModeSubType() == 0 && bean.getOpModeType() == 3) {
            holder.button_clear.setVisibility(View.VISIBLE);
        } else {
            holder.button_clear.setVisibility(View.GONE);
        }

        if (bean.getOpModeType() < 3) {
            holder.button_delete.setVisibility(View.VISIBLE);
            holder.button_edit.setVisibility(View.VISIBLE);
            holder.button_delete.setOnClickListener(v -> callback.remove(bean, position));
            holder.button_edit.setOnClickListener(v -> {
                PasswordProOnlineDetailActivity.startActivity(v.getContext(), bean, mDevId, 1);
            });
        } else {
            holder.button_delete.setVisibility(View.GONE);
            holder.button_edit.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDeviceName;
        private final Button button_clear;
        private final Button button_delete;
        private final Button button_edit;

        public ViewHolder(@NotNull View itemView) {
            super(itemView);
            tvDeviceName = itemView.findViewById(R.id.tvDeviceName);
            button_clear = itemView.findViewById(R.id.button_clear);
            button_delete = itemView.findViewById(R.id.button_delete);
            button_edit = itemView.findViewById(R.id.button_edit);
        }
    }

    public interface Callback {
        void remove(ProTempPasswordItem passwordItem, int position);
    }

    public void remove(int position) {
        data.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, data.size() - position);
    }
}