package com.tuya.smart.android.demo.camera;

import static com.tuya.smart.android.demo.camera.utils.Constants.ARG1_OPERATE_FAIL;
import static com.tuya.smart.android.demo.camera.utils.Constants.ARG1_OPERATE_SUCCESS;
import static com.tuya.smart.android.demo.camera.utils.Constants.INTENT_DEV_ID;
import static com.tuya.smart.android.demo.camera.utils.Constants.MSG_CONNECT;
import static com.tuya.smart.android.demo.camera.utils.Constants.MSG_GET_VIDEO_CLARITY;
import static com.tuya.smart.android.demo.camera.utils.Constants.MSG_MUTE;
import static com.tuya.smart.android.demo.camera.utils.Constants.MSG_SCREENSHOT;
import static com.tuya.smart.android.demo.camera.utils.Constants.MSG_SET_CLARITY;
import static com.tuya.smart.android.demo.camera.utils.Constants.MSG_TALK_BACK_BEGIN;
import static com.tuya.smart.android.demo.camera.utils.Constants.MSG_TALK_BACK_OVER;
import static com.tuya.smart.android.demo.camera.utils.Constants.MSG_VIDEO_RECORD_BEGIN;
import static com.tuya.smart.android.demo.camera.utils.Constants.MSG_VIDEO_RECORD_FAIL;
import static com.tuya.smart.android.demo.camera.utils.Constants.MSG_VIDEO_RECORD_OVER;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.alibaba.fastjson.JSONObject;
import com.thingclips.loguploader.TLogSDK;
import com.thingclips.loguploader.api.LogFileCallback;
import com.thingclips.smart.android.camera.sdk.ThingIPCSdk;
import com.thingclips.smart.android.camera.sdk.api.IThingIPCCloud;
import com.thingclips.smart.android.camera.sdk.api.IThingIPCCore;
import com.thingclips.smart.android.camera.sdk.api.IThingIPCDoorbell;
import com.thingclips.smart.camera.camerasdk.thingplayer.callback.AbsP2pCameraListener;
import com.thingclips.smart.camera.camerasdk.thingplayer.callback.OnRenderDirectionCallback;
import com.thingclips.smart.camera.camerasdk.thingplayer.callback.OperationDelegateCallBack;
import com.thingclips.smart.camera.ipccamerasdk.p2p.ICameraP2P;
import com.thingclips.smart.camera.middleware.p2p.IThingSmartCameraP2P;
import com.thingclips.smart.camera.middleware.widget.AbsVideoViewCallback;
import com.thingclips.smart.camera.middleware.widget.ThingCameraView;
import com.thingclips.smart.camera.utils.chaos.L;
import com.thingclips.smart.home.sdk.ThingHomeSdk;
import com.thingclips.smart.ipc.camera.autotesting.activity.AutoCameraTestingProgramListActivity;
import com.thingclips.smart.ipc.camera.cloudtool.activity.CloudToolHomeActivity;
import com.thingclips.smart.sdk.api.IResultCallback;
import com.thingclips.smart.sdk.api.IThingDevice;
import com.thingclips.smart.sdk.bean.DeviceBean;
import com.tuya.appsdk.sample.resource.HomeModel;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.camera.utils.CameraPTZHelper;
import com.tuya.smart.android.demo.camera.utils.Constants;
import com.tuya.smart.android.demo.camera.utils.DPConstants;
import com.tuya.smart.android.demo.camera.utils.MessageUtil;
import com.tuya.smart.android.demo.camera.utils.ToastUtil;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Map;

/**
 * @author chenbj
 */
