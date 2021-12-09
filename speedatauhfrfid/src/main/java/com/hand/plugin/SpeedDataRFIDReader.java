package com.hand.plugin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import com.google.gson.Gson;
import com.hand.baselibrary.jsbridge.CallbackContext;
import com.hand.baselibrary.jsbridge.HippiusPlugin;
import com.hand.baselibrary.utils.LogUtils;
import com.speedata.libuhf.IUHFService;
import com.speedata.libuhf.UHFManager;
import com.speedata.libuhf.bean.SpdInventoryData;
import com.speedata.libuhf.interfaces.OnSpdInventoryListener;

import org.json.JSONObject;

import java.util.Objects;

import androidx.annotation.Nullable;


public class SpeedDataRFIDReader extends HippiusPlugin {
    private static final String TAG = "SpeedDataRFIDReader";
    private IUHFService mUHFService;
    private boolean hasInit = false;
    private final static String ACTION_INIT = "init";
    private final static String ACTION_START_SCAN = "startScan";//寻卡
    private final static String ACTION_STOP_SCAN = "stopScan";//停止寻卡
    private final static String ACTION_REGISTER_RECEIVER = "registerReceiver";
    private final static String ACTION_UNREGISTER_RECEIVER = "unRegisterReceiver";
    private final static String ACTION_RELEASE = "release";//释放资源，释放资源后，再次扫描需要重新init
    private final static String ACTION_OPEN_SOUND = "openSound";
    private final static String ACTION_CLOSE_SOUND = "closeSound";

    private CallbackContext mSpeedScanCallbackContext;
    private SoundPool soundPool;
    private int soundId;
    private Gson mGson;
    private int freqRegion;
    private int antennaPower;
    private boolean needSound;
    private boolean inSearch;//是否正在扫描
    private boolean onceScan = false;

    private boolean init() {
        try {
            if (!hasInit) {
                hasInit = true;
                mUHFService = UHFManager.getUHFService(getActivity());
            }
        } catch (Exception ignore) {
        }
        return mUHFService != null;
    }


