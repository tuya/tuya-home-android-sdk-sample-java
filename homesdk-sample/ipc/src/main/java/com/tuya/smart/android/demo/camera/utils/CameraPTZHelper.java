package com.tuya.smart.android.demo.camera.utils;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.tuya.drawee.view.DecryptImageView;
import com.tuya.smart.android.camera.sdk.TuyaIPCSdk;
import com.tuya.smart.android.camera.sdk.api.ITuyaIPCPTZ;
import com.tuya.smart.android.camera.sdk.bean.CollectionPointBean;
import com.tuya.smart.android.camera.sdk.constant.PTZDPModel;
import com.tuya.smart.android.demo.R;
import com.tuya.smart.android.device.bean.EnumSchemaBean;
import com.tuya.smart.home.sdk.callback.ITuyaResultCallback;
import com.tuya.smart.sdk.api.IResultCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by HuangXin on 2021/9/25.
 */
public class CameraPTZHelper implements View.OnClickListener, View.OnLongClickListener {

    private static final String TAG = "CameraPTZHelper";
    private final ITuyaIPCPTZ tuyaIPCPTZ;
    private View ptzBoard;
    private Context context;
    private ProgressDialog progressDialog;
    private int collectionPointSize;

    public CameraPTZHelper(String devId) {
        tuyaIPCPTZ = TuyaIPCSdk.getPTZInstance(devId);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void bindPtzBoard(View ptzBoard) {
        this.ptzBoard = ptzBoard;
        this.context = ptzBoard.getContext();
        progressDialog = new ProgressDialog(ptzBoard.getContext());
        ptzBoard.findViewById(R.id.tv_ptz_close).setOnClickListener(this);
        //PTZ Control
        ptzBoard.findViewById(R.id.tv_ptz_left).setOnTouchListener(new PTZControlTouchListener("6"));
        ptzBoard.findViewById(R.id.tv_ptz_top).setOnTouchListener(new PTZControlTouchListener("0"));
        ptzBoard.findViewById(R.id.tv_ptz_right).setOnTouchListener(new PTZControlTouchListener("2"));
        ptzBoard.findViewById(R.id.tv_ptz_bottom).setOnTouchListener(new PTZControlTouchListener("4"));
        boolean isPTZControl = tuyaIPCPTZ.querySupportByDPCode(PTZDPModel.DP_PTZ_CONTROL);
        ptzBoard.findViewById(R.id.group_ptz_control).setVisibility(isPTZControl ? View.VISIBLE : View.GONE);
        //Focal
        ptzBoard.findViewById(R.id.tv_focal_increase).setOnTouchListener(new FocalTouchListener("1"));
        ptzBoard.findViewById(R.id.tv_focal_reduce).setOnTouchListener(new FocalTouchListener("0"));
        boolean isSupportZoom = tuyaIPCPTZ.querySupportByDPCode(PTZDPModel.DP_ZOOM_CONTROL);
        ptzBoard.findViewById(R.id.group_focal).setVisibility(isSupportZoom ? View.VISIBLE : View.GONE);
        //Collection Point
        ptzBoard.findViewById(R.id.tv_collection_add).setOnClickListener(this);
        ptzBoard.findViewById(R.id.tv_collection_delete).setOnClickListener(this);
        ptzBoard.findViewById(R.id.tv_collection_item).setOnClickListener(this);
        ptzBoard.findViewById(R.id.tv_collection_item).setOnLongClickListener(this);
        boolean isSupportCollection = tuyaIPCPTZ.querySupportByDPCode(PTZDPModel.DP_MEMORY_POINT_SET);
        ptzBoard.findViewById(R.id.group_collection).setVisibility(isSupportCollection ? View.VISIBLE : View.GONE);
        //Cruise
        TextView tvCruiseSwitch = ptzBoard.findViewById(R.id.tv_cruise_switch);
        tvCruiseSwitch.setOnClickListener(this);
        boolean isCruiseOpen = Boolean.TRUE == tuyaIPCPTZ.getCurrentValue(PTZDPModel.DP_CRUISE_SWITCH, Boolean.class);
        tvCruiseSwitch.setText(isCruiseOpen ? "Opened" : "closed");
        ptzBoard.findViewById(R.id.tv_cruise_mode).setOnClickListener(this);
        ptzBoard.findViewById(R.id.tv_cruise_time).setOnClickListener(this);
        boolean isSupportCruise = tuyaIPCPTZ.querySupportByDPCode(PTZDPModel.DP_CRUISE_SWITCH);
        ptzBoard.findViewById(R.id.group_cruise).setVisibility(isSupportCruise ? View.VISIBLE : View.GONE);
        //Tracking
        TextView tTrackingSwitch = ptzBoard.findViewById(R.id.tv_tracking_switch);
        tTrackingSwitch.setOnClickListener(this);
        boolean isTrackingOpen = Boolean.TRUE == tuyaIPCPTZ.getCurrentValue(PTZDPModel.DP_MOTION_TRACKING, Boolean.class);
        tTrackingSwitch.setText(isTrackingOpen ? "Opened" : "closed");
        boolean isSupportTracking = tuyaIPCPTZ.querySupportByDPCode(PTZDPModel.DP_MOTION_TRACKING);
        ptzBoard.findViewById(R.id.group_tracking).setVisibility(isSupportTracking ? View.VISIBLE : View.GONE);
        //Preset Point
        ptzBoard.findViewById(R.id.tv_preset_select).setOnClickListener(this);
        boolean isSupportPreset = tuyaIPCPTZ.querySupportByDPCode(PTZDPModel.DP_PRESET_POINT);
        ptzBoard.findViewById(R.id.group_preset).setVisibility(isSupportPreset ? View.VISIBLE : View.GONE);

        View tvPtzEmpty = ptzBoard.findViewById(R.id.tv_ptz_empty);
        boolean isNotSupportPTZ = !isPTZControl && !isSupportZoom && !isSupportCollection && !isSupportCruise && !isSupportTracking && !isSupportPreset;
        tvPtzEmpty.setVisibility(isNotSupportPTZ ? View.VISIBLE : View.GONE);
    }

    public void show() {
        if (ptzBoard == null) {
            return;
        }
        ptzBoard.setAlpha(0f);
        ptzBoard.setVisibility(View.VISIBLE);
        ptzBoard.animate().alpha(1f).setDuration(200).start();
        requestCollectionPointList();
    }

    public void dismiss() {
        if (ptzBoard == null) {
            return;
        }
        ptzBoard.animate().alpha(0f).setDuration(200).start();
        ptzBoard.postDelayed(() -> ptzBoard.setVisibility(View.INVISIBLE), 200);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_ptz_close) {
            dismiss();
        } else if (v.getId() == R.id.tv_collection_add) {
            addCollectionPoint();
        } else if (v.getId() == R.id.tv_collection_delete) {
            deleteCollectionPoint();
        } else if (v.getId() == R.id.tv_collection_item) {
            if (v.getTag() instanceof CollectionPointBean) {
                tuyaIPCPTZ.viewCollectionPoint((CollectionPointBean) v.getTag(), new ResultCallback("viewCollectionPoint"));
            }
        } else if (v.getId() == R.id.tv_cruise_switch) {
            boolean isOpen = Boolean.TRUE == tuyaIPCPTZ.getCurrentValue(PTZDPModel.DP_CRUISE_SWITCH, Boolean.class);
            tuyaIPCPTZ.publishDps(PTZDPModel.DP_CRUISE_SWITCH, !isOpen, new ResultCallback("cruise_switch") {
                @Override
                public void onSuccess() {
                    super.onSuccess();
                    boolean isOpen = Boolean.TRUE == tuyaIPCPTZ.getCurrentValue(PTZDPModel.DP_CRUISE_SWITCH, Boolean.class);
                    ((TextView) v).setText(isOpen ? "Opened" : "closed");
                }
            });
        } else if (v.getId() == R.id.tv_cruise_mode) {
            Map<String, String> map = new HashMap<>();
            map.put("0", context.getString(R.string.ipc_panoramic_cruise));
            map.put("1", context.getString(R.string.ipc_collection_point_cruise));
            List<String> itemList = new ArrayList<>();
            List<String> modeList = new ArrayList<>();
            EnumSchemaBean enumSchemaBean = tuyaIPCPTZ.getSchemaProperty(PTZDPModel.DP_CRUISE_MODE, EnumSchemaBean.class);
            String[] range = enumSchemaBean.getRange().toArray(new String[0]);
            for (String s : range) {
                String title = map.get(s);
                if (!TextUtils.isEmpty(title)) {
                    itemList.add(title);
                    modeList.add(s);
                }
            }
            showSelectDialog(itemList.toArray(new String[0]), (dialog, which) -> {
                String mode = modeList.get(which);
                tuyaIPCPTZ.setCruiseMode(mode, new ResultCallback("setCruiseMode " + mode));
            });
        } else if (v.getId() == R.id.tv_cruise_time) {
            String[] items = new String[]{context.getString(R.string.ipc_full_day_cruise), context.getString(R.string.ipc_custom_cruise)};
            showSelectDialog(items, (dialog, which) -> {
                if (which == 0) {
                    tuyaIPCPTZ.publishDps(PTZDPModel.DP_CRUISE_TIME_MODE, "0", new ResultCallback("cruise_time_mode 0"));
                } else if (which == 1) {
                    tuyaIPCPTZ.setCruiseTiming("09:00", "16:00", new ResultCallback("cruise_time_mode 1"));
                }
            });
        } else if (v.getId() == R.id.tv_tracking_switch) {
            onClickTracking((TextView) v);
        } else if (v.getId() == R.id.tv_preset_select) {
            onClickPreset();
        }
    }