public class CameraPanelActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "CameraPanelActivity";
    private Toolbar toolbar;
    private ThingCameraView mVideoView;
    private ImageView muteImg;
    private TextView qualityTv;
    private TextView speakTxt, recordTxt, photoTxt, replayTxt, settingTxt, cloudStorageTxt, messageCenterTxt, deviceInfoTxt;

    private static final int ASPECT_RATIO_WIDTH = 9;
    private static final int ASPECT_RATIO_HEIGHT = 16;
    private boolean isSpeaking = false;
    private boolean isRecording = false;
    private boolean isPlay = false;
    private int previewMute = ICameraP2P.MUTE;
    private int videoClarity = ICameraP2P.HD;
    private String currVideoClarity;
    private String devId;
    private IThingSmartCameraP2P mCameraP2P;
    CameraPTZHelper cameraPTZHelper;
    private boolean reConnect;

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CONNECT:
                    handleConnect(msg);
                    break;
                case MSG_SET_CLARITY:
                    handleClarity(msg);
                    break;
                case MSG_MUTE:
                    handleMute(msg);
                    break;
                case MSG_SCREENSHOT:
                    handlesnapshot(msg);
                    break;
                case MSG_VIDEO_RECORD_BEGIN:
                    ToastUtil.shortToast(CameraPanelActivity.this, getString(R.string.operation_suc));
                    break;
                case MSG_VIDEO_RECORD_FAIL:
                    ToastUtil.shortToast(CameraPanelActivity.this, getString(R.string.operation_failed));
                    break;
                case MSG_VIDEO_RECORD_OVER:
                    handleVideoRecordOver(msg);
                    break;
                case MSG_TALK_BACK_BEGIN:
                    handleStartTalk(msg);
                    break;
                case MSG_TALK_BACK_OVER:
                    handleStopTalk(msg);
                    break;
                case MSG_GET_VIDEO_CLARITY:
                    handleGetVideoClarity(msg);
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void handleStopTalk(Message msg) {
        if (msg.arg1 == ARG1_OPERATE_SUCCESS) {
            ToastUtil.shortToast(CameraPanelActivity.this, getString(R.string.operation_suc));
        } else {
            ToastUtil.shortToast(CameraPanelActivity.this, getString(R.string.operation_failed));
        }
    }

    private void handleStartTalk(Message msg) {
        if (msg.arg1 == ARG1_OPERATE_SUCCESS) {
            ToastUtil.shortToast(CameraPanelActivity.this, getString(R.string.operation_suc));
        } else {
            ToastUtil.shortToast(CameraPanelActivity.this, getString(R.string.operation_failed));
        }
    }

    private void handleVideoRecordOver(Message msg) {
        if (msg.arg1 == ARG1_OPERATE_SUCCESS) {
            ToastUtil.shortToast(CameraPanelActivity.this, getString(R.string.operation_suc));
        } else {
            ToastUtil.shortToast(CameraPanelActivity.this, getString(R.string.operation_failed));
        }
    }

    private void handlesnapshot(Message msg) {
        if (msg.arg1 == ARG1_OPERATE_SUCCESS) {
            ToastUtil.shortToast(CameraPanelActivity.this, getString(R.string.operation_suc));
        } else {
            ToastUtil.shortToast(CameraPanelActivity.this, getString(R.string.operation_failed));
        }
    }

    private void handleMute(Message msg) {
        if (msg.arg1 == ARG1_OPERATE_SUCCESS) {
            muteImg.setSelected(previewMute == ICameraP2P.MUTE);
        } else {
            ToastUtil.shortToast(CameraPanelActivity.this, getString(R.string.operation_failed));
        }
    }


    private void handleClarity(Message msg) {
        if (msg.arg1 == ARG1_OPERATE_SUCCESS) {
            qualityTv.setText(videoClarity == ICameraP2P.HD ? getString(R.string.hd) : getString(R.string.sd));
        } else {
            ToastUtil.shortToast(CameraPanelActivity.this, getString(R.string.operation_failed));
        }
    }

    private void handleConnect(Message msg) {
        if (msg.arg1 == ARG1_OPERATE_SUCCESS) {
            preview();
        } else {
            Object obj = msg.obj;
            ToastUtil.shortToast(CameraPanelActivity.this, getString(R.string.connect_failed) + " : " + obj);
        }
    }

    private void handleGetVideoClarity(Message msg) {
        if (msg.arg1 == ARG1_OPERATE_SUCCESS && !TextUtils.isEmpty(currVideoClarity)) {
            String info = getString(R.string.other);
            if (currVideoClarity.equals(String.valueOf(ICameraP2P.HD))) {
                info = getString(R.string.hd);
            } else if (currVideoClarity.equals(String.valueOf(ICameraP2P.STANDEND))) {
                info = getString(R.string.sd);
            }
            ToastUtil.shortToast(CameraPanelActivity.this, getString(R.string.get_current_clarity) + info);
        } else {
            ToastUtil.shortToast(CameraPanelActivity.this, getString(R.string.operation_failed));
        }
    }

    /**
     * the lower power Doorbell device change to true
     */
    private boolean isDoorbell = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_panel);
        initView();
        initData();
        initListener();

        if (querySupportByDPID(DPConstants.PTZ_CONTROL)) {
            mVideoView.setOnRenderDirectionCallback(new OnRenderDirectionCallback() {

                @Override
                public void onLeft() {
                    cameraPTZHelper.ptzControl(DPConstants.PTZ_LEFT);
                }

                @Override
                public void onRight() {
                    cameraPTZHelper.ptzControl(DPConstants.PTZ_RIGHT);
                }

                @Override
                public void onUp() {
                    cameraPTZHelper.ptzControl(DPConstants.PTZ_UP);
                }

                @Override
                public void onDown() {
                    cameraPTZHelper.ptzControl(DPConstants.PTZ_DOWN);
                }

                @Override
                public void onCancel() {
                    cameraPTZHelper.ptzStop();
                }
            });
        }
    }

    private void initView() {
        toolbar = findViewById(R.id.toolbar_view);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        mVideoView = findViewById(R.id.camera_video_view);
        muteImg = findViewById(R.id.camera_mute);
        qualityTv = findViewById(R.id.camera_quality);
        speakTxt = findViewById(R.id.speak_Txt);
        recordTxt = findViewById(R.id.record_Txt);
        photoTxt = findViewById(R.id.photo_Txt);
        replayTxt = findViewById(R.id.replay_Txt);
        settingTxt = findViewById(R.id.setting_Txt);
        settingTxt.setOnClickListener(this);
        deviceInfoTxt = findViewById(R.id.info_Txt);
        deviceInfoTxt.setOnClickListener(this);
        findViewById(R.id.get_clarity_Txt).setOnClickListener(this);
        cloudStorageTxt = findViewById(R.id.cloud_Txt);
        messageCenterTxt = findViewById(R.id.message_center_Txt);
        findViewById(R.id.debug_Txt).setOnClickListener(this);
        findViewById(R.id.ptz_Txt).setOnClickListener(this);

        WindowManager windowManager = (WindowManager) this.getSystemService(WINDOW_SERVICE);
        int width = windowManager.getDefaultDisplay().getWidth();
        int height = width * ASPECT_RATIO_WIDTH / ASPECT_RATIO_HEIGHT;
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
        layoutParams.addRule(RelativeLayout.BELOW, R.id.toolbar_view);
        findViewById(R.id.camera_video_view_Rl).setLayoutParams(layoutParams);

        muteImg.setSelected(true);
    }

    private void initData() {
        devId = getIntent().getStringExtra(INTENT_DEV_ID);
        IThingIPCCore cameraInstance = ThingIPCSdk.getCameraInstance();
        if (cameraInstance != null) {
            mCameraP2P = cameraInstance.createCameraP2P(devId);
        }
        mVideoView.setViewCallback(new AbsVideoViewCallback() {
            @Override
            public void onCreated(Object o) {
                super.onCreated(o);
                if (null != mCameraP2P) {
                    mCameraP2P.generateCameraView(o);
                }
            }
        });
        mVideoView.createVideoView(devId);
        if (null == mCameraP2P) {
            showNotSupportToast();
        } else {

        }
        cameraPTZHelper = new CameraPTZHelper(devId);
        cameraPTZHelper.bindPtzBoard(findViewById(R.id.sv_ptz_board));
    }

    private void showNotSupportToast() {
        ToastUtil.shortToast(CameraPanelActivity.this, getString(R.string.not_support_device));
    }

    private void preview() {
        mCameraP2P.startPreview(videoClarity, new OperationDelegateCallBack() {
            @Override
            public void onSuccess(int sessionId, int requestId, String data) {
                Log.d(TAG, "start preview onSuccess");
                isPlay = true;
            }

            @Override
            public void onFailure(int sessionId, int requestId, int errCode) {
                Log.d(TAG, "start preview onFailure, errCode: " + errCode);
                isPlay = false;
            }
        });
    }

    private void initListener() {
        if (mCameraP2P == null) {
            return;
        }

        muteImg.setOnClickListener(this);
        qualityTv.setOnClickListener(this);
        speakTxt.setOnClickListener(this);
        recordTxt.setOnClickListener(this);
        photoTxt.setOnClickListener(this);
        replayTxt.setOnClickListener(this);

        cloudStorageTxt.setOnClickListener(this);
        messageCenterTxt.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.camera_mute) {
            muteClick();
        } else if (id == R.id.camera_quality) {
            setVideoClarity();
        } else if (id == R.id.speak_Txt) {
            speakClick();
        } else if (id == R.id.record_Txt) {
            recordClick();
        } else if (id == R.id.photo_Txt) {
            snapShotClick();
        } else if (id == R.id.replay_Txt) {
            Intent intent = new Intent(CameraPanelActivity.this, CameraPlaybackActivity.class);
            intent.putExtra(INTENT_DEV_ID, devId);
            startActivity(intent);
        } else if (id == R.id.setting_Txt) {
            Intent intent1 = new Intent(CameraPanelActivity.this, CameraSettingActivity.class);
            intent1.putExtra(INTENT_DEV_ID, devId);
            startActivity(intent1);
        } else if (id == R.id.cloud_Txt) {
            // 判断设备是否支持云存储
            boolean isSupportCloudStorage = false;
            IThingIPCCloud cloud = ThingIPCSdk.getCloud();
            if (cloud != null) {
                isSupportCloudStorage = cloud.isSupportCloudStorage(devId);
            }
            if (!isSupportCloudStorage) {
                ToastUtil.shortToast(CameraPanelActivity.this, getString(R.string.not_support));
                return;
            }
            Intent intent2 = new Intent(CameraPanelActivity.this, CameraCloudStorageActivity.class);
            intent2.putExtra(INTENT_DEV_ID, devId);
            startActivity(intent2);
        } else if (id == R.id.message_center_Txt) {
            Intent intent3 = new Intent(CameraPanelActivity.this, AlarmDetectionActivity.class);
            intent3.putExtra(INTENT_DEV_ID, devId);
            startActivity(intent3);
        } else if (id == R.id.info_Txt) {
            Intent intent4 = new Intent(CameraPanelActivity.this, CameraInfoActivity.class);
            intent4.putExtra(INTENT_DEV_ID, devId);
            startActivity(intent4);
        } else if (id == R.id.get_clarity_Txt) {
            if (mCameraP2P != null) {
                mCameraP2P.getVideoClarity(new OperationDelegateCallBack() {
                    @Override
                    public void onSuccess(int i, int i1, String s) {
                        currVideoClarity = s;
                        mHandler.sendMessage(MessageUtil.getMessage(MSG_GET_VIDEO_CLARITY, ARG1_OPERATE_SUCCESS));
                    }

                    @Override
                    public void onFailure(int i, int i1, int i2) {
                        mHandler.sendMessage(MessageUtil.getMessage(MSG_GET_VIDEO_CLARITY, ARG1_OPERATE_FAIL, i2));
                    }
                });
            }
        } else if (id == R.id.debug_Txt) {
            String[] items = new String[]{getString(R.string.ipc_sdk_autotest_tools), getString(R.string.ipc_cloud_debug_tools)};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setItems(items, (dialog, which) -> {
                if (which == 0) {
                    Intent intent = new Intent(CameraPanelActivity.this, AutoCameraTestingProgramListActivity.class);
                    intent.putExtra("extra_current_home_id", HomeModel.getCurrentHome(this));
                    startActivity(intent);
                } else if (which == 1) {
                    Intent intent = new Intent(CameraPanelActivity.this, CloudToolHomeActivity.class);
                    intent.putExtra("extra_current_home_id", HomeModel.getCurrentHome(this));
                    startActivity(intent);
                }
            });
            builder.setNegativeButton(getString(R.string.ipc_close), (dialog, which) -> dialog.dismiss());
            builder.create().show();
        } else if (id == R.id.ptz_Txt) {
            cameraPTZHelper.show();
        }
    }

    private void recordClick() {
        if (!isRecording) {
            String picPath = getExternalFilesDir(null).getPath() + "/" + devId;
            File file = new File(picPath);
            if (!file.exists()) {
                file.mkdirs();
            }
            String fileName = System.currentTimeMillis() + ".mp4";
            mCameraP2P.startRecordLocalMp4(picPath, fileName, CameraPanelActivity.this, new OperationDelegateCallBack() {
                @Override
                public void onSuccess(int sessionId, int requestId, String data) {
                    isRecording = true;
                    mHandler.sendEmptyMessage(MSG_VIDEO_RECORD_BEGIN);
                    //returns the recorded thumbnail path （.jpg）
                    Log.i(TAG, "record :" + data);
                }

                @Override
                public void onFailure(int sessionId, int requestId, int errCode) {
                    mHandler.sendEmptyMessage(MSG_VIDEO_RECORD_FAIL);
                }
            });
            recordStatue(true);
        } else {
            mCameraP2P.stopRecordLocalMp4(new OperationDelegateCallBack() {
                @Override
                public void onSuccess(int sessionId, int requestId, String data) {
                    isRecording = false;
                    mHandler.sendMessage(MessageUtil.getMessage(MSG_VIDEO_RECORD_OVER, ARG1_OPERATE_SUCCESS));
                }

                @Override
                public void onFailure(int sessionId, int requestId, int errCode) {
                    isRecording = false;
                    mHandler.sendMessage(MessageUtil.getMessage(MSG_VIDEO_RECORD_OVER, ARG1_OPERATE_FAIL, errCode));
                }
            });
            recordStatue(false);
        }
    }

    private void snapShotClick() {
        String picPath = getExternalFilesDir(null).getPath() + "/" + devId;
        File file = new File(picPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        mCameraP2P.snapshot(picPath, fileName, CameraPanelActivity.this, new OperationDelegateCallBack() {
            @Override
            public void onSuccess(int sessionId, int requestId, String data) {
                mHandler.sendMessage(MessageUtil.getMessage(MSG_SCREENSHOT, ARG1_OPERATE_SUCCESS));
                Log.i(TAG, "snapshot :" + data);
            }

            @Override
            public void onFailure(int sessionId, int requestId, int errCode) {
                mHandler.sendMessage(MessageUtil.getMessage(MSG_SCREENSHOT, ARG1_OPERATE_FAIL, errCode));
            }
        });
    }

    private void muteClick() {
        int mute;
        mute = previewMute == ICameraP2P.MUTE ? ICameraP2P.UNMUTE : ICameraP2P.MUTE;
        mCameraP2P.setMute(mute, new OperationDelegateCallBack() {
            @Override
            public void onSuccess(int sessionId, int requestId, String data) {
                previewMute = Integer.valueOf(data);
                mHandler.sendMessage(MessageUtil.getMessage(MSG_MUTE, ARG1_OPERATE_SUCCESS));
            }

            @Override
            public void onFailure(int sessionId, int requestId, int errCode) {
                mHandler.sendMessage(MessageUtil.getMessage(MSG_MUTE, ARG1_OPERATE_FAIL, errCode));
            }
        });
    }

    private void speakClick() {
        if (isSpeaking) {
            mCameraP2P.stopAudioTalk(new OperationDelegateCallBack() {
                @Override
                public void onSuccess(int sessionId, int requestId, String data) {
                    isSpeaking = false;
                    mHandler.sendMessage(MessageUtil.getMessage(MSG_TALK_BACK_OVER, ARG1_OPERATE_SUCCESS));
                }

                @Override
                public void onFailure(int sessionId, int requestId, int errCode) {
                    isSpeaking = false;
                    mHandler.sendMessage(MessageUtil.getMessage(MSG_TALK_BACK_OVER, ARG1_OPERATE_FAIL, errCode));

                }
            });
        } else {
            if (Constants.hasRecordPermission()) {
                mCameraP2P.startAudioTalk(new OperationDelegateCallBack() {
                    @Override
                    public void onSuccess(int sessionId, int requestId, String data) {
                        isSpeaking = true;
                        mHandler.sendMessage(MessageUtil.getMessage(MSG_TALK_BACK_BEGIN, ARG1_OPERATE_SUCCESS));
                    }

                    @Override
                    public void onFailure(int sessionId, int requestId, int errCode) {
                        isSpeaking = false;
                        mHandler.sendMessage(MessageUtil.getMessage(MSG_TALK_BACK_BEGIN, ARG1_OPERATE_FAIL));
                    }
                });
            } else {
                Constants.requestPermission(CameraPanelActivity.this, Manifest.permission.RECORD_AUDIO, Constants.EXTERNAL_AUDIO_REQ_CODE, "open_recording");
            }
        }
    }

    private void setVideoClarity() {
        mCameraP2P.setVideoClarity(videoClarity == ICameraP2P.HD ? ICameraP2P.STANDEND : ICameraP2P.HD, new OperationDelegateCallBack() {
            @Override
            public void onSuccess(int sessionId, int requestId, String data) {
                videoClarity = Integer.valueOf(data);
                mHandler.sendMessage(MessageUtil.getMessage(MSG_SET_CLARITY, ARG1_OPERATE_SUCCESS));
            }

            @Override
            public void onFailure(int sessionId, int requestId, int errCode) {
                mHandler.sendMessage(MessageUtil.getMessage(MSG_SET_CLARITY, ARG1_OPERATE_FAIL, errCode));
            }
        });
    }

    private void recordStatue(boolean isRecording) {
        speakTxt.setEnabled(!isRecording);
        photoTxt.setEnabled(!isRecording);
        replayTxt.setEnabled(!isRecording);
        recordTxt.setEnabled(true);
        recordTxt.setSelected(isRecording);
    }

    /**
     * 获取离线日志保存路径
     */
    private void getLogPath() {
        TLogSDK.fetchLogs(this, new LogFileCallback() {
            @Override
            public void onSuccess(ArrayList<String> logPaths) {
                for (String s : logPaths) {
                    L.i(TAG, "Local Log path:" + s);
                }
            }

            @Override
            public void onFail(String errorMsg) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVideoView.onResume();
        //must register again,or can't callback
        if (null != mCameraP2P) {
            mCameraP2P.registerP2PCameraListener(p2pCameraListener);
            mCameraP2P.generateCameraView(mVideoView.createdView());
            if (mCameraP2P.isConnecting()) {
                mCameraP2P.startPreview(new OperationDelegateCallBack() {
                    @Override
                    public void onSuccess(int sessionId, int requestId, String data) {
                        isPlay = true;
                    }

                    @Override
                    public void onFailure(int sessionId, int requestId, int errCode) {
                        Log.d(TAG, "start preview onFailure, errCode: " + errCode);
                    }
                });
            }
            if (!mCameraP2P.isConnecting()) {
                IThingIPCCore cameraInstance = ThingIPCSdk.getCameraInstance();
                if (cameraInstance != null && cameraInstance.isLowPowerDevice(devId)) {
                    IThingIPCDoorbell doorbell = ThingIPCSdk.getDoorbell();
                    if (doorbell != null) {
                        doorbell.wirelessWake(devId);
                    }
                }
                mCameraP2P.connect(devId, new OperationDelegateCallBack() {
                    @Override
                    public void onSuccess(int i, int i1, String s) {
                        mHandler.sendMessage(MessageUtil.getMessage(MSG_CONNECT, ARG1_OPERATE_SUCCESS));
                    }

                    @Override
                    public void onFailure(int i, int i1, int i2) {
                        mHandler.sendMessage(MessageUtil.getMessage(MSG_CONNECT, ARG1_OPERATE_FAIL, i2));
                    }
                });
            }
        }
    }

    private final AbsP2pCameraListener p2pCameraListener = new AbsP2pCameraListener() {
        @Override
        public void onReceiveSpeakerEchoData(ByteBuffer pcm, int sampleRate) {
            if (null != mCameraP2P) {
                int length = pcm.capacity();
                Log.d(TAG, "receiveSpeakerEchoData pcmlength " + length + " sampleRate " + sampleRate);
                byte[] pcmData = new byte[length];
                pcm.get(pcmData, 0, length);
                mCameraP2P.sendAudioTalkData(pcmData, length);
            }
        }

        @Override
        public void onSessionStatusChanged(Object camera, int sessionId, int sessionStatus) {
            super.onSessionStatusChanged(camera, sessionId, sessionStatus);
            if (sessionStatus == -3 || sessionStatus == -105) {
                if (!reConnect) {
                    reConnect = true;
                    // 遇到超时/鉴权失败，建议重连一次
                    mCameraP2P.connect(devId, new OperationDelegateCallBack() {
                        @Override
                        public void onSuccess(int i, int i1, String s) {
                            mHandler.sendMessage(MessageUtil.getMessage(MSG_CONNECT, ARG1_OPERATE_SUCCESS));
                        }

                        @Override
                        public void onFailure(int i, int i1, int i2) {
                            mHandler.sendMessage(MessageUtil.getMessage(MSG_CONNECT, ARG1_OPERATE_FAIL, i2));
                        }
                    });
                }
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        mVideoView.onPause();
        if (null != mCameraP2P) {
            if (isSpeaking) {
                mCameraP2P.stopAudioTalk(null);
            }
            if (isPlay) {
                mCameraP2P.stopPreview(new OperationDelegateCallBack() {
                    @Override
                    public void onSuccess(int sessionId, int requestId, String data) {

                    }

                    @Override
                    public void onFailure(int sessionId, int requestId, int errCode) {

                    }
                });
                isPlay = false;
            }
            mCameraP2P.removeOnP2PCameraListener();
            mCameraP2P.disconnect(new OperationDelegateCallBack() {
                @Override
                public void onSuccess(int i, int i1, String s) {

                }

                @Override
                public void onFailure(int i, int i1, int i2) {

                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mHandler) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if (null != mCameraP2P) {
            mCameraP2P.destroyP2P();
        }
    }

    /**
     * 设置智能画框的属性来控制框的样式（如框的颜色，画笔宽度，闪烁频率等），需要设备上报的 SEI 信息支持
     *
     * @param rectFeaturesJson 格式
     *                         {
     *                         "SmartRectFeature":[
     *                         {
     *                         "type":0,
     *                         "index":0,
     *                         "brushWidth":1,
     *                         "flashFps":{
     *                         "drawKeepFrames":2,
     *                         "stopKeepFrames":2
     *                         },
     *                         "rgb":0xFF0000,
     *                         "shape":0
     *                         },
     *                         {
     *                         "type":0,
     *                         "index":1,
     *                         "brushWidth":2,
     *                         "flashFps":{
     *                         "drawKeepFrames":3,
     *                         "stopKeepFrames":2
     *                         },
     *                         "rgb":0x00FF00,
     *                         "shape":1
     *                         }
     *                         ]
     *                         }
     */
    private void setSmartRectFeatures(String rectFeaturesJson) {
        mCameraP2P.setSmartRectFeatures(rectFeaturesJson);
    }

    /**
     * 支持拉伸/缩放，左右/上下镜像，90/180/270度旋转等。
     *
     * @param renderFeaturesJson 格式
     *                           {
     *                           "DecPostProcess":{
     *                           "video":[
     *                           {
     *                           "restype":"4",
     *                           "oldres":"944*1080",
     *                           "newres":"1920*1080"
     *                           },
     *                           {
     *                           "restype":"2",
     *                           "oldres":"944*1080",
     *                           "newres":"1920*1080"
     *                           }
     *                           ],
     *                           "mirror":0,
     *                           "rotation":2
     *                           }
     *                           }
     */
    private void setDeviceFeatures(String renderFeaturesJson) {
        mCameraP2P.setDeviceFeatures(renderFeaturesJson);
    }

    private boolean querySupportByDPID(String dpId) {
        DeviceBean deviceBean = ThingHomeSdk.getDataInstance().getDeviceBean(devId);
        if (deviceBean != null) {
            Map<String, Object> dps = deviceBean.getDps();
            return dps != null && dps.get(dpId) != null;
        }
        return false;
    }

    private IThingDevice iTuyaDevice;

    private void publishDps(String dpId, Object value) {
        if (iTuyaDevice == null) {
            iTuyaDevice = ThingHomeSdk.newDeviceInstance(devId);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(dpId, value);
        String dps = jsonObject.toString();
        iTuyaDevice.publishDps(dps, new IResultCallback() {
            @Override
            public void onError(String code, String error) {
                Log.e(TAG, "publishDps err " + dps);
            }

            @Override
            public void onSuccess() {
                Log.i(TAG, "publishDps suc " + dps);
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_camera_panel, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_remove_device) {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setCancelable(true)
                    .setTitle(getString(R.string.remove_device_dialog))
                    .setPositiveButton(getString(R.string.confirm), (dialog1, which) ->
                            unBindDevice())
                    .create();
            dialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void unBindDevice() {
        ThingHomeSdk.newDeviceInstance(devId).removeDevice(new IResultCallback() {
            @Override
            public void onError(String s, String s1) {
                ToastUtil.shortToast(CameraPanelActivity.this, s1);
            }

            @Override
            public void onSuccess() {
                mHandler.removeCallbacksAndMessages(null);
                CameraPanelActivity.this.finish();
            }
        });
    }
}