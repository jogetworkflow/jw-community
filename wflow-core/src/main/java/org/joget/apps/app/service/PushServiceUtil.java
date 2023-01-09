package org.joget.apps.app.service;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.HttpResponse;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.joget.apps.app.model.AppDefinition;
import org.joget.commons.util.LogUtil;
import org.joget.directory.dao.UserMetaDataDao;
import org.joget.directory.model.UserMetaData;
import org.springframework.beans.BeansException;
import org.springframework.util.StringUtils;

public class PushServiceUtil {

    public static final String WEBPUSH_SUBSCRIPTION_USER_METADATA = "WEBPUSH_SUBSCRIPTION";
    public static final String WEBPUSH_SUBSCRIPTION_DELIMITER = "|";
    
    static String pushServicePublicKey;
    static String pushServicePrivateKey;
    static PushService pushServiceInstance;
    
    public void setPublicKey(String publicKey) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
        pushServicePublicKey = publicKey;
    }
    
    public void setPrivateKey(String privateKey) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
        pushServicePrivateKey = privateKey;
    }
    
    synchronized static PushService getPushService() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
        if (pushServiceInstance == null) {
            if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
                Security.addProvider(new BouncyCastleProvider());
            }
            pushServiceInstance = new PushService();
            pushServiceInstance.setPublicKey(pushServicePublicKey);
            pushServiceInstance.setPrivateKey(pushServicePrivateKey);
        }
        return pushServiceInstance;
    }
    
    /**
     * Stores push subscription JSON in the user metadata table.
     * @param username
     * @param subscriptionJson
     * @return 
     */
    public static Boolean storeUserPushSubscription(String username, String deviceId, String appId, String userviewId, String subscriptionJson) {
        UserMetaDataDao userMetaDataDao = (UserMetaDataDao)AppUtil.getApplicationContext().getBean("userMetaDataDao");
        UserMetaData data = userMetaDataDao.getUserMetaData(username, WEBPUSH_SUBSCRIPTION_USER_METADATA);
        boolean isNew = false;
        if (data == null) {
            data = new UserMetaData();
            data.setUsername(username);
            data.setKey(WEBPUSH_SUBSCRIPTION_USER_METADATA);
            isNew = true;
        }
        String subscriptions = data.getValue();
        String[] subscriptionArray = new String[0];
        if (subscriptions != null && !subscriptions.trim().isEmpty()) {
            subscriptionArray = StringUtils.delimitedListToStringArray(subscriptions, WEBPUSH_SUBSCRIPTION_DELIMITER);
        }
        
        //remove same deviceId, appId, userviewId
        Set<String> duplicate = new HashSet<String>();
        for (String s : subscriptionArray) {
            if (s.contains("::" + deviceId + "::" + appId+ "::" + userviewId)) {
                duplicate.add(s);
            }
        }
        for (String s : duplicate) {
            subscriptionArray = ArrayUtils.removeAllOccurences(subscriptionArray, s);
        }
        
        subscriptionArray = ArrayUtils.add(subscriptionArray, subscriptionJson + "::" + deviceId + "::" + appId+ "::" + userviewId);
        String newSubscriptions = StringUtils.arrayToDelimitedString(subscriptionArray, WEBPUSH_SUBSCRIPTION_DELIMITER);
        data.setValue(newSubscriptions);
        Boolean result = (isNew) ? userMetaDataDao.addUserMetaData(data) : userMetaDataDao.updateUserMetaData(data);
        LogUtil.debug(PushServiceUtil.class.getName(), "Update push subscription " + subscriptionJson + ": " + result);
        return result;
    }

    /**
     * Removes push subscription JSON from the user metadata table.
     * @param username
     * @param subscriptionJson
     * @return 
     */
    public static Boolean removeUserPushSubscription(String username, String subscriptionJson) {
        UserMetaDataDao userMetaDataDao = (UserMetaDataDao)AppUtil.getApplicationContext().getBean("userMetaDataDao");
        UserMetaData data = userMetaDataDao.getUserMetaData(username, WEBPUSH_SUBSCRIPTION_USER_METADATA);
        String subscriptions = data.getValue();
        String[] subscriptionArray = new String[0];
        if (subscriptions != null && !subscriptions.trim().isEmpty()) {
            subscriptionArray = StringUtils.delimitedListToStringArray(subscriptions, WEBPUSH_SUBSCRIPTION_DELIMITER);
        }
        subscriptionArray = ArrayUtils.removeAllOccurences(subscriptionArray, subscriptionJson);
        String newSubscriptions = StringUtils.arrayToDelimitedString(subscriptionArray, WEBPUSH_SUBSCRIPTION_DELIMITER);
        data.setValue(newSubscriptions);
        Boolean result = userMetaDataDao.updateUserMetaData(data);
        LogUtil.debug(PushServiceUtil.class.getName(), "Remove push subscription " + subscriptionJson + ": " + result);
        return result;
    }
    
    /**
     * Send web push message to all subscriptions for a specific username.
     * @param username
     * @param title
     * @param text
     * @param url
     * @param icon
     * @param badge
     * @param removeOnFailure If true, subscription will be removed from user metadata upon failure.
     * @return number of messages successfully sent
     */
    public static int sendUserPushNotification(String username, String title, String text, String url, String icon, String badge, boolean removeOnFailure) {
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        String appId = null;
        if (appDef != null) {
            appId = appDef.getAppId();
        }
        
        int result = 0;
        UserMetaDataDao userMetaDataDao = (UserMetaDataDao)AppUtil.getApplicationContext().getBean("userMetaDataDao");
        UserMetaData data = userMetaDataDao.getUserMetaData(username, WEBPUSH_SUBSCRIPTION_USER_METADATA);
        if (data != null) {
            String subscriptions = data.getValue();
            String[] subscriptionArray = new String[0];
            if (subscriptions != null && !subscriptions.trim().isEmpty()) {
                subscriptionArray = StringUtils.delimitedListToStringArray(subscriptions, WEBPUSH_SUBSCRIPTION_DELIMITER);
            }
            
            //only send 1 per device
            Map<String, String> uniqueDevice = new HashMap<String, String>();
            
            for (String subscriptionJson: subscriptionArray) {
                if (subscriptionJson != null && !subscriptionJson.isEmpty()) {
                    if (subscriptionJson.contains("::")) {
                        String[] temp = subscriptionJson.split("::");
                        if (uniqueDevice.containsKey(temp[1])) {
                            if (url.contains("/web/userview/")) {
                                if (url.contains("/web/userview/"+temp[2]+"/"+temp[3])) {
                                    uniqueDevice.put(temp[1], subscriptionJson);
                                }
                            } else if (temp[2].equals(appId)) {
                                uniqueDevice.put(temp[1], subscriptionJson);
                            }
                        } else {
                            uniqueDevice.put(temp[1], subscriptionJson);
                        }
                    } else {
                        uniqueDevice.put(subscriptionJson, subscriptionJson);
                    }
                }
            }
            
            for (String k : uniqueDevice.keySet()) {
                String subscriptionJson = uniqueDevice.get(k);
                boolean newResult = sendPushNotification(subscriptionJson, title, text, url, icon, badge);
                if (!newResult && removeOnFailure) {
                    PushServiceUtil.removeUserPushSubscription(username, subscriptionJson);
                }
                if (newResult) {
                    result++;
                }
            }
        }

        return result;
    }
    
    /**
     * Send web push message to specific subscription JSON
     * @param subscriptionJson
     * @param title
     * @param text
     * @param url
     * @param icon
     * @param badge
     * @return true if message successfully sent
     * @throws BeansException
     * @throws JsonSyntaxException 
     */
    public static boolean sendPushNotification(String subscriptionJson, String title, String text, String url, String icon, String badge) throws BeansException, JsonSyntaxException {
        boolean result = false;
        if (subscriptionJson.contains("::")) {
            subscriptionJson = subscriptionJson.substring(0, subscriptionJson.indexOf("::"));
        }
        Subscription subscription = new Gson().fromJson(subscriptionJson, Subscription.class);
        String endpoint = subscription.endpoint;
        String userPublicKey = subscription.keys.p256dh;
        String userAuth = subscription.keys.auth;
        String payload = "{\"title\":\"" + StringEscapeUtils.escapeJavaScript(title) + "\", \"text\":\"" + StringEscapeUtils.escapeJavaScript(text) + "\", \"url\":\"" + StringEscapeUtils.escapeJavaScript(url) + "\",\"icon\":\"" + StringEscapeUtils.escapeJavaScript(icon) + "\",\"badge\":\"" + StringEscapeUtils.escapeJavaScript(badge) + "\"}";
        try {
            PushService pushService = PushServiceUtil.getPushService();
            Notification notification = new Notification(endpoint, userPublicKey, userAuth, payload);
            HttpResponse httpResponse = pushService.send(notification);
            LogUtil.debug(PushServiceUtil.class.getName(), "Send push subscription " + subscriptionJson + ": " + httpResponse.getStatusLine().getStatusCode());
            result = result || (httpResponse.getStatusLine().getStatusCode() == 201);
        } catch(Exception e) {
            LogUtil.error(PushServiceUtil.class.getName(), e, "Error sending push subscription " + subscriptionJson);
        }
        return result;
    }
     
}
