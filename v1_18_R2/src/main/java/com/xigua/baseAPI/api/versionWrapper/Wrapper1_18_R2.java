package com.xigua.baseAPI.api.versionWrapper;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.network.syncher.DataWatcherSerializer;
import net.minecraft.world.entity.EntitySize;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;

public final class Wrapper1_18_R2
implements VersionWrapper {
    private final Map<String, DataWatcherObject<String>> neteaseEntityIdentifier = new ConcurrentHashMap<String, DataWatcherObject<String>>();

    @Override
    public String getCustomItemIdentifier(ItemStack spigotItemStack) {
        if (spigotItemStack == null) {
            return null;
        }
        net.minecraft.world.item.ItemStack nmsCopy = CraftItemStack.asNMSCopy((ItemStack)spigotItemStack);
        if (!nmsCopy.s()) {
            return null;
        }
        NBTTagCompound compound = nmsCopy.t();
        if (!compound.e("netease_identifier")) {
            return null;
        }
        return compound.l("netease_identifier");
    }

    @Override
    public ItemStack setCustomItemIdentifier(ItemStack spigotItemStack, String customIdentifier) {
        if (spigotItemStack == null) {
            return null;
        }
        net.minecraft.world.item.ItemStack nmsCopy = CraftItemStack.asNMSCopy((ItemStack)spigotItemStack);
        NBTTagCompound compound = nmsCopy.s() ? nmsCopy.t() : new NBTTagCompound();
        compound.a("netease_identifier", customIdentifier);
        nmsCopy.c(compound);
        return CraftItemStack.asCraftMirror((net.minecraft.world.item.ItemStack)nmsCopy);
    }

    @Override
    public ItemStack setItemLayer(ItemStack itemStack, int layer, String texture) {
        if (itemStack == null) {
            return null;
        }
        net.minecraft.world.item.ItemStack nmsCopy = CraftItemStack.asNMSCopy((ItemStack)itemStack);
        NBTTagCompound compound = nmsCopy.s() ? nmsCopy.t() : new NBTTagCompound();
        String tag = this.layerToTag(layer);
        if (tag == null) {
            return itemStack;
        }
        compound.a(tag, texture);
        nmsCopy.c(compound);
        return CraftItemStack.asCraftMirror((net.minecraft.world.item.ItemStack)nmsCopy);
    }

    @Override
    public String getItemLayer(ItemStack itemStack, int layer) {
        if (itemStack == null) {
            return null;
        }
        net.minecraft.world.item.ItemStack nmsCopy = CraftItemStack.asNMSCopy((ItemStack)itemStack);
        if (!nmsCopy.s()) {
            return null;
        }
        String tag = this.layerToTag(layer);
        if (tag == null) {
            return null;
        }
        NBTTagCompound compound = nmsCopy.t();
        if (!compound.e(tag)) {
            return null;
        }
        return compound.l(tag);
    }

    @Override
    public ItemStack removeItemLayer(ItemStack itemStack, int layer) {
        if (itemStack == null) {
            return null;
        }
        net.minecraft.world.item.ItemStack nmsCopy = CraftItemStack.asNMSCopy((ItemStack)itemStack);
        if (!nmsCopy.s()) {
            return itemStack;
        }
        String tag = this.layerToTag(layer);
        if (tag == null) {
            return itemStack;
        }
        NBTTagCompound compound = nmsCopy.t();
        if (!compound.e(tag)) {
            return itemStack;
        }
        compound.r(tag);
        nmsCopy.c(compound);
        return CraftItemStack.asCraftMirror((net.minecraft.world.item.ItemStack)nmsCopy);
    }

    @Override
    public String getCustomEntityIdentifier(Entity entity) {
        DataWatcherObject<String> neteaseEntityNbt;
        net.minecraft.world.entity.Entity spigotEntity = ((CraftEntity)entity).getHandle();
        if (this.entityHasNeteaseNbt(spigotEntity, (neteaseEntityNbt = this.getNetaseEntityNbt(spigotEntity.getClass())).a())) {
            String identifierWithPrefix = (String)spigotEntity.ai().a(neteaseEntityNbt);
            return this.customEntityIdentifierWithoutPrefix(identifierWithPrefix);
        }
        return null;
    }

    @Override
    public Entity setCustomEntityIdentifier(Entity entity, String customIdentifier) {
        net.minecraft.world.entity.Entity spigotEntity = ((CraftEntity)entity).getHandle();
        DataWatcherObject<String> neteaseEntityNbt = this.getNetaseEntityNbt(spigotEntity.getClass());
        String customIdentifierWithPrefix = this.customEntityIdentifierWithPrefix(customIdentifier);
        if (this.entityHasNeteaseNbt(spigotEntity, neteaseEntityNbt.a())) {
            spigotEntity.ai().b(neteaseEntityNbt, customIdentifierWithPrefix);
            entity.addScoreboardTag(customIdentifierWithPrefix);
            return spigotEntity.getBukkitEntity();
        }
        spigotEntity.ai().registrationLocked = false;
        spigotEntity.ai().a(neteaseEntityNbt, customIdentifierWithPrefix);
        spigotEntity.ai().registrationLocked = true;
        entity.addScoreboardTag(customIdentifierWithPrefix);
        return spigotEntity.getBukkitEntity();
    }

    @Override
    public void setEntitySize(Entity entity, float height, float width) {
        net.minecraft.world.entity.Entity spigotEntity = ((CraftEntity)entity).getHandle();
        EntitySize newSize = new EntitySize(width, height, false);
        try {
            Field entitySize = spigotEntity.getClass().getDeclaredField("aZ");
            entitySize.setAccessible(true);
            entitySize.set(spigotEntity, newSize);
        }
        catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        catch (SecurityException e) {
            e.printStackTrace();
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public boolean entityHasNeteaseNbt(net.minecraft.world.entity.Entity spigotEntity, int dataKey) {
        try {
            DataWatcher dataWatcher = spigotEntity.ai();
            Field dataWatherMap = dataWatcher.getClass().getDeclaredField("f");
            dataWatherMap.setAccessible(true);
            Map dataMap = (Map)dataWatherMap.get(dataWatcher);
            return dataMap.containsKey(dataKey);
        }
        catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        catch (SecurityException e) {
            e.printStackTrace();
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    public DataWatcherObject<String> getNetaseEntityNbt(Class<? extends net.minecraft.world.entity.Entity> entityClass) {
        return this.neteaseEntityIdentifier.computeIfAbsent(entityClass.getName(), _key -> DataWatcher.a((Class)entityClass, (DataWatcherSerializer)DataWatcherRegistry.d));
    }

    @Override
    public void addEntity(Player player, Entity entity, CreatureSpawnEvent.SpawnReason reason) {
        net.minecraft.world.entity.Entity spigotEntity = ((CraftEntity)entity).getHandle();
        CraftWorld world = (CraftWorld)player.getWorld();
        world.addEntity(spigotEntity, reason);
    }

    @Override
    public void addEntity(World world, Entity entity, CreatureSpawnEvent.SpawnReason reason) {
        net.minecraft.world.entity.Entity spigotEntity = ((CraftEntity)entity).getHandle();
        CraftWorld craftWorld = (CraftWorld)world;
        craftWorld.addEntity(spigotEntity, reason);
    }

    @Override
    public ItemStack removeGameProfileTag(ItemStack itemStack) {
        if (itemStack == null) {
            return null;
        }
        net.minecraft.world.item.ItemStack nmsCopy = CraftItemStack.asNMSCopy((ItemStack)itemStack);
        if (!nmsCopy.s()) {
            return itemStack;
        }
        NBTTagCompound compound = nmsCopy.t();
        compound.r("Id");
        compound.r("Name");
        compound.r("Properties");
        compound.r("netease_identifier");
        nmsCopy.c(compound);
        return CraftItemStack.asCraftMirror((net.minecraft.world.item.ItemStack)nmsCopy);
    }
}

