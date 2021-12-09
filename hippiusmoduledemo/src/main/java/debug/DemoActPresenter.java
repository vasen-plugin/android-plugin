package debug;

import com.hand.baselibrary.activity.BasePresenter;
import com.hand.baselibrary.bean.TenantUserInfo;
import com.hand.baselibrary.bean.UserInfo;
import com.hand.baselibrary.config.ConfigKeys;
import com.hand.baselibrary.config.Hippius;
import com.hand.baselibrary.config.SPConfig;
import com.hand.baselibrary.net.RetrofitClient;
import com.hand.baselibrary.rxbus.RxBus;
import com.hand.baselibrary.rxbus.UserInfoEvent;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class DemoActPresenter extends BasePresenter<IDemoActivity> {

    private ApiService apiService;
    private String mUserId;
    public DemoActPresenter() {
        apiService = RetrofitClient.getInstance().getService(ApiService.class);
        mUserId = SPConfig.getString(ConfigKeys.SP_USERID,"");
    }

    //获取用户信息相关
    public void getUserInfo() {
        Observable<UserInfo> userInfoObservable = apiService
                .getUserInfo(mUserId).subscribeOn(Schedulers.io());
        Observable<ArrayList<TenantUserInfo>> tenantUserInfosObservable = apiService
                .getTenantUserInfos(null, null).subscribeOn(Schedulers.io());
        Disposable disposable = Observable
                .mergeArrayDelayError(userInfoObservable, tenantUserInfosObservable)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onUserInfoAccept, this::onUserInfoError, this::onUserInfoComplete);
    }

    private UserInfo userInfo;
    private ArrayList<TenantUserInfo> tenantUserInfos;

    private void onUserInfoAccept(Object o) {
        if (o instanceof UserInfo) {
            userInfo = (UserInfo) o;
        } else if (o instanceof ArrayList) {
            tenantUserInfos = (ArrayList<TenantUserInfo>) o;
            Hippius.putConfig(ConfigKeys.TENANT_USER_INFO, tenantUserInfos);
        }
    }

    private void onUserInfoError(Throwable e) {
        e.printStackTrace();
    }

    private void onUserInfoComplete() {
        if (userInfo != null || tenantUserInfos != null) {
            UserInfoEvent userInfoEvent = new UserInfoEvent(userInfo, tenantUserInfos);
            RxBus.get().postSticky(userInfoEvent);
        }
    }
}
