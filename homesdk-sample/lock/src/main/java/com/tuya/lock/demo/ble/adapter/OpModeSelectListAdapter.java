package com.tuya.lock.demo.ble.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tuya.lock.demo.R;
import com.tuya.lock.demo.ble.bean.UnlockInfo;
import com.thingclips.smart.sdk.optimus.lock.bean.ble.UnlockInfoBean;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class OpModeSelectListAdapter extends RecyclerView.Adapter<OpModeSelectListAdapter.ViewHolder> {

    public List<UnlockInfo> data = new ArrayList<>();

    public List<UnlockInfoBean> selectData = new ArrayList<>();

    @NotNull
    public List<UnlockInfo> getData() {
        return this.data;
    }

    public void setData(List<UnlockInfo> list) {
        this.data = list;
    }

    public List<UnlockInfoBean> getSelectList() {
        return this.selectData;
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
            UnlockInfoBean infoBean = bean.infoBean;
            itemHolder.name_view.setText(infoBean.getUnlockName());

            itemHolder.checkbox_view.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        selectData.add(bean.infoBean);
                    } else {
                        selectData.remove(bean.infoBean);
                    }
                }
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
        public CheckBox checkbox_view;
        public Button edit_view;

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