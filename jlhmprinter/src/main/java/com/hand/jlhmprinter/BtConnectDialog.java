package com.hand.jlhmprinter;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.hand.baselibrary.config.Hippius;
import com.hand.baselibrary.utils.LogUtils;

public class BtConnectDialog extends ProgressDialog {
    private static final String TAG = "BtConnectDialog";
    private Context mContext;

    public BluetoothAdapter mBluetoothAdapter;
    private Bluetooth mBluetooth;
    private Callback mCallback;

    public BtConnectDialog(Context context, Callback callback) {
        super(context);
        mContext = context;
        mCallback = callback;
        getBlueDeviceList();
    }

    private void getBlueDeviceList() {
        if ((mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()) == null) {
            Toast.makeText(Hippius.getApplicationContext(),
                    "没有找到蓝牙适配器", Toast.LENGTH_LONG).show();
            return;
        }
        mBluetooth = Bluetooth.getBluetooth(mContext);
        initBT();
    }

    private BluetoothDevice device;
    private boolean hasReceive = false;

    private void initBT() {
        mBluetooth.doDiscovery();
        mBluetooth.getData(bluetoothDevice -> {
            if (bluetoothDevice != null && bluetoothDevice.getAddress() != null && !hasReceive) {
                hasReceive = true;
                device = bluetoothDevice;
                mBluetooth.disReceiver();
                new Handler(Looper.getMainLooper()).post(() -> doConnect());
            }
        });
    }

    private void doConnect() {
        LogUtils.e(TAG,"doConnect"+device.getName()+"==="+device.getAddress());
        Bluetooth.setOnBondState(device, new Bluetooth.OnBondState() {
            @Override
            public void bondSuccess() {
                new Handler(Looper.getMainLooper()).post(() -> onResult());
            }
        });
        if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
            onResult();
        } else {
            new Thread(device::createBond).start();
        }
    }

    private void onResult() {
        if (mCallback != null) {
            mCallback.onResult(device.getAddress());
        }
    }

    public interface Callback {
        void onResult(String address);
    }
}
