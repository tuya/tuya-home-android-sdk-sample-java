package com.tuya.lock.demo.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.alibaba.fastjson.JSONObject;
import com.tuya.lock.demo.R;
import com.tuya.lock.demo.activity.code.ShowCodeActivity;
import com.tuya.lock.demo.constant.Constant;
import com.tuya.lock.demo.utils.Utils;
import com.thingclips.smart.android.device.bean.SchemaBean;
import com.thingclips.smart.home.sdk.ThingHomeSdk;
import com.thingclips.smart.optimus.lock.api.bean.Record;
import com.thingclips.smart.sdk.bean.DeviceBean;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 告警记录adapter
 */
public class RecordListAdapter extends RecyclerView.Adapter<RecordListAdapter.ViewHolder> {

    public List<Record.DataBean> data = new ArrayList<>();
    private DeviceBean deviceBean;

    public void setDevice(String devId) {
        deviceBean = ThingHomeSdk.getDataInstance().getDeviceBean(devId);
    }

    public final void setData(List<Record.DataBean> list) {
        this.data = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.lock_records_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Record.DataBean itemData = data.get(position);
        if (null != itemData) {
            if (null == deviceBean || null == deviceBean.getSchemaMap()) {
                Log.e(Constant.TAG, "deviceBean OR getSchemaMap is null");
                return;
            }
            for (Map.Entry<String, SchemaBean> schemaBean : deviceBean.getSchemaMap().entrySet()) {
                if (schemaBean.getKey().equals(itemData.dpId)) {
                    SchemaBean schemaItem = schemaBean.getValue();
                    holder.userNameView.setText(schemaItem.name);
                    break;
                }
            }
            holder.unlockTimeView.setText(Utils.getDateDay(itemData.createTime));
            holder.unlockTypeView.setText(itemData.userName);
            if (itemData.tags == 1) {
                holder.unlockTagsView.setText("劫持");
            } else {
                holder.unlockTagsView.setText("");
            }
            holder.itemView.setOnClickListener(v -> {
                ShowCodeActivity.startActivity(v.getContext(), JSONObject.toJSONString(itemData));
            });
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView userNameView;
        private final TextView unlockTimeView;
        private final TextView unlockTypeView;
        private final TextView unlockTagsView;

        public ViewHolder(@NotNull View itemView) {
            super(itemView);
            userNameView = itemView.findViewById(R.id.userName);
            unlockTimeView = itemView.findViewById(R.id.unlockTime);
            unlockTypeView = itemView.findViewById(R.id.unlockType);
            unlockTagsView = itemView.findViewById(R.id.unlockTags);
        }
    }
}