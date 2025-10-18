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
import com.thingclips.sdk.os.ThingOSDevice;
import com.thingclips.smart.android.device.bean.SchemaBean;
import com.thingclips.smart.optimus.lock.api.zigbee.response.MemberInfoBean;
import com.thingclips.smart.sdk.bean.DeviceBean;
import com.thingclips.smart.sdk.optimus.lock.bean.ble.UnlockDetail;
import com.tuya.lock.demo.R;
import com.tuya.lock.demo.ble.activity.code.ShowCodeActivity;
import com.tuya.lock.demo.ble.utils.Utils;
import com.tuya.lock.demo.zigbee.activity.MemberDetailActivity;
import com.tuya.lock.demo.zigbee.activity.OpModeListActivity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;

public class MemberListAdapter extends RecyclerView.Adapter<MemberListAdapter.ViewHolder> {

    public ArrayList<MemberInfoBean> data = new ArrayList<>();
    private Callback callback;
    private String mDevId;

    @NotNull
    public final ArrayList<MemberInfoBean> getData() {
        return this.data;
    }

    public void setDevId(String devId) {
        mDevId = devId;
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
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_zigbee_member_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MemberInfoBean bean = data.get(position);
        DeviceBean deviceBean = ThingOSDevice.getDeviceBean(mDevId);
        String isAdminString = "";
        if (bean.getUserType() == 10) {
            isAdminString = "[Admin] ";
        } else if (bean.getUserType() == 50) {
            isAdminString = "[Owner] ";
        }
        String name = isAdminString + bean.getNickName();
        holder.tvDeviceName.setText(name);

        StringBuilder statusList = new StringBuilder();
        for (UnlockDetail item : bean.getUnlockDetail()) {
            String opModeName = "unknown";
            for (Map.Entry<String, SchemaBean> schemaBean : deviceBean.getSchemaMap().entrySet()) {
                if (TextUtils.equals(schemaBean.getKey(), String.valueOf(item.getDpId()))) {
                    SchemaBean schemaItem = schemaBean.getValue();
                    opModeName = schemaItem.name;
                    break;
                }
            }
            statusList.append(opModeName);
            statusList.append("(");
            statusList.append(item.getCount());
            statusList.append(") ");
        }
        holder.tvStatus.setText(statusList.toString());

        holder.itemView.setOnClickListener(v -> {
            ShowCodeActivity.startActivity(v.getContext(), JSONObject.toJSONString(bean));
        });

        holder.user_update.setOnClickListener(v -> {
            MemberDetailActivity.startActivity(v.getContext(), bean, mDevId, 1);
        });

        holder.user_unlock.setOnClickListener(v -> {
            OpModeListActivity.startActivity(v.getContext(), mDevId, bean);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (null != callback) {
                callback.remove(bean, position);
            }
            return false;
        });

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
        void remove(MemberInfoBean infoBean, int position);
    }

    public void remove(int position) {
        data.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, data.size() - position);
    }
}