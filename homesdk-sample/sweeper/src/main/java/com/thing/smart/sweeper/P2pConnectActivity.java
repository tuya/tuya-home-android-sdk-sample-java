package com.thing.smart.sweeper;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.thingclips.smart.optimus.sdk.ThingOptimusSdk;
import com.thingclips.smart.optimus.sweeper.api.IThingSweeperKitSdk;
import com.thingclips.smart.optimus.sweeper.api.IThingSweeperP2P;
import com.thingclips.smart.sweepe.p2p.bean.SweeperP2PBean;
import com.thingclips.smart.sweepe.p2p.callback.SweeperP2PCallback;
import com.thingclips.smart.sweepe.p2p.callback.SweeperP2PDataCallback;
import com.thingclips.smart.sweepe.p2p.manager.DownloadType;

/**
 * create by nielev on 2023/2/22
 */
public class P2pConnectActivity extends AppCompatActivity {

    private IThingSweeperP2P mSweeperP2P;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_p2p_connect);
        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //get device id
        String devId = getIntent().getStringExtra("deviceId");

        //get sweeperp2p
        IThingSweeperKitSdk iThingSweeperKitSdk = ThingOptimusSdk.getManager(IThingSweeperKitSdk.class);
        if(null != iThingSweeperKitSdk){
            mSweeperP2P = iThingSweeperKitSdk.getSweeperP2PInstance(devId);
            TextView tvP2pConnectShow = findViewById(R.id.tvP2pConnectShow);
            TextView tvP2pDownloadDataStatus = findViewById(R.id.tvP2pDownloadDataStatus);
            //p2p connect
            findViewById(R.id.btnStartConnectP2pStep).setOnClickListener(v -> mSweeperP2P.connectDeviceByP2P(new SweeperP2PCallback() {
                @Override
                public void onSuccess() {
                    tvP2pConnectShow.setText(getString(R.string.p2p_connect_status)+" true");
                    //p2p connect suc, start get Sweeper data
                    mSweeperP2P.startObserverSweeperDataByP2P(DownloadType.P2PDownloadTypeStill, new SweeperP2PCallback() {
                        @Override
                        public void onSuccess() {
                            //start suc
                            tvP2pDownloadDataStatus.setText(getString(R.string.p2p_download_data_status)+" true");
                        }

                        @Override
                        public void onFailure(int i) {
                            //start failure
                            tvP2pDownloadDataStatus.setText(getString(R.string.p2p_download_data_status)+" false");
                        }
                    }, new SweeperP2PDataCallback() {
                        @Override
                        public void receiveData(int i, @Nullable SweeperP2PBean sweeperP2PBean) {
                            //get Data
                        }

                        @Override
                        public void onFailure(int i) {

                        }
                    });
                }

                @Override
                public void onFailure(int i) {
                    tvP2pConnectShow.setText(getString(R.string.p2p_connect_status)+" false");
                }
            }));

            findViewById(R.id.btnStopP2PData).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSweeperP2P.stopObserverSweeperDataByP2P(new SweeperP2PCallback() {
                        @Override
                        public void onSuccess() {
                            //stop suc
                        }

                        @Override
                        public void onFailure(int i) {
                            //stop suc
                        }
                    });
                }
            });
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //p2p stop
        if(null != mSweeperP2P) mSweeperP2P.onDestroyP2P();
    }


}