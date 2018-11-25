package com.example.yuminkim.childhold.util;

import com.onesignal.OSPermissionSubscriptionState;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

public class PushMessageUtil {
    public static void sendPushNotification(String userId, String childName, boolean isRide) {
        //FIXME: Push from me modify send to other
        OSPermissionSubscriptionState status = OneSignal.getPermissionSubscriptionState();
        boolean isSubscribed = status.getSubscriptionStatus().getSubscribed();

        if (!isSubscribed)
            return;

        try {
            String ride = isRide ? "승차" : "하차";
            JSONObject notificationContent = new JSONObject(
                    "{'contents': " +
                        "{'en': '" + childName + "(이)가 " + ride + "하였습니다.'}," +
                        "'include_player_ids': ['" + userId + "'], " +
                        "'headings': {'en': '승하차알림'}" +
                    "} ");
            //"'big_picture': 'http://i.imgur.com/DKw1J2F.gif'}"
            OneSignal.postNotification(notificationContent, null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
