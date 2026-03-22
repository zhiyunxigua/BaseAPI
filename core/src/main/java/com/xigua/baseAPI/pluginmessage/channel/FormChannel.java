package com.xigua.baseAPI.pluginmessage.channel;

import com.google.common.base.Charsets;
import com.xigua.baseAPI.BaseAPI;
import com.xigua.baseAPI.util.PluginMessageUtils;
import it.unimi.dsi.fastutil.objects.Object2ShortMap;
import it.unimi.dsi.fastutil.objects.Object2ShortMaps;
import it.unimi.dsi.fastutil.objects.Object2ShortOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMaps;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import com.xigua.cumulus.form.Form;
import com.xigua.cumulus.form.impl.FormDefinition;
import com.xigua.cumulus.form.impl.FormDefinitions;
import com.xigua.baseAPI.pluginmessage.PluginMessageChannel;
import org.bukkit.Bukkit;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class FormChannel implements PluginMessageChannel {
    private final BaseAPI plugin;
    private final FormDefinitions formDefinitions = FormDefinitions.instance();
    private final Short2ObjectMap<Form> storedForms =
            Short2ObjectMaps.synchronize(new Short2ObjectOpenHashMap<>());
    // Tracking for players last opened form
    private final Object2ShortMap<UUID> uuidForms =
            Object2ShortMaps.synchronize(new Object2ShortOpenHashMap<>());
    private final AtomicInteger nextFormId = new AtomicInteger(1);

    private final PluginMessageUtils pluginMessageUtils;

    public FormChannel(BaseAPI plugin, PluginMessageUtils pluginMessageUtils) {
        this.plugin = plugin;
        this.pluginMessageUtils = pluginMessageUtils;
    }

    @Override
    public String getIdentifier() {
        return "floodgate:form";
    }

    @Override
    public Result handleProxyCall(
            byte[] data,
            UUID sourceUuid,
            String sourceUsername,
            Identity sourceIdentity
    ) {
        if (sourceIdentity == Identity.SERVER) {
            // send it to the client
            return Result.forward();
        }

        if (sourceIdentity == Identity.PLAYER) {
            if (data.length < 2) {
                return Result.kick("Invalid form response");
            }

            short formId = getFormId(data);

            // if the bit is not set, it's for the connected server
            if ((formId & 0x8000) == 0) {
                return Result.forward();
            }

            if (!callResponseConsumer(data)) {
                Bukkit.getLogger().severe(String.format(
                        "Couldn't find stored form with id %s for player %s",
                        formId, sourceUsername));
            }
        }
        return Result.handled();
    }

    @Override
    public Result handleServerCall(byte[] data, UUID playerUuid, String playerUsername) {
        callResponseConsumer(data);
        return Result.handled();
    }

    public boolean sendForm(UUID player, Form form) {
        // Player can only open one form at a time, so we make old ones invalid
        playerRemoved(player);
        byte[] formData = createFormData(player, form);
        return pluginMessageUtils.sendMessage(player, getIdentifier(), formData);
    }

    public byte[] createFormData(UUID player, Form form) {
        short formId = getNextFormId();
        uuidForms.put(player, formId);
        storedForms.put(formId, form);

        FormDefinition<Form, ?, ?> definition = formDefinitions.definitionFor(form);

        byte[] jsonData =
                definition.codec()
                        .jsonData(form)
                        .getBytes(Charsets.UTF_8);

        byte[] data = new byte[jsonData.length + 3];
        data[0] = (byte) definition.formType().ordinal();
        data[1] = (byte) (formId >> 8 & 0xFF);
        data[2] = (byte) (formId & 0xFF);
        System.arraycopy(jsonData, 0, data, 3, jsonData.length);
        return data;
    }

    protected boolean callResponseConsumer(byte[] data) {
        Form storedForm = storedForms.remove(getFormId(data));
        if (storedForm != null) {
            String responseData = new String(data, 2, data.length - 2, Charsets.UTF_8);
            try {
                formDefinitions.definitionFor(storedForm)
                        .handleFormResponse(storedForm, responseData);
            } catch (Exception e) {
                Bukkit.getLogger().severe("Error while processing form response!" + e);
            }
            return true;
        }
        return false;
    }

    protected short getFormId(byte[] data) {
        return (short) ((data[0] & 0xFF) << 8 | data[1] & 0xFF);
    }

    protected short getNextFormId() {
        // signed bit is used to check if the form is from a proxy or a server
        return (short) nextFormId.getAndUpdate(
                (number) -> number == Short.MAX_VALUE ? 0 : number + 1);
    }

    public void playerRemoved(UUID correctUuid) {
        short key = uuidForms.removeShort(correctUuid);
        if (key != 0) {
            Form storedForm = storedForms.remove(key);
            if (storedForm != null) {
                try {
                    formDefinitions.definitionFor(storedForm)
                            .handleFormResponse(storedForm, "null");
                } catch (Exception e) {
                    Bukkit.getLogger().severe("Error while processing form response!" + e);
                }
            }
        }
    }
}
