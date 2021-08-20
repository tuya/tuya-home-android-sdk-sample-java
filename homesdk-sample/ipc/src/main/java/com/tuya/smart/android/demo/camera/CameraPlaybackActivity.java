package com.tuya.smart.android.demo.camera;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.tuya.smart.android.camera.sdk.TuyaIPCSdk;
import com.tuya.smart.android.camera.sdk.api.ITuyaIPCCore;
import com.tuya.smart.android.camera.timeline.OnBarMoveListener;
import com.tuya.smart.android.camera.timeline.OnSelectedTimeListener;
import com.tuya.smart.android.camera.timeline.TimeBean;
import com.tuya.smart.android.camera.timeline.TuyaTimelineView;
import com.tuya.smart.android.common.utils.L;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.camera.bean.RecordInfoBean;
import com.tuya.smart.android.demo.camera.bean.TimePieceBean;
import com.tuya.smart.android.demo.camera.utils.MessageUtil;
import com.tuya.smart.android.demo.camera.utils.ToastUtil;
import com.tuya.smart.camera.camerasdk.typlayer.callback.AbsP2pCameraListener;
import com.tuya.smart.camera.camerasdk.typlayer.callback.OperationDelegateCallBack;
import com.tuya.smart.camera.ipccamerasdk.bean.MonthDays;
import com.tuya.smart.camera.ipccamerasdk.p2p.ICameraP2P;
import com.tuya.smart.camera.middleware.p2p.ITuyaSmartCameraP2P;
import com.tuya.smart.camera.middleware.widget.AbsVideoViewCallback;
import com.tuya.smart.camera.middleware.widget.TuyaCameraView;
import com.tuya.smart.camera.utils.AudioUtils;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tuya.smart.android.demo.camera.utils.Constants.ARG1_OPERATE_FAIL;
import static com.tuya.smart.android.demo.camera.utils.Constants.ARG1_OPERATE_SUCCESS;
import static com.tuya.smart.android.demo.camera.utils.Constants.INTENT_DEV_ID;
import static com.tuya.smart.android.demo.camera.utils.Constants.MSG_DATA_DATE;
import static com.tuya.smart.android.demo.camera.utils.Constants.MSG_DATA_DATE_BY_DAY_FAIL;
import static com.tuya.smart.android.demo.camera.utils.Constants.MSG_DATA_DATE_BY_DAY_SUCC;
import static com.tuya.smart.android.demo.camera.utils.Constants.MSG_MUTE;


/**
 * @author chenbj
 */
