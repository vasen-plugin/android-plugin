package debug;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.hand.baselibrary.activity.BaseActivity;
import com.hand.baselibrary.activity.ISplashActivity;
import com.hand.baselibrary.dto.AppVersionResponse;
import com.hand.hippiusmoduledemo.R;
import com.hand.hippiusmoduledemo.R2;

import androidx.annotation.Nullable;
import butterknife.BindView;

public class MainActivity extends BaseActivity<MainActPresenter,IMainActivity>
        implements IMainActivity, ISplashActivity {
    private String loginAccount = "13162972797";
    private String loginPassword = "hand123";
    @BindView(R2.id.tv_content)
    TextView tvContent;
    String mMessage = "";

    @Override
    protected int setStatusBarView() {
        return R.id.status_bar_view;
    }

    @Override
    protected Object setLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected void onBindView(@Nullable Bundle savedInstanceState) {
    }

    @Override
    protected MainActPresenter createPresenter() {
        return new MainActPresenter();
    }

    @Override
    protected IMainActivity createView() {
        return this;
    }

    public void login(View v){
        mMessage = "";
        getPresenter().startLogin(loginAccount,loginPassword);
    }

    @Override
    public void onMessage(String message) {
        this.mMessage = this.mMessage + message;
        tvContent.setText(this.mMessage);
    }

    @Override
    public void onComplete() {
        tvContent.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this,DemoActivity.class);
                startActivity(intent);
            }
        },500);

    }

    @Override
    public void onCollectDeviceSuccess() {

    }

    @Override
    public void onGlobalAppInfo(boolean success, AppVersionResponse appVersionResponse, String message) {

    }
}