    private void onClickTracking(TextView textView) {
        progressDialog.show();
        boolean isOpen = Boolean.TRUE == tuyaIPCPTZ.getCurrentValue(PTZDPModel.DP_MOTION_TRACKING, Boolean.class);
        tuyaIPCPTZ.publishDps(PTZDPModel.DP_MOTION_TRACKING, !isOpen, new ResultCallback("motion_tracking") {
            @Override
            public void onSuccess() {
                super.onSuccess();
                boolean isOpen = Boolean.TRUE == tuyaIPCPTZ.getCurrentValue(PTZDPModel.DP_MOTION_TRACKING, Boolean.class);
                textView.setText(isOpen ? "Opened" : "closed");
                progressDialog.dismiss();
            }

            @Override
            public void onError(String code, String error) {
                super.onError(code, error);
                progressDialog.dismiss();
            }
        });
    }

    private void onClickPreset() {
        EnumSchemaBean enumSchemaBean = tuyaIPCPTZ.getSchemaProperty(PTZDPModel.DP_PRESET_POINT, EnumSchemaBean.class);
        String[] items = enumSchemaBean.getRange().toArray(new String[0]);
        showSelectDialog(items, (dialog, which) -> tuyaIPCPTZ.publishDps(PTZDPModel.DP_PRESET_POINT, items[which], new ResultCallback("ipc_preset_set " + items[which])));
    }

