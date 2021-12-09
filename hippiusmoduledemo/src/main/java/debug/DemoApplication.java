package debug;

import android.content.Context;

import com.alibaba.android.arouter.launcher.ARouter;
import com.hand.baselibrary.BaseApplication;

public class DemoApplication extends BaseApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        ARouter.openLog();
        ARouter.openDebug();
        //}
        ARouter.init(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }
}
