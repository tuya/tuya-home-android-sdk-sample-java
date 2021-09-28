package com.tuya.smart.android.demo.camera.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.tuya.drawee.view.DecryptImageView;
import com.tuya.smart.android.camera.sdk.TuyaIPCSdk;
import com.tuya.smart.android.camera.sdk.api.ITuyaIPCTool;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.camera.utils.BitmapUtils;
import com.tuya.smart.android.demo.camera.utils.ToastUtil;
import com.tuya.smart.home.sdk.callback.ITuyaResultCallback;
import com.tuya.smart.ipc.messagecenter.bean.CameraMessageBean;

import java.io.File;
import java.util.List;
import java.util.Objects;

/**
 * Created by huangdaju on 2018/3/5.
 */

public class AlarmDetectionAdapter extends RecyclerView.Adapter<AlarmDetectionAdapter.MyViewHolder> {

    private LayoutInflater mInflater;
    private List<CameraMessageBean> cameraMessageBeans;
    private OnItemListener listener;
    private Context context;

    public AlarmDetectionAdapter(Context context, List<CameraMessageBean> cameraMessageBeans) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
        this.cameraMessageBeans = cameraMessageBeans;
    }

    public void updateAlarmDetectionMessage(List<CameraMessageBean> messageBeans) {
        if (null != cameraMessageBeans) {
            cameraMessageBeans.clear();
            cameraMessageBeans.addAll(messageBeans);
            notifyDataSetChanged();
        }
    }

    public void setListener(OnItemListener listener) {
        this.listener = listener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(mInflater.inflate(R.layout.camera_newui_more_motion_recycle_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final CameraMessageBean ipcVideoBean = cameraMessageBeans.get(position);
        holder.mTvStartTime.setText(ipcVideoBean.getDateTime());
        holder.mTvDescription.setText(ipcVideoBean.getMsgTypeContent());
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (null != listener) {
                    listener.onLongClick(ipcVideoBean);
                }
                return false;
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != listener) {
                    listener.onItemClick(ipcVideoBean);
                }
            }
        });
        holder.showPicture(ipcVideoBean);
    }

    @Override
    public int getItemCount() {
        return cameraMessageBeans.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView mTvStartTime;
        private TextView mTvDescription;
        private DecryptImageView mSnapshot;
        private Button mBtn;

        public MyViewHolder(final View view) {
            super(view);
            mTvStartTime = view.findViewById(R.id.tv_time_range_start_time);
            mTvDescription = view.findViewById(R.id.tv_alarm_detection_description);
            mSnapshot = view.findViewById(R.id.iv_time_range_snapshot);
            mBtn = view.findViewById(R.id.btn_download_img);
        }

        private void showPicture(CameraMessageBean cameraMessageBean) {
            String attachPics = cameraMessageBean.getAttachPics();
            mSnapshot.setVisibility(View.VISIBLE);
            if (attachPics.contains("@")) {
                int index = attachPics.lastIndexOf("@");
                try {
                    String decryption = attachPics.substring(index + 1);
                    String imageUrl = attachPics.substring(0, index);
                    mSnapshot.setImageURI(imageUrl, decryption.getBytes());
                    //show download encryptedImg button
                    mBtn.setVisibility(View.VISIBLE);
                    mBtn.setOnClickListener(v -> {
                        ITuyaIPCTool tool = TuyaIPCSdk.getTool();
                        if (tool != null) {
                            tool.downloadEncryptedImg(imageUrl, decryption, new ITuyaResultCallback<Bitmap>() {
                                @Override
                                public void onSuccess(Bitmap result) {
//                                        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Camera/";
                                    String path = Objects.requireNonNull(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)).getPath() + "/Camera";
                                    File file = new File(path);
                                    if (!file.exists()) {
                                        file.mkdirs();
                                    }
                                    if (BitmapUtils.savePhotoToSDCard(result, path)) {
                                        ToastUtil.shortToast(context, context.getString(R.string.download_suc));
                                    }
                                }

                                @Override
                                public void onError(String errorCode, String errorMessage) {
                                    Log.e("AlarmDetectionAdapter", "download encrypted img err: " + errorCode + errorMessage);
                                }
                            });
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Uri uri = null;
                try {
                    uri = Uri.parse(attachPics);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                DraweeController controller = Fresco.newDraweeControllerBuilder().setUri(uri).build();
                mSnapshot.setController(controller);
            }
        }
    }


    public interface OnItemListener {
        void onLongClick(CameraMessageBean o);

        void onItemClick(CameraMessageBean o);
    }

}