public class CameraPlaybackActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "CameraPlaybackActivity";
    private Toolbar toolbar;
    private TuyaCameraView mVideoView;
    private ImageView muteImg;
    private EditText dateInputEdt;
    private RecyclerView queryRv;
    private TuyaTimelineView timelineView;
    private Button queryBtn, startBtn, pauseBtn, resumeBtn, stopBtn;

    private ITuyaSmartCameraP2P mCameraP2P;
    private static final int ASPECT_RATIO_WIDTH = 9;
    private static final int ASPECT_RATIO_HEIGHT = 16;
    private String devId;
    private CameraPlaybackTimeAdapter adapter;
    private List<TimePieceBean> queryDateList;

    private boolean isPlayback = false;

    protected Map<String, List<String>> mBackDataMonthCache;
    protected Map<String, List<TimePieceBean>> mBackDataDayCache;
    private int mPlaybackMute = ICameraP2P.MUTE;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_MUTE:
                    handleMute(msg);
                    break;
                case MSG_DATA_DATE:
                    handleDataDate(msg);
                    break;
                case MSG_DATA_DATE_BY_DAY_SUCC:
                case MSG_DATA_DATE_BY_DAY_FAIL:
                    handleDataDay(msg);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void handleDataDay(Message msg) {
        if (msg.arg1 == ARG1_OPERATE_SUCCESS) {
            queryDateList.clear();
            //Timepieces with data for the query day
            List<TimePieceBean> timePieceBeans = mBackDataDayCache.get(mCameraP2P.getDayKey());
            if (timePieceBeans != null) {
                queryDateList.addAll(timePieceBeans);
                List<TimeBean> timelineData = new ArrayList<>();
                for(TimePieceBean bean: timePieceBeans) {
                    TimeBean b = new TimeBean();
                    b.setStartTime(bean.getStartTime());
                    b.setEndTime(bean.getEndTime());
                    timelineData.add(b);
                }
                timelineView.setCurrentTimeConfig(timePieceBeans.get(0).getEndTime()*1000L);
                timelineView.setRecordDataExistTimeClipsList(timelineData);
            } else {
                showErrorToast();
            }
            adapter.notifyDataSetChanged();
        } else {

        }
    }

    private void handleDataDate(Message msg) {
        if (msg.arg1 == ARG1_OPERATE_SUCCESS) {
            List<String> days = mBackDataMonthCache.get(mCameraP2P.getMonthKey());

            try {
                if (days.size() == 0) {
                    showErrorToast();
                    return;
                }
                final String inputStr = dateInputEdt.getText().toString();
                if (!TextUtils.isEmpty(inputStr) && inputStr.contains("/")) {
                    String[] substring = inputStr.split("/");
                    int year = Integer.parseInt(substring[0]);
                    int mouth = Integer.parseInt(substring[1]);
                    int day = Integer.parseInt(substring[2]);
                    mCameraP2P.queryRecordTimeSliceByDay(year, mouth, day, new OperationDelegateCallBack() {
                        @Override
                        public void onSuccess(int sessionId, int requestId, String data) {
                            L.e(TAG, inputStr + " --- " + data);
                            parsePlaybackData(data);
                        }

                        @Override
                        public void onFailure(int sessionId, int requestId, int errCode) {
                            mHandler.sendEmptyMessage(MSG_DATA_DATE_BY_DAY_FAIL);
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {

        }
    }

    private void parsePlaybackData(Object obj) {
        RecordInfoBean recordInfoBean = JSONObject.parseObject(obj.toString(), RecordInfoBean.class);
        if (recordInfoBean.getCount() != 0) {
            List<TimePieceBean> timePieceBeanList = recordInfoBean.getItems();
            if (timePieceBeanList != null && timePieceBeanList.size() != 0) {
                mBackDataDayCache.put(mCameraP2P.getDayKey(), timePieceBeanList);
            }
            mHandler.sendMessage(MessageUtil.getMessage(MSG_DATA_DATE_BY_DAY_SUCC, ARG1_OPERATE_SUCCESS));
        } else {
            mHandler.sendMessage(MessageUtil.getMessage(MSG_DATA_DATE_BY_DAY_FAIL, ARG1_OPERATE_FAIL));
        }
    }

    private void handleMute(Message msg) {
        if (msg.arg1 == ARG1_OPERATE_SUCCESS) {
            muteImg.setSelected(mPlaybackMute == ICameraP2P.MUTE);
        } else {
            ToastUtil.shortToast(CameraPlaybackActivity.this, getString(R.string.operation_failed));
        }
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_playback);
        initView();
        initData();
        initListener();
    }

    private void initView() {
        toolbar = findViewById(R.id.toolbar_view);
        timelineView = findViewById(R.id.timeline);

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mVideoView = findViewById(R.id.camera_video_view);
        muteImg = findViewById(R.id.camera_mute);
        dateInputEdt = findViewById(R.id.date_input_edt);
        queryBtn = findViewById(R.id.query_btn);
        startBtn = findViewById(R.id.start_btn);
        pauseBtn = findViewById(R.id.pause_btn);
        resumeBtn = findViewById(R.id.resume_btn);
        stopBtn = findViewById(R.id.stop_btn);
        queryRv = findViewById(R.id.query_list);

        //It is best to set the aspect ratio to 16:9
        WindowManager windowManager = (WindowManager) this.getSystemService(WINDOW_SERVICE);
        int width = windowManager.getDefaultDisplay().getWidth();
        int height = width * ASPECT_RATIO_WIDTH / ASPECT_RATIO_HEIGHT;
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
        layoutParams.addRule(RelativeLayout.BELOW, R.id.toolbar_view);
        findViewById(R.id.camera_video_view_Rl).setLayoutParams(layoutParams);

        timelineView.setOnBarMoveListener(new OnBarMoveListener() {
            @Override
            public void onBarMove(long l, long l1, long l2) {

            }

            @Override
            public void onBarMoveFinish(long startTime, long endTime, long currentTime) {
                timelineView.setCanQueryData();
                timelineView.setQueryNewVideoData(false);
                if (startTime != -1 && endTime != -1) {
                    playback((int)startTime, (int)endTime, (int)currentTime);
                }
            }

            @Override
            public void onBarActionDown() {

            }
        });
        timelineView.setOnSelectedTimeListener(new OnSelectedTimeListener() {
            @Override
            public void onDragging(long selectStartTime, long selectEndTime) {

            }
        });
    }

    private void initData() {
        mBackDataMonthCache = new HashMap<>();
        mBackDataDayCache = new HashMap<>();
        devId = getIntent().getStringExtra(INTENT_DEV_ID);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        queryRv.setLayoutManager(mLayoutManager);
        queryRv.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        queryDateList = new ArrayList<>();
        adapter = new CameraPlaybackTimeAdapter(this, queryDateList);
        queryRv.setAdapter(adapter);

        ITuyaIPCCore cameraInstance = TuyaIPCSdk.getCameraInstance();
        if (cameraInstance != null) {
            mCameraP2P = cameraInstance.createCameraP2P(devId);
        }
        mVideoView.setViewCallback(new AbsVideoViewCallback() {
            @Override
            public void onCreated(Object o) {
                super.onCreated(o);
                if (mCameraP2P != null) {
                    mCameraP2P.generateCameraView(mVideoView.createdView());
                }
            }
        });
        mVideoView.createVideoView(devId);
        if (!mCameraP2P.isConnecting()) {
            mCameraP2P.connect(devId, new OperationDelegateCallBack() {
                @Override
                public void onSuccess(int i, int i1, String s) {

                }

                @Override
                public void onFailure(int i, int i1, int i2) {

                }
            });
        }

        muteImg.setSelected(true);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        Date date = new Date(System.currentTimeMillis());
        dateInputEdt.setText(simpleDateFormat.format(date));
    }

    private void initListener() {
        muteImg.setOnClickListener(this);
        queryBtn.setOnClickListener(this);
        startBtn.setOnClickListener(this);
        pauseBtn.setOnClickListener(this);
        resumeBtn.setOnClickListener(this);
        stopBtn.setOnClickListener(this);
        adapter.setListener(new CameraPlaybackTimeAdapter.OnTimeItemListener() {
            @Override
            public void onClick(TimePieceBean timePieceBean) {
                playback(timePieceBean.getStartTime(), timePieceBean.getEndTime(), timePieceBean.getStartTime());
            }
        });
    }

    private void playback(int startTime, int endTime, int playTime) {
        mCameraP2P.startPlayBack(startTime,
                endTime,
                playTime, new OperationDelegateCallBack() {
                    @Override
                    public void onSuccess(int sessionId, int requestId, String data) {
                        isPlayback = true;
                    }

                    @Override
                    public void onFailure(int sessionId, int requestId, int errCode) {
                        isPlayback = false;
                    }
                }, new OperationDelegateCallBack() {
                    @Override
                    public void onSuccess(int sessionId, int requestId, String data) {
                        isPlayback = false;
                    }

                    @Override
                    public void onFailure(int sessionId, int requestId, int errCode) {
                        isPlayback = false;
                    }
                });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.camera_mute) {
            muteClick();
        } else if (id == R.id.query_btn) {
            queryDayByMonthClick();
        } else if (id == R.id.start_btn) {
            startPlayback();
        } else if (id == R.id.pause_btn) {
            pauseClick();
        } else if (id == R.id.resume_btn) {
            resumeClick();
        } else if (id == R.id.stop_btn) {
            stopClick();
        }
    }

    private void startPlayback() {
        if (null != queryDateList && queryDateList.size() > 0) {
            TimePieceBean timePieceBean = queryDateList.get(0);
            if (null != timePieceBean) {
                mCameraP2P.startPlayBack(timePieceBean.getStartTime(), timePieceBean.getEndTime(), timePieceBean.getStartTime(), new OperationDelegateCallBack() {
                    @Override
                    public void onSuccess(int sessionId, int requestId, String data) {
                        isPlayback = true;
                    }

                    @Override
                    public void onFailure(int sessionId, int requestId, int errCode) {

                    }
                }, new OperationDelegateCallBack() {
                    @Override
                    public void onSuccess(int sessionId, int requestId, String data) {
                        isPlayback = false;
                    }

                    @Override
                    public void onFailure(int sessionId, int requestId, int errCode) {

                    }
                });
            }
        } else {
            ToastUtil.shortToast(this, getString(R.string.no_data));
        }
    }

    private void stopClick() {
        mCameraP2P.stopPlayBack(new OperationDelegateCallBack() {
            @Override
            public void onSuccess(int sessionId, int requestId, String data) {

            }

            @Override
            public void onFailure(int sessionId, int requestId, int errCode) {

            }
        });
        isPlayback = false;
    }

    private void resumeClick() {
        mCameraP2P.resumePlayBack(new OperationDelegateCallBack() {
            @Override
            public void onSuccess(int sessionId, int requestId, String data) {
                isPlayback = true;
            }

            @Override
            public void onFailure(int sessionId, int requestId, int errCode) {

            }
        });
    }

    private void pauseClick() {
        mCameraP2P.pausePlayBack(new OperationDelegateCallBack() {
            @Override
            public void onSuccess(int sessionId, int requestId, String data) {
                isPlayback = false;
            }

            @Override
            public void onFailure(int sessionId, int requestId, int errCode) {

            }
        });
    }

    private void queryDayByMonthClick() {
        if (!mCameraP2P.isConnecting()) {
            ToastUtil.shortToast(CameraPlaybackActivity.this, getString(R.string.connect_first));
            return;
        }
        String inputStr = dateInputEdt.getText().toString();
        if (TextUtils.isEmpty(inputStr)) {
            return;
        }
        if (inputStr.contains("/")) {
            String[] substring = inputStr.split("/");
            if (substring.length > 2) {
                try {
                    int year = Integer.parseInt(substring[0]);
                    int mouth = Integer.parseInt(substring[1]);
                    mCameraP2P.queryRecordDaysByMonth(year, mouth, new OperationDelegateCallBack() {
                        @Override
                        public void onSuccess(int sessionId, int requestId, String data) {
                            MonthDays monthDays = JSONObject.parseObject(data, MonthDays.class);
                            mBackDataMonthCache.put(mCameraP2P.getMonthKey(), monthDays.getDataDays());
                            L.e(TAG,   "MonthDays --- " + data);

                            mHandler.sendMessage(MessageUtil.getMessage(MSG_DATA_DATE, ARG1_OPERATE_SUCCESS));
                        }

                        @Override
                        public void onFailure(int sessionId, int requestId, int errCode) {
                            mHandler.sendMessage(MessageUtil.getMessage(MSG_DATA_DATE, ARG1_OPERATE_FAIL));
                        }
                    });
                } catch (Exception e) {
                    ToastUtil.shortToast(CameraPlaybackActivity.this, getString(R.string.input_err));
                }
            }
        }
    }

    private void muteClick() {
        int mute;
        mute = mPlaybackMute == ICameraP2P.MUTE ? ICameraP2P.UNMUTE : ICameraP2P.MUTE;
        mCameraP2P.setMute(mute, new OperationDelegateCallBack() {
            @Override
            public void onSuccess(int sessionId, int requestId, String data) {
                mPlaybackMute = Integer.valueOf(data);
                mHandler.sendMessage(MessageUtil.getMessage(MSG_MUTE, ARG1_OPERATE_SUCCESS));
            }

            @Override
            public void onFailure(int sessionId, int requestId, int errCode) {
                mHandler.sendMessage(MessageUtil.getMessage(MSG_MUTE, ARG1_OPERATE_FAIL));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVideoView.onResume();
        if (null != mCameraP2P) {
            AudioUtils.getModel(this);
            mCameraP2P.registerP2PCameraListener(p2pCameraListener);
            mCameraP2P.generateCameraView(mVideoView.createdView());
        }
    }

    private AbsP2pCameraListener p2pCameraListener = new AbsP2pCameraListener() {
        @Override
        public void onReceiveFrameYUVData(int i, ByteBuffer byteBuffer, ByteBuffer byteBuffer1, ByteBuffer byteBuffer2, int i1, int i2, int i3, int i4, long l, long l1, long l2, Object o) {
            super.onReceiveFrameYUVData(i, byteBuffer, byteBuffer1, byteBuffer2, i1, i2, i3, i4, l, l1, l2, o);
            timelineView.setCurrentTimeInMillisecond(l*1000L);
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        mVideoView.onPause();
        if (isPlayback) {
            mCameraP2P.stopPlayBack(null);
        }
        if (null != mCameraP2P) {
            mCameraP2P.removeOnP2PCameraListener();
            if (isFinishing()) {
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
        AudioUtils.changeToNomal(this);
    }


    private void showErrorToast() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtil.shortToast(CameraPlaybackActivity.this, getString(R.string.no_data));
            }
        });
    }

}