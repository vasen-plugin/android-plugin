package com.hand.plugin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.launcher.ARouter;
import com.hand.TestActivity;
import com.hand.baselibrary.communication.IQRCodeProvider;
import com.hand.baselibrary.jsbridge.CallbackContext;
import com.hand.baselibrary.jsbridge.HippiusPlugin;
import com.hand.baselibrary.utils.LogUtils;
import com.hand.baselibrary.utils.StringUtils;
import com.hand.baselibrary.utils.Utils;
import com.hand.jlhmprinter.BTListActivity;
import com.hand.jlhmprinter.Bluetooth;
import com.hand.jlhmprinter.BtConnectDialog;
import com.hand.jlhmprinter.R;
import com.hand.template.TemplatePrinterFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import androidx.annotation.Nullable;
import cpcl.PrinterHelper;


public class HMPrinter extends HippiusPlugin implements BtConnectDialog.Callback {
    private static final String TAG = "HMPrinter";

    /**
     * btAdapter为null |  已开启  | 已经调用开启方法，状态未知
     */
    private enum ENABLE_BT_STATUS {
        EMPTY_BT, ENABLE, OPENING
    }

    /**
     * 打印设备已连接 ｜ 打印设备正在连接 ｜ 打印设备已断开
     */
    private enum DEVICE_CONNECT_STATUS {
        CONNECTED, CONNECTING, DISCONNECT
    }

    public final static String ACTION_ENABLE_BT = "enableBT";//开启手机蓝牙
    public final static String ACTION_CONNECT_BY_BT = "connectByBT";//通过蓝牙连接打印机
    public final static String ACTION_DISCONNECT = "disconnect";//断开与打印机的连接
    public final static String ACTION_PRINT = "print";//打印
    private TemplatePrinterFactory mTemplatePrinterFactory;
    private CallbackContext mBTConnectCallbackContext;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private DEVICE_CONNECT_STATUS mConnectStatus = DEVICE_CONNECT_STATUS.DISCONNECT;
    private boolean autoConnect;

    @Autowired
    IQRCodeProvider mQRCodeProvider;

    public HMPrinter() {
        ARouter.getInstance().inject(this);
    }

    public Bitmap crateBitmap(int length, String code) {
        Bitmap bitmap = null;
        if (mQRCodeProvider != null) {
            bitmap = mQRCodeProvider.createQRCode(code, length, null);
        } else {
            Toast.makeText(getActivity().getApplicationContext(),
                    "QRCode Provider is null", Toast.LENGTH_SHORT).show();
        }
        return bitmap;
    }

    @Override
    public String execute(String action, JSONObject args, CallbackContext callbackContext) {
        if (ACTION_ENABLE_BT.equals(action)) {
            ENABLE_BT_STATUS status = enableBT();
            callbackContext.onSuccess(status.name());
        } else if (ACTION_CONNECT_BY_BT.equals(action)) {
            mConnectStatus = DEVICE_CONNECT_STATUS.CONNECTING;
            autoConnect = args.optBoolean("autoConnect", false);
            getHandler().post(() -> preConnectByBt(callbackContext));
        } else if (ACTION_DISCONNECT.equals(action)) {
            getHandler().post(() -> disconnect(callbackContext));
        } else if (ACTION_PRINT.equals(action)) {
           // print(args, callbackContext);
            if (mConnectStatus == DEVICE_CONNECT_STATUS.CONNECTED) {
                print(args, callbackContext);
            } else if (mConnectStatus == DEVICE_CONNECT_STATUS.DISCONNECT) {
                callbackContext.onError(makeResult(-1, "请先连接打印机"));
            } else if (mConnectStatus == DEVICE_CONNECT_STATUS.CONNECTING) {
                callbackContext.onError(makeResult(-2, "打印机连接中，请稍后！"));
            }
            // TestActivity.startActivity(getActivity());
        }
        return null;
    }

    private void print(JSONObject args, CallbackContext callbackContext) {
        if (mTemplatePrinterFactory == null) {
            mTemplatePrinterFactory = new TemplatePrinterFactory(this);
        }
        try {
            JSONArray array = args.getJSONArray("list");
            mTemplatePrinterFactory.addTask(array);
            mTemplatePrinterFactory.execute();
        } catch (JSONException e) {
            e.printStackTrace();
            callbackContext.onError("no list found");
        }

    }

    /**
     * 断开蓝牙设备连接
     */
    private void disconnect(@Nullable CallbackContext callbackContext) {
        boolean success = false;
        String message = "";
        try {
            if (mConnectDialog != null) {
                mConnectDialog.dismiss();
            }
            PrinterHelper.portClose();
            success = true;
        } catch (Exception e) {
            message = e.getMessage();
            e.printStackTrace();
        }
        if (callbackContext != null) {
            if (success) {
                mConnectStatus = DEVICE_CONNECT_STATUS.DISCONNECT;
                callbackContext.onSuccess(makeResult(0, "success"));
            } else {
                callbackContext.onSuccess(makeResult(-1, message));
            }
        }
    }

    /**
     * 打开手机蓝牙
     */
    @SuppressLint("MissingPermission")
    private ENABLE_BT_STATUS enableBT() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            if (mBluetoothAdapter.isEnabled()) {
                return ENABLE_BT_STATUS.ENABLE;
            } else {
                mBluetoothAdapter.enable();
                return ENABLE_BT_STATUS.OPENING;
            }
        } else {
            return ENABLE_BT_STATUS.EMPTY_BT;
        }
    }

    //获取蓝牙权限
    private void preConnectByBt(CallbackContext callbackContext) {
        this.mBTConnectCallbackContext = callbackContext;
        if (hasPermissions(Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            connectByBt();
        } else {
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.ACCESS_FINE_LOCATION}, 214);
        }
    }

    private Runnable startBTListRunnable = new Runnable() {
        @Override
        public void run() {
            if (mBluetoothAdapter.isEnabled()) {
                if (btProgressDialog != null) {
                    btProgressDialog.dismiss();
                }
                doConnect();
            } else {
                getHandler().postDelayed(this, 100);
            }
        }
    };
    ProgressDialog btProgressDialog;

    //获取蓝牙设备列表
    private void connectByBt() {
        if (mBluetoothAdapter == null) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
            btProgressDialog = new ProgressDialog(getActivity());
            btProgressDialog.setMessage(Utils.getString(R.string.bt_open));
            btProgressDialog.show();
            getHandler().postDelayed(startBTListRunnable, 500);
        } else {
            doConnect();
        }
    }

    private BtConnectDialog mConnectDialog;

    private void doConnect() {
        Set<BluetoothDevice> bonedDevices = mBluetoothAdapter.getBondedDevices();
        List<BluetoothDevice> bonedPrintDevices = new ArrayList<>();
        for (BluetoothDevice bluetoothDevice : bonedDevices) {
            if (bluetoothDevice.getBluetoothClass().getMajorDeviceClass() == 1536) {
                bonedPrintDevices.add(bluetoothDevice);
            }
        }
        if (bonedPrintDevices.size() == 1) {
            connectDV(bonedPrintDevices.get(0).getAddress(), true);
        } else {
            doConnectWithList();
        }
    }

    private void doConnectWithList() {
        Intent intent = new Intent(getActivity(), BTListActivity.class);
        startActivityForResult(intent, 211);
    }

    //通过蓝牙连接打印设备
    private void connectDV(String btAddress, boolean fromBond) {
        if (StringUtils.isEmpty(btAddress)) {
            mConnectStatus = DEVICE_CONNECT_STATUS.DISCONNECT;
            return;
        }
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(Utils.getString(R.string.bt_connect));
        progressDialog.show();
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    final int result = PrinterHelper.portOpenBT(btAddress);
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (result == 0) {
                                mConnectStatus = DEVICE_CONNECT_STATUS.CONNECTED;
                                onConnectBTResult(true, progressDialog, result, null, fromBond);
                            } else {
                                mConnectStatus = DEVICE_CONNECT_STATUS.DISCONNECT;
                                onConnectBTResult(false, progressDialog, result, "Connect Error", fromBond);
                            }
                        }
                    });
                } catch (Exception e) {
                    mConnectStatus = DEVICE_CONNECT_STATUS.DISCONNECT;
                    onConnectBTResult(false, progressDialog, -100, e.getMessage(), fromBond);
                }
            }
        }.start();
    }

    private void onConnectBTResult(boolean success, ProgressDialog progressDialog,
                                   int result, String message, boolean fromBond) {
        new Handler(Looper.getMainLooper()).post(() -> {
            progressDialog.dismiss();
            if (success) {
                mBTConnectCallbackContext.onSuccess(makeResult(result, "success"));
            } else {
                if (fromBond) {
                    doConnectWithList();
                } else {
                    mBTConnectCallbackContext.onError(makeResult(result, makeResult(result, message)));
                }
            }
        });
    }

    private String makeResult(int code, String message) {
        JSONObject object = new JSONObject();
        try {
            object.put("code", code);
            object.put("message", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }

    public Handler getHandler() {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }
        return mHandler;
    }

    @Override
    protected void onDestroyView() {
        super.onDestroyView();
        disconnect(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == 211 && resultCode == Activity.RESULT_OK) {
            if (intent != null) {
                String address = intent.getStringExtra(Intent.EXTRA_RETURN_RESULT);
                connectDV(address, false);
            }
        }
    }

    @Override
    public void onResult(String address) {
        LogUtils.e(TAG, "onResult");
        connectDV(address, false);
        if (mConnectDialog != null) {
            mConnectDialog.dismiss();
        }
    }

    @Override
    protected void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 214) {
            connectByBt();
        }
    }
}
