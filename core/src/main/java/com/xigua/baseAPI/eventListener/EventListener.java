package com.xigua.baseAPI.eventListener;

import com.xigua.baseAPI.BaseAPI;
import com.xigua.baseAPI.api.events.ClientLoadAddonFinishEvent;
import com.xigua.baseAPI.api.events.NeteasePythonEvent;
import com.xigua.baseAPI.api.events.PlayerBuyItemSuccessEvent;
import com.xigua.baseAPI.api.events.PlayerUrgeShipEvent;
import com.xigua.baseAPI.api.playerInfo.PlayerInfo;
import com.xigua.cumulus.form.SimpleForm;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
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
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        this.plugin.removePlayerInfo(player);
    }

    @EventHandler
    public void onNeteasePython(NeteasePythonEvent event) {
        Player player = event.getPlayer();
        if (Objects.equals(event.getNamespace(), "neteaseShop") && Objects.equals(event.getSystemName(), "neteaseShopBeh")) {
            switch (event.getEventName()) {
                case "clientEnterEvent":
                    PlayerInfo playerInfo = this.plugin.getPlayerInfo(player);
                    if (playerInfo == null) {
                        this.plugin.getLogger().info("player info is null,from shop");
                        return;
                    }
                    HashMap<String, Object> resData = new HashMap<String, Object>();
                    resData.put("gameId", playerInfo.getGameId());
                    resData.put("isTestServer", playerInfo.isTestServer());
                    resData.put("useCustomShop", this.plugin.getConfigManager().getUseCustomShop());
                    resData.put("cacheTime", 1);
                    resData.put("uid", playerInfo.getProxyUid());
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

