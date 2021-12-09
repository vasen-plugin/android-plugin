package com.hand.hippius.google_push;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.hand.baselibrary.activity.IBaseHomeActivity;
import com.hand.baselibrary.config.ConfigKeys;
import com.hand.baselibrary.config.SPConfig;
import com.hand.baselibrary.utils.LogUtils;
import com.hand.pushlibrary.HPushClient;
import com.hand.pushlibrary.IPush;

import java.io.IOException;

public class FCMPush implements IPush {
    private static final String TAG = "FCMPush";
    @Override
    public void init(Application application) {
        getNewToken();
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle bundle) {
                if (activity instanceof IBaseHomeActivity) {
                    LogUtils.e(TAG, "Here is home activity!");
                    //当推送token更新或者上次注册失败时需要重新注册
                    if (SPConfig.getBoolean(ConfigKeys.SP_RE_REGISTER_PUSH_TOKEN, false)
                            ||SPConfig.getBoolean(ConfigKeys.SP_REGISTER_PUSH_FAILED,false)) {
                        HPushClient.getInstance().registerDevice();
                        SPConfig.putBoolean(ConfigKeys.SP_RE_REGISTER_PUSH_TOKEN, false);
                        SPConfig.putBoolean(ConfigKeys.SP_REGISTER_PUSH_FAILED,false);
                    }
                }
            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }

    private void getNewToken(){
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        LogUtils.e("Google Push","==========");
                        if (!task.isSuccessful()&&task.getException()!=null) {
                            LogUtils.e("Google Push",task.getException().getMessage()+"");
                            return;
                        }
                        // Get new Instance ID token
                        if(task.getResult()!=null) {
                            String token = task.getResult().getToken();
                            String pushToken = SPConfig.getString(ConfigKeys.SP_FCM_PUSH_TOKEN, null);
                            if (pushToken != null&&!pushToken.equals(token)){
                                //pushToken如果发生变化，做记录在HomeActivity页面重新进行上报
                                SPConfig.putBoolean(ConfigKeys.SP_RE_REGISTER_PUSH_TOKEN,true);
                            }
                            LogUtils.e(TAG,token+"");
                            SPConfig.putString(ConfigKeys.SP_FCM_PUSH_TOKEN, token);
                        }
                    }
                });
    }

    @Override
    public String getPushName() {
        return "FCM";
    }

    @Override
    public String getPushToken() {
        return SPConfig.getString(ConfigKeys.SP_FCM_PUSH_TOKEN, null);
    }

    @Override
    public void login() {
       // FirebaseMessaging.getInstance().setAutoInitEnabled(true);

    }

    @Override
    public void logout() {
        SPConfig.putString(ConfigKeys.SP_FCM_PUSH_TOKEN,"");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FirebaseInstanceId.getInstance().deleteInstanceId();
                    getNewToken();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
