package debug;

import android.os.Bundle;


import com.hand.baselibrary.activity.BaseActivity;
import com.hand.baselibrary.activity.ISplashActivity;
import com.hand.baselibrary.dto.AppVersionResponse;
import com.hand.hippiusmoduledemo.DemoFragment;
import com.hand.hippiusmoduledemo.R;

import androidx.annotation.Nullable;

public class DemoActivity extends BaseActivity<DemoActPresenter,IDemoActivity>
        implements IDemoActivity, ISplashActivity {

    @Override
    protected Object setLayout() {
        return R.layout.activity_demo;
    }

    @Override
    protected void onBindView(@Nullable Bundle savedInstanceState) {
        getPresenter().getUserInfo();
        loadRootFragment(R.id.flt_container,new DemoFragment());
    }

    @Override
    protected DemoActPresenter createPresenter() {
        return new DemoActPresenter();
    }

    @Override
    protected IDemoActivity createView() {
        return this;
    }

    @Override
    public void onCollectDeviceSuccess() {

    }

    @Override
    public void onGlobalAppInfo(boolean success, AppVersionResponse appVersionResponse, String message) {

    }
}
