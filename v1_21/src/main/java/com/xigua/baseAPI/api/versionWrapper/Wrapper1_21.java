package com.xigua.baseAPI.api.versionWrapper;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.item.component.CustomData;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Wrapper1_21 implements VersionWrapper {

    private final Map<String, EntityDataAccessor<String>> neteaseEntityIdentifier = new ConcurrentHashMap<>();
    private final NamespacedKey key;

    public Wrapper1_21() {
        this.key = new NamespacedKey("spigotmaster", "netease_identifier");
    }

    @Override
    public String getCustomItemIdentifier(ItemStack spigotItemStack) {
        if (spigotItemStack == null) {
            return null;
        }
        net.minecraft.world.item.ItemStack nmsCopy = CraftItemStack.asNMSCopy(spigotItemStack);
        // 使用 getOrDefault 防止 NullPointerException
        CustomData customData = nmsCopy.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (customData.isEmpty()) {
            return null;
        }

        CompoundTag tag = customData.copyTag();
        if (!tag.contains("netease_identifier")) {
            return null;
        }
        return tag.getString("netease_identifier");
    }

    @Override
    public ItemStack setCustomItemIdentifier(ItemStack spigotItemStack, String customIdentifier) {
        if (spigotItemStack == null) {
            return null;
        }
        net.minecraft.world.item.ItemStack nmsCopy = CraftItemStack.asNMSCopy(spigotItemStack);

        CustomData customData = nmsCopy.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();

        tag.putString("netease_identifier", customIdentifier);
        nmsCopy.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return CraftItemStack.asBukkitCopy(nmsCopy);
    }

    @Override
    public String getItemLayer(ItemStack itemStack, int layer) {
        if (itemStack == null) {
            return null;
        }
        net.minecraft.world.item.ItemStack nmsCopy = CraftItemStack.asNMSCopy(itemStack);

        CustomData customData = nmsCopy.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (customData.isEmpty()) {
            return null;
        }

        String tagKey = layerToTag(layer);
        if (tagKey == null) return null;

        CompoundTag tag = customData.copyTag();
        if (!tag.contains(tagKey)) {
            return null;
        }
        return tag.getString(tagKey);
    }

    @Override
    public ItemStack setItemLayer(ItemStack itemStack, int layer, String texture) {
        if (itemStack == null) {
            return null;
        }
        net.minecraft.world.item.ItemStack nmsCopy = CraftItemStack.asNMSCopy(itemStack);

        String tagKey = layerToTag(layer);
        if (tagKey == null) return itemStack;

        CustomData customData = nmsCopy.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();

        tag.putString(tagKey, texture);

        nmsCopy.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return CraftItemStack.asBukkitCopy(nmsCopy);
    }

    @Override
    public ItemStack removeItemLayer(ItemStack itemStack, int layer) {
        if (itemStack == null) {
            return null;
        }
        net.minecraft.world.item.ItemStack nmsCopy = CraftItemStack.asNMSCopy(itemStack);

        CustomData customData = nmsCopy.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (customData.isEmpty()) {
            return itemStack;
        }

        String tagKey = layerToTag(layer);
        if (tagKey == null) return itemStack;

        CompoundTag tag = customData.copyTag();
        if (!tag.contains(tagKey)) {
            return itemStack;
        }

        tag.remove(tagKey);

        nmsCopy.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return CraftItemStack.asBukkitCopy(nmsCopy);
    }

    @Override
    public String getCustomEntityIdentifier(org.bukkit.entity.Entity entity) {
        if (entity == null) return null;
        PersistentDataContainer pdc = entity.getPersistentDataContainer();

        if (pdc.has(key, PersistentDataType.STRING)) {
            return customEntityIdentifierWithoutPrefix(pdc.get(key, PersistentDataType.STRING));
        }
        return null;
    }

    @Override
    public org.bukkit.entity.Entity setCustomEntityIdentifier(org.bukkit.entity.Entity entity, String customIdentifier) {
        if (entity == null) return null;
        String customIdentifierWithPrefix = customEntityIdentifierWithPrefix(customIdentifier);

        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        pdc.set(key, PersistentDataType.STRING, customIdentifierWithPrefix);

        if (!entity.getScoreboardTags().contains(customIdentifierWithPrefix)) {
            entity.addScoreboardTag(customIdentifierWithPrefix);
        }
        return entity;
    }

    @Override
    public void setEntitySize(org.bukkit.entity.Entity entity, float width, float height) {
        Entity spigotEntity = ((CraftEntity) entity).getHandle();
        EntityDimensions newSize = EntityDimensions.scalable(width, height);

        try {
            Field dimensionsField = Entity.class.getDeclaredField("dimensions");
            dimensionsField.setAccessible(true);
            dimensionsField.set(spigotEntity, newSize);
            spigotEntity.refreshDimensions();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public String layerToTag(int layer) {
        switch (layer) {
            case 0: return "layer0";
            case 1: return "layer1";
            case 2: return "layer2";
            case 3: return "layer3";
            default: return null;
        }
    }

    public String customEntityIdentifierWithPrefix(String identifier) {
        return "netease:" + identifier;
    }

    public String customEntityIdentifierWithoutPrefix(String identifierWithPrefix) {
        if (identifierWithPrefix == null || !identifierWithPrefix.startsWith("netease:")) {
            return identifierWithPrefix;
        }
        return identifierWithPrefix.substring("netease:".length());
    }

    @Override
    public void addEntity(Player player, org.bukkit.entity.Entity entity, CreatureSpawnEvent.SpawnReason reason) {
        Entity spigotEntity = ((CraftEntity) entity).getHandle();
        CraftWorld world = (CraftWorld) player.getWorld();
        world.addEntity(spigotEntity, reason);
    }

    @Override
    public void addEntity(World world, org.bukkit.entity.Entity entity, CreatureSpawnEvent.SpawnReason reason) {
        Entity spigotEntity = ((CraftEntity) entity).getHandle();
        CraftWorld craftWorld = (CraftWorld) world;
        craftWorld.addEntity(spigotEntity, reason);
    }

    @Override
    public ItemStack removeGameProfileTag(ItemStack itemStack) {
        if (itemStack == null) {
            return null;
        }
        net.minecraft.world.item.ItemStack nmsCopy = CraftItemStack.asNMSCopy(itemStack);

        // 1. 清理 CustomData 中的旧数据
        CustomData customData = nmsCopy.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (!customData.isEmpty()) {
            CompoundTag tag = customData.copyTag();
            boolean changed = false;
            // 移除可能存在的旧版属性
            if(tag.contains("Id")) { tag.remove("Id"); changed = true; }
            if(tag.contains("Name")) { tag.remove("Name"); changed = true; }
            if(tag.contains("Properties")) { tag.remove("Properties"); changed = true; }
            if(tag.contains("netease_identifier")) { tag.remove("netease_identifier"); changed = true; }

            if (changed) {
                nmsCopy.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            }
        }

        // 2. 清理 1.21 新版 Profile 组件 (头颅皮肤数据)
        nmsCopy.remove(DataComponents.PROFILE);

        return CraftItemStack.asBukkitCopy(nmsCopy);
    }
}