    public void ptzControl(String direction) {
        tuyaIPCPTZ.publishDps(PTZDPModel.DP_PTZ_CONTROL, direction, new ResultCallback("ptzControl"));
    }

    public void ptzStop() {
        tuyaIPCPTZ.publishDps(PTZDPModel.DP_PTZ_STOP, true, new ResultCallback("ptzStop"));
    }

    private void requestCollectionPointList() {
        tuyaIPCPTZ.requestCollectionPointList(new ITuyaResultCallback<List<CollectionPointBean>>() {
            @Override
            public void onSuccess(List<CollectionPointBean> result) {
                DecryptImageView decryptImageView = ptzBoard.findViewById(R.id.iv_collection);
                TextView tvName = ptzBoard.findViewById(R.id.tv_collection_item);
                if (result != null && result.size() > 0) {
                    collectionPointSize = result.size();
                    CollectionPointBean collectionPointBean = result.get(result.size() - 1);
                    try {
                        for (int i = 0; i < result.size(); i++) {
                            CollectionPointBean item = result.get(i);
                            if (Integer.parseInt(item.getMpId()) > Integer.parseInt(collectionPointBean.getMpId())) {
                                collectionPointBean = item;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    tvName.setText(collectionPointBean.getName());
                    if (collectionPointBean.getEncryption() instanceof JSONObject) {
                        JSONObject jsonObject = (JSONObject) collectionPointBean.getEncryption();
                        Object key = jsonObject.get("key");
                        if (key == null) {
                            decryptImageView.setImageURI(collectionPointBean.getPic());
                        } else {
                            decryptImageView.setImageURI(collectionPointBean.getPic(), key.toString().getBytes());
                        }
                    } else {
                        decryptImageView.setImageURI(collectionPointBean.getPic());
                    }
                    tvName.setTag(collectionPointBean);
                } else {
                    collectionPointSize = 0;
                    tvName.setText("");
                    tvName.setTag(null);
                    decryptImageView.setImageResource(0);
                }
            }

            @Override
            public void onError(String errorCode, String errorMessage) {

            }
        });
    }

    private void addCollectionPoint() {
        progressDialog.show();
        tuyaIPCPTZ.addCollectionPoint("Collection" + collectionPointSize++, new IResultCallback() {
            @Override
            public void onError(String code, String error) {
                Log.d(TAG, "addCollectionPoint invoke error");
                progressDialog.dismiss();
            }

            @Override
            public void onSuccess() {
                ptzBoard.postDelayed(() -> {
                    requestCollectionPointList();
                    progressDialog.dismiss();
                },1000);
            }
        });
    }

    private void deleteCollectionPoint() {
        TextView tvCollectionItem = ptzBoard.findViewById(R.id.tv_collection_item);
        if (!(tvCollectionItem.getTag() instanceof CollectionPointBean)) {
            Toast.makeText(context, "Operation failed", Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.show();
        List<CollectionPointBean> items = new ArrayList<>();
        CollectionPointBean item = (CollectionPointBean) tvCollectionItem.getTag();
        items.add(item);
        tuyaIPCPTZ.deleteCollectionPoints(items, new IResultCallback() {
            @Override
            public void onError(String code, String error) {
                Log.d(TAG, "deleteCollectionPoint invoke error");
                progressDialog.dismiss();
            }

            @Override
            public void onSuccess() {
                requestCollectionPointList();
                progressDialog.dismiss();
            }
        });
    }

    @Override
    public boolean onLongClick(View v) {
        if (v.getId() == R.id.tv_collection_item) {
            TextView tvCollectionItem = ptzBoard.findViewById(R.id.tv_collection_item);
            if (tvCollectionItem.getTag() instanceof CollectionPointBean) {
                CollectionPointBean item = (CollectionPointBean) tvCollectionItem.getTag();
                String nameNew = item.getName() + " New";
                tuyaIPCPTZ.modifyCollectionPoint(item, nameNew, new IResultCallback() {
                    @Override
                    public void onError(String code, String error) {
                        Toast.makeText(context, "Operation failed", Toast.LENGTH_SHORT).show();
                    }

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onSuccess() {
                        ((TextView) v).setText(nameNew);
                        Toast.makeText(context, "Operation success", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
        return true;
    }

    private void showSelectDialog(String[] items, DialogInterface.OnClickListener onClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setItems(items, onClickListener);
        builder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    class ResultCallback implements IResultCallback {
        private final String method;

        public ResultCallback(String method) {
            this.method = method;
        }

        @Override
        public void onError(String code, String error) {
            Log.d(TAG, method + " invoke error: " + error);
            Toast.makeText(context, "Operation failed", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onSuccess() {
            Log.d(TAG, method + " invoke success");
            Toast.makeText(context, "Operation success", Toast.LENGTH_SHORT).show();
        }
    }

    private class PTZControlTouchListener implements View.OnTouchListener {

        String direction;

        public PTZControlTouchListener(String direction) {
            this.direction = direction;
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    ptzControl(direction);
                    break;
                case MotionEvent.ACTION_UP:
                    ptzStop();
                default:
                    break;
            }
            return true;
        }
    }

    private class FocalTouchListener implements View.OnTouchListener {

        String zoom;

        public FocalTouchListener(String zoom) {
            this.zoom = zoom;
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    tuyaIPCPTZ.publishDps(PTZDPModel.DP_ZOOM_CONTROL, zoom, new ResultCallback("zoom_control" + zoom));
                    break;
                case MotionEvent.ACTION_UP:
                    tuyaIPCPTZ.publishDps(PTZDPModel.DP_ZOOM_STOP, true, new ResultCallback("zoom_stop"));
                default:
                    break;
            }
            return true;
        }
    }
}
