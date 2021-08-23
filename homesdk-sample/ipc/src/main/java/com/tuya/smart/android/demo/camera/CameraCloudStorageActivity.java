package com.tuya.smart.android.demo.camera;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.tuya.smart.android.camera.sdk.TuyaIPCSdk;
import com.tuya.smart.android.camera.sdk.api.ITuyaIPCCloud;
import com.tuya.smart.android.camera.sdk.bean.CloudStatusBean;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.camera.utils.ToastUtil;
import com.tuya.smart.camera.camerasdk.typlayer.callback.IRegistorIOTCListener;
import com.tuya.smart.camera.camerasdk.typlayer.callback.OnP2PCameraListener;
import com.tuya.smart.camera.camerasdk.typlayer.callback.OperationCallBack;
import com.tuya.smart.camera.camerasdk.typlayer.callback.OperationDelegateCallBack;
import com.tuya.smart.camera.ipccamerasdk.cloud.ITYCloudCamera;
import com.tuya.smart.camera.middleware.cloud.bean.CloudDayBean;
import com.tuya.smart.camera.middleware.cloud.bean.TimePieceBean;
import com.tuya.smart.camera.middleware.cloud.bean.TimeRangeBean;
import com.tuya.smart.camera.middleware.widget.AbsVideoViewCallback;
import com.tuya.smart.camera.middleware.widget.TuyaCameraView;
import com.tuya.smart.home.sdk.callback.ITuyaResultCallback;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.tuya.smart.android.demo.camera.utils.Constants.INTENT_DEV_ID;
import static com.tuya.smart.android.demo.camera.utils.Constants.INTENT_P2P_TYPE;

/**
 * @author surgar
 */
public class CameraCloudStorageActivity extends AppCompatActivity {

