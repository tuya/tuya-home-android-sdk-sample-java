package com.tuya.lock.demo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.tuya.lock.demo.R;
import com.tuya.lock.demo.activity.code.ShowCodeActivity;
import com.tuya.lock.demo.utils.Utils;
import com.thingclips.smart.home.sdk.ThingHomeSdk;
import com.thingclips.smart.optimus.lock.api.bean.ProRecord;
import com.thingclips.smart.sdk.bean.DeviceBean;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class RecordProListAdapter extends RecyclerView.Adapter<RecordProListAdapter.ViewHolder> {

    public List<ProRecord.DataBean> data = new ArrayList<>();
    private DeviceBean deviceBean;

    public void setDevice(String devId) {
        deviceBean = ThingHomeSdk.getDataInstance().getDeviceBean(devId);
    }

    public final void setData(List<ProRecord.DataBean> list) {
        this.data = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.lock_records_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProRecord.DataBean itemData = data.get(position);
        if (null != itemData) {
            holder.userNameView.setText(itemData.userName);
            holder.unlockTimeView.setText(Utils.getDateDay(itemData.time));
            holder.unlockTypeView.setText(itemData.data);
            holder.unlockTagsView.setText(itemData.logType);
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