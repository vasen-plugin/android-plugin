package debug;

import com.google.gson.Gson;
import com.hand.baselibrary.activity.BasePresenter;
import com.hand.baselibrary.bean.AccessToken;
import com.hand.baselibrary.bean.Error;
import com.hand.baselibrary.bean.UserInfo;
import com.hand.baselibrary.config.ConfigKeys;
import com.hand.baselibrary.config.Hippius;
import com.hand.baselibrary.config.SPConfig;
import com.hand.baselibrary.dto.DeviceInfo;
import com.hand.baselibrary.dto.DeviceResponse;
import com.hand.baselibrary.net.RetrofitClient;
import com.hand.baselibrary.utils.DeviceUtil;
import com.hand.baselibrary.utils.EncryptionUtils;
import com.hand.baselibrary.utils.LogUtils;
import com.hand.baselibrary.utils.Utils;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActPresenter extends BasePresenter<IMainActivity> {
    private static final String TAG = "MainActPresenter";
    private ApiService apiService;
    public static String Key = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAJL0JkqsUoK6kt3JyogsgqNp9VDGDp+t3ZAGMbVoMPdHNT2nfiIVh9ZMNHF7g2XiAa8O8AQWyh2PjMR0NiUSVQMCAwEAAQ==";

    public MainActPresenter() {
        apiService = RetrofitClient.getInstance().getService(ApiService.class);
    }

    public void startLogin(String account, String password) {
        getView().onMessage("开始登陆");
        password = EncryptionUtils.RSA.encrypt(password, Key);
        Disposable disposable = apiService.getAccessToken(account, password,
                DeviceUtil.getDeviceID())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onLoginSuccess, this::onLoginError);
    }

    private void onLoginSuccess(AccessToken accessToken) {
        LogUtils.e(TAG,accessToken.getAccessToken());
        Hippius.setAccessToken(accessToken.getAccessToken());
        getView().onMessage("\nAccessToken已获取-使用方式：Hippius.getAccessToken()\n-----------");
        SPConfig.putLong(ConfigKeys.SP_LAST_LOGIN_TIME,System.currentTimeMillis());
        doOtherRequest();
    }

    private void onLoginError(Throwable e) {
        getView().onMessage("获取AccessToken失败");
    }

    //获取登陆用户的userid
    private void doOtherRequest() {
        Disposable disposable = apiService.getUserInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onAcceptUserInfo,this::onUserInfoError);
    }

    private void onAcceptUserInfo(UserInfo userInfo){
        if(!userInfo.isFailed()) {
            SPConfig.putString(ConfigKeys.SP_USERID, userInfo.getUserId());
            getView().onMessage("\nUserID已获取-使用方式：SPConfig.getString(ConfigKeys.SP_USERID,\"\")\n-----------");
            getView().onComplete();
        }else{
            getView().onMessage("\n获取UserId失败-"+userInfo.getMessage());
        }
    }

    private void onUserInfoError(Throwable e){
        getView().onMessage("\n获取UserId失败");
    }
}
