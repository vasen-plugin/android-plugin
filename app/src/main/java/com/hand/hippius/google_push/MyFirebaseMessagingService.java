package com.hand.hippius.google_push;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.hand.baselibrary.config.ConfigKeys;
import com.hand.baselibrary.config.SPConfig;
import com.hand.baselibrary.utils.LogUtils;
import com.hand.pushlibrary.HPushClient;
import com.hand.pushlibrary.NotificationMessage;
import com.hand.pushlibrary.PassThroughMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMessagingServ";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        LogUtils.e(TAG, remoteMessage.getMessageId() + "===" + remoteMessage.getMessageType() + ":new message received!");
        Map<String, String> map = remoteMessage.getData();
       /* Set<String> set = map.keySet();
        Iterator<String> iterator = set.iterator();
        while (iterator.hasNext()){
            String key = iterator.next();
            LogUtils.e(key,map.get(key));
        }*/
        //透传消息
        if (map != null && remoteMessage.getNotification() == null) {
            String content = map.get("content");
            JSONObject object = null;
            try {
                object = new JSONObject(content);
                String extra = object.getString("extra");
                String type = object.getString("type");
                String body = object.getString("body");
                PassThroughMessage passThroughMessage = new PassThroughMessage();
                passThroughMessage.setBody(body);
                passThroughMessage.setExtra(extra);
                passThroughMessage.setType(type);
                HPushClient.getInstance().onPassThroughMsgReceived(passThroughMessage);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        //通知栏消息
        if (remoteMessage.getNotification() != null) {
            LogUtils.e(TAG, remoteMessage.getNotification().getBody());
            LogUtils.e(TAG, remoteMessage.getNotification().getTitle());
            HPushClient.getInstance().notification(remoteMessage.getMessageId().hashCode(),
                    remoteMessage.getNotification().getTitle(),remoteMessage.getNotification().getBody());
            HPushClient.getInstance().onNotificationMsgReceived(new NotificationMessage());
        }
        super.onMessageReceived(remoteMessage);
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        LogUtils.e(TAG, token + "===" + ":on google push token received!");
        String pushToken = SPConfig.getString(ConfigKeys.SP_FCM_PUSH_TOKEN, null);
        if (pushToken != null && !pushToken.equals(token)) {
            //pushToken如果发生变化，做记录在HomeActivity页面重新进行上报
            SPConfig.putBoolean(ConfigKeys.SP_RE_REGISTER_PUSH_TOKEN, true);
        }
    }
}
