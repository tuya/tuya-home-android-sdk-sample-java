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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.demo.camera.adapter.AlarmDetectionAdapter;
import com.tuya.smart.android.demo.camera.utils.DateUtils;
import com.tuya.smart.android.demo.camera.utils.MessageUtil;
import com.tuya.smart.android.demo.camera.utils.TimeZoneUtils;
import com.tuya.smart.android.demo.camera.utils.ToastUtil;
import com.tuya.smart.android.network.Business;
import com.tuya.smart.android.network.http.BusinessResponse;
import com.tuya.smart.ipc.messagecenter.bean.CameraMessageBean;
import com.tuya.smart.ipc.messagecenter.bean.CameraMessageClassifyBean;
import com.tuya.smart.ipc.messagecenter.business.CameraMessageBusiness;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

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
    private CameraMessageBusiness messageBusiness;
    private CameraMessageClassifyBean selectClassify;
    private EditText dateInputEdt;
    private RecyclerView queryRv;
    private Button queryBtn;
    private AlarmDetectionAdapter adapter;
    private int day, year, month;
    private int offset = 0;

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
        if (null != messageBusiness) {
            long time = DateUtils.getCurrentTime(year, month, day);
            long startTime = DateUtils.getTodayStart(time);
            long endTime = DateUtils.getTodayEnd(time) - 1L;
            JSONObject object = new JSONObject();
            object.put("msgSrcId", devId);
            object.put("startTime", startTime);
            object.put("endTime", endTime);
            object.put("msgType", 4);
            object.put("limit", 30);
            object.put("keepOrig", true);
            object.put("offset", offset);
            if (null != selectClassify) {
                object.put("msgCodes", selectClassify.getMsgCode());
            }
            messageBusiness.getAlarmDetectionMessageList(object.toJSONString(), new Business.ResultListener<JSONObject>() {
                @Override
                public void onFailure(BusinessResponse businessResponse, JSONObject jsonObject, String s) {
                    mHandler.sendMessage(MessageUtil.getMessage(MSG_GET_ALARM_DETECTION, ARG1_OPERATE_FAIL));
                }

                @Override
                public void onSuccess(BusinessResponse businessResponse, JSONObject jsonObject, String s) {
                    List<CameraMessageBean> msgList;
                    try {
                        msgList = JSONArray.parseArray(jsonObject.getString("datas"), CameraMessageBean.class);
                    } catch (Exception e) {
                        msgList = null;
                    }
                    if (msgList != null) {
                        offset += msgList.size();
                        mCameraMessageList = msgList;
                        mHandler.sendMessage(MessageUtil.getMessage(MSG_GET_ALARM_DETECTION, ARG1_OPERATE_SUCCESS));
                    } else {
                        mHandler.sendMessage(MessageUtil.getMessage(MSG_GET_ALARM_DETECTION, ARG1_OPERATE_FAIL));
                    }
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
        messageBusiness = new CameraMessageBusiness();
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
                    startActivity(intent);
                }
            }
        });
        queryRv.setAdapter(adapter);
    }

    public void queryCameraMessageClassify(String devId) {
        if (messageBusiness != null) {
            messageBusiness.queryAlarmDetectionClassify(devId, new Business.ResultListener<ArrayList<CameraMessageClassifyBean>>() {
                @Override
                public void onFailure(BusinessResponse businessResponse, ArrayList<CameraMessageClassifyBean> cameraMessageClassifyBeans, String s) {
                    mHandler.sendEmptyMessage(MOTION_CLASSIFY_FAILED);
                }

                @Override
                public void onSuccess(BusinessResponse businessResponse, ArrayList<CameraMessageClassifyBean> cameraMessageClassifyBeans, String s) {
                    selectClassify = cameraMessageClassifyBeans.get(0);
                    mHandler.sendEmptyMessage(MOTION_CLASSIFY_SUCCESS);
                }
            });
        }
    }


    public void deleteCameraMessageClassify(CameraMessageBean cameraMessageBean) {
        mWaitingDeleteCameraMessageList.add(cameraMessageBean);
//        StringBuilder ids = new StringBuilder();
        if (messageBusiness != null) {
//            for (int i = 0; i < mWaitingDeleteCameraMessageList.size(); i++) {
//                ids.append(mWaitingDeleteCameraMessageList.get(i).getId());
//                if (i != mWaitingDeleteCameraMessageList.size() - 1) {
//                    ids.append(",");
//                }
//            }
            messageBusiness.deleteAlarmDetectionMessageList(cameraMessageBean.getId(), new Business.ResultListener<Boolean>() {
                @Override
                public void onFailure(BusinessResponse businessResponse, Boolean aBoolean, String s) {
                    mHandler.sendMessage(MessageUtil.getMessage(MSG_DELETE_ALARM_DETECTION, ARG1_OPERATE_FAIL));
                }

                @Override
                public void onSuccess(BusinessResponse businessResponse, Boolean aBoolean, String s) {
                    mCameraMessageList.removeAll(mWaitingDeleteCameraMessageList);
                    mWaitingDeleteCameraMessageList.clear();
                    mHandler.sendMessage(MessageUtil.getMessage(MSG_DELETE_ALARM_DETECTION, ARG1_OPERATE_SUCCESS));
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
        JSONObject object = new JSONObject();
        object.put("msgSrcId", devId);
        object.put("timeZone", TimeZoneUtils.getTimezoneGCMById(TimeZone.getDefault().getID()));
        object.put("month", year + "-" + month);
        messageBusiness.queryAlarmDetectionDaysByMonth(object.toJSONString(),
                new Business.ResultListener<JSONArray>() {
                    @Override
                    public void onFailure(BusinessResponse businessResponse, JSONArray objects, String s) {
                        mHandler.sendEmptyMessage(ALARM_DETECTION_DATE_MONTH_FAILED);
                    }

                    @Override
                    public void onSuccess(BusinessResponse businessResponse, JSONArray objects, String s) {
                        List<Integer> dates = JSONArray.parseArray(objects.toJSONString(), Integer.class);
                        if (dates.size() > 0) {
                            Collections.sort(dates);
                            day = dates.get(dates.size() - 1);
                        }
                        mHandler.sendEmptyMessage(ALARM_DETECTION_DATE_MONTH_SUCCESS);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mHandler) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if (null != messageBusiness) {
            messageBusiness.onDestroy();
        }
    }
}
