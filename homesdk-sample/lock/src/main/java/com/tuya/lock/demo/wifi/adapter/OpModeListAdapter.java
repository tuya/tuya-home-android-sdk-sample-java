package com.tuya.lock.demo.wifi.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.thingclips.smart.android.device.bean.SchemaBean;
import com.thingclips.smart.camera.utils.chaos.L;
import com.thingclips.smart.optimus.lock.api.ThingUnlockType;
import com.thingclips.smart.optimus.lock.api.bean.UnlockRelation;
import com.thingclips.smart.sdk.optimus.lock.utils.StandardDpConverter;
import com.tuya.lock.demo.R;
import com.tuya.lock.demo.ble.activity.code.ShowCodeActivity;
import com.tuya.lock.demo.ble.constant.Constant;
import com.tuya.lock.demo.zigbee.bean.UnlockInfo;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class OpModeListAdapter extends RecyclerView.Adapter<OpModeListAdapter.ViewHolder> {

    public ArrayList<UnlockInfo> data = new ArrayList<>();
    private Callback mCallback;
    private String mDevId;

    public void setDeviceId(String deviceId) {
        mDevId = deviceId;
    }

    @NotNull
    public final List<UnlockRelation> getData() {
        List<UnlockRelation> itemList = new ArrayList<>();
        for (UnlockInfo item: data) {
            if (item.type == 1) {
                UnlockRelation unlockRelation = new UnlockRelation();
                unlockRelation.unlockType = item.dpCode;
                unlockRelation.passwordNumber = Integer.parseInt(item.name);
                itemList.add(unlockRelation);
            }
        }
        return itemList;
    }

    public final void setData(List<UnlockRelation> list) {
        data.clear();
        List<UnlockInfo> unlockFingerList = new ArrayList<>();
        List<UnlockInfo> unlockPasswordList = new ArrayList<>();

        String fingerName = "";
        String passwordName = "";
        for (Map.Entry<String, SchemaBean> schemaBean : StandardDpConverter.getSchemaMap(mDevId).entrySet()) {
            if (!TextUtils.isEmpty(fingerName) && !TextUtils.isEmpty(passwordName)) {
                break;
            }
            if (TextUtils.equals(schemaBean.getValue().code, ThingUnlockType.FINGERPRINT)) {
                SchemaBean schemaItem = schemaBean.getValue();
                fingerName = schemaItem.name;
            } else if (TextUtils.equals(schemaBean.getValue().code, ThingUnlockType.PASSWORD)) {
                SchemaBean schemaItem = schemaBean.getValue();
                passwordName = schemaItem.name;
            }
        }

        UnlockInfo unlockFinger = new UnlockInfo();
        unlockFinger.type = 0;
        unlockFinger.dpCode = ThingUnlockType.FINGERPRINT;
        unlockFinger.name = fingerName;
        unlockFingerList.add(unlockFinger);

        UnlockInfo unlockPassword = new UnlockInfo();
        unlockPassword.type = 0;
        unlockPassword.dpCode = ThingUnlockType.PASSWORD;
        unlockPassword.name = passwordName;
        unlockPasswordList.add(unlockPassword);


        for (UnlockRelation itemDetail: list) {
            if (TextUtils.equals(itemDetail.unlockType, ThingUnlockType.FINGERPRINT)) {
                UnlockInfo unlockInfo = new UnlockInfo();
                unlockInfo.type = 1;
                unlockInfo.dpCode = ThingUnlockType.FINGERPRINT;
                unlockInfo.name = String.valueOf(itemDetail.passwordNumber);
                unlockFingerList.add(unlockInfo);
            }
            if (TextUtils.equals(itemDetail.unlockType, ThingUnlockType.PASSWORD)) {
                UnlockInfo unlockInfo = new UnlockInfo();
                unlockInfo.type = 1;
                unlockInfo.dpCode = ThingUnlockType.PASSWORD;
                unlockInfo.name = String.valueOf(itemDetail.passwordNumber);
                unlockPasswordList.add(unlockInfo);
            }
        }

        data.addAll(unlockFingerList);
        data.addAll(unlockPasswordList);

        L.i(Constant.TAG,"setData======>" +  JSONObject.toJSONString(data));
    }

    public void addCallback(Callback callback) {
        this.mCallback = callback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.unlock_mode_list_head, parent, false);
            return new HeadHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.unlock_mode_list_item, parent, false);
            return new ItemHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UnlockInfo bean = data.get(position);
        if (holder instanceof HeadHolder) {
            HeadHolder headHolder = (HeadHolder) holder;
            String title = bean.name;
            headHolder.name_view.setText(title);
            headHolder.add_view.setOnClickListener(v -> {
                if (null != mCallback) {
                    mCallback.add(bean, position);
                }
            });
        } else {
            ItemHolder itemHolder = (ItemHolder) holder;
            itemHolder.name_view.setText(bean.name);

            itemHolder.delete_view.setOnClickListener(v -> {
                if (null != mCallback) {
                    mCallback.delete(bean, position);
                }
            });

            itemHolder.edit_view.setOnClickListener(v -> {
                if (null != mCallback) {
                    mCallback.edit(bean, position);
                }
            });

            itemHolder.itemView.setOnClickListener(v -> {
                ShowCodeActivity.startActivity(v.getContext(), JSONObject.toJSONString(bean));
            });
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public int getItemViewType(int position) {
        UnlockInfo bean = data.get(position);
        return bean.type;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NotNull View itemView) {
            super(itemView);
        }
    }

    static class HeadHolder extends ViewHolder {
        public TextView name_view;
        public Button add_view;

        public HeadHolder(@NotNull View itemView) {
            super(itemView);
            name_view = itemView.findViewById(R.id.name_view);
            add_view = itemView.findViewById(R.id.add_view);
        }
    }

    static class ItemHolder extends ViewHolder {
        public TextView name_view;
        public Button delete_view;
        public Button edit_view;

        public ItemHolder(@NotNull View itemView) {
            super(itemView);
            name_view = itemView.findViewById(R.id.name_view);
            delete_view = itemView.findViewById(R.id.delete_view);
            edit_view = itemView.findViewById(R.id.edit_view);
        }
    }

    public interface Callback {

        void edit(UnlockInfo info, int position);

        void delete(UnlockInfo info, int position);

        void add(UnlockInfo info, int position);

    }

    public void remove(int position) {
        data.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, data.size() - position);
    }

}