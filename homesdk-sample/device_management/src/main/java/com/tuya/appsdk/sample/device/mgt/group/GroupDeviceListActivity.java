package com.tuya.appsdk.sample.device.mgt.group;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tuya.appsdk.sample.device.mgt.R;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.api.ITuyaGroup;
import com.tuya.smart.sdk.bean.DeviceBean;

import java.util.List;

/**
 * Group's device list page.
 * This page supports group renaming, adding devices, deleting devices, dissolving groups and other operations.
 *
 * Note that when all the sub devices in the current group are removed,
 * we will automatically dissolve the group.
 * This is because when we add a self device, we need to get the corresponding PCC{@link DeviceBean#getCategory()} of the device in the group,
 * When there is no corresponding child device in the group, we cannot get the corresponding PCC.
 */
/**
 * @author AoBing
 *
 * The device list of the current group will be displayed here, and the selected GroupId can be obtained through the Intent.
 * Some operations of the group can be performed here:
 *
 * 1. Jump to group control,
 *      because there are the same devices in a group,
 *      you only need to take the information of one device and pass it to the control Activity.
 *
 * 2. To add a sub-device to the group,
 *      you need to confirm that the pcc of the device and the group are consistent;
 *
 * 3. Rename the group;
 *
 * 4. Disband the group
 */

public class GroupDeviceListActivity extends AppCompatActivity {

    private final String TAG = "GroupDeviceListActivity";
    private RecyclerView rvList;
    private GroupDeviceListAdapter adapter;
    private List<DeviceBean> deviceBeanList;
    private long groupId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_device_list_activity);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getGroupDeviceList();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        getGroupDeviceList();
    }

    private void renameGroup(String newName) {
        TuyaHomeSdk.newGroupInstance(groupId).renameGroup(newName, new IResultCallback() {
            @Override
            public void onError(String code, String error) {
                Log.d(TAG, "rename group error:" + error);
            }

            @Override
            public void onSuccess() {
                Log.d(TAG, "rename group success");
                Toast.makeText(GroupDeviceListActivity.this, "rename success", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void disbandGroup() {
        TuyaHomeSdk.newGroupInstance(groupId).dismissGroup(new IResultCallback() {
            @Override
            public void onError(String code, String error) {
                Log.d(TAG, "disband group error:" + error);
            }

            @Override
            public void onSuccess() {
                Log.d(TAG, "disband Group success");
                Toast.makeText(GroupDeviceListActivity.this, "disband group success", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void removeDevice(String devId, int pos) {
        if (groupId != -1) {
            ITuyaGroup mGroup = TuyaHomeSdk.newSigMeshGroupInstance(groupId);
            mGroup.removeDevice(devId, new IResultCallback() {
                @Override
                public void onError(String code, String error) {
                    Log.d(TAG, "delete Device error:" + error);
                }

                @Override
                public void onSuccess() {
                    Log.d(TAG, "delete success");
                    deviceBeanList.remove(pos);
                    adapter.notifyItemRemoved(pos);
                    adapter.notifyItemRangeChanged(pos, deviceBeanList.size());

                }
            });
        }
    }

    private void getGroupDeviceList() {
        groupId = getIntent().getLongExtra("groupId", -1);
        deviceBeanList = TuyaHomeSdk.getDataInstance().getGroupDeviceList(groupId);
        if (groupId != -1 && deviceBeanList != null && deviceBeanList.size() > 0) {
            Log.d(TAG, "start show adapter");
            adapter = new GroupDeviceListAdapter(deviceBeanList);
            rvList.setLayoutManager(new LinearLayoutManager(
                    this, RecyclerView.VERTICAL, false));
            rvList.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        } else {
            Log.d(TAG, "null error");
        }
    }

    private void initView() {
        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        rvList = findViewById(R.id.rvList);


    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_group_device_list, menu);
        return true;
    }

    /**
     * Gets the array of child devices that the current device has already added
     * @return
     */
    private String[] getAddedDevices(){
        String[] deviceIdArray = null;
        if (deviceBeanList != null && !deviceBeanList.isEmpty()){
            int size = deviceBeanList.size();
            deviceIdArray = new String[size];
            for(int i = 0 ; i < size ; i++){
                deviceIdArray[i] = deviceBeanList.get(i).getDevId();
            }
        }
        return deviceIdArray;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_add_device) {

            if (groupId != -1 && deviceBeanList != null && deviceBeanList.size() > 0) {
                Intent intent = new Intent(this, GroupCreateActivity.class);
                intent.putExtra("groupId", groupId);
                intent.putExtra("addedDevices", getAddedDevices());
                startActivity(intent);
            } else {
                Toast.makeText(this, "device list is null", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "device list is null");
            }

        } else if (item.getItemId() == R.id.menu_group_rename) {
            renameDialog();
        } else if (item.getItemId() == R.id.menu_group_disband) {
            disbandGroup();
        } else if (item.getItemId() == R.id.menu_control_group) {

            if (groupId != -1 && deviceBeanList != null && deviceBeanList.size() > 0) {
                Intent intent = new Intent(this, GroupControlActivity.class);
                intent.putExtra("groupId", groupId);
                intent.putExtra("deviceId", deviceBeanList.get(0).getDevId());
                startActivity(intent);
            } else {
                Toast.makeText(this, "device list is null", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "device list is null");
            }

        }

        return super.onOptionsItemSelected(item);
    }

    private void renameDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.group_dialog_rename, null);
        EditText editText = view.findViewById(R.id.et_group_rename);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(true)
                .setTitle(getString(R.string.group_dialog_rename_title))
                .setView(view)
                .setPositiveButton(getString(R.string.group_dialog_rename_btn), (dialog1, which) -> {

                    String newName = editText.getText().toString();
                    if (!TextUtils.isEmpty(newName)) {
                        renameGroup(newName);
                    }

                }).create();
        dialog.show();
    }

    private static class VH extends RecyclerView.ViewHolder {
        TextView mTvDeviceName;
        LinearLayout mLlItemRoot;

        public VH(@NonNull View itemView) {
            super(itemView);
            mTvDeviceName = itemView.findViewById(R.id.tv_group_device_list_item);
            mLlItemRoot = itemView.findViewById(R.id.ll_item_root);
        }
    }

    private class GroupDeviceListAdapter extends RecyclerView.Adapter<VH> {

        private final List<DeviceBean> deviceBeanList;

        public GroupDeviceListAdapter(List<DeviceBean> deviceBeanList) {
            this.deviceBeanList = deviceBeanList;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.group_device_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            holder.mTvDeviceName.setText(deviceBeanList.get(position).getName());
            holder.mLlItemRoot.setOnClickListener(v -> {
                AlertDialog dialog = new AlertDialog.Builder(GroupDeviceListActivity.this)
                        .setCancelable(true)
                        .setTitle(getString(R.string.group_dialog_remove_device_title))
                        .setPositiveButton(getString(R.string.group_dialog_remove_device_btn), (dialog1, which) ->
                                removeDevice(deviceBeanList.get(position).getDevId(), position))
                        .create();
                dialog.show();
            });
        }

        @Override
        public int getItemCount() {
            return deviceBeanList.size();
        }
    }
}