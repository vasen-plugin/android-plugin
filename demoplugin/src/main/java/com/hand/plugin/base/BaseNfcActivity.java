package com.hand.plugin.base;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;

import androidx.appcompat.app.AppCompatActivity;

/**
 *activity有四种启动方式：
 *1.标准模式，每次都会创建一个新实例
 *2.singleTop:如果该实例在任务栈的栈顶，那么会服用该实例，否则新建该实例
 *3.singleTask:如果该实例在任务栈中存在，会复用该实例，该实例距离栈顶间的实例都会被移除出栈，否则新创建实例。
 *4.singleInstance:单例模式，在新的栈中创建该实例后，全局只有一个该实例，调用时都会复用该实例。
 *
 * 当launchMode设置为singleTop模式时，第一次启动该activity调用onCreate方法，第二次启动该activity(必须在栈顶)将不会创建新的Activity实例，
   将调用onNewIntent方法，重用该实例(所以我们获取intent传递过来的Tag数据操作放在onNewIntent方法中执行)
   启动的顺序为：onCreate->onStart->onResume（在栈顶存放该activity,便于再次调用时恢复该activity）->onPause->                      第一次
                                                                                                  onNewIntent->onResume 第二次
 * 当launchMode设置为singleTask,singleInstance模式时，第一次启动该activity调用onCreate方法，
   第二次启动该activity将不会创建新的Activity实例，将调用onNewIntent方法，重用该实例
   启动的顺序为：onCreate->onStart->onResume->onPause->                                                                    第一次
                                                    跳转A--->onNewIntent--->onRestart--->onStart--->onResume             第二次
 */
public class BaseNfcActivity extends AppCompatActivity {
    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;
    //PendingIntent是intent对象的包装器，主要作用是授权外部应用使用包含的intent

    /**
     * 启动Activity，界面可见时
     */
    @Override
    protected void onStart() {
        super.onStart();
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        //一旦截获NFC消息，就会通过PendingIntent调用窗口
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()), 0);
        //PendingIntent.getActivity启动activity的intent
    }

    /**
     * 获得焦点，按钮可以点击
     */
    @Override
    public void onResume() {
        super.onResume();
        //设置处理优于所有其他NFC的处理
        if (mNfcAdapter != null)
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
    }

    /**
     * 暂停Activity，界面获取焦点，按钮可以点击
     */
    @Override
    public void onPause() {
        super.onPause();
        //恢复默认状态
        if (mNfcAdapter != null)
            mNfcAdapter.disableForegroundDispatch(this);
    }
}
