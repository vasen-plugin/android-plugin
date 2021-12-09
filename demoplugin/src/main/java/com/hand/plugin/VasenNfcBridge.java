package com.hand.plugin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.hand.baselibrary.jsbridge.CallbackContext;
import com.hand.baselibrary.jsbridge.HippiusPlugin;
import com.hand.plugin.ui.ReadTextActivity;
import com.hand.plugin.ui.WriteTextActivity;

import org.json.JSONException;
import org.json.JSONObject;



public class VasenNfcBridge extends HippiusPlugin {
    @Override
    public String execute(String action, JSONObject args, CallbackContext callbackContext) {
        if(action.equals("readFromNFC")){
            Intent intent = new Intent(this.getActivity(), ReadTextActivity.class);
            getActivity().startActivity(intent);
            //startActivity实际上是通过反射，获取到类的Class类来动态加载这个类，显示页面和处理逻辑
//            Intent intent1=new Intent();
//            intent1.setAction(Intent.ACTION_VIEW);
//            //action实现隐式启动，系统可以根据action获取到要跳转的activity，必须要设置一个category，才能隐式启动
//            //可以在manifest.xml中<activity>定义<intent-filter>来实现隐式跳转（可以定义action,category,data等）
//            intent1.addCategory(Intent.CATEGORY_DEFAULT);
//            //Category指定当前动作（Action）被执行的环境,CATEGORY_DEFAULT默认环境(所有intent都可以激活)，还有CATEGORY_HOME（手机的主页面）等环境
//            intent1.setData(Uri.parse("content://com.android.contacts/contacts/1"));
//            //data是为intent操作提供的数据，格式为uri，可以通过解析字符串获得资源的uri。可以直接把数据放到跳转的Class类中 Uri字符串的格式：scheme://host:port/path
//             getActivity().startActivity(intent1);
            //flag用来设置activity的启动方式（launchMode）
        }
        if(action.equals("openNFC")){
            NfcUtils nfcUtils=new NfcUtils(this.getActivity());
        }
        if(action.equals("writeIntoNFC")){
           Intent intent=new Intent(this.getActivity(), WriteTextActivity.class);
//            intent的Extra属性是用来在不同的Activity之间进行数据交换的，通过key-value（键值对）的形式。我们首先要知道Intent的这两个方法：
//            putExtras(): 用来把Bundle对象作为附加信息进行添加
//            getExtras(): 获取bundle中保存的信息
//            Bundle对象其实就如同一个map对象，可以存入多个键值对形式的数据
            Bundle bundle=new Bundle();
            try {
                bundle.putString("mid",args.getString("materialID"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            intent.putExtras(bundle);
            this.getActivity().startActivity(intent);
        }
        return null;
    }
//总结：intent的属性包含component(实际上是类的Class类，显式跳转)，action(隐式跳转),category（activity运行环境）
// ,data（传到activity上的uri数据）,extra（本activity传到跳转activity的数据）和flag（启动模式）
}
