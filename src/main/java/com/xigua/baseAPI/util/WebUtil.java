package com.xigua.baseAPI.util;

import com.xigua.baseAPI.BaseAPI;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xigua.baseAPI.manager.ConfigManager;
import lombok.Setter;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.bukkit.entity.Player;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebUtil {
    private static final CloseableHttpAsyncClient httpClient;
    @Setter
    private static BaseAPI plugin;
    public static String SHIP_ITEM_URL;  // 通知网易服务器mc游戏中发货成功
    public static String GET_ORDER_ITEM_URL;  // 查询指定玩家未发货订单的信息
    public static String GET_UID_URL;  // 通过UUID获取UID
    public static String GET_UUID_URL;  // 通过UID和昵称获取UUID


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
            ConfigManager.NeteaseShop shop = plugin.getConfigManager().getNeteaseShop();
            String json;
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("gameid", shop.getGameId(player.getUniqueId()));
            jsonObject.addProperty("uuid", player.getUniqueId().toString());
            json = jsonObject.toString();
            String validUrl = shop.getShopServerUrl(player.getUniqueId()) + GET_ORDER_ITEM_URL;
            HashMap<String, String> headMaps = new HashMap<>();
            String signStr = WebUtil.getServerSign(shop.getGameKey(player.getUniqueId()), "POST", GET_ORDER_ITEM_URL, json);
            headMaps.put("Netease-Server-Sign", signStr);
            StringEntity entity = new StringEntity(json);
            WebUtil.postFormWithExtraHead(validUrl, headMaps, entity, callback);
        }
        catch (Exception e) {
            plugin.getLogger().warning("http client init get item orders post url error:" + e);
            e.printStackTrace();
        }
    }

    public static String getServerSign(String signKey, String method, String path, String httpBody) throws Exception {
        String str2sign = method + path + httpBody;
        String signStr = WebUtil.hmacWithJava("HmacSHA256", str2sign, signKey);
        if (signStr.length() < 64) {
            int preLen = 64 - signStr.length();
            String preZero = "0";
            signStr = String.join("", Collections.nCopies(preLen, preZero)) + signStr;
        }
        return signStr;
    }

    public static void postFinPlayerOrder(Player player, List<String> orderIds, FutureCallback<HttpResponse> callback) {
        try {
            ConfigManager.NeteaseShop shop = plugin.getConfigManager().getNeteaseShop();
            String json = "";
            JsonObject jsonObject = new JsonObject();
            JsonArray orderIdsArray = new JsonArray();
            for (String str : orderIds) {
                orderIdsArray.add(str);
            }
            jsonObject.addProperty("gameid", shop.getGameId(player.getUniqueId()));
            jsonObject.addProperty("uuid", player.getUniqueId().toString());
            jsonObject.add("orderid_list", orderIdsArray);
            json = jsonObject.toString();
            String validUrl = shop.getShopServerUrl(player.getUniqueId()) + SHIP_ITEM_URL;
            HashMap<String, String> headMaps = new HashMap<>();
            String signStr = WebUtil.getServerSign(shop.getGameKey(player.getUniqueId()), "POST", SHIP_ITEM_URL, json);
            headMaps.put("Netease-Server-Sign", signStr);
            StringEntity entity = new StringEntity(json);
            WebUtil.postFormWithExtraHead(validUrl, headMaps, entity, callback);
        }
        catch (Exception e) {
            plugin.getLogger().warning("http client init ship item orders post url error:" + e);
            e.printStackTrace();
        }
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

    public static String hmacWithJava(String algorithm, String data, String key) throws Exception {
        SecretKeySpec skey = new SecretKeySpec(key.getBytes(), algorithm);
        Mac mac = Mac.getInstance(algorithm);
        mac.init(skey);
        mac.update(data.getBytes());
        byte[] res = mac.doFinal();
        return new BigInteger(1, res).toString(16);
    }

    /**
     * 通过玩家的UUID获取UID
     * @param player 玩家对象
     * @param callback 回调函数
     */
    public static void getUidFromUuid(Player player, FutureCallback<HttpResponse> callback) {
        try {
            ConfigManager.NeteaseShop shop = plugin.getConfigManager().getNeteaseShop();
            String json = "";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("uuid", player.getUniqueId().toString());
            json = jsonObject.toString();
            String baseUrl = shop.getShopServerUrl(player.getUniqueId());
            String validUrl = baseUrl + GET_UID_URL;
            String path = GET_UID_URL;
            String signKey = shop.getGameKey(player.getUniqueId());
            HashMap<String, String> headMaps = new HashMap<>();
            String signStr = WebUtil.getServerSign(signKey, "POST", path, json);
            headMaps.put("Netease-Server-Sign", signStr);
            StringEntity entity = new StringEntity(json, StandardCharsets.UTF_8);
            WebUtil.postFormWithExtraHead(validUrl, headMaps, entity, callback);
        } catch (Exception e) {
            plugin.getLogger().warning("获取UID请求失败: " + e);
            e.printStackTrace();
        }
    }

    static {
        SHIP_ITEM_URL = "/ship-mc-item-order";
        GET_ORDER_ITEM_URL = "/get-mc-item-order-list";
        GET_UID_URL = "/uid-from-uuid";
        httpClient = HttpAsyncClients.createDefault();
    }
}

