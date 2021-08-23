package com.tuya.smart.android.demo.camera;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tuya.smart.android.camera.sdk.TuyaIPCSdk;
import com.tuya.smart.android.camera.sdk.api.ITYCameraMessage;
import com.tuya.smart.android.camera.sdk.api.ITuyaIPCMsg;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.camera.adapter.AlarmDetectionAdapter;
import com.tuya.smart.android.demo.camera.utils.DateUtils;
import com.tuya.smart.android.demo.camera.utils.MessageUtil;
import com.tuya.smart.android.demo.camera.utils.ToastUtil;
import com.tuya.smart.home.sdk.callback.ITuyaResultCallback;
import com.tuya.smart.ipc.messagecenter.bean.CameraMessageBean;
import com.tuya.smart.ipc.messagecenter.bean.CameraMessageClassifyBean;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.tuya.smart.android.demo.camera.utils.Constants.ALARM_DETECTION_DATE_MONTH_FAILED;
import static com.tuya.smart.android.demo.camera.utils.Constants.ALARM_DETECTION_DATE_MONTH_SUCCESS;
import static com.tuya.smart.android.demo.camera.utils.Constants.ARG1_OPERATE_FAIL;
import static com.tuya.smart.android.demo.camera.utils.Constants.ARG1_OPERATE_SUCCESS;
import static com.tuya.smart.android.demo.camera.utils.Constants.INTENT_DEV_ID;
import static com.tuya.smart.android.demo.camera.utils.Constants.MOTION_CLASSIFY_FAILED;
import static com.tuya.smart.android.demo.camera.utils.Constants.MOTION_CLASSIFY_SUCCESS;
import static com.tuya.smart.android.demo.camera.utils.Constants.MSG_DELETE_ALARM_DETECTION;
import static com.tuya.smart.android.demo.camera.utils.Constants.MSG_GET_ALARM_DETECTION;

/**
 * huangdaju
 * 2019-11-19
 **/

public class AlarmDetectionActivity extends AppCompatActivity implements View.OnClickListener {
    private String devId;
    private List<CameraMessageBean> mWaitingDeleteCameraMessageList;
    protected List<CameraMessageBean> mCameraMessageList;
    private CameraMessageClassifyBean selectClassify;
    private EditText dateInputEdt;
    private RecyclerView queryRv;
    private Button queryBtn;
    private AlarmDetectionAdapter adapter;
    private int day, year, month;
    private int offset = 0;
    private ITYCameraMessage mTyCameraMessage;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ALARM_DETECTION_DATE_MONTH_FAILED:
                    handlAlarmDetectionDateFail(msg);
                    break;
                case ALARM_DETECTION_DATE_MONTH_SUCCESS:
                    handlAlarmDetectionDateSuccess(msg);
                    break;
                case MSG_GET_ALARM_DETECTION:
                    handleAlarmDetection();
                    break;
                case MSG_DELETE_ALARM_DETECTION:
                    handleDeleteAlarmDetection();
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void handleDeleteAlarmDetection() {
        mCameraMessageList.removeAll(mWaitingDeleteCameraMessageList);
        adapter.updateAlarmDetectionMessage(mCameraMessageList);
        adapter.notifyDataSetChanged();
    }

    private void handleAlarmDetection() {
        adapter.updateAlarmDetectionMessage(mCameraMessageList);
        adapter.notifyDataSetChanged();
    }

    private void handlAlarmDetectionDateFail(Message msg) {

    }

