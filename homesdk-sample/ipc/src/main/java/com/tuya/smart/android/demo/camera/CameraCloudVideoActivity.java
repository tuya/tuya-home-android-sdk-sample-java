package com.tuya.smart.android.demo.camera;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSONObject;
import com.tuya.smart.android.camera.sdk.TuyaIPCSdk;
import com.tuya.smart.android.camera.sdk.api.ITuyaIPCCore;
import com.tuya.smart.android.camera.sdk.api.ITuyaIPCMsg;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.camera.utils.MessageUtil;
import com.tuya.smart.camera.camerasdk.typlayer.callback.AbsP2pCameraListener;
import com.tuya.smart.camera.camerasdk.typlayer.callback.IRegistorIOTCListener;
import com.tuya.smart.camera.camerasdk.typlayer.callback.OperationCallBack;
import com.tuya.smart.camera.camerasdk.typlayer.callback.OperationDelegateCallBack;
import com.tuya.smart.camera.ipccamerasdk.msgvideo.ITYCloudVideo;
import com.tuya.smart.camera.ipccamerasdk.p2p.ICameraP2P;
import com.tuya.smart.camera.middleware.widget.TuyaCameraView;
import com.tuya.smart.android.demo.camera.utils.ToastUtil;

import static com.tuya.smart.android.demo.camera.utils.Constants.ARG1_OPERATE_FAIL;
import static com.tuya.smart.android.demo.camera.utils.Constants.ARG1_OPERATE_SUCCESS;
import static com.tuya.smart.android.demo.camera.utils.Constants.MSG_MUTE;

public class CameraCloudVideoActivity extends AppCompatActivity {

    private final int OPERATE_SUCCESS = 1;
    private final int OPERATE_FAIL = 0;
    private final int MSG_CLOUD_VIDEO_DEVICE = 1000;

    private ProgressBar mProgressBar;
    private TuyaCameraView mCameraView;

    private ITYCloudVideo mCloudVideo;
    private String playUrl;
    private String encryptKey;
    private int playDuration;
    private String cachePath;
    private String mDevId;
    private ImageView muteImg;
    private int previewMute = ICameraP2P.MUTE;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CLOUD_VIDEO_DEVICE:
                    startplay();
                    break;
                case MSG_MUTE:
                    handleMute(msg);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void startplay() {
        if (mCloudVideo != null) {
            mCloudVideo.playVideo(playUrl, 0, encryptKey, new OperationCallBack() {
                @Override
                public void onSuccess(int i, int i1, String s, Object o) {
                    Log.d("mcloudCamera", "onsuccess");
                }

                @Override
                public void onFailure(int i, int i1, int i2, Object o) {

                }
            }, new OperationCallBack() {
                @Override
                public void onSuccess(int i, int i1, String s, Object o) {
                    Log.d("mcloudCamera", "finish onsuccess");
                }

                @Override
                public void onFailure(int i, int i1, int i2, Object o) {

                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_cloud_video);
        initData();
        initview();
        initCloudCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCloudVideo != null) {
            mCloudVideo.stopVideo(null);
            mCloudVideo.removeOnDelegateP2PCameraListener();
            mCloudVideo.deinitCloudVideo();
        }
    }

    private void initData() {
        playUrl = getIntent().getStringExtra("playUrl");
        encryptKey = getIntent().getStringExtra("encryptKey");
        playDuration = getIntent().getIntExtra("playDuration", 0);
        mDevId = getIntent().getStringExtra("devId");
        cachePath = getApplication().getCacheDir().getPath();

    }

    private void initCloudCamera() {
        ITuyaIPCMsg message = TuyaIPCSdk.getMessage();
        if (message != null) {
            mCloudVideo = message.createVideoMessagePlayer();
        }
        if (mCloudVideo != null) {
            mCloudVideo.registerP2PCameraListener(new AbsP2pCameraListener() {
                @Override
                public void receiveFrameDataForMediaCodec(int i, byte[] bytes, int i1, int i2, byte[] bytes1, boolean b, int i3) {
                    super.receiveFrameDataForMediaCodec(i, bytes, i1, i2, bytes1, b, i3);
                }
            });
            mCloudVideo.generateCloudCameraView((IRegistorIOTCListener) mCameraView.createdView());
            mCloudVideo.createCloudDevice(cachePath, mDevId, new OperationDelegateCallBack() {
                @Override
                public void onSuccess(int sessionId, int requestId, String data) {
                    mHandler.sendMessage(MessageUtil.getMessage(MSG_CLOUD_VIDEO_DEVICE, OPERATE_SUCCESS));
                }

                @Override
                public void onFailure(int sessionId, int requestId, int errCode) {

                }
            });
        }
    }

    private void initview() {
        mProgressBar = findViewById(R.id.camera_cloud_video_progressbar);
        mCameraView = findViewById(R.id.camera_cloud_video_view);
        mCameraView.createVideoView(mDevId);
        findViewById(R.id.btn_pause_video_msg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCloudVideo != null) {
                    mCloudVideo.pauseVideo(null);
                }
            }
        });
        findViewById(R.id.btn_resume_video_msg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCloudVideo != null) {
                    mCloudVideo.resumeVideo(null);
                }
            }
        });
        muteImg = findViewById(R.id.camera_mute);
        muteImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                muteClick();
            }
        });
        muteImg.setSelected(true);
    }

    private void muteClick() {
        if (mCloudVideo != null) {
            int mute = previewMute == ICameraP2P.MUTE ? ICameraP2P.UNMUTE : ICameraP2P.MUTE;
            mCloudVideo.setCloudVideoMute(mute, new OperationDelegateCallBack() {
                @Override
                public void onSuccess(int sessionId, int requestId, String data) {
                    try {
                        JSONObject jsonObject = JSONObject.parseObject(data);
                        Object value = jsonObject.get("mute");
                        previewMute = Integer.valueOf(value.toString());
                        mHandler.sendMessage(MessageUtil.getMessage(MSG_MUTE, ARG1_OPERATE_SUCCESS));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int sessionId, int requestId, int errCode) {
                    mHandler.sendMessage(MessageUtil.getMessage(MSG_MUTE, ARG1_OPERATE_FAIL));
                }
            });
        }
    }

    private void handleMute(Message msg) {
        if (msg.arg1 == ARG1_OPERATE_SUCCESS) {
            muteImg.setSelected(previewMute == ICameraP2P.MUTE);
        } else {
            ToastUtil.shortToast(CameraCloudVideoActivity.this, getString(R.string.operation_failed));
        }
    }

}
