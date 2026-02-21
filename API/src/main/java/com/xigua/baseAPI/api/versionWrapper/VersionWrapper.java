/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.World
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.Player
 *  org.bukkit.event.entity.CreatureSpawnEvent$SpawnReason
 *  org.bukkit.inventory.ItemStack
 */
package com.xigua.baseAPI.api.versionWrapper;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;

public interface VersionWrapper {
    public static final String customEntityPrefix = "__netease_entity_identifier,";

    public String getCustomItemIdentifier(ItemStack var1);

    public ItemStack setCustomItemIdentifier(ItemStack var1, String var2);

    public String getCustomEntityIdentifier(Entity var1);

    public Entity setCustomEntityIdentifier(Entity var1, String var2);

    public void setEntitySize(Entity var1, float var2, float var3);

    default public String customEntityIdentifierWithPrefix(String customIdentifier) {
        return customEntityPrefix + customIdentifier;
    }

    default public String customEntityIdentifierWithoutPrefix(String customIdentifier) {
        if (customIdentifier.startsWith(customEntityPrefix)) {
            return customIdentifier.substring(customEntityPrefix.length());
        }
        return customIdentifier;
    }

    public ItemStack setItemLayer(ItemStack var1, int var2, String var3);

    public String getItemLayer(ItemStack var1, int var2);

    public ItemStack removeItemLayer(ItemStack var1, int var2);

    public ItemStack removeGameProfileTag(ItemStack var1);

    public void addEntity(Player var1, Entity var2, CreatureSpawnEvent.SpawnReason var3);

    public void addEntity(World var1, Entity var2, CreatureSpawnEvent.SpawnReason var3);

    default public String layerToTag(int layer) {
        switch (layer) {
            case 3: {
                return "top_layer_3";
            }
            case 2: {
                return "top_layer_2";
            }
            case 1: {
                return "top_layer_1";
            }
            case -1: {
                return "bottom_layer_1";
            }
            case -2: {
                return "bottom_layer_2";
            }
        }
        return null;
    }
}

