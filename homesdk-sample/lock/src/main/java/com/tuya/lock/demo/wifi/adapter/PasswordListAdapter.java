package com.tuya.lock.demo.wifi.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.thingclips.smart.optimus.lock.api.bean.TempPassword;
import com.tuya.lock.demo.R;
import com.tuya.lock.demo.ble.activity.code.ShowCodeActivity;
import com.tuya.lock.demo.ble.utils.Utils;
import com.tuya.lock.demo.zigbee.utils.Constant;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PasswordListAdapter extends RecyclerView.Adapter<PasswordListAdapter.ViewHolder> {

    public List<TempPassword> data = new ArrayList<>();
    private Callback callback;

    @NotNull
    public final List<TempPassword> getData() {
        return this.data;
    }

    public final void setData(List<TempPassword> list) {
        this.data = list;
    }

    public void addCallback(Callback callback) {
        this.callback = callback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_zigbee_password_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TempPassword bean = data.get(position);

        Log.i(Constant.TAG, JSONObject.toJSONString(bean));

        holder.tvDeviceName.setText(bean.name);
        String timeStr = Utils.getDateOneDay(bean.effectiveTime) + " ~ " + Utils.getDateOneDay(bean.invalidTime);

        holder.time_view.setText(timeStr);

        holder.itemView.setOnClickListener(v -> {
            ShowCodeActivity.startActivity(v.getContext(), JSONObject.toJSONString(bean));
        });

        holder.itemView.setOnLongClickListener(v -> {
            callback.remove(bean, position);
            return false;
        });

        holder.password_view.setVisibility(View.GONE);
        holder.schedule_view.setVisibility(View.GONE);

        holder.tvDeviceStatus.setText(getStatus(bean.status));
    }

    private String getStatus(int status) {
        String statusStr = "";
        switch (status) {
            case TempPassword.Status.REMOVED:
                statusStr = "Removed";
                break;
            case TempPassword.Status.INVALID:
                statusStr = "Invalid";
                break;
            case TempPassword.Status.TO_BE_PUBILSH:
                statusStr = "ToBePublish";
                break;
            case TempPassword.Status.WORKING:
                statusStr = "Working";
                break;
            case TempPassword.Status.TO_BE_DELETED:
                statusStr = "ToBeDeleted";
                break;
            case TempPassword.Status.EXPIRED:
                statusStr = "Expired";
                break;
        }
        return statusStr;
    }

    @Override
    public int getItemCount() {
        return data.size();
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
        void remove(TempPassword bean, int position);
    }

    public void remove(int position) {
        data.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, data.size() - position);
    }

}