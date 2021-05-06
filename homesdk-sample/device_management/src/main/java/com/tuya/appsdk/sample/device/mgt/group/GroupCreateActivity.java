package com.tuya.appsdk.sample.device.mgt.group;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.tuya.appsdk.sample.device.mgt.R;
import com.tuya.appsdk.sample.resource.HomeModel;
import com.tuya.smart.android.blemesh.api.ITuyaBlueMeshDevice;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.api.ITuyaGroup;
import com.tuya.smart.sdk.api.bluemesh.IAddGroupCallback;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.bean.SigMeshBean;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author zongwu.lin
 * This activity has two functions, one for creating a group, and one for adding devices to an already created group.
 * If the intent is sent with a {@link #groupId} of -1, we assume we are creating the group, we set the {@link #isCreateGroup} is true,
 * If the intent is not, we assume we are adding the device to the current group,we set the {@link #isCreateGroup} is false.
 * When we are adding the device to the group,we need get the added devices from the intent,because we should filter the devices those already added.
 * Tips:We do not allow offline devices or sigmesh-wifi devices to be added, either by creating groups or by adding devices to groups
 */
public class GroupCreateActivity extends AppCompatActivity {
    // When isgroup is true, it means to create a group. Then this collection represents all the devices below
    // When isgroup is false, it means that devices are added to the group. Then this list is used to place all devices that are not added to the current group
    private final List<DeviceBean> deviceBeanList = new ArrayList<>();
    private DeviceListAdapter adapter;
    // Represents all the selected device sets. The reason why set is used is to prevent repeated operation of devices
    private final Set<DeviceBean> selectDeviceIdList = new HashSet<>();
    private static final String TAG = "GroupCreateActivity";
    private String meshId;
    private long groupId;
    private boolean isCreateGroup = false;
    // Represents devices that have previously been added to the group
    private String[] addedDeviceIdArray;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        groupId = getIntent().getLongExtra("groupId", -1);
        isCreateGroup = groupId == -1;
        initView();
        initHomeDeviceData();
    }

    private void initView(){
        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(isCreateGroup ? R.string.group_create : R.string.group_add_select_device_to_group);
        toolbar.setNavigationOnClickListener(v -> finish());
        RecyclerView rvDeviceList = findViewById(R.id.rv_device_list);
        adapter = new DeviceListAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this );
        rvDeviceList.setLayoutManager(layoutManager);
        rvDeviceList.setItemAnimator( new DefaultItemAnimator());
        rvDeviceList.setAdapter(adapter);
    }

    /**
     * When isgroup is true, the list of sigmesh devices under the home will be displayed.
     * When isgroup is false, the semah device that has been added is filtered out.
     */
    private void initHomeDeviceData(){
        long homeId = HomeModel.getCurrentHome(this);
        if (!isCreateGroup){
            // When isgroup is false, it means that the current interface is to add the same PCC device to the existing group.
            // When we display the device list, we need to filter the added word devices.
            // {@link #addedDeviceIdArray} stores the deviceid set of the word device list that has been added by the group.
            // When we query all the child devices in the mesh corresponding to the family,
            // if the child device already exists in our addeddeviceidarray, we need to filter it.
            addedDeviceIdArray = getIntent().getStringArrayExtra("addedDevices");
        }
        TuyaHomeSdk.newHomeInstance(homeId).getHomeDetail(new ITuyaHomeResultCallback() {
            @Override
            public void onSuccess(HomeBean bean) {
                List<SigMeshBean> sigMeshBeans = bean.getSigMeshList();
                if (sigMeshBeans != null && !sigMeshBeans.isEmpty()) {
                    SigMeshBean sigMeshBean = null;
                    for (SigMeshBean meshBean : sigMeshBeans){
                        if (meshBean != null){
                            sigMeshBean = meshBean;
                            break;
                        }
                    }
                    if (sigMeshBean == null){
                        Log.e(TAG,"there is no available sigmeshbean");
                        return;
                    }

                    meshId = sigMeshBean.getMeshId();
                    List<DeviceBean> meshSubDevList = TuyaHomeSdk.newSigMeshDeviceInstance(sigMeshBean.getMeshId()).getMeshSubDevList();
                    if (meshSubDevList != null && !meshSubDevList.isEmpty()) {
                        for (DeviceBean deviceBean : meshSubDevList) {
                            if (isCreateGroup) {
                                deviceBeanList.add(deviceBean);
                            }else{
                                // Filter the added sub devices
                                if (!isDeviceAddedInGroup(deviceBean.devId)){
                                    deviceBeanList.add(deviceBean);
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onError(String errorCode, String errorMsg) {

            }
        });
    }

    /**
     * Determine whether a device has been added to the current group
     * @param devId
     * @return
     */
    private boolean isDeviceAddedInGroup(String devId){
        for(String addedDevId : addedDeviceIdArray){
            if (addedDevId.equals(devId)){
                return true;
            }
        }
        return false;
    }

    static class DeviceListAdapter extends RecyclerView.Adapter<DeviceVH>{
        private final GroupCreateActivity activity;
        private String pcc;
        public DeviceListAdapter(GroupCreateActivity activity){
            this.activity = activity;
        }

        @NonNull
        @Override
        public DeviceVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(activity).inflate(R.layout.item_select_device_pcc, parent,false);
            return new DeviceVH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DeviceVH holder, int position) {
            DeviceBean deviceBean = activity.deviceBeanList.get(position);
            if (deviceBean != null){
                holder.tvDevice.setText(deviceBean.getName() + "(" + deviceBean.getCategory() + ")");
                boolean isOnline = deviceBean.getIsOnline();
                boolean isSigMeshWifi = deviceBean.isSigMeshWifi();
                holder.tvDeviceIsOnline.setText(isOnline ? R.string.device_mgt_online : R.string.device_mgt_offline);
                if (isOnline && !isSigMeshWifi) {
                    holder.tvDevice.setTextColor(activity.getResources().getColor(R.color.color_enable));
                    holder.tvDeviceIsOnline.setTextColor(activity.getResources().getColor(R.color.color_enable));
                } else {
                    holder.tvDevice.setTextColor(activity.getResources().getColor(R.color.color_disable));
                    holder.tvDeviceIsOnline.setTextColor(activity.getResources().getColor(R.color.color_disable));
                }
                // Because only devices of the same PCC (devicebean? Category) can be added to the same group.
                // When creating a group (isgroup = true), if no device is selected, any online sub device can be selected.
                // However, as long as there are selected online sub devices,
                // it means that the PCC of the current group has been determined,
                // then only the online sub devices of the same PCC can be selected.
                holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        if (!TextUtils.isEmpty(pcc)){
                            if (deviceBean.getIsOnline() && !deviceBean.isSigMeshWifi() && TextUtils.equals(pcc, deviceBean.getCategory())){
                                activity.selectDeviceIdList.add(deviceBean);
                            }else {
                                holder.checkBox.setChecked(false);
                                if (!deviceBean.getIsOnline()){
                                    Toast.makeText(activity, R.string.cannot_select_offline_device, Toast.LENGTH_SHORT).show();
                                }else if (deviceBean.isSigMeshWifi()){
                                    Toast.makeText(activity, R.string.cannot_select_wifi_device, Toast.LENGTH_SHORT).show();
                                }else if (!TextUtils.equals(pcc, deviceBean.getCategory())){
                                    Toast.makeText(activity, R.string.cannot_select_different_pcc_device, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }else{
                            if (deviceBean.getIsOnline() && !deviceBean.isSigMeshWifi()){
                                pcc = deviceBean.getCategory();
                                activity.selectDeviceIdList.add(deviceBean);
                            }else{
                                holder.checkBox.setChecked(false);
                                if (!deviceBean.getIsOnline()){
                                    Toast.makeText(activity, R.string.cannot_select_offline_device, Toast.LENGTH_SHORT).show();
                                }else if (deviceBean.isSigMeshWifi()){
                                    Toast.makeText(activity, R.string.cannot_select_wifi_device, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                    } else {
                        if (activity.selectDeviceIdList != null && activity.selectDeviceIdList.size() > 0) {
                            activity.selectDeviceIdList.remove(deviceBean);
                        }
                        if (activity.selectDeviceIdList.isEmpty()){
                            pcc = null;
                        }
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return activity.deviceBeanList.size();
        }
    }
    static class DeviceVH extends RecyclerView.ViewHolder{
        public TextView tvDevice, tvDeviceIsOnline;
        public CheckBox checkBox;
        public LinearLayout llDeviceItem;
        public DeviceVH(@NonNull View itemView) {
            super(itemView);
            llDeviceItem = itemView.findViewById(R.id.ll_device_item);
            tvDevice = itemView.findViewById(R.id.tv_device);
            tvDeviceIsOnline = itemView.findViewById(R.id.tv_device_online);
            checkBox = itemView.findViewById(R.id.check_by_group_add_device);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        if (isCreateGroup) {
            getMenuInflater().inflate(R.menu.menu_group_create, menu);
            return true;
        }else{
            getMenuInflater().inflate(R.menu.menu_group_add_device, menu);
            return true;
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (isCreateGroup) {// create group
            if (item.getItemId() == R.id.menu_item_group_create) {
                if (!selectDeviceIdList.isEmpty()) {
                    showInputDialog();
                } else {
                    Toast.makeText(GroupCreateActivity.this, R.string.create_group_tips, Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "add group refuse,please select devices");
                }
            }
        }else{// add devices to group
            if (item.getItemId() == R.id.menu_item_group_add_device) {
                if (!selectDeviceIdList.isEmpty()) {
                    Iterator iterator = selectDeviceIdList.iterator();
                    addDeviceToGroup(iterator,groupId);
                } else {
                    Toast.makeText(GroupCreateActivity.this, R.string.create_group_tips, Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "add group refuse,please select devices");
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * When isgroup is true, it means that the current activity is to create a group.
     * After checking the online sub devices to be added, click create to pop up the box.
     * This dialog is used to let the user enter groupname and localid
     */
    private void showInputDialog(){
        final View v = LayoutInflater.from(this).inflate(R.layout.group_create_dialog, null);
        TextInputEditText etGroupName = v.findViewById(R.id.et_group_name);
        TextInputEditText etLocalId = v.findViewById(R.id.et_local_id);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.group_create).setView(v)
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton(R.string.confirm, (dialog, which) -> {
            String groupName = etGroupName.getText().toString();
            String localID = etLocalId.getText().toString();
            if (TextUtils.isEmpty(groupName) || TextUtils.isEmpty(localID)){
                return;
            }
            dialog.dismiss();
            showProgressDialog();
            addGroup(groupName,localID);
        });
        builder.show();
    }

    /**
     * Create a group under the mesh corresponding to the current home。
     * @param groupName
     * @param localId
     */
    private void addGroup(String groupName,String localId){
        if (selectDeviceIdList == null || selectDeviceIdList.isEmpty()){
            dismissProgressDialog();
            Log.i(TAG, "create group return , becasue the selected devices is empty");
            return;
        }

        // get pcc
        Iterator<DeviceBean> iterator = selectDeviceIdList.iterator();
        String pcc = null;
        while (iterator.hasNext()){
            DeviceBean deviceBean = iterator.next();
            if (deviceBean != null){
                pcc = deviceBean.getCategory();
                break;
            }
        }
        // add group
        ITuyaBlueMeshDevice mTuyaSigMeshDevice= TuyaHomeSdk.newSigMeshDeviceInstance(meshId);
        mTuyaSigMeshDevice.addGroup(groupName,pcc, localId, new IAddGroupCallback() {
            @Override
            public void onError(String errorCode, String errorMsg) {
                dismissProgressDialog();
                // create group failed
                Log.i(TAG, "create group failed,errorCode:" + errorCode + ",errorMsg:" + errorMsg);
            }

            @Override
            public void onSuccess(long groupId) {
                //create group success
                GroupCreateActivity.this.groupId = groupId;
                Iterator iterator = selectDeviceIdList.iterator();
                addDeviceToGroup(iterator,groupId);
                Toast.makeText(GroupCreateActivity.this, getString(R.string.group_create_success_msg), Toast.LENGTH_SHORT).show();
            }
        });
    }
    ProgressDialog dialog;
    private void showProgressDialog(){
        if (dialog == null) {
            dialog =new ProgressDialog(this);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setTitle(R.string.please_wait_for_a_minute);
        }
        dialog.show();
    }

    private void dismissProgressDialog(){
        if (dialog != null){
            dialog.dismiss();
        }
    }
    /**
     * Add device to current group
     * When the operating device is a single point Bluetooth device,
     * using this constructor, since adding devices to a group is asynchronous,
     * and only a single device can be added each time,
     * we need to try recursion to judge whether all the selected sub devices to be added have been added.
     * Here, we simplify the processing method.
     * As long as each device to be added has been added,
     * we will perform the next processing.
     * @param groupId
     */
    private void addDeviceToGroup(Iterator iterator,long groupId){
        if (iterator.hasNext()){
            Object objDevice =  iterator.next();
            if (objDevice instanceof DeviceBean) {
                DeviceBean deviceBean = (DeviceBean) objDevice;
                ITuyaGroup mGroup = TuyaHomeSdk.newSigMeshGroupInstance(groupId);
                mGroup.addDevice(deviceBean.devId, new IResultCallback() {
                    @Override
                    public void onError(String code, String errorMsg) {
                        // add device to current group failed
                        Log.i(TAG, "add device whose groupId is " + deviceBean.getDevId() + "to current group failed,code:" + code + ",errorMsg:" + errorMsg);
                    }

                    @Override
                    public void onSuccess() {
                        // add device to current group success
                        // do something
                        Log.i(TAG, "add group whose groupId is " + deviceBean.getDevId() + " to current home success");
                        addDeviceToGroup(iterator, groupId);
                    }
                });
            }
        }else{
            dismissProgressDialog();
            Intent intent = new Intent(GroupCreateActivity.this, GroupDeviceListActivity.class);
            intent.putExtra("groupId", groupId);
            startActivity(intent);
            finish();
        }
    }
}