    private static final String TAG = CameraCloudStorageActivity.class.getSimpleName();
    private TuyaCameraView mVideoView;
    private String devId;
    private ITYCloudCamera cloudCamera;
    private List<CloudDayBean> dayBeanList = new ArrayList<>();
    private List<TimePieceBean> timePieceBeans = new ArrayList<>();
    private int soundState;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_cloud_storage);

        devId = getIntent().getStringExtra(INTENT_DEV_ID);

        ITuyaIPCCloud cloud = TuyaIPCSdk.getCloud();
        if (cloud != null) {
            cloudCamera = cloud.createCloudCamera();
        }

        mVideoView = findViewById(R.id.camera_cloud_video_view);
        mVideoView.setViewCallback(new AbsVideoViewCallback() {
            @Override
            public void onCreated(Object o) {
                super.onCreated(o);
                if (o instanceof IRegistorIOTCListener && cloudCamera != null) {
                    cloudCamera.generateCloudCameraView((IRegistorIOTCListener) o);
                }
            }
        });
        mVideoView.createVideoView(devId);

        if (cloudCamera != null) {
            String cachePath = getApplication().getCacheDir().getPath();
            cloudCamera.createCloudDevice(cachePath, devId);
        }

        findViewById(R.id.status_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cloudCamera != null) {
                    cloudCamera.queryCloudServiceStatus(devId, new ITuyaResultCallback<CloudStatusBean>() {
                        @Override
                        public void onSuccess(CloudStatusBean result) {
                            //Get cloud storage status
                            ToastUtil.shortToast(CameraCloudStorageActivity.this, getString(R.string.current_state) + result.getStatus());
                        }

                        @Override
                        public void onError(String errorCode, String errorMessage) {
                            ToastUtil.shortToast(CameraCloudStorageActivity.this, getString(R.string.err_code) + errorCode);
                        }
                    });
                }
            }
        });

        findViewById(R.id.buy_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //use cloud service purchase component
            }
        });

        findViewById(R.id.query_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cloudCamera != null) {
                    //1. Get device cloud storage-related data
                    cloudCamera.getCloudDays(devId, new ITuyaResultCallback<List<CloudDayBean>>() {
                        @Override
                        public void onSuccess(List<CloudDayBean> result) {
                            if (result == null || result.isEmpty()) {
                                ToastUtil.shortToast(CameraCloudStorageActivity.this, getString(R.string.no_data));
                            } else {
                                dayBeanList.clear();
                                dayBeanList.addAll(result);
                                ToastUtil.shortToast(CameraCloudStorageActivity.this, getString(R.string.operation_suc));
                            }
                        }

                        @Override
                        public void onError(String errorCode, String errorMessage) {
                            ToastUtil.shortToast(CameraCloudStorageActivity.this, getString(R.string.err_code) + errorCode);
                        }
                    });
                }
            }
        });

        findViewById(R.id.query_time_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //2. Get time slice at a specified time
                if (dayBeanList.size() > 0) {
                    getAppointedDayCloudTimes(dayBeanList.get(0));
                } else {
                    ToastUtil.shortToast(CameraCloudStorageActivity.this, getString(R.string.no_data));
                }
            }
        });


        findViewById(R.id.start_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timePieceBeans.size() > 0) {
                    playCloudDataWithStartTime(timePieceBeans.get(0).getStartTime(), timePieceBeans.get(0).getEndTime(), true);
                    ToastUtil.shortToast(CameraCloudStorageActivity.this, getString(R.string.operation_suc));
                } else {
                    ToastUtil.shortToast(CameraCloudStorageActivity.this, getString(R.string.no_data));
                }
            }
        });

        findViewById(R.id.pause_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pausePlayCloudVideo();
            }
        });

        findViewById(R.id.resume_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resumePlayCloudVideo();
            }
        });

        findViewById(R.id.stop_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopPlayCloudVideo();
            }
        });
        findViewById(R.id.camera_mute).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMuteValue(soundState == 0 ? 1 : 0);
            }
        });

        findViewById(R.id.snapshot_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snapshot();
            }
        });
        findViewById(R.id.record_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCloudRecordLocalMP4();
            }
        });
        findViewById(R.id.record_end).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopCloudRecordLocalMP4();
            }
        });
    }

    private void getAppointedDayCloudTimes(CloudDayBean dayBean) {
        if (dayBean == null) {
            return;
        }
        getTimeLineInfoByTimeSlice(devId, String.valueOf(dayBean.getCurrentStartDayTime()), String.valueOf(dayBean.getCurrentDayEndTime()));
    }

    /**
     * Get the time slice of the specified time.
     *
     * @param devId  Device id.
     * @param timeGT Start time.
     * @param timeLT End time.
     */
    void getTimeLineInfoByTimeSlice(String devId, String timeGT, String timeLT) {
        if (cloudCamera != null) {
            cloudCamera.getTimeLineInfo(devId, Long.parseLong(timeGT), Long.parseLong(timeLT), new ITuyaResultCallback<List<TimePieceBean>>() {
                @Override
                public void onSuccess(List<TimePieceBean> result) {
                    if (result == null || result.isEmpty()) {
                        ToastUtil.shortToast(CameraCloudStorageActivity.this, getString(R.string.no_data));
                    } else {
                        timePieceBeans.clear();
                        timePieceBeans.addAll(result);
                        ToastUtil.shortToast(CameraCloudStorageActivity.this, getString(R.string.operation_suc));
                    }
                }

                @Override
                public void onError(String errorCode, String errorMessage) {
                    ToastUtil.shortToast(CameraCloudStorageActivity.this, getString(R.string.err_code) + errorCode);
                }
            });
        }
    }

    /**
     * Obtain the corresponding motion detection data according to the beginning and end of the time segment.
     *
     * @param devId  Device id.
     * @param timeGT Start time.
     * @param timeLT End time.
     * @param offset Which page, default 0
     * @param limit  The number of items pulled each time, the default is -1, which means all data
     */
    void getMotionDetectionByTimeSlice(String devId, final String timeGT, final String timeLT, int offset, int limit) {
        if (cloudCamera != null) {
            cloudCamera.getMotionDetectionInfo(devId, Long.parseLong(timeGT), Long.parseLong(timeLT), offset, limit, new ITuyaResultCallback<List<TimeRangeBean>>() {
                @Override
                public void onSuccess(List<TimeRangeBean> result) {

                }

                @Override
                public void onError(String errorCode, String errorMessage) {

                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVideoView.onResume();
        if (null != cloudCamera) {
            if (mVideoView.createdView() instanceof IRegistorIOTCListener) {
                cloudCamera.generateCloudCameraView((IRegistorIOTCListener) mVideoView.createdView());
            }
            cloudCamera.registorOnP2PCameraListener(new OnP2PCameraListener() {
                @Override
                public void receiveFrameDataForMediaCodec(int i, byte[] bytes, int i1, int i2, byte[] bytes1, boolean b, int i3) {

                }

                @Override
                public void onReceiveFrameYUVData(int i, ByteBuffer byteBuffer, ByteBuffer byteBuffer1, ByteBuffer byteBuffer2, int i1, int i2, int i3, int i4, long l, long l1, long l2, Object o) {

                }

                @Override
                public void onSessionStatusChanged(Object o, int i, int i1) {

                }

                @Override
                public void onReceiveAudioBufferData(int i, int i1, int i2, long l, long l1, long l2) {

                }

                @Override
                public void onReceiveSpeakerEchoData(ByteBuffer byteBuffer, int i) {

                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVideoView.onPause();
        if (null != cloudCamera) {
            cloudCamera.removeOnP2PCameraListener();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != cloudCamera) {
            cloudCamera.destroy();
            cloudCamera.deinitCloudCamera();
        }
    }

    long getTodayEnd(long currentTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(currentTime));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        return calendar.getTimeInMillis();
    }

    private void playCloudDataWithStartTime(int startTime, int endTime, final boolean isEvent) {
        if (cloudCamera != null) {
            cloudCamera.playCloudDataWithStartTime(startTime, endTime, isEvent,
                    new OperationCallBack() {
                        @Override
                        public void onSuccess(int sessionId, int requestId, String data, Object camera) {
                            //playing
                        }

                        @Override
                        public void onFailure(int sessionId, int requestId, int errCode, Object camera) {

                        }
                    }, new OperationCallBack() {
                        @Override
                        public void onSuccess(int sessionId, int requestId, String data, Object camera) {
                            //playCompleted
                        }

                        @Override
                        public void onFailure(int sessionId, int requestId, int errCode, Object camera) {
                        }
                    });
        }
    }

    /**
     * play resume
     */
    private void resumePlayCloudVideo() {
        if (cloudCamera != null) {
            cloudCamera.resumePlayCloudVideo(new OperationDelegateCallBack() {
                @Override
                public void onSuccess(int sessionId, int requestId, String data) {
                }

                @Override
                public void onFailure(int sessionId, int requestId, int errCode) {
                    ToastUtil.shortToast(CameraCloudStorageActivity.this, getString(R.string.operation_failed));
                }
            });
        }
    }

    /**
     * play pause
     */
    private void pausePlayCloudVideo() {
        if (cloudCamera != null) {
            cloudCamera.pausePlayCloudVideo(new OperationDelegateCallBack() {
                @Override
                public void onSuccess(int sessionId, int requestId, String data) {
                }

                @Override
                public void onFailure(int sessionId, int requestId, int errCode) {
                    ToastUtil.shortToast(CameraCloudStorageActivity.this, getString(R.string.operation_failed));
                }
            });
        }
    }

    /**
     * play stop
     */
    private void stopPlayCloudVideo() {
        if (cloudCamera != null) {
            cloudCamera.stopPlayCloudVideo(new OperationDelegateCallBack() {
                @Override
                public void onSuccess(int sessionId, int requestId, String data) {
                }

                @Override
                public void onFailure(int sessionId, int requestId, int errCode) {
                    ToastUtil.shortToast(CameraCloudStorageActivity.this, getString(R.string.operation_failed));
                }
            });
        }
    }

    /**
     * record start
     */
    public void startCloudRecordLocalMP4() {
        if (cloudCamera != null) {
            String path = getExternalFilesDir(null).getPath() + "/" + devId;
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
            cloudCamera.startRecordLocalMp4(path, System.currentTimeMillis() + ".mp4", new OperationDelegateCallBack() {
                @Override
                public void onSuccess(int sessionId, int requestId, String data) {
                    ToastUtil.shortToast(CameraCloudStorageActivity.this, getString(R.string.operation_suc));
                }

                @Override
                public void onFailure(int sessionId, int requestId, int errCode) {
                    ToastUtil.shortToast(CameraCloudStorageActivity.this, getString(R.string.operation_failed));
                }
            });
        }
    }

    /**
     * record stop
     */
    public void stopCloudRecordLocalMP4() {
        if (cloudCamera != null) {
            cloudCamera.stopRecordLocalMp4(new OperationDelegateCallBack() {
                @Override
                public void onSuccess(int sessionId, int requestId, String data) {
                    ToastUtil.shortToast(CameraCloudStorageActivity.this, getString(R.string.operation_suc));
                }

                @Override
                public void onFailure(int sessionId, int requestId, int errCode) {
                    ToastUtil.shortToast(CameraCloudStorageActivity.this, getString(R.string.operation_failed));
                }
            });
        }
    }

    public void snapshot() {
        if (cloudCamera != null) {
            String path = getExternalFilesDir(null).getPath() + "/" + devId;
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
            cloudCamera.snapshot(path, new OperationDelegateCallBack() {
                @Override
                public void onSuccess(int sessionId, int requestId, String data) {
                    ToastUtil.shortToast(CameraCloudStorageActivity.this, getString(R.string.operation_suc));
                }

                @Override
                public void onFailure(int sessionId, int requestId, int errCode) {
                    ToastUtil.shortToast(CameraCloudStorageActivity.this, getString(R.string.operation_failed));
                }
            });
        }
    }

    public void setMuteValue(int mute) {
        if (cloudCamera != null) {
            cloudCamera.setCloudMute(mute, new OperationDelegateCallBack() {
                @Override
                public void onSuccess(int sessionId, int requestId, String data) {
                    soundState = Integer.valueOf(data);
                }

                @Override
                public void onFailure(int sessionId, int requestId, int errCode) {
                    ToastUtil.shortToast(CameraCloudStorageActivity.this, getString(R.string.operation_failed));
                }
            });
        }
    }

    public void getMuteValue() {
        if (cloudCamera != null) {
            cloudCamera.getCloudMute();
        }
    }

}
