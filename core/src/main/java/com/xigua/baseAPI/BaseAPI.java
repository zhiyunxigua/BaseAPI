package com.xigua.baseAPI;

import com.xigua.baseAPI.api.CoinAPI;
import com.xigua.baseAPI.api.playerInfo.PlayerInfo;
import com.xigua.baseAPI.eventListener.EventListener;
import com.xigua.baseAPI.manager.*;
import com.xigua.baseAPI.pluginmessage.ChannelRegistry;
import com.xigua.baseAPI.pluginmessage.channel.FormChannel;
import com.xigua.baseAPI.pluginmessage.channel.NeteaseCustomChannel;
import com.xigua.baseAPI.pluginmessage.channel.TransferChannel;
import com.xigua.baseAPI.util.PluginMessageUtils;
import com.xigua.baseAPI.util.WebUtil;
import com.xigua.cumulus.form.Form;
import com.xigua.cumulus.form.util.FormBuilder;
import lombok.Getter;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.util.EntityUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.msgpack.core.annotations.Nullable;


public final class BaseAPI extends JavaPlugin {
    private final String MESSAGE_NAME = "floodgate:netease";
    private final String FORM_MESSAGE_NAME = "floodgate:form";
    public static String SHOP_MOD_NAME = "neteaseShop";
    public static String SHOP_SERVER_SYS = "neteaseShopDev";
    public static String SHOP_CLIENT_SYS = "neteaseShopBeh";
    @Getter
    private EventListener eventListener;
    @Getter
    private String serverVersion;
    private Map<UUID, PlayerInfo> playerInfos = new ConcurrentHashMap<UUID, PlayerInfo>();

    @Getter
    private ConfigManager configManager;      // 配置文件管理
    @Getter
    private CommandManager commandManager;    // 指令管理
    @Getter
    private PlayerManager playerManager;      // 玩家数据管理
    @Getter
    private DatabaseManager databaseManager;  // 数据库
    @Getter
    public CoinAPI coinAPI;

    private ChannelRegistry registry;
    private PluginMessageUtils pluginMessageUtils;
    private FormChannel formChannel;
    private TransferChannel transferChannel;
    private NeteaseCustomChannel neteaseChannel;

