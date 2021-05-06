package com.tuya.smart.android.demo.camera.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tuya.smart.android.demo.R;

import java.util.List;

/**
 * @author Taki
 * @date 2/19/21
 */
public class CameraInfoAdapter extends RecyclerView.Adapter<CameraInfoAdapter.ViewHolder> {

    private List<String> data;

    public CameraInfoAdapter(List<String> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.recycler_camera_info, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.getTextView().setText(data.get(position));
        holder.getTextView().setMaxLines(5);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        public ViewHolder(View view) {
            super(view);
            textView = (TextView) view.findViewById(R.id.recycle_item_info_text);
        }

        public TextView getTextView() {
            return textView;
        }
    }
}
