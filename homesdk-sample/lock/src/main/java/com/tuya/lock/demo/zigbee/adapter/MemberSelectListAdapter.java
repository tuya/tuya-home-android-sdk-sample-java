package com.tuya.lock.demo.zigbee.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.thingclips.smart.optimus.lock.api.zigbee.response.MemberInfoBean;
import com.tuya.lock.demo.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class MemberSelectListAdapter extends RecyclerView.Adapter<MemberSelectListAdapter.ViewHolder> {

    public ArrayList<MemberInfoBean> data = new ArrayList<>();
    private Callback callback;

    @NotNull
    public final ArrayList<MemberInfoBean> getData() {
        return this.data;
    }

    public final void setData(ArrayList<MemberInfoBean> list) {
        this.data = list;
    }

    public void setAlloc(Callback callback) {
        this.callback = callback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.member_select_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MemberInfoBean bean = data.get(position);
        holder.opMode_name_view.setText(bean.getNickName());

        holder.itemView.setOnClickListener(v -> {
            if (null != callback) {
                callback.alloc(bean, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView opMode_name_view;

        public ViewHolder(@NotNull View itemView) {
            super(itemView);
            opMode_name_view = itemView.findViewById(R.id.opMode_name);
        }
    }

    public interface Callback {
        void alloc(MemberInfoBean infoBean, int position);
    }
}