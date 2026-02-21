package com.xigua.baseAPI.util;

import com.xigua.baseAPI.BaseAPI;
import com.xigua.baseAPI.api.playerInfo.PlayerInfo;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Setter;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebUtil {
    private static final CloseableHttpAsyncClient httpClient;
    @Setter
    private static BaseAPI plugin;
    public static String SKIN_VALID_URL;
    public static String SHIP_ITEM_URL;
    public static String GET_ORDER_ITEM_URL;
    public static String REPORT_FRIEND_ONLINE_URL;
    public static String GAS_SERVER_BASE_URL;
    public static String TEST_GAS_SERVER_BASE_URL;

    public static void startHttpClient() {
        plugin.getLogger().info("http client start");
        httpClient.start();
        plugin.getLogger().info("http client start success");
    }

    public static void stopHttpClient() {
        plugin.getLogger().info("http client start closing");
        try {
            httpClient.close();
            plugin.getLogger().info("http client close success");
        }
        catch (IOException e) {
            plugin.getLogger().warning("http client close error:" + e);
            e.printStackTrace();
        }
    }

    public static void postGetItemOrders(Player player, FutureCallback<HttpResponse> callback) {
        try {
            PlayerInfo playerinfo = plugin.getPlayerInfo(player);
            String json = "";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("gameid", playerinfo.getGameId());
            jsonObject.addProperty("uuid", playerinfo.getUuid().toString());
            json = jsonObject.toString();
            String validUrl = WebUtil.getShipBaseUrl(playerinfo.getGasServerUrl(), playerinfo.isTestServer()) + GET_ORDER_ITEM_URL;
            HashMap<String, String> headMaps = new HashMap<String, String>();
            String signStr = WebUtil.getServerSign(playerinfo.getGameKey(), "POST", GET_ORDER_ITEM_URL, json);
            headMaps.put("Netease-Server-Sign", signStr);
            StringEntity entity = new StringEntity(json);
            WebUtil.postFormWithExtraHead(validUrl, headMaps, entity, callback);
        }
        catch (Exception e) {
            plugin.getLogger().warning("http client init get item orders post url error:" + e);
            e.printStackTrace();
        }
    }

    public static void postFinPlayerOrder(Player player, List<String> orderIds, FutureCallback<HttpResponse> callback) {
        try {
            PlayerInfo playerinfo = plugin.getPlayerInfo(player);
            String json = "";
            JsonObject jsonObject = new JsonObject();
            JsonArray orderIdsArray = new JsonArray();
            for (String str : orderIds) {
                orderIdsArray.add(str);
            }
            jsonObject.addProperty("gameid", playerinfo.getGameId());
            jsonObject.addProperty("uuid", playerinfo.getUuid().toString());
            jsonObject.add("orderid_list", orderIdsArray);
            json = jsonObject.toString();
            String validUrl = WebUtil.getShipBaseUrl(playerinfo.getGasServerUrl(), playerinfo.isTestServer()) + SHIP_ITEM_URL;
            HashMap<String, String> headMaps = new HashMap<String, String>();
            String signStr = WebUtil.getServerSign(playerinfo.getGameKey(), "POST", SHIP_ITEM_URL, json);
            headMaps.put("Netease-Server-Sign", signStr);
            StringEntity entity = new StringEntity(json);
            WebUtil.postFormWithExtraHead(validUrl, headMaps, entity, callback);
        }
        catch (Exception e) {
            plugin.getLogger().warning("http client init ship item orders post url error:" + e);
            e.printStackTrace();
        }
    }

    public static void postSkinValidForm(String validUrl, List<String> md5List, List<String> uidList, List<String> geoList, FutureCallback<HttpResponse> callback) {
        String json = "";
        JsonObject jsonObject = new JsonObject();
        JsonArray md5ListArray = new JsonArray();
        for (String str : md5List) {
            md5ListArray.add(str);
        }
        JsonArray uidListArray = new JsonArray();
        for (String str : uidList) {
            uidListArray.add(str);
        }
        JsonArray geoListArray = new JsonArray();
        for (String str : geoList) {
            geoListArray.add(str);
        }
        jsonObject.addProperty("sign", "62bd6025d29b17ff");
        jsonObject.add("md5_list", md5ListArray);
        jsonObject.add("uid_list", uidListArray);
        jsonObject.add("geo_list", geoListArray);
        json = jsonObject.toString();
        try {
            StringEntity entity = new StringEntity(json);
            WebUtil.postForm(validUrl, entity, callback);
        }
        catch (UnsupportedEncodingException e) {
            plugin.getLogger().warning("http client init skin valid post url error:" + e);
            e.printStackTrace();
        }
    }

    public static void postForm(String url, AbstractHttpEntity Entity2, FutureCallback<HttpResponse> callback) {
        HttpPost post = new HttpPost(url);
        post.addHeader("Content-Type", "application/json");
        post.addHeader("Accept", "application/json");
        post.setEntity(Entity2);
        httpClient.execute(post, callback);
    }

    public static void postFormWithExtraHead(String url, Map<String, String> headMap, AbstractHttpEntity Entity2, FutureCallback<HttpResponse> callback) {
        HttpPost post = new HttpPost(url);
        post.addHeader("Content-Type", "application/json");
        post.addHeader("Accept", "application/json");
        for (Map.Entry<String, String> entry : headMap.entrySet()) {
            post.addHeader(entry.getKey(), entry.getValue());
        }
        post.setEntity(Entity2);
        httpClient.execute(post, callback);
    }

    public static String getShipBaseUrl(String gasServerUrl, Boolean isTestServer) {
        if (!gasServerUrl.equals("")) {
            return gasServerUrl;
        }
        if (isTestServer.booleanValue()) {
            return TEST_GAS_SERVER_BASE_URL;
        }
        return GAS_SERVER_BASE_URL;
    }

    public static String getServerSign(String signKey, String method, String path, String httpBody) throws Exception {
        String str2sign = method + path + httpBody;
        String signStr = WebUtil.hmacWithJava("HmacSHA256", str2sign, signKey);
        if (signStr.length() < 64) {
            int preLen = 64 - signStr.length();
            String preZero = "0";
            signStr = String.join((CharSequence)"", Collections.nCopies(preLen, preZero)) + signStr;
        }
        return signStr;
    }

    public static String hmacWithJava(String algorithm, String data, String key) throws Exception {
        SecretKeySpec skey = new SecretKeySpec(key.getBytes(), algorithm);
        Mac mac = Mac.getInstance(algorithm);
        mac.init(skey);
        mac.update(data.getBytes());
        byte[] res = mac.doFinal();
        return new BigInteger(1, res).toString(16);
    }

    public static String toMd5WithPreZero(String string) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        byte[] tempRes = digest.digest(string.getBytes(StandardCharsets.UTF_8));
        StringBuffer buf = new StringBuffer("");
        for (int i = 0; i < tempRes.length; ++i) {
            int cur = tempRes[i];
            if (cur < 0) {
                cur += 256;
            }
            if (cur < 16) {
                buf.append("0");
            }
            buf.append(Integer.toHexString(cur));
        }
        return buf.toString();
    }

    public static void reportFriendOnline() {
        if (Bukkit.getOnlinePlayers().size() > 1) {
            plugin.getLogger().info("send reportFriendOnline");
            JsonArray uids = new JsonArray();
            String webServerUrl = null;
            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerInfo playerinfo = plugin.getPlayerInfo(player);
                uids.add(Long.toString(playerinfo.getProxyUid()));
                if (webServerUrl != null) continue;
                webServerUrl = playerinfo.getWebServerUrl();
            }
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("sign", "62bd6025d29b17ff");
            jsonObject.add("uids", uids);
            jsonObject.addProperty("game_type", "network");
            String json = jsonObject.toString();
            String url = webServerUrl + REPORT_FRIEND_ONLINE_URL;
            try {
                StringEntity entity = new StringEntity(json);
                WebUtil.postForm(url, entity, null);
            }
            catch (UnsupportedEncodingException e) {
                plugin.getLogger().warning("http report friend online url error:" + e);
                e.printStackTrace();
            }
        }
    }

    static {
        SKIN_VALID_URL = "/pe/web/skin-md5-valid-inner";
        SHIP_ITEM_URL = "/ship-mc-item-order";
        GET_ORDER_ITEM_URL = "/get-mc-item-order-list";
        REPORT_FRIEND_ONLINE_URL = "/game-server-info/collect-play-list";
        GAS_SERVER_BASE_URL = "http://gasproxy.mc.netease.com:60002";
        TEST_GAS_SERVER_BASE_URL = "http://gasproxy.mc.netease.com:60001";
        httpClient = HttpAsyncClients.createDefault();
    }
}

