package com.tuya.appsdk.sample.device.mgt.group;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tuya.appsdk.sample.device.mgt.R;
import com.tuya.appsdk.sample.resource.HomeModel;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;
import com.tuya.smart.sdk.bean.GroupBean;

import java.util.ArrayList;
import java.util.List;

public class GroupListActivity extends AppCompatActivity {
    private List<GroupBean> groupBeanList = new ArrayList<>();
    private long homeId;
    private GroupListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_list_activity);
        initView();
        homeId = HomeModel.getCurrentHome(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }

    private void refreshData() {
        if (groupBeanList != null){
            groupBeanList.clear();
        }
        TuyaHomeSdk.newHomeInstance(homeId).getHomeDetail(new ITuyaHomeResultCallback() {
            @Override
            public void onSuccess(HomeBean bean) {
                if (bean != null){
                    groupBeanList.addAll(bean.getGroupList());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(String errorCode, String errorMsg) {

            }
        });
    }

    private void initView() {
        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        RecyclerView rvGroupList = findViewById(R.id.rv_group_list);
        adapter = new GroupListAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this );
        rvGroupList.setLayoutManager(layoutManager);
        rvGroupList.setItemAnimator( new DefaultItemAnimator());
        rvGroupList.setAdapter(adapter);
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_group_list, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_add_group) {
            Intent intent = new Intent(this, GroupCreateActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    static class GroupListAdapter extends RecyclerView.Adapter<GroupVH>{
        private GroupListActivity activity;

        public GroupListAdapter(GroupListActivity activity){
            this.activity = activity;
        }

        @NonNull
        @Override
        public GroupVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(activity).inflate(R.layout.item_group_list, parent,false);
            GroupVH vh = new GroupVH(view);
            return vh;
        }

        @Override
        public void onBindViewHolder(@NonNull GroupVH holder, int position) {
            final GroupBean groupBean = activity.groupBeanList.get(position);
            if (groupBean != null){
                holder.tvGroupName.setText(groupBean.getLocalId() + " - " + groupBean.getCategory() + " - " + groupBean.getName());
                holder.flGroupItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(activity,GroupDeviceListActivity.class);
                        intent.putExtra("groupId", groupBean.getId());
                        activity.startActivity(intent);
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return (activity != null && activity.groupBeanList != null) ? activity.groupBeanList.size() : 0;
        }
    }
    static class GroupVH extends RecyclerView.ViewHolder{
        public TextView tvGroupName;
        public FrameLayout flGroupItem;
        public GroupVH(@NonNull View itemView) {
            super(itemView);
            flGroupItem = itemView.findViewById(R.id.fl_group_item);
            tvGroupName = itemView.findViewById(R.id.tv_group_name);
        }
    }
}