package com.tuya.smart.android.demo.camera;

import static com.tuya.smart.android.demo.camera.utils.Constants.INTENT_DEV_ID;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tuya.smart.android.camera.sdk.TuyaIPCSdk;
import com.tuya.smart.android.camera.sdk.api.ITuyaIPCCloud;
import com.tuya.smart.android.camera.sdk.bean.CloudStatusBean;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.camera.adapter.CameraCloudVideoDateAdapter;
import com.tuya.smart.android.demo.camera.adapter.CameraVideoTimeAdapter;
import com.tuya.smart.android.demo.camera.utils.ToastUtil;
import com.tuya.smart.camera.camerasdk.typlayer.callback.AbsP2pCameraListener;
import com.tuya.smart.camera.camerasdk.typlayer.callback.IRegistorIOTCListener;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * @author surgar
 */
public class CameraCloudStorageActivity extends AppCompatActivity {

    private static final String TAG = CameraCloudStorageActivity.class.getSimpleName();
    private TuyaCameraView mVideoView;
    private String devId;
    private ITYCloudCamera cloudCamera;
    private CameraVideoTimeAdapter timeAdapter;
    private CameraCloudVideoDateAdapter dateAdapter;
    private RecyclerView timeRv;
    private RecyclerView dateRv;
    private List<CloudDayBean> dayBeanList = new ArrayList<>();
    private List<TimePieceBean> timePieceBeans = new ArrayList<>();
    private int soundState;
    public static final int SERVES_RUNNING = 10010;
    public static final int SERVES_EXPIRED = 10011;
    public static final int NO_SERVICE = 10001;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_cloud_storage);

        devId = getIntent().getStringExtra(INTENT_DEV_ID);

        initView();

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

        if (cloudCamera != null) {
            //must query cloud service status before use
            cloudCamera.queryCloudServiceStatus(devId, new ITuyaResultCallback<CloudStatusBean>() {
                @Override
                public void onSuccess(CloudStatusBean result) {
                    TextView tv = findViewById(R.id.status_tv);
                    tv.setText(getString(R.string.cloud_status) + getServiceStatus(result.getStatus()));
                    if (result.getStatus() == SERVES_EXPIRED || result.getStatus() == SERVES_RUNNING) {
                        findViewById(R.id.query_btn).setVisibility(View.VISIBLE);
                        findViewById(R.id.ll_bottom).setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onError(String errorCode, String errorMessage) {
                    ToastUtil.shortToast(CameraCloudStorageActivity.this, getString(R.string.operation_failed));
                }
            });
        }

        findViewById(R.id.query_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cloudCamera != null) {
                    TimeZone timeZone = TimeZone.getDefault();
                    cloudCamera.getCloudDays(devId, timeZone.getID(), new ITuyaResultCallback<List<CloudDayBean>>() {
                        @Override
                        public void onSuccess(List<CloudDayBean> result) {
                            if (result == null || result.isEmpty()) {
                                ToastUtil.shortToast(CameraCloudStorageActivity.this, getString(R.string.no_data));
                            } else {
                                dayBeanList.clear();
                                dayBeanList.addAll(result);
                                dateAdapter.notifyDataSetChanged();
                                dateRv.scrollToPosition(dayBeanList.size() - 1);
                                ToastUtil.shortToast(CameraCloudStorageActivity.this, getString(R.string.operation_suc));
                            }
                        }

                        @Override
                        public void onError(String errorCode, String errorMessage) {
                            ToastUtil.shortToast(CameraCloudStorageActivity.this, getString(R.string.operation_failed));
                        }
                    });
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

    private void initView() {
        timeRv = findViewById(R.id.timeRv);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        timeRv.setLayoutManager(mLayoutManager);
        timeRv.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        timeAdapter = new CameraVideoTimeAdapter(this, timePieceBeans);
        timeRv.setAdapter(timeAdapter);
        timeAdapter.setListener(new CameraVideoTimeAdapter.OnTimeItemListener() {
            @Override
            public void onClick(TimePieceBean bean) {
                playCloudDataWithStartTime(bean.getStartTime(), bean.getEndTime(), bean.isEvent());
            }
        });

        dateRv = findViewById(R.id.dateRv);
        LinearLayoutManager mDateLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        dateRv.setLayoutManager(mDateLayoutManager);
        dateAdapter = new CameraCloudVideoDateAdapter(this, dayBeanList);
        dateRv.setAdapter(dateAdapter);
        dateAdapter.setListener(new CameraCloudVideoDateAdapter.OnTimeItemListener() {
            @Override
            public void onClick(CloudDayBean dayBean) {
                getTimeLineInfoByTimeSlice(devId, String.valueOf(dayBean.getCurrentStartDayTime()), String.valueOf(dayBean.getCurrentDayEndTime()));
            }
        });
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
                        timeAdapter.notifyDataSetChanged();
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

    @Override
    protected void onResume() {
        super.onResume();
        mVideoView.onResume();
        if (null != cloudCamera) {
            if (mVideoView.createdView() instanceof IRegistorIOTCListener) {
                cloudCamera.generateCloudCameraView((IRegistorIOTCListener) mVideoView.createdView());
            }
            cloudCamera.registerP2PCameraListener(new AbsP2pCameraListener() {
                @Override
                public void onSessionStatusChanged(Object camera, int sessionId, int sessionStatus) {
                    super.onSessionStatusChanged(camera, sessionId, sessionStatus);
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

    private String getServiceStatus(int code) {
        if (code == SERVES_EXPIRED) {
            return getString(R.string.ipc_sdk_service_expired);
        } else if (code == SERVES_RUNNING) {
            return getString(R.string.ipc_sdk_service_running);
        } else if (code == NO_SERVICE) {
            return getString(R.string.ipc_sdk_no_service);
        } else {
            return String.valueOf(code);
        }
    }
}
