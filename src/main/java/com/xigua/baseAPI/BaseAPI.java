package com.xigua.baseAPI;

import com.xigua.baseAPI.api.TopBar;
import com.xigua.baseAPI.api.TextBoard;
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

import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.util.EntityUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.msgpack.core.annotations.Nullable;


public final class BaseAPI extends JavaPlugin {
    public static String SHOP_MOD_NAME = "neteaseShop";
    public static String SHOP_SERVER_SYS = "neteaseShopDev";
    public static String SHOP_CLIENT_SYS = "neteaseShopBeh";
    @Getter
    private EventListener eventListener;
    @Getter
    private String serverVersion;

    @Getter
    private boolean isTestServer;

    @Getter
    private ConfigManager configManager;      // 配置文件管理
    @Getter
    private CommandManager commandManager;    // 指令管理
    @Getter
    private PlayerManager playerManager;      // 玩家数据管理

    private ChannelRegistry registry;
    private PluginMessageUtils pluginMessageUtils;
    private FormChannel formChannel;
    private TransferChannel transferChannel;
    private NeteaseCustomChannel neteaseChannel;

    @Override
    public void onEnable() {
        // 注册管理器
        configManager = new ConfigManager(this);
        if (configManager.getIsDebug()) {
            this.getLogger().info("debug启动");
        }
        commandManager = new CommandManager(this);
        playerManager = new PlayerManager(this);
        isTestServer = configManager.isTestServer();


        // 获取服务器版本
        serverVersion = Bukkit.getVersion();
        this.pluginMessageUtils = new PluginMessageUtils(this);
        // 注册插件消息
        this.formChannel = new FormChannel(this, pluginMessageUtils);
        this.transferChannel = new TransferChannel(this, pluginMessageUtils);
        this.neteaseChannel = new NeteaseCustomChannel(this, pluginMessageUtils);

        this.registry = new ChannelRegistry(this);
        registry.registerChannel(formChannel);
        registry.registerChannel(transferChannel);
        registry.registerChannel(neteaseChannel);
        // 启动网易商城订单服务
        WebUtil.setPlugin(this);
        WebUtil.startHttpClient();

        this.eventListener = new EventListener(this);

        this.getServer().getPluginManager().registerEvents(this.eventListener, this);

        // 启动ping发送
        if (configManager.getEnablePing()) {
            startPingTask();
        }

        this.getLogger().info("BaseAPI 启动成功");
    }

    @Override
    public void onDisable() {
        WebUtil.stopHttpClient();
        // Plugin shutdown logic
    }

    public boolean notifyToClient(Player player, String event, Map<String, Object> data) {
        return this.notifyToClient(player.getUniqueId(), event, data);
    }

    public boolean notifyToClient(UUID uuid, String event, Map<String, Object> data) {
        return this.notifyToClient(uuid, configManager.getClientNamespace(), configManager.getClientSystemName(), event, data);
    }

    public boolean notifyToClient(Player player, String namespace, String system, String event, Map<String, Object> data) {
        return this.notifyToClient(player.getUniqueId(), namespace, system, event, data);
    }

    public boolean notifyToClient(UUID uuid, String namespace, String system, String event, Map<String, Object> data) {
        if (configManager.getIsDebug()) {
            getLogger().info(String.format("发送PyRPC消息 %s:%s:%s 数据:%s", namespace, system, event, data));
        }
        return neteaseChannel.sendModEvent(uuid, namespace, system, event, data);
    }

    public boolean notifyToMultiClients(Collection<? extends Player> players, String namespace, String system, String event, Map<String, Object> data) {
        boolean success = true;
        for (Player player : players) {
            success &= this.notifyToClient(player.getUniqueId(), namespace, system, event, data);
        }
        return success;
    }

    public boolean notifyToClientsNearby(@Nullable Player except, Location loc, double dist, String namespace, String system, String event, Map<String, Object> data) {
        boolean success = false;
        for (Player player : loc.getWorld().getPlayers()) {
            double dz;
            double dy;
            if (except == player) continue;
            Location loc2 = player.getLocation();
            double dx = loc.getX() - loc2.getX();
            if (!(dx * dx + (dy = loc.getY() - loc2.getY()) * dy + (dz = loc.getZ() - loc2.getZ()) * dz < dist * dist)) continue;
            success |= this.notifyToClient(player.getUniqueId(), namespace, system, event, data);
        }
        return success;
    }

    public boolean broadcastToAllClient(@Nullable Player except, World world, String namespace, String system, String event, Map<String, Object> data) {
        boolean success = false;
        for (Player player : world.getPlayers()) {
            if (except == player) continue;
            success |= this.notifyToClient(player.getUniqueId(), namespace, system, event, data);
        }
        return success;
    }

