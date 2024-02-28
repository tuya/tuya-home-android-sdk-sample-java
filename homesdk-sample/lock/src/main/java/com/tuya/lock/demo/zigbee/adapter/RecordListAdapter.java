package com.tuya.lock.demo.zigbee.adapter;

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
import com.thingclips.smart.android.common.utils.L;
import com.thingclips.smart.android.device.bean.SchemaBean;
import com.thingclips.smart.home.sdk.ThingHomeSdk;
import com.thingclips.smart.optimus.lock.api.zigbee.response.RecordBean;
import com.thingclips.smart.sdk.bean.DeviceBean;
import com.thingclips.smart.sdk.optimus.lock.bean.ZigBeeDatePoint;
import com.thingclips.smart.sdk.optimus.lock.utils.LockUtil;
import com.tuya.lock.demo.R;
import com.tuya.lock.demo.ble.activity.code.ShowCodeActivity;
import com.tuya.lock.demo.ble.utils.Utils;
import com.tuya.lock.demo.zigbee.activity.MemberSelectListActivity;
import com.tuya.lock.demo.zigbee.utils.Constant;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 告警记录adapter
 */
public class RecordListAdapter extends RecyclerView.Adapter<RecordListAdapter.ViewHolder> {

    public List<RecordBean.DataBean> data = new ArrayList<>();
    private DeviceBean deviceBean;

    public void setDevice(String devId) {
        deviceBean = ThingHomeSdk.getDataInstance().getDeviceBean(devId);
    }

    public final void setData(List<RecordBean.DataBean> list) {
        this.data = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_zigbee_lock_records, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecordBean.DataBean itemData = data.get(position);
        if (null != itemData) {
            if (null == deviceBean || null == deviceBean.getSchemaMap()) {
                L.e(Constant.TAG, "deviceBean OR getSchemaMap is null");
                return;
            }
            for (Map.Entry<String, SchemaBean> schemaBean : deviceBean.getSchemaMap().entrySet()) {
                if (TextUtils.equals(schemaBean.getKey(), String.valueOf(itemData.getDpId()))) {
                    SchemaBean schemaItem = schemaBean.getValue();
                    String recordTitle = itemData.getUserName() + schemaItem.name + "(" + itemData.getDpValue() + ")";
                    if (itemData.getTags() == 1) {
                        recordTitle = "[hiJack]" + recordTitle;
                    }
                    holder.userNameView.setText(recordTitle);
                    break;
                }
            }
            holder.unlockTimeView.setText(Utils.getDateDay(itemData.getGmtCreate()));
            if (TextUtils.equals(LockUtil.convertCode2Id(deviceBean.devId, ZigBeeDatePoint.UNLOCK_FINGERPRINT), String.valueOf(itemData.getDpId())) ||
                    TextUtils.equals(LockUtil.convertCode2Id(deviceBean.devId, ZigBeeDatePoint.UNLOCK_PASSWORD), String.valueOf(itemData.getDpId())) ||
                    TextUtils.equals(LockUtil.convertCode2Id(deviceBean.devId, ZigBeeDatePoint.UNLOCK_CARD), String.valueOf(itemData.getDpId()))) {
                if (TextUtils.isEmpty(itemData.getUnlockName())) {
                    holder.bindView.setVisibility(View.VISIBLE);
                    holder.bindView.setOnClickListener(v -> {
                        List<String> list = new ArrayList<>();
                        String unlockId = itemData.getDpId() + "-" + itemData.getDpValue();
                        list.add(unlockId);
                        MemberSelectListActivity.startActivity(holder.bindView.getContext(), deviceBean.devId, list, 1);
                    });
                } else {
                    holder.bindView.setVisibility(View.GONE);
                }
            } else {
                holder.bindView.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(v -> {
                ShowCodeActivity.startActivity(v.getContext(), JSONObject.toJSONString(itemData));
            });

            if (!TextUtils.isEmpty(itemData.getAvatar())) {
                Utils.showImageUrl(itemData.getAvatar(), holder.user_face);
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
        private final ImageView user_face;

        public ViewHolder(@NotNull View itemView) {
            super(itemView);
            userNameView = itemView.findViewById(R.id.userName);
            unlockTimeView = itemView.findViewById(R.id.unlockTime);
            user_face = itemView.findViewById(R.id.user_face);
            bindView = itemView.findViewById(R.id.bindView);
        }
    }
}