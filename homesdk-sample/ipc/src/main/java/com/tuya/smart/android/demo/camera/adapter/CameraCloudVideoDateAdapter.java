package com.tuya.smart.android.demo.camera.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.tuya.smart.android.demo.R;
import com.tuya.smart.camera.middleware.cloud.bean.CloudDayBean;

import java.util.List;

/**
 * Created by huangdaju on 2018/3/5.
 */

public class CameraCloudVideoDateAdapter extends RecyclerView.Adapter<CameraCloudVideoDateAdapter.MyViewHolder> {

    private LayoutInflater mInflater;
    private List<CloudDayBean> dateList;
    private OnTimeItemListener listener;

    public CameraCloudVideoDateAdapter(Context context, List<CloudDayBean> dateList) {
        mInflater = LayoutInflater.from(context);
        this.dateList = dateList;
    }

    public void setListener(OnTimeItemListener listener) {
        this.listener = listener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(mInflater.inflate(R.layout.activity_camera_video_date_tem, parent, false));
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        CloudDayBean bean = dateList.get(position);
        holder.mDate.setText(bean.getUploadDay());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != listener) {
                    listener.onClick(bean);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return dateList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView mDate;

        public MyViewHolder(final View view) {
            super(view);
            mDate = view.findViewById(R.id.tv_date);
        }
    }

    public interface OnTimeItemListener {
        void onClick(CloudDayBean date);
    }
}
