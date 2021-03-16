/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2021 Tuya Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.tuya.appsdk.sample.home.list.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tuya.appsdk.sample.home.detail.HomeDetailActivity;
import com.tuya.appsdk.sample.resource.HomeModel;
import com.tuya.appsdk.sample.user.R;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.HomeBean;

import java.util.ArrayList;

/**
 * Home List Adapter
 *
 * @author chuanfeng <a href="mailto:developer@tuya.com"/>
 * @since 2021/2/21 10:02 AM
 */
public class HomeListAdapter extends RecyclerView.Adapter<HomeListAdapter.ViewHolder> {

    public ArrayList<HomeBean> data = new ArrayList<>();
    private final int type;

    public HomeListAdapter(int type) {
        this.type = type;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder holder = new ViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.home_item_list, parent, false));

        if (type == HomeListPageType.LIST) {
            holder.ivIcon.setImageResource(R.drawable.ic_next);
            holder.itemView.setOnClickListener(v -> {
                // Home Detail
                Intent intent = new Intent(v.getContext(), HomeDetailActivity.class);
                intent.putExtra("homeId", data.get(holder.getAdapterPosition()).getHomeId());
                v.getContext().startActivity(intent);
            });
        } else if (type == HomeListPageType.SWITCH) {
            holder.itemView.setOnClickListener(v -> {
                // Switch Home
                HomeBean bean = data.get(holder.getAdapterPosition());
                TuyaHomeSdk.newHomeInstance(bean.getHomeId());
                HomeModel.INSTANCE.setCurrentHome(v.getContext(), bean.getHomeId());
                notifyDataSetChanged();
            });
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HomeBean bean = data.get(position);
        holder.tvName.setText(bean.getName());

        if (type == HomeListPageType.SWITCH) {
            // Switch Home Type
            if (HomeModel.getCurrentHome(holder.itemView.getContext()) == bean.getHomeId()) {
                holder.ivIcon.setImageResource(R.drawable.ic_check);
            } else {
                holder.ivIcon.setImageResource(0);
            }
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final ImageView ivIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvName);
            ivIcon = itemView.findViewById(R.id.ivIcon);
        }

    }
}
