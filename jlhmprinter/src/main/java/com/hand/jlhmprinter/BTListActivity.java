package com.hand.jlhmprinter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.hand.baselibrary.activity.BaseActivity;
import com.hand.baselibrary.activity.BasePresenter;
import com.hand.baselibrary.activity.IBaseActivity;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;

public class BTListActivity extends BaseActivity {

    @BindView(R2.id.rcv_device)
    RecyclerView rcvDevices;
    @BindView(R2.id.swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R2.id.tv_search_tip)
    TextView tvSearchTip;
    private ArrayList<BluetoothDevice> mData = new ArrayList<>();
    private BTListAdapter mBTListAdapter;
    public BluetoothAdapter mBluetoothAdapter;
    private Bluetooth mBluetooth;

    private ProgressDialog progressDialog;

    @Override
    protected int setStatusBarView() {
        return R.id.status_bar_view;
    }

    @Override
    protected Object setLayout() {
        return R.layout.activity_layout_bt_list;
    }

    @Override
    protected void onBindView(@Nullable Bundle savedInstanceState) {
        init();
    }

    private void init() {
        mBTListAdapter = new BTListAdapter(this, mData);
        rcvDevices.setLayoutManager(new LinearLayoutManager(this));
        rcvDevices.setAdapter(mBTListAdapter);
        getBlueDeviceList();
        mBTListAdapter.setOnItemClickListener((parent, view, position, id) -> {
            BluetoothDevice device = mData.get(position);
            Bluetooth.setOnBondState(device, new Bluetooth.OnBondState() {
                @Override
                public void bondSuccess() {
                    if (progressDialog != null && progressDialog.isShowing())
                        progressDialog.dismiss();
                    Intent intent = new Intent();
                    intent.putExtra(Intent.EXTRA_RETURN_RESULT, device.getAddress());
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });
            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                Intent intent = new Intent();
                intent.putExtra(Intent.EXTRA_RETURN_RESULT, device.getAddress());
                setResult(RESULT_OK, intent);
                finish();
            } else {
                progressDialog = new ProgressDialog(BTListActivity.this);
                progressDialog.setMessage(getString(R.string.bt_connect));
                progressDialog.show();
                new Thread(device::createBond).start();
            }
        });
    }

    private void getBlueDeviceList() {
        if ((mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()) == null) {
            Toast.makeText(this, "没有找到蓝牙适配器", Toast.LENGTH_LONG).show();
            return;
        }
       /* if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 2);
        }*/
        mBluetooth = Bluetooth.getBluetooth(this);
        initBT();
        swipeRefreshLayout.setOnRefreshListener(() -> {
            initBT();
            if (swipeRefreshLayout.isRefreshing())
                swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void initBT() {
        mData.clear();
        mBTListAdapter.notifyDataSetChanged();
        tvSearchTip.setVisibility(View.VISIBLE);
        mBluetooth.doDiscovery();
        mBluetooth.getData(new Bluetooth.toData() {
            @Override
            public void succeed(BluetoothDevice bluetoothDevice) {
                for (BluetoothDevice printBT : mData) {
                    if (bluetoothDevice.getAddress().equals(printBT.getAddress())) {
                        return;
                    }
                }
                mData.add(bluetoothDevice);
                mBTListAdapter.notifyDataSetChanged();
                if(mData.size()>0){
                    tvSearchTip.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    @Override
    protected IBaseActivity createView() {
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBluetooth != null) {
            mBluetooth.disReceiver();
        }
    }
}
