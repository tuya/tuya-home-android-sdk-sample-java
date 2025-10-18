package com.tuya.lock.demo.zigbee.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.thingclips.smart.optimus.lock.api.zigbee.response.OpModeBean;
import com.tuya.lock.demo.R;
import com.tuya.lock.demo.ble.activity.code.ShowCodeActivity;
import com.tuya.lock.demo.zigbee.bean.UnlockInfo;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class OpModeSelectListAdapter extends RecyclerView.Adapter<OpModeSelectListAdapter.ViewHolder> {

    public List<UnlockInfo> data = new ArrayList<>();

    public List<OpModeBean> selectData = new ArrayList<>();

    @NotNull
    public List<UnlockInfo> getData() {
        return this.data;
    }

    public void setData(List<UnlockInfo> list) {
        this.data = list;
    }

    public List<String> getSelectList() {
        List<String> unlockIds = new ArrayList<>();
        for (OpModeBean item : selectData) {
            unlockIds.add(item.getUnlockId());
        }
        return unlockIds;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.unlock_mode_list_head, parent, false);
            return new HeadHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.unlock_mode_select_list_item, parent, false);
            return new ItemHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UnlockInfo bean = data.get(position);
        if (holder instanceof HeadHolder) {
            HeadHolder headHolder = (HeadHolder) holder;
            String title = bean.name + " (" + bean.count + ")";
            headHolder.name_view.setText(title);
            headHolder.add_view.setVisibility(View.GONE);
        } else {
            ItemHolder itemHolder = (ItemHolder) holder;
            OpModeBean infoBean = bean.infoBean;
            itemHolder.name_view.setText(infoBean.getUnlockName());

            itemHolder.checkbox_view.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectData.add(bean.infoBean);
                } else {
                    selectData.remove(bean.infoBean);
                }
            });

            itemHolder.itemView.setOnClickListener(v -> ShowCodeActivity.startActivity(v.getContext(), JSONObject.toJSONString(bean)));
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
        public CheckBox checkbox_view;

        public ItemHolder(@NotNull View itemView) {
            super(itemView);
            name_view = itemView.findViewById(R.id.name_view);
            checkbox_view = itemView.findViewById(R.id.checkbox_view);
        }
    }

    public void remove(int position) {
        data.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, data.size() - position);
    }

}