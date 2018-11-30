package com.example.yuminkim.childhold.util;

import com.onesignal.OSPermissionSubscriptionState;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

public class PushMessageUtil {
    public static void sendPushNotification(String userId, String childName, boolean isRide) {
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

    public static void sendPushNotificationForSafetyEnd(String userIds) {
        OSPermissionSubscriptionState status = OneSignal.getPermissionSubscriptionState();
        boolean isSubscribed = status.getSubscriptionStatus().getSubscribed();

        if (!isSubscribed)
            return;

        try {
            JSONObject notificationContent = new JSONObject(
                    "{'contents': " +
                            "{'en': '아이들이 모두 안전하게 하차하였습니다.'}," +
                            "'include_player_ids': ['" + userIds + "'], " +
                            "'headings': {'en': '승하차알림'}" +
                            "} ");
            OneSignal.postNotification(notificationContent, null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void sendAbsentPushNotification(String driverId, String childId, String childName, boolean isRide) {
        OSPermissionSubscriptionState status = OneSignal.getPermissionSubscriptionState();
        boolean isSubscribed = status.getSubscriptionStatus().getSubscribed();

        if (!isSubscribed)
            return;

        try {
            String comment = childName + (isRide ? "(이)가 탑승 할 예정입니다." : "(이)가 탑승하지 않을 예정입니다.");
            JSONObject notificationContent = new JSONObject(
                    "{'contents': " +
                            "{'en': '" + comment + "운행에 참고바랍니다.'}," +
                            "'include_player_ids': ['" + driverId + "'], " +
                            "'headings': {'en': '승하차알림'}," +
                            "'data': { 'id': '" + childId + "' }" +
                            "} ");
            OneSignal.postNotification(notificationContent, null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
