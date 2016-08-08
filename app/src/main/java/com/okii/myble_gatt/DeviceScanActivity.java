package com.okii.myble_gatt;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class DeviceScanActivity extends ListActivity {
    private static final String TAG = DeviceScanActivity.class.getSimpleName();
    private BluetoothAdapter mBluetoothAdapter;//蓝牙适配器
    private BluetoothLeScanner mBluetoothLeScanner; //BLE扫描器
    private LeDeviceListAdapter mDeviceListAdapter;
    private boolean mScanning;//是否在扫描
    private Handler mHandler;

    private static final int REQUEST_ENABLE_BT = 1;//蓝牙开启请求码
    private static final long SCAN_PERIOD = 200000;

    private static final String BLE_FILTER_ADDRESS = "EC:A9:FA:56:91:55";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setTitle("扫描BLE设备");
        mHandler = new Handler();

        //检查设备是否支持BLE
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            Toast.makeText(DeviceScanActivity.this, "不支持BLE", Toast.LENGTH_SHORT).show();
            //退出应用
            finish();
        }

        //初始化蓝牙适配器
        final BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        //蓝牙扫描器,需要minSdk >= 21
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        //检查设备是否支持蓝牙功能
        if (mBluetoothAdapter == null){
            Toast.makeText(DeviceScanActivity.this, "蓝牙功能不可用", Toast.LENGTH_SHORT).show();
            //退出应用
            finish();
            return;
        }

        Log.i(TAG,"-----onCreate-----");


    }

    //创建Menu(右上角)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        if (!mScanning){
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        }else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(R.layout.actionbar_indeterminate_progress);
        }

        return true;
    }

   //Menu Item被点击
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.menu_scan:
                //先清除设备
                mDeviceListAdapter.clear();
                scanLeDevice(true);
                break;
            case R.id.menu_stop:
                scanLeDevice(false);
                break;
        }

        return true;
    }

    //Activity生命周期方法，继续
    @Override
    protected void onResume() {
        super.onResume();

        //确保蓝牙功能开启。如果蓝牙未开启，弹出一个对话框询问是否开启蓝牙
        if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT);
        }

        //初始化列表Adapter
        mDeviceListAdapter = new LeDeviceListAdapter();
        setListAdapter(mDeviceListAdapter);

        //扫描蓝牙设备
        scanLeDevice(true);
        Log.i(TAG,"-----onResume-----");

    }
    //Activity生命周期方法，停止
    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        mDeviceListAdapter.clear();
        Log.i(TAG,"-----onPause-----");

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG,"-----onStart-----");

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG,"-----onStop-----");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"-----onDestroy-----");
    }

    //扫描蓝牙设备
    private void scanLeDevice(final boolean enable){
        if (enable){

            //一段时间后停止扫描
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    //mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    //新API
                    mBluetoothLeScanner.stopScan(mScanCallback);
                    //隐藏Menu
                    invalidateOptionsMenu();
                }
            },SCAN_PERIOD);

            mScanning = true;
            //扫描蓝牙设备
            //旧扫描API
            //mBluetoothAdapter.startLeScan(mLeScanCallback);

            //新扫描API,需要minSdk >= 21
            //Filters
            ScanFilter.Builder filterBuilder = new ScanFilter.Builder();
            filterBuilder.setDeviceAddress(BLE_FILTER_ADDRESS);
            ArrayList<ScanFilter> filters = new ArrayList<ScanFilter>();
            filters.add(filterBuilder.build());
            //Settings
            ScanSettings.Builder settingsBuilder = new ScanSettings.Builder();
            //settingsBuilder.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES);
            //settingsBuilder.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE);
            settingsBuilder.setScanMode(ScanSettings.SCAN_MODE_BALANCED);

            //mBluetoothLeScanner.startScan(filters,settingsBuilder.build(),mScanCallback);
            mBluetoothLeScanner.startScan(mScanCallback);

        }else {
            mScanning = false;
            //mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mBluetoothLeScanner.stopScan(mScanCallback);
        }
        invalidateOptionsMenu();
    }

    //Activiy数据回调方法
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_CANCELED){
            //用户取消开启蓝牙，退出应用
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);

    }



    //内部类，设备列表适配器，用于显示扫描到的设备列表
    private class LeDeviceListAdapter extends BaseAdapter{

        private ArrayList<BluetoothDevice> mLeDevices;//设备数组
        private LayoutInflater mInflator; //打气筒

        //构造方法
        public LeDeviceListAdapter(){
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = DeviceScanActivity.this.getLayoutInflater();
        }

        //添加设备
        public void addDevice(BluetoothDevice device){
            if (!mLeDevices.contains(device)){
                mLeDevices.add(device);
            }
        }

        //获取单个设备
        public BluetoothDevice getDevice(int position){
            return mLeDevices.get(position);
        }

        //清除所有设备
        public void clear(){
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int position) {
            return getDevice(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //生成ListView，view复用
            ViewHodler viewHolder;
            if (convertView == null){
                convertView = mInflator.inflate(R.layout.listitem_device,null);
                viewHolder = new ViewHodler();
                viewHolder.deviceName = (TextView)convertView.findViewById(R.id.device_name);
                viewHolder.deviceAddress = (TextView)convertView.findViewById(R.id.device_address);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHodler) convertView.getTag();
            }

            BluetoothDevice device = mLeDevices.get(position);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0){
                viewHolder.deviceName.setText(deviceName);
            }else {
                viewHolder.deviceName.setText("未知设备");
            }
            viewHolder.deviceAddress.setText(device.getAddress());

            return convertView;
        }
    }
    //点击Item事件
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

       final BluetoothDevice device = mDeviceListAdapter.getDevice(position);
        if (device == null) return;
        Intent intent = new Intent(this,DeviceControlActivity.class);
        Bundle data = new Bundle();
        data.putString(DeviceControlActivity.EXTRAS_DEVICE_NAME,device.getName());
        data.putString(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS,device.getAddress());
        intent.putExtras(data);
        //跳转前停止扫描
        if (mScanning == true){
            mBluetoothLeScanner.stopScan(mScanCallback);
            mScanning = false;
        }
        startActivity(intent);

    }

    static class ViewHodler{
        TextView deviceName;//设备名称
        TextView deviceAddress;//设备MAC地址
    }

    //设备扫描回调
    //旧API
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            //回到UI线程进行操作
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mDeviceListAdapter.addDevice(device);
                    //通知列表刷新
                    mDeviceListAdapter.notifyDataSetChanged();
                }
            });

        }
    };
    //新API 需要 minSdk >= 21
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, final ScanResult result) {
            super.onScanResult(callbackType, result);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    BluetoothDevice device = result.getDevice();
                    mDeviceListAdapter.addDevice(device);
                    mDeviceListAdapter.notifyDataSetChanged();
                }
            });
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

}