    public boolean broadcastToAllClient(@Nullable Player except, String namespace, String system, String event, Map<String, Object> data) {
        boolean success = false;
        for (Player player : this.getServer().getOnlinePlayers()) {
            if (except == player) continue;
            success |= this.notifyToClient(player.getUniqueId(), namespace, system, event, data);
        }
        return success;
    }

    private void startPingTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // 获取所有在线玩家
                Collection<? extends Player> players = Bukkit.getOnlinePlayers();

                if (!players.isEmpty()) {
                    Map<String, Object> data = new HashMap<>();
                    for (Player player : players) {
                        Map<String, Object> playerInfo = new HashMap<>();
                        playerInfo.put("name", player.getDisplayName());
                        playerInfo.put("value", player.getPing());

                        data.put(String.valueOf(player.getEntityId()), playerInfo);
                    }
                    // 调用通知方法，发送给所有玩家
                    notifyToMultiClients(players, configManager.getClientNamespace(), configManager.getClientSystemName(), "pong", data);
                }
            }
        }.runTaskTimer(this, 0L, 20L); // 20 ticks = 1秒
    }

    public boolean sendTopBar(UUID uuid, TopBar topBar) {
        return notifyToClient(uuid, "initTopBar", topBar.toHashMap());
    }

    public TextBoard.Builder createTextBoard(String text) {
        return TextBoard.create(text);
    }

    public boolean initTextBoard(Player player) {
        return TextBoard.init(player);
    }

    public boolean initTextBoards(Collection<? extends Player> players) {
        return TextBoard.init(players);
    }

    public boolean sendTopBar(TopBar topBar) {
        HashMap<String, Object> data = topBar.toHashMap();
        for (Player player : this.getServer().getOnlinePlayers()) {
            notifyToClient(player.getUniqueId(), "initTopBar", data);
        }
        return true;
    }

    public boolean updateTopBar(UUID uuid, TopBar topBar) {
        return notifyToClient(uuid, "updateTopBar", topBar.flushAllChanges());
    }

    public boolean updateTopBar(TopBar topBar) {
        HashMap<String, Object> data = topBar.flushAllChanges();
        for (Player player : this.getServer().getOnlinePlayers()) {
            notifyToClient(player.getUniqueId(), "updateTopBar", data);
        }
        return true;
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

    public void getPlayerOrderList(final Player player, final FutureCallback<Map<String, Object>> callback) {
        FutureCallback<HttpResponse> _cb = new FutureCallback<>() {
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

                    HashMap<String, Object> res = new HashMap<>();
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

                    HashMap<String, Object> res = new HashMap<>();
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

    /**
     * 获取玩家的UID（网易用户ID）
     * @param player 玩家
     */
    public long getPlayerUid(final Player player) {
        return this.playerManager.getCachedUid(player);
    }

    /**
     * 获取玩家的UID（网易用户ID）
     * @param player 玩家
     * @param callback 回调函数，返回的Map中包含 uid 字段
     */
    public void queryPlayerUid(final Player player, final FutureCallback<Map<String, Object>> callback) {
        FutureCallback<HttpResponse> _cb = new FutureCallback<>() {
            @Override
            public void completed(HttpResponse response) {
                try {
                    String responseStr = EntityUtils.toString(response.getEntity(), "UTF-8");
                    JsonObject object = JsonParser.parseString(responseStr).getAsJsonObject();
                    int resCode = object.get("code").getAsInt();

                    if (response.getStatusLine().getStatusCode() != 200 || resCode != 0) {
                        String message = object.has("message") ? object.get("message").getAsString() : "未知错误";
                        Exception ex = new Exception("获取UID失败: " + message);
                        Bukkit.getScheduler().runTask(BaseAPI.this, () -> callback.failed(ex));
                        return;
                    }

                    // 解析返回的uid
                    JsonObject entity = object.getAsJsonObject("entity");
                    long uid = entity.get("uid").getAsLong();

                    HashMap<String, Object> res = new HashMap<>();
                    res.put("player", player);
                    res.put("uid", uid);
                    res.put("json_result", object);

                    Bukkit.getScheduler().runTask(BaseAPI.this, () -> callback.completed(res));

                } catch (Exception e) {
                    e.printStackTrace();
                    Bukkit.getScheduler().runTask(BaseAPI.this, () ->
                            callback.failed(new Exception("解析UID响应失败: " + e.getMessage()))
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

        WebUtil.getUidFromUuid(player, _cb);
    }
}
