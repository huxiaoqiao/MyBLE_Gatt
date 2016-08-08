package com.okii.myble_gatt;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

public class DeviceControlActivity extends Activity {

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView mConnectionState;
    private TextView mDataField;
    private String mDeviceName;
    private String mDeviceAddress;

    private ExpandableListView mGattServiceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);

        Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        //创建UI引用
        ((TextView)findViewById(R.id.device_address)).setText(mDeviceAddress);
        mGattServiceList = (ExpandableListView)findViewById(R.id.gatt_services_list);
        //设置ExpandableListView的点击事件
        mGattServiceList.setOnChildClickListener(serviceListClickListener);
        mConnectionState = (TextView)findViewById(R.id.connection_state);
        mDataField = (TextView)findViewById(R.id.data_value);

        getActionBar().setTitle(mDeviceName);
        //给左上角图标的左边加上一个返回的图标
        getActionBar().setDisplayHomeAsUpEnabled(true);

        //绑定蓝牙服务
        // TODO: 16/8/8

    }

    //创建Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    //Menu Item点击事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private ExpandableListView.OnChildClickListener serviceListClickListener = new ExpandableListView.OnChildClickListener() {
        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
            return false;
        }
    };

}