    @Override
    public void onEnable() {
        this.getLogger().info("BaseAPI onEnable");
        saveDefaultConfig();
        // 获取服务器版本
        serverVersion = Bukkit.getVersion();
        // 应用版本适配器
        //this.versionWrapper = new VersionMatcher().match();
        this.pluginMessageUtils = new PluginMessageUtils(this);
        // 注册插件消息
        this.formChannel = new FormChannel(this, pluginMessageUtils);
        this.transferChannel = new TransferChannel(this, pluginMessageUtils);
        this.neteaseChannel = new NeteaseCustomChannel(this, pluginMessageUtils, playerInfos);

        this.registry = new ChannelRegistry(this);
        registry.registerChannel(formChannel);
        registry.registerChannel(transferChannel);
        registry.registerChannel(neteaseChannel);
        //
        WebUtil.setPlugin(this);
        WebUtil.startHttpClient();

        this.eventListener = new EventListener(this);

        this.getServer().getPluginManager().registerEvents(this.eventListener, this);
        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){

            @Override
            public void run() {
                WebUtil.reportFriendOnline();
            }
        }, 0L, 6000L);

        // 注册管理器
        configManager = new ConfigManager(this);
        commandManager = new CommandManager(this);
        playerManager = new PlayerManager(this);
        databaseManager = new DatabaseManager(this);

        System.out.println("BaseAPI 启动成功");
    }

    @Override
    public void onDisable() {
        WebUtil.stopHttpClient();
        // Plugin shutdown logic
    }

    public boolean notifyToClient(Player player, String event, Map<String, Object> data) {
        return neteaseChannel.sendModEvent(player.getUniqueId(), "Common", "main", event, data);
    }

    public boolean notifyToClient(Player player, String namespace, String system, String event, Map<String, Object> data) {
        return neteaseChannel.sendModEvent(player.getUniqueId(), namespace, system, event, data);
    }

    public boolean notifyToMultiClients(List<Player> players, String namespace, String system, String event, Map<String, Object> data) {
        for (Player player : players) {
            return neteaseChannel.sendModEvent(player.getUniqueId(), namespace, system, event, data);
        }
        return false;
    }

    public boolean notifyToClientsNearby(@Nullable Player except, Location loc, double dist, String namespace, String system, String event, Map<String, Object> data) {
        for (Player player : loc.getWorld().getPlayers()) {
            double dz;
            double dy;
            if (except == player) continue;
            Location loc2 = player.getLocation();
            double dx = loc.getX() - loc2.getX();
            if (!(dx * dx + (dy = loc.getY() - loc2.getY()) * dy + (dz = loc.getZ() - loc2.getZ()) * dz < dist * dist)) continue;
            return neteaseChannel.sendModEvent(player.getUniqueId(), namespace, system, event, data);
        }
        return false;
    }

    public boolean broadcastToAllClient(@Nullable Player except, World world, String namespace, String system, String event, Map<String, Object> data) {
        for (Player player : world.getPlayers()) {
            if (except == player) continue;
            return neteaseChannel.sendModEvent(player.getUniqueId(), namespace, system, event, data);
        }
        return false;
    }

    public boolean broadcastToAllClient(@Nullable Player except, String namespace, String system, String event, Map<String, Object> data) {
        for (Player player : this.getServer().getOnlinePlayers()) {
            if (except == player) continue;
            return neteaseChannel.sendModEvent(player.getUniqueId(), namespace, system, event, data);
        }
        return false;
    }

    public boolean sendForm(UUID uuid, Form form) {
        return formChannel.sendForm(uuid, form);
    }

    public boolean sendForm(UUID uuid, FormBuilder<?, ?, ?> formBuilder) {
        return sendForm(uuid, formBuilder.build());
    }

    public boolean transferPlayer(UUID uuid, String address, int port) {
        return transferChannel.sendTransfer(uuid, address, port);
    }

    public void openShop(Player player) {
        this.notifyToClient(player, SHOP_MOD_NAME, SHOP_SERVER_SYS, "OpenShopEvent", new HashMap<String, Object>());
    }

    public void closeShop(Player player) {
        this.notifyToClient(player, SHOP_MOD_NAME, SHOP_SERVER_SYS, "CloseShopEvent", new HashMap<String, Object>());
    }

    public void showHintOne(Player player, @Nullable String text) {
        HashMap<String, Object> data = new HashMap<String, Object>();
        ArrayList<String> hints = new ArrayList<String>();
        hints.add(text);
        data.put("hint", hints);
        text = text == null ? "" : text;
        this.notifyToClient(player, SHOP_MOD_NAME, SHOP_SERVER_SYS, "ShowHintEvent", data);
    }

    public void showHintTwo(Player player, String head, @Nullable String tail) {
        tail = tail == null ? "" : tail;
        HashMap<String, Object> data = new HashMap<String, Object>();
        ArrayList<String> hints = new ArrayList<String>();
        hints.add(head);
        hints.add(tail);
        data.put("hint", hints);
        this.notifyToClient(player, SHOP_MOD_NAME, SHOP_SERVER_SYS, "ShowHintEvent", data);
    }

    public void getPlayerOrderList(final Player player, final FutureCallback<Map<String, Object>> callback) {
        FutureCallback<HttpResponse> _cb = new FutureCallback<>(){
            @Override
            public void completed(HttpResponse response) {
                try {
                    String responseStr = EntityUtils.toString(response.getEntity());
                    JsonObject object = JsonParser.parseString(responseStr).getAsJsonObject();
                    int resCode = object.get("code").getAsInt();

                    if (response.getStatusLine().getStatusCode() != 200 || resCode != 0) {
                        Exception ex = new Exception("请求失败");
                        Bukkit.getScheduler().runTask(BaseAPI.this, () -> callback.failed(ex));
                        return;
                    }

                    HashMap<String, Object> res = new HashMap<String, Object>();
                    res.put("player", player);
                    res.put("json_result", object);

                    Bukkit.getScheduler().runTask(BaseAPI.this, () -> callback.completed(res));

                } catch (Exception e) {
                    e.printStackTrace();
                    Bukkit.getScheduler().runTask(BaseAPI.this, () ->
                            callback.failed(new Exception("请求玩家订单失败,json数据解析失败"))
                    );
                }
            }

            @Override
            public void failed(Exception ex) {
                Bukkit.getScheduler().runTask(BaseAPI.this, () -> callback.failed(ex));
            }

            @Override
            public void cancelled() {
                Bukkit.getScheduler().runTask(BaseAPI.this, () -> callback.cancelled());
            }
        };
        WebUtil.postGetItemOrders(player, _cb);
    }

    public void finPlayerOrder(final Player player, List<String> orderList, final FutureCallback<Map<String, Object>> callback) {
        FutureCallback<HttpResponse> _cb = new FutureCallback<HttpResponse>(){
            @Override
            public void completed(HttpResponse response) {
                try {
                    String responseStr = EntityUtils.toString(response.getEntity());
                    JsonObject object = JsonParser.parseString(responseStr).getAsJsonObject();
                    int resCode = object.get("code").getAsInt();

                    if (response.getStatusLine().getStatusCode() != 200 || resCode != 0) {
                        Exception ex = new Exception("通知失败");
                        Bukkit.getScheduler().runTask(BaseAPI.this, () -> callback.failed(ex));
                        return;
                    }

                    HashMap<String, Object> res = new HashMap<String, Object>();
                    res.put("player", player);
                    res.put("json_result", object);

                    Bukkit.getScheduler().runTask(BaseAPI.this, () -> callback.completed(res));

                } catch (Exception e) {
                    e.printStackTrace();
                    Bukkit.getScheduler().runTask(BaseAPI.this, () ->
                            callback.failed(new Exception("通知网易服务器修改订单失败,json数据解析失败"))
                    );
                }
            }

            @Override
            public void failed(Exception ex) {
                Bukkit.getScheduler().runTask(BaseAPI.this, () -> callback.failed(ex));
            }

            @Override
            public void cancelled() {
                Bukkit.getScheduler().runTask(BaseAPI.this, () -> callback.cancelled());
            }
        };
        WebUtil.postFinPlayerOrder(player, orderList, _cb);
    }

    public List<Long> getAllPlayerUids() {
        ArrayList<Long> res = new ArrayList<Long>();
        for (PlayerInfo info : this.playerInfos.values()) {
            res.add(info.getProxyUid());
        }
        return res;
    }

    public List<String> getAllPlayerUidStrs() {
        ArrayList<String> res = new ArrayList<String>();
        for (PlayerInfo info : this.playerInfos.values()) {
            long tmp = info.getProxyUid();
            res.add(Long.toString(tmp));
        }
        return res;
    }

    public long getPlayerUid(UUID uuid) {
        return this.getPlayerInfo(uuid).getProxyUid();
    }

    public long getPlayerUid(Player player) {
        return this.getPlayerInfo(player).getProxyUid();
    }

    public PlayerInfo getPlayerInfo(Player player) {
        return this.playerInfos.getOrDefault(player.getUniqueId(), null);
    }

    public PlayerInfo getPlayerInfo(UUID uuid) {
        return this.playerInfos.getOrDefault(uuid, null);
    }

    public void removePlayerInfo(Player player) {
        UUID javaUuid = player.getUniqueId();
        this.playerInfos.remove(javaUuid);
    }

}
