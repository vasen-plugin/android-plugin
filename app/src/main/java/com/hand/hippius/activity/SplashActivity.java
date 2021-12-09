package com.hand.hippius.activity;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;

import androidx.annotation.Nullable;

import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.launcher.ARouter;
import com.google.gson.Gson;
import com.hand.baselibrary.ResultCallback;
import com.hand.baselibrary.activity.BaseAppActivity;
import com.hand.baselibrary.activity.ISplashActivity;
import com.hand.baselibrary.bean.AppMaintenanceList;
import com.hand.baselibrary.bean.Application;
import com.hand.baselibrary.bean.HippiusConfig;
import com.hand.baselibrary.communication.IAppsProvider;
import com.hand.baselibrary.communication.IMessageProvider;
import com.hand.baselibrary.config.ConfigKeys;
import com.hand.baselibrary.config.Constants;
import com.hand.baselibrary.config.Hippius;
import com.hand.baselibrary.config.SPConfig;
import com.hand.baselibrary.dto.AppVersionResponse;
import com.hand.baselibrary.greendao.GreenDaoManager;
import com.hand.baselibrary.greendao.bean.TAdsForbidden;
import com.hand.baselibrary.greendao.bean.TAdsInfo;
import com.hand.baselibrary.greendao.gen.TAdsForbiddenDao;
import com.hand.baselibrary.greendao.gen.TAdsInfoDao;
import com.hand.baselibrary.network_monitor.NetworkUtils;
import com.hand.baselibrary.utils.DeviceUtil;
import com.hand.baselibrary.utils.ImageLoadUtils;
import com.hand.baselibrary.utils.LogUtils;
import com.hand.baselibrary.utils.StringUtils;
import com.hand.baselibrary.utils.Utils;
import com.hand.baselibrary.widget.FullVideoView;
import com.hand.baselibrary.widget.TipDialog;
import com.hand.hippius.R;
import com.hand.hippius.R2;
import com.hand.hippius.presenter.SplashActivityPresenter;
import com.hand.loginbaselibrary.FragmentProvider;
import com.hand.mainlibrary.activity.HomeActivity;
import com.hand.webview.WebActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SplashActivity extends BaseAppActivity<SplashActivityPresenter, ISplashActivity>
        implements ISplashActivity {

    private static final String TAG = "SplashActivity";
    private final int PAGE_GUIDE = 0;
    private final int PAGE_LOGIN = 1;
    private final int PAGE_HOME = 2;
    private final int PAGE_LOGIN_UNLOCK_FINGERPRINT = 3;
    private final int PAGE_LOGIN_UNLOCK_PATTERN = 4;
    private String userId;

    @Autowired
    IMessageProvider messageProvider;
    @Autowired
    IAppsProvider appsProvider;
    @BindView(R2.id.rlt_ads)
    RelativeLayout rltAds;
    @BindView(R2.id.tv_skip_time)
    TextView tvSkipTime;
    @BindView(R2.id.img_adds)
    ImageView imgAdds;
    @BindView(R2.id.view_bottom)
    View vBottom;
    @BindView(R2.id.tv_no_show_again)
    TextView tvNoShowAgain;
    @BindView(R2.id.bar)
    View bar;
    @BindView(R2.id.video_adds)
    FullVideoView videoView;
    TAdsInfoDao tAdsInfoDao;
    TAdsForbiddenDao tAdsForbiddenDao;
    String nowTime;

    @Override
    protected Object setLayout() {
        return R.layout.app_activity_splash;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNavigationBarStatusBarTranslucent();
        readIntent(getIntent());
        if (!isTaskRoot()) {
            boolean restart = getIntent().getBooleanExtra("RESTART", false);
            if (!restart) {
                finish();
                return;
            }
        }
        ARouter.getInstance().inject(this);
        tAdsInfoDao = GreenDaoManager.getInstance().getDaoSession().getTAdsInfoDao();
        tAdsForbiddenDao = GreenDaoManager.getInstance().getDaoSession().getTAdsForbiddenDao();
        setTime();
        setViewBottomHeight();
        FragmentProvider.getInstance().setGlobalBaseUrl();
        if (Hippius.getConfig(ConfigKeys.HIPPIUS_CONFIG) == null || isIMNeedConnected()) {
            initConfig();
        } else {
            onConfigComplete(true);
        }
    }

    private void readIntent(Intent intent) {
        Uri uri = intent.getData();
        if (uri != null) {
            getIntent().putExtra("RESTART", true);
            LogUtils.e(TAG, uri.toString() + "");
            String t = uri.getQueryParameter("t");
            if ("submenu".equals(t)) {
                LogUtils.e(TAG, uri.toString() + "==" + t);
                Hippius.putConfig(ConfigKeys.SCHEME_OPEN_APP, uri);
            }
            if ("third_login".equals(t)) {
                Hippius.putConfig(ConfigKeys.SCHEME_THIRD_PARTY_AUTH, uri);
            }
        }
    }

    public void setNavigationBarStatusBarTranslucent() {
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setNavigationBarColor(Color.TRANSPARENT);
            //getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    private boolean isIMNeedConnected() {
        //如果im未连接则需要重新初始化应用
        if (Constants.IM_ENABLE && messageProvider != null) {
            if (!messageProvider.isConnected()) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected AppMaintenanceList.MaintenanceInfo getMaintenanceInfo(String menuId) {
        if (appsProvider != null) {
            return appsProvider.getMaintenanceInfoByMenuId(menuId);
        }
        return null;
    }


    @Override
    protected void onBindView(@Nullable Bundle savedInstanceState) {
    }

    private void setTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault());
        Date date = new Date(System.currentTimeMillis());
        nowTime = simpleDateFormat.format(date);
    }

    private void setViewBottomHeight() {
        int viewBottomHeight = DeviceUtil.getSplashIconHeight();
        View bottomView = findViewById(R.id.view_bottom);
        ViewGroup.LayoutParams layoutParams = bottomView.getLayoutParams();
        //正常不应该减，-50底部没有明显差别，但是图可以显示的跟全些
        if (viewBottomHeight > 540) {
            viewBottomHeight = viewBottomHeight - 50;
        }
        layoutParams.height = viewBottomHeight;
    }

    //读取配置文件
    private void initConfig() {
        FragmentProvider.getInstance().setGlobalBaseUrl();
        Disposable disposable = Observable.create(new ObservableOnSubscribe<HippiusConfig>() {
            @Override
            public void subscribe(ObservableEmitter<HippiusConfig> emitter) throws Exception {
                if (userId == null) {
                    userId = SPConfig.getString(ConfigKeys.SP_USERID, "");
                }
                HippiusConfig hippiusConfig = getHippiusConfig();
                emitter.onNext(hippiusConfig);
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onHippiusConfigAccept, this::onHippiusConfigError,
                        this::onHippiusConfigComplete);
        //获取设备擦除状态
        if(!StringUtils.isEmpty(Hippius.getAccessToken())) {
            getPresenter().getDeviceStatus();
        }
        //获取app全局配置信息
        getPresenter().getGlobeAppInfo();
        //连接im
        if (Constants.IM_ENABLE && messageProvider != null) {
            if (userId == null) {
                userId = SPConfig.getString(ConfigKeys.SP_USERID, "");
            }
            String imToken = SPConfig
                    .getString(ConfigKeys.SP_IM_TOKEN + Constants.IM_PLATFORM, "");
            messageProvider.connectIM(userId, imToken, new ResultCallback() {
                @Override
                public void onSuccess(Object o) {
                    LogUtils.e(TAG, "IM CONNECT SUCCESS");
                    messageProvider.sendMessageUpdateEvent(true);
                }

                @Override
                public void onError(int code, String message) {
                    LogUtils.e(TAG, "IM CONNECT ERROR");
                }
            });
            onIMConnectComplete();

        } else {
            onIMConnectComplete();
        }
    }

    private boolean globalInfoComplete = false;
    private boolean hippiusConfigComplete = false;

    @Override
    public void onGlobalAppInfo(boolean success, AppVersionResponse appVersionResponse, String message) {
        if (success&&appVersionResponse.isForbidDeviceBrand()) {
            showDeviceForbidDialog();
            return;
        }
        globalInfoComplete = true;
        Hippius.putConfig(ConfigKeys.GLOBAL_SET_INFO, appVersionResponse);
        if (hippiusConfigComplete) {
            onConfigComplete(false);
        }
    }

    public void showDeviceForbidDialog() {
        TipDialog.Builder builder = new TipDialog.Builder()
                .setContent(Utils.getString(R.string.base_only_specified_device))
                .setOkText(Utils.getString(R.string.base_ok))
                .setCancelable(false)
                .setContentCenter()
                .setOnOkClickListener((dialog, which) -> {
                    dialog.dismiss();
                    onBackPressedSupport();
                    new Handler(getMainLooper()).postDelayed(() -> {
                        System.exit(0);
                    }, 300);
                });
        TipDialog tipDialog = builder.build(this);
        tipDialog.show();
    }

    private void onHippiusConfigComplete() {
        hippiusConfigComplete = true;
        if (globalInfoComplete) {
            onConfigComplete(false);
        }
    }

    private void onHippiusConfigAccept(HippiusConfig config) {
        Hippius.putConfig(ConfigKeys.HIPPIUS_CONFIG, config);
    }

    private void onHippiusConfigError(Throwable e) {
        Toast("There are something error when read " +
                "application config,please connect developer!");
    }

    private int nextPageType;

    private void onConfigComplete(boolean startFromAlive) {
        nextPageType = getPageType();
        LogUtils.e(TAG, nextPageType + "=============");
        switch (nextPageType) {
            case PAGE_GUIDE:
                ARouter.getInstance().build("/guide/guideactivity").navigation();
                SPConfig.putBoolean(ConfigKeys.FIRST_LAUNCH, false);
                finish();
                break;
            case PAGE_LOGIN:
                Utils.login(null);
                finish();
                break;
            case PAGE_HOME:
            case PAGE_LOGIN_UNLOCK_FINGERPRINT:
            case PAGE_LOGIN_UNLOCK_PATTERN:
                if (startFromAlive) {
                    onAdsComplete(true);
                } else {
                    getPresenter().collectDevice();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onCollectDeviceSuccess() {
        if (!showAds()) {
            onAdsComplete(false);
        }
    }

    private boolean adsFinish = false;

    private void onAdsComplete(boolean startFromAlive) {
        if (startFromAlive) {
            adsFinish = true;
            openNext(startFromAlive);
        } else {
            adsFinish = true;
            if (imConnectFinish) {
                openNext(false);
            }
        }
    }

    private boolean imConnectFinish = false;

    private void onIMConnectComplete() {
        imConnectFinish = true;
        if (adsFinish) {
            openNext(false);
        }
    }

    private void openNext(boolean startFromAlive) {
        switch (nextPageType) {
            case PAGE_HOME:
                HomeActivity.startActivity(this);
                finish(startFromAlive);
                break;
            case PAGE_LOGIN_UNLOCK_FINGERPRINT:
                Bundle bundle = new Bundle();
                bundle.putInt(Constants.LoginPage.TYPE,
                        Constants.LoginPage.TYPE_UNLOCK_FINGERPRINT);
                Utils.login(bundle);
                finish(startFromAlive);
                break;
            case PAGE_LOGIN_UNLOCK_PATTERN:
                Bundle bundle2 = new Bundle();
                bundle2.putInt(Constants.LoginPage.TYPE,
                        Constants.LoginPage.TYPE_UNLOCK_PATTERN);
                Utils.login(bundle2);
                finish(startFromAlive);
                break;
            default:
                break;
        }
    }


    public void finish(boolean startFromAlive) {
        LogUtils.e(TAG, "7=============");
        if (!startFromAlive) {
            overridePendingTransition(R.anim.h_fragment_enter, R.anim.slide_out_left);
        }
        super.finish();
    }

    @OnClick(R2.id.tv_no_show_again)
    void onNoShowAgain(View view) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (tAdsInfo != null) {
            String userId = getUserId();
            TAdsForbidden tAdsForbidden = new TAdsForbidden();
            tAdsForbidden.setUserId(userId);
            tAdsForbidden.setAdsId(tAdsInfo.getId());
            tAdsForbiddenDao.insertInTx(tAdsForbidden);
        }
        onAdsComplete(false);
    }

    @OnClick(R2.id.tv_skip_time)
    void onSkipTime() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        LogUtils.e(TAG, "1=============");
        onAdsComplete(false);
    }

    @OnClick({R2.id.img_adds, R2.id.video_adds})
    void onAddsClick(View v) {
        if (tAdsInfo != null &&
                !StringUtils.isEmpty(tAdsInfo.getRedirectUrl())) {
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }

            WebActivity.startActivityForResult(this,
                    tAdsInfo.getRedirectUrl(), tAdsInfo.getMaterialTitle(), 1);
        } else if (tAdsInfo != null && "submenu".equals(tAdsInfo.getMaterialType())) {
            if (appsProvider != null) {
                Application application = appsProvider
                        .getApplicationFromCacheById(tAdsInfo.getSubMenuId());
                if (application != null) {
                    openApplication(application);
                }
            }
        }

    }

    private TAdsInfo tAdsInfo;
    private CountDownTimer countDownTimer;

    private boolean showAds() {
        tAdsInfo = getAvailableAds();
        boolean showFlag = tAdsInfo != null;
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(rltAds.getLayoutParams());
        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        lp.setMargins(0, DeviceUtil.getStatusBarHeight() + Utils.getDimen(R.dimen.dp_5), Utils.getDimen(R.dimen.dp_15), 0);
        rltAds.setLayoutParams(lp);
        rltAds.setVisibility(showFlag ? View.VISIBLE : View.GONE);
        if (!showFlag) {
            return false;
        }
        if (0 == tAdsInfo.getShowIconFlag()) {
            ViewGroup.LayoutParams layoutParams = vBottom.getLayoutParams();
            layoutParams.height = 0;
        }
        tAdsInfo.setShowTime(tAdsInfo.getShowTime() == 0 ? 3 : tAdsInfo.getShowTime());
        setTvSkipTime(tAdsInfo.getShowTime());
        LogUtils.e(TAG, tAdsInfo.getSplashType() + "=====" + tAdsInfo.getFileName());
        if ("video".equals(tAdsInfo.getSplashType())) {
            videoView.setVisibility(View.VISIBLE);
            videoView.setOnPreparedListener(mp -> mp.setOnInfoListener((mp1, what, extra) -> {
                if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                    videoView.setBackgroundColor(Color.TRANSPARENT);
                }
                return true;
            }));
            imgAdds.setVisibility(View.GONE);
            videoView.setVideoPath("file://" + tAdsInfo.getFileName());
            videoView.start();
        } else {
            videoView.setVisibility(View.GONE);
            ImageLoadUtils.loadImageFile(imgAdds, tAdsInfo.getFileName());
        }
        countDownTimer = new CountDownTimer(tAdsInfo.getShowTime() * 1000 + 150,
                1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int dTime = (int) (millisUntilFinished / 1000.f);
                setTvSkipTime(dTime);
            }

            @Override
            public void onFinish() {
                onAdsComplete(false);
            }
        }.start();
        return showFlag;
    }

    private void setTvSkipTime(int time) {
        //ViewGroup.LayoutParams layoutParams = imgAdds.getLayoutParams();
        //layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        if (tAdsInfo.getNotShowFlag() == 0) {
            tvNoShowAgain.setVisibility(View.GONE);
            bar.setVisibility(View.GONE);
        }
        tvSkipTime.setText(String.format(Utils.getString(R.string.base_skip_time),
                time));
    }

    private TAdsInfo getAvailableAds() {
        List<TAdsInfo> tadsInfos = tAdsInfoDao.queryBuilder()
                .where(TAdsInfoDao.Properties.ValidDateFrom.lt(nowTime),
                        TAdsInfoDao.Properties.ValidDateTo.gt(nowTime)).list();
        if (tadsInfos != null && tadsInfos.size() > 0) {
            TAdsInfo tAdsInfo = tadsInfos.get(0);
            List<TAdsForbidden> tAdsForbiddenList = tAdsForbiddenDao
                    .queryBuilder()
                    .where(TAdsForbiddenDao.Properties.AdsId.eq(tAdsInfo.getId()),
                            TAdsForbiddenDao.Properties.UserId.eq(getUserId())).list();
            if (tAdsForbiddenList != null && tAdsForbiddenList.size() > 0) {
                return null;
            } else {
                return tAdsInfo;
            }
        }
        return null;
    }

    private int getPageType() {
        if (isFirstLaunch() && Utils.getDrawable("main_guide_0", this) != null) {
            return PAGE_GUIDE;
        } else if (!StringUtils.isEmpty(Hippius.getAccessToken())) {
            if (FragmentProvider.getInstance().mustStartFromLogin()) {
                return PAGE_LOGIN;
            }
            if (SPConfig.getBoolean(ConfigKeys.SP_PATTERN_LOCK_ENABLE + getUserId(), false)) {
                return PAGE_LOGIN_UNLOCK_PATTERN;
            } else if (Utils.isFingerprintEnable() &&
                    SPConfig.getBoolean(ConfigKeys.SP_FINGERPRINT_LOCK_ENABLE + getUserId(),
                            false)) {
                return PAGE_LOGIN_UNLOCK_FINGERPRINT;
            }
            return PAGE_HOME;
        } else {
            return PAGE_LOGIN;
        }
    }

    private String getUserId() {
        if (userId == null) {
            userId = SPConfig.getString(ConfigKeys.SP_USERID, "");
        }
        return userId;
    }

    private HippiusConfig getHippiusConfig() throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        InputStream inputStream = getAssets().open("hippius_config.json");
        BufferedReader bf = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = bf.readLine()) != null) {
            stringBuilder.append(line);
        }
        String config = stringBuilder.toString();
        inputStream.close();
        bf.close();
        HippiusConfig hippiusConfig = new Gson().fromJson(config, HippiusConfig.class);
        if (hippiusConfig != null && hippiusConfig.getFunctions() != null) {
            ArrayList<HippiusConfig.Function> functions = hippiusConfig.getFunctions();
            for (HippiusConfig.Function function : functions) {
                String pageCode = function.getPageCode();
                if ("SimpleApplication".equals(pageCode)) {
                    function.setPageCode("app");
                }
            }
        }
        return hippiusConfig;
    }

    private boolean isFirstLaunch() {
        return SPConfig.getBoolean(ConfigKeys.FIRST_LAUNCH, true);
    }

    @Override
    protected SplashActivityPresenter createPresenter() {
        return new SplashActivityPresenter();
    }

    @Override
    protected ISplashActivity createView() {
        return this;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 || resultCode == RESULT_OK) {
            onAdsComplete(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (videoView.isPlaying()) {
            videoView.stopPlayback();
        }
    }
}