    private void handlAlarmDetectionDateSuccess(Message msg) {
        if (null != mTyCameraMessage && selectClassify != null) {
            long time = DateUtils.getCurrentTime(year, month, day);
            int startTime = DateUtils.getTodayStart(time);
            int endTime = DateUtils.getTodayEnd(time) - 1;
            mTyCameraMessage.getAlarmDetectionMessageList(devId, startTime, endTime, selectClassify.getMsgCode(), offset, 30, new ITuyaResultCallback<List<CameraMessageBean>>() {
                @Override
                public void onSuccess(List<CameraMessageBean> result) {
                    if (result != null) {
                        offset += result.size();
                        mCameraMessageList = result;
                        mHandler.sendMessage(MessageUtil.getMessage(MSG_GET_ALARM_DETECTION, ARG1_OPERATE_SUCCESS));
                    } else {
                        mHandler.sendMessage(MessageUtil.getMessage(MSG_GET_ALARM_DETECTION, ARG1_OPERATE_FAIL));
                    }
                }

                @Override
                public void onError(String errorCode, String errorMessage) {
                    mHandler.sendMessage(MessageUtil.getMessage(MSG_GET_ALARM_DETECTION, ARG1_OPERATE_FAIL));
                }
            });
        }
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_message);
        devId = getIntent().getStringExtra(INTENT_DEV_ID);
        initView();
        initData();
        initListener();
    }

    private void initListener() {
        queryBtn.setOnClickListener(this);
    }

    private void initView() {
        dateInputEdt = findViewById(R.id.date_input_edt);
        queryBtn = findViewById(R.id.query_btn);
        queryRv = findViewById(R.id.query_list);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        Date date = new Date(System.currentTimeMillis());
        dateInputEdt.setHint(simpleDateFormat.format(date));
        dateInputEdt.setText(simpleDateFormat.format(date));
    }

    private void initData() {
        mWaitingDeleteCameraMessageList = new ArrayList<>();
        mCameraMessageList = new ArrayList<>();
        ITuyaIPCMsg message = TuyaIPCSdk.getMessage();
        if (message != null) {
            mTyCameraMessage = message.createCameraMessage();
        }
        queryCameraMessageClassify(devId);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        queryRv.setLayoutManager(mLayoutManager);
        queryRv.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        adapter = new AlarmDetectionAdapter(this, mCameraMessageList);
        adapter.setListener(new AlarmDetectionAdapter.OnItemListener() {
            @Override
            public void onLongClick(CameraMessageBean o) {
                deleteCameraMessageClassify(o);
            }

            @Override
            public void onItemClick(CameraMessageBean o) {
                //if type is video, jump to CameraCloudVideoActivity
                if (o.getAttachVideos() != null && o.getAttachVideos().length > 0) {
                    Intent intent = new Intent(AlarmDetectionActivity.this, CameraCloudVideoActivity.class);
                    String attachVideo = o.getAttachVideos()[0];
                    String playUrl = attachVideo.substring(0, attachVideo.lastIndexOf('@'));
                    String encryptKey = attachVideo.substring(attachVideo.lastIndexOf('@') + 1);
                    intent.putExtra("playUrl", playUrl);
                    intent.putExtra("encryptKey", encryptKey);
                    intent.putExtra("devId",devId);
                    startActivity(intent);
                }
            }
        });
        queryRv.setAdapter(adapter);
    }

    public void queryCameraMessageClassify(String devId) {
        if (mTyCameraMessage != null) {
            mTyCameraMessage.queryAlarmDetectionClassify(devId, new ITuyaResultCallback<List<CameraMessageClassifyBean>>() {
                @Override
                public void onSuccess(List<CameraMessageClassifyBean> result) {
                    selectClassify = result.get(0);
                    mHandler.sendEmptyMessage(MOTION_CLASSIFY_SUCCESS);
                }

                @Override
                public void onError(String errorCode, String errorMessage) {
                    mHandler.sendEmptyMessage(MOTION_CLASSIFY_FAILED);
                }
            });
        }
    }


    public void deleteCameraMessageClassify(CameraMessageBean cameraMessageBean) {
        mWaitingDeleteCameraMessageList.add(cameraMessageBean);
        if (mTyCameraMessage != null) {
            List<String> ids = new ArrayList<>();
            ids.add(cameraMessageBean.getId());
            mTyCameraMessage.deleteMotionMessageList(ids, new ITuyaResultCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    mCameraMessageList.removeAll(mWaitingDeleteCameraMessageList);
                    mWaitingDeleteCameraMessageList.clear();
                    mHandler.sendMessage(MessageUtil.getMessage(MSG_DELETE_ALARM_DETECTION, ARG1_OPERATE_SUCCESS));
                }

                @Override
                public void onError(String errorCode, String errorMessage) {
                    mHandler.sendMessage(MessageUtil.getMessage(MSG_DELETE_ALARM_DETECTION, ARG1_OPERATE_FAIL));
                }
            });
        }
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.query_btn) {
            queryAlarmDetectionByMonth();
        }
    }

    private void queryAlarmDetectionByMonth() {
        String inputStr = dateInputEdt.getText().toString();
        if (TextUtils.isEmpty(inputStr)) {
            ToastUtil.shortToast(this, getString(R.string.not_input_query_data));
            return;
        }
        String[] substring = inputStr.split("/");
        year = Integer.parseInt(substring[0]);
        month = Integer.parseInt(substring[1]);
        if (mTyCameraMessage != null) {
            mTyCameraMessage.queryMotionDaysByMonth(devId, year, month, new ITuyaResultCallback<List<String>>() {
                @Override
                public void onSuccess(List<String> result) {
                    if (result.size() > 0) {
                        Collections.sort(result);
                        day = Integer.parseInt(result.get(result.size() - 1));
                    }
                    mHandler.sendEmptyMessage(ALARM_DETECTION_DATE_MONTH_SUCCESS);
                }

                @Override
                public void onError(String errorCode, String errorMessage) {
                    mHandler.sendEmptyMessage(ALARM_DETECTION_DATE_MONTH_FAILED);
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
        if (null != mTyCameraMessage) {
            mTyCameraMessage.destroy();
        }
    }
}
