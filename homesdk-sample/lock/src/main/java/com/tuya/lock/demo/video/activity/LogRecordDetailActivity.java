package com.tuya.lock.demo.video.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.thingclips.thinglock.videolock.api.IVideoLockManager;
import com.thingclips.thinglock.videolock.bean.LogsListBean;
import com.tuya.lock.demo.R;
import com.tuya.lock.demo.ble.constant.Constant;
import com.tuya.lock.demo.ble.view.EncryptImageView;
import com.tuya.lock.demo.video.adapter.RecordListAdapter;

/**
 * 解密图片
 */
public class LogRecordDetailActivity extends AppCompatActivity {

    private EncryptImageView decrypt_view;

    private LogsListBean.MediaInfo mediaInfo;

    public static void startActivity(Context context, LogsListBean.MediaInfo mediaInfo) {
        Intent intent = new Intent(context, LogRecordDetailActivity.class);
        intent.putExtra(Constant.CODE_DATA, JSONObject.toJSONString(mediaInfo));
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_record_detail);

        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setTitle(getString(R.string.lock_log_list));

        String data = getIntent().getStringExtra(Constant.CODE_DATA);

        if (null != data) {
            mediaInfo = JSONObject.parseObject(data, LogsListBean.MediaInfo.class);
        }


        decrypt_view = findViewById(R.id.decrypt_view);
        decrypt_view.setEncryptImageViewLoadListener(new EncryptImageView.EncryptImageViewLoadListener() {
            @Override
            public void success(String url, int width, int height) {
                Log.e("setImageURI", "success width:" + width + ", height:" + height);
            }

            @Override
            public void failure(String url, String error) {
                Log.e("setImageURI", "failure error:" + error);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        decrypt_view.setImageURI(mediaInfo.fileUrl, mediaInfo.fileKey.getBytes());
    }
}
