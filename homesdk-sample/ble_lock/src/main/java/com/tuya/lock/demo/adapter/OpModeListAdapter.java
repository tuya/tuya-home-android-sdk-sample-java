package com.tuya.lock.demo.adapter;

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
import com.tuya.lock.demo.bean.UnlockInfo;
import com.thingclips.smart.sdk.optimus.lock.bean.ble.UnlockInfoBean;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class OpModeListAdapter extends RecyclerView.Adapter<OpModeListAdapter.ViewHolder> {

    public List<UnlockInfo> data = new ArrayList<>();
    private Callback mCallback;

    @NotNull
    public final List<UnlockInfo> getData() {
        return this.data;
    }

    public final void setData(List<UnlockInfo> list) {
        this.data = list;
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
            String title = bean.name + " (" + bean.count + ")";
            headHolder.name_view.setText(title);
            headHolder.add_view.setOnClickListener(v -> {
                if (null != mCallback) {
                    mCallback.add(bean, position);
                }
            });
        } else {
            ItemHolder itemHolder = (ItemHolder) holder;
            UnlockInfoBean infoBean = bean.infoBean;
            String hiJack = bean.infoBean.getUnlockAttr() == 1 ? "（劫持）" : "";
            String lockName = infoBean.getUnlockName() + hiJack;
            itemHolder.name_view.setText(lockName);

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