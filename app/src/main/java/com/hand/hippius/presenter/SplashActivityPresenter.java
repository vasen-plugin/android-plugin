package com.hand.hippius.presenter;

import android.os.Handler;
import android.os.Looper;

import com.hand.baselibrary.activity.BasePresenter;
import com.hand.baselibrary.activity.ISplashActivity;
import com.hand.baselibrary.bean.HippiusConfig;
import com.hand.baselibrary.bean.UserDeviceStatus;
import com.hand.baselibrary.config.ConfigKeys;
import com.hand.baselibrary.config.Constants;
import com.hand.baselibrary.config.Hippius;
import com.hand.baselibrary.config.SPConfig;
import com.hand.baselibrary.dto.AppVersionResponse;
import com.hand.baselibrary.dto.DeviceInfo;
import com.hand.baselibrary.dto.DeviceResponse;
import com.hand.baselibrary.net.RetrofitClient;
import com.hand.baselibrary.utils.DeviceUtil;
import com.hand.baselibrary.utils.LogUtils;
import com.hand.baselibrary.utils.TimerRecord;
import com.hand.baselibrary.utils.Utils;
import com.hand.mainlibrary.net.ApiService;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SplashActivityPresenter extends BasePresenter<ISplashActivity> {

    private static final String TAG = "SplashActivityPresenter";
    ApiService apiService;

    public SplashActivityPresenter() {
        apiService = RetrofitClient.getInstance().getService(ApiService.class);
    }

    //仅在上一次登录后，提交设备信息失败才会在splash页重新提交设备信息
    public void collectDevice() {
        if ((Hippius.getAccessToken() != null
                && SPConfig.getString(ConfigKeys.SP_DEVICE_ID_FROM_SERVER, null) == null)) {
            RetrofitClient.getInstance().setConnectTimeOut(Constants.SHORT_TIME_OUT);
            Disposable disposable = apiService.collectDevice(DeviceInfo.getDeviceInfo())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::onCollectAccept, this::onError);

        } else {
            getView().onCollectDeviceSuccess();
        }
    }

    private void onCollectAccept(DeviceResponse deviceResponse) {
        if (!deviceResponse.isFailed()) {
            LogUtils.e(TAG, deviceResponse.getDeviceId() + "");
            SPConfig.putString(ConfigKeys.SP_DEVICE_ID_FROM_SERVER,
                    deviceResponse.getDeviceId());
            SPConfig.putString(ConfigKeys.APP_VERSION, DeviceUtil.getAppVersion());
        }
        RetrofitClient.getInstance().setConnectTimeOut(Constants.TIME_OUT);
        getView().onCollectDeviceSuccess();
    }

    private void onError(Throwable throwable) {
        RetrofitClient.getInstance().setConnectTimeOut(Constants.TIME_OUT);
        getView().onCollectDeviceSuccess();
    }

    //获取设备是否需要擦除，不影响后续登录进度，若状态是擦除，则直接清除token，后续重新登录时会进行擦除数据的操作
    public void getDeviceStatus() {
        Disposable disposable = apiService.getUserDeviceStatus(DeviceUtil.getDeviceID())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onUserDeviceAccept,this::onUserDeviceAcceptError);
    }

    private void onUserDeviceAccept(UserDeviceStatus userDeviceStatus) {
        if(!userDeviceStatus.isFailed()&&userDeviceStatus.isWipe()){
            Hippius.setAccessToken("");
        }
    }

    private void onUserDeviceAcceptError(Throwable throwable) {

    }

    public void getGlobeAppInfo() {
        String applicationId = Hippius.getApplicationContext().getPackageName();
        RetrofitClient.getInstance().setConnectTimeOut(Constants.SHORT_TIME_OUT);
        Disposable disposable = apiService.checkAppUpdatePublic(applicationId,
                DeviceUtil.getAppVersion(), "android", DeviceUtil.getDeviceType())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onGlobeAppInfo, this::onGlobeAppInfoError);
    }


    private void onGlobeAppInfo(AppVersionResponse appVersionResponse) {
        RetrofitClient.getInstance().setConnectTimeOut(Constants.TIME_OUT);
        if (!appVersionResponse.isFailed()) {
            if ("Y".equals(appVersionResponse.getKeyEncrypt())) {
                Hippius.putConfig(ConfigKeys.ENCRYPT_PRIMARY_KEY, true);
            } else {
                Hippius.putConfig(ConfigKeys.ENCRYPT_PRIMARY_KEY, false);
            }
            getView().onGlobalAppInfo(true, appVersionResponse, null);
        } else {
            Hippius.putConfig(ConfigKeys.ENCRYPT_PRIMARY_KEY, false);
            getView().onGlobalAppInfo(false, appVersionResponse, appVersionResponse.getMessage());
        }
    }

    private void onGlobeAppInfoError(Throwable e) {
        RetrofitClient.getInstance().setConnectTimeOut(Constants.TIME_OUT);
        Hippius.putConfig(ConfigKeys.ENCRYPT_PRIMARY_KEY, false);
        String[] errors = getError(e);
        getView().onGlobalAppInfo(false, null, errors[1]);
    }

}
