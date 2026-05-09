package com.xigua.baseAPI.eventListener;

import com.xigua.baseAPI.BaseAPI;
import com.xigua.baseAPI.api.events.ClientLoadAddonFinishEvent;
import com.xigua.baseAPI.api.events.NeteasePythonEvent;
import com.xigua.baseAPI.api.events.PlayerBuyItemSuccessEvent;
import com.xigua.baseAPI.api.events.PlayerUrgeShipEvent;
import com.xigua.baseAPI.manager.ConfigManager;
import com.xigua.baseAPI.manager.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Objects;

public class EventListener
implements Listener {
    private BaseAPI plugin;

    public EventListener(BaseAPI plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            PlayerManager playerManager = plugin.getPlayerManager();
            if (!playerManager.hasCachedUid(player)) {
                playerManager.getOrFetchUid(player, new PlayerManager.UidCallback() {
                    @Override
                    public void onResult(long uid) {
                    }

                    @Override
                    public void onError(Exception ex) {
                        plugin.getLogger().warning("获取玩家 " + player.getName() + " UID失败: " + ex.getMessage());
                    }
                });
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerManager playerManager = plugin.getPlayerManager();
        if (playerManager != null) {
            playerManager.removePlayerCache(player);
        }
    }

    @EventHandler
    public void onNeteasePython(NeteasePythonEvent event) {
        Player player = event.getPlayer();
        ConfigManager.NeteaseShop shop = plugin.getConfigManager().getNeteaseShop();
        if (Objects.equals(event.getNamespace(), "neteaseShop") && Objects.equals(event.getSystemName(), "neteaseShopBeh")) {
            switch (event.getPyEventName()) {
                case "clientEnterEvent":
                    HashMap<String, Object> resData = new HashMap<>();
                    resData.put("gameId", shop.getBE().getGameId());
                    resData.put("isTestServer", plugin.getConfigManager().isTestServer());
                    resData.put("useCustomShop", false);
                    resData.put("cacheTime", 1);
                    resData.put("uid", plugin.getPlayerUid(player));
                    resData.put("platformUid", "");
                    this.plugin.notifyToClient(player, "neteaseShop", "neteaseShopDev", "serverReadyEvent", resData);
                    break;
                case "clientForceShipEvent", "UrgeShipEvent":
                    Bukkit.getPluginManager().callEvent(new PlayerUrgeShipEvent(player));
                    break;
                case "clientBuyItemPaySuccessEvent", "StoreBuySuccServerEvent":
                    Bukkit.getPluginManager().callEvent(new PlayerBuyItemSuccessEvent(player));
            }
        }
    }

    @EventHandler
    public void onClientLoadAddonFinish(ClientLoadAddonFinishEvent event) {
        HashMap<String, Object> resData = new HashMap<String, Object>();
        resData.put("open", false);
        this.plugin.notifyToClient(event.getPlayer(), "Minecraft", "chatExtension", "ChatExtensionOpenStateChangedEvent", resData);
    }
}
