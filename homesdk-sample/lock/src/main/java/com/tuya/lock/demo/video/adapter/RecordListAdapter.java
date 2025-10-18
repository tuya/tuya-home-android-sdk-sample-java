package com.tuya.lock.demo.video.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.thingclips.smart.android.common.utils.L;
import com.thingclips.smart.home.sdk.ThingHomeSdk;
import com.thingclips.smart.sdk.bean.DeviceBean;
import com.thingclips.thinglock.videolock.bean.LogsListBean;
import com.tuya.lock.demo.R;
import com.tuya.lock.demo.ble.activity.code.ShowCodeActivity;
import com.tuya.lock.demo.ble.utils.Utils;
import com.tuya.lock.demo.video.activity.LogRecordDetailActivity;
import com.tuya.lock.demo.zigbee.utils.Constant;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 告警记录adapter
 */
public class RecordListAdapter extends RecyclerView.Adapter<RecordListAdapter.ViewHolder> {

    public List<LogsListBean.LogsInfoBean> data = new ArrayList<>();
    private DeviceBean deviceBean;

    public void setDevice(String devId) {
        deviceBean = ThingHomeSdk.getDataInstance().getDeviceBean(devId);
    }

    public final void setData(List<LogsListBean.LogsInfoBean> list) {
        this.data = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_lock_records, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LogsListBean.LogsInfoBean itemData = data.get(position);
        if (null != itemData) {
            if (null == deviceBean || null == deviceBean.getSchemaMap()) {
                L.e(Constant.TAG, "deviceBean OR getSchemaMap is null");
                return;
            }
            holder.userNameView.setText(itemData.logCategory);
            holder.unlockTimeView.setText(Utils.getDateDay(itemData.time));

            holder.itemView.setOnClickListener(v -> {
                ShowCodeActivity.startActivity(v.getContext(), JSONObject.toJSONString(itemData));
            });

            if (null != itemData.mediaInfoList && itemData.mediaInfoList.size() > 0) {
                LogsListBean.MediaInfo mediaInfo = itemData.mediaInfoList.get(0);
                holder.bindView.setVisibility(View.VISIBLE);
                holder.bindView.setText("点击查看");
                holder.bindView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LogRecordDetailActivity.startActivity(v.getContext(), mediaInfo);
                    }
                });

            } else {
                holder.bindView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView userNameView;
        private final TextView unlockTimeView;
        private final Button bindView;

        public ViewHolder(@NotNull View itemView) {
            super(itemView);
            userNameView = itemView.findViewById(R.id.userName);
            unlockTimeView = itemView.findViewById(R.id.unlockTime);
            bindView = itemView.findViewById(R.id.bindView);
        }
    }
}