    @Override
    public String execute(String action, JSONObject args, CallbackContext callbackContext) {

        if (ACTION_INIT.equals(action)) {
            freqRegion = args.optInt("freqRegion", 1);
            antennaPower = args.optInt("antennaPower", 30);
            boolean flag = init();
            if (!flag) {
                callbackContext.onError(makeResult(-1, "init error"));
                return null;
            } else {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        openDev(callbackContext);
                    }
                }, 1000);
            }
        } else if (ACTION_START_SCAN.equals(action)) {
            if (hasInit) {
                needSound = args.optBoolean("needSound", false);
                onceScan = args.optBoolean("onceScan", false);
                mSpeedScanCallbackContext = callbackContext;
                startScan(callbackContext);
            } else {
                callbackContext.onError(makeResult(-1, "not init"));
            }
        } else if (ACTION_STOP_SCAN.equals(action)) {
            if (hasInit) {
                stopScan(callbackContext);
            } else {
                callbackContext.onError(makeResult(-1, "not init"));
            }
        } else if (ACTION_REGISTER_RECEIVER.equals(action)) {
            if (hasInit) {
                needSound = args.optBoolean("needSound", false);
                onceScan = args.optBoolean("onceScan", false);
                mSpeedScanCallbackContext = callbackContext;
                initReceiver();
            } else {
                callbackContext.onError(makeResult(-1, "not init"));
            }
        } else if (ACTION_UNREGISTER_RECEIVER.equals(action)) {
            if (hasInit) {
                mSpeedScanCallbackContext = null;
                unRegisterReceiver();
                stopScan(null);
            } else {
                callbackContext.onError(makeResult(-1,"not init"));
            }
        } else if (ACTION_RELEASE.equals(action)) {
            if(hasInit) {
                release();
            }else{
                callbackContext.onError(makeResult(-1,"not init"));
            }
        } else if (ACTION_OPEN_SOUND.equals(action)) {
            needSound = true;
            callbackContext.onSuccess(makeResult(1, "openSoundSuccess"));
        } else if (ACTION_CLOSE_SOUND.equals(action)) {
            needSound = false;
            callbackContext.onSuccess(makeResult(1, "closeSoundSuccess"));
        }
        return null;
    }

    private void unRegisterReceiver() {
        try {
            getActivity().unregisterReceiver(receiver);
        } catch (Exception ignore) {
        }
    }

    private void initReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(START_SCAN);
        filter.addAction(STOP_SCAN);
        getActivity().registerReceiver(receiver, filter);
    }

    public static final String START_SCAN = "com.spd.action.start_uhf";
    public static final String STOP_SCAN = "com.spd.action.stop_uhf";
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("RECEIVE", "================" + intent.getAction());
            String action = intent.getAction();
            switch (Objects.requireNonNull(action)) {
                case START_SCAN:
                    //启动超高频扫描 Start uhf scan
                    if (inSearch) {
                        return;
                    }
                    startScan(mSpeedScanCallbackContext);
                    break;
                case STOP_SCAN:
                    if (inSearch) {
                        stopScan(null);
                    }
                    break;
                default:
                    break;
            }
        }
    };


    private void startScan(CallbackContext callbackContext) {
        if (mUHFService == null) {
            callbackContext.onError(makeResult(-3, "not init"));
            return;
        }
        if (inSearch) {
            return;
        }

        //取消掩码 Cancel the mask
        mUHFService.selectCard(1, "", false);
        mUHFService.inventoryStart();
        inSearch = true;
    }

    private void stopScan(@Nullable CallbackContext callbackContext) {
        if (mUHFService == null || !inSearch) {
            if (callbackContext != null) {
                callbackContext.onError(makeResult(-4, "no scan runing"));
            }
            return;
        }
        mUHFService.inventoryStop();
        if (callbackContext != null) {
            callbackContext.onSuccess(makeResult(1, "stop success"));
        }
        inSearch = false;
    }


    private void openDev(CallbackContext callbackContext) {
        try {
            int result = mUHFService.openDev();
            if (result == 0) {
                initParam();
                callbackContext.onSuccess(makeResult(1, "open dev success"));
            } else {
                hasInit = false;
                callbackContext.onError(makeResult(-2, "open dev error"));
            }
        } catch (Exception e) {
            hasInit = false;
            callbackContext.onError(makeResult(-3, "open dev error"));
        }
    }

    private void initParam() {
        initSoundPool();
        mGson = new Gson();
        int i;
        i = mUHFService.setFreqRegion(freqRegion);
        SystemClock.sleep(600);
        i = mUHFService.setAntennaPower(antennaPower);
        Log.d("zzc:", "===isFirstInit===setAntennaPower:" + i);
        SystemClock.sleep(100);
        if (!UHFManager.getUHFModel().equals(UHFManager.FACTORY_YIXIN)) {
            i = mUHFService.setQueryTagGroup(0, 0, 0);
            Log.d("zzc:", "===isFirstInit===setQueryTagGroup:" + i);
        }
        SystemClock.sleep(100);
        i = mUHFService.setInvMode(0, 0, 12);
        Log.d("zzc:", "===isFirstInit===setInvMode:" + i);
        if (UHFManager.getUHFModel().contains(UHFManager.FACTORY_XINLIAN)) {
            mUHFService.setLowpowerScheduler(50, 0);
            mUHFService.setTagfoucs(true);
        }
        mUHFService.setOnInventoryListener(new OnSpdInventoryListener() {
            @Override
            public void getInventoryData(SpdInventoryData var1) {
                LogUtils.e(TAG, "epc:" + var1.epc + "  rssi:" + var1.rssi + "  tid" + var1.tid);
                // handler.sendMessage(handler.obtainMessage(1, var1));
                Log.d("UHFService", "回调");
                if (needSound) {
                    soundPool.play(soundId, 1, 1, 0, 0, 1);
                }
                if (mSpeedScanCallbackContext != null) {
                    mSpeedScanCallbackContext.onSuccess(mGson.toJson(var1));
                }
                if (onceScan) {
                    stopScan(null);
                }
            }

            @Override
            public void onInventoryStatus(int status) {
                /*if (jishu % 99 == 0) {
                    handler.sendMessage(handler.obtainMessage(-1, status));
                    jishu = 0;
                }
                Log.d("UHFService", "盘点失败" + status);
                jishu++;*/
                mUHFService.inventoryStart();
            }
        });
    }

    public void initSoundPool() {
        soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        soundId = soundPool.load("/system/media/audio/ui/VideoRecord.ogg", 0);
    }


    private void release() {
        if (inSearch) {
            stopScan(null);
        }
        hasInit = false;
        if (mUHFService != null) {
            mUHFService.closeDev();
            mUHFService = null;
            UHFManager.closeUHFService();
        }
    }

    private String makeResult(int code, String message) {
        JSONObject object = new JSONObject();
        try {
            object.put("code", code);
            object.put("message", message);
        } catch (Exception ignore) {
        }
        return object.toString();
    }

    @Override
    protected void onDestroyView() {
        if (receiver != null) {
            unRegisterReceiver();
        }
        super.onDestroyView();
        release();
    }
}
