package com.xigua.baseAPI.api;

import com.xigua.baseAPI.BaseAPI;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TextBoard {
    public static final String EVENT_NAME = "textBoard";
    private static final Set<TextBoard> REGISTRY = ConcurrentHashMap.newKeySet();

    private final String boardId;
    private String text;
    private BindType bindType;
    private Integer bindEntityId;
    private Vec3 pos;
    private Vec3 offset;
    private Rotation rot;
    private Scale scale;
    private Color textColor;
    private Color boardColor;
    private boolean faceCamera;
    private boolean depthTest;

    private final Set<String> dirtyFields = new LinkedHashSet<>();
    private final Set<UUID> viewers = ConcurrentHashMap.newKeySet();

    private TextBoard(Builder builder) {
        this.boardId = builder.boardId;
        this.text = builder.text;
        this.bindType = builder.bindType;
        this.bindEntityId = builder.bindEntityId;
        this.pos = builder.pos;
        this.offset = builder.offset;
        this.rot = builder.rot;
        this.scale = builder.scale;
        this.textColor = builder.textColor;
        this.boardColor = builder.boardColor;
        this.faceCamera = builder.faceCamera;
        this.depthTest = builder.depthTest;
        REGISTRY.add(this);
    }

    public static Builder create(String text) {
        return new Builder(text);
    }

    public static boolean init(Player player) {
        Objects.requireNonNull(player, "player");
        return initInternal(player.getUniqueId(), player);
    }

    public static boolean init(Collection<? extends Player> players) {
        Objects.requireNonNull(players, "players");
        boolean success = true;
        for (Player player : players) {
            if (player == null) {
                continue;
            }
            success &= initInternal(player.getUniqueId(), player);
        }
        return success;
    }

    private static boolean initInternal(UUID uuid, Player player) {
        HashMapBuilder data = new HashMapBuilder()
                .put("action", "init");
        boolean success = plugin().notifyToClient(player, EVENT_NAME, data.build());
        for (TextBoard board : REGISTRY) {
            board.viewers.remove(uuid);
        }
        return success;
    }

    public String getBoardId() {
        return boardId;
    }

    public String getText() {
        return text;
    }

    public BindType getBindType() {
        return bindType;
    }

    public TextBoard setText(String text) {
        this.text = Objects.requireNonNull(text, "text");
        return markDirty("text");
    }

    public TextBoard setPos(double x, double y, double z) {
        requireWorldBinding("setPos");
        this.pos = new Vec3(x, y, z);
        return markDirty("pos");
    }

    public TextBoard setOffset(double x, double y, double z) {
        requireEntityBinding("setOffset");
        this.offset = new Vec3(x, y, z);
        return markDirty("offset");
    }

    public TextBoard setRot(float x, float y, float z) {
        this.rot = new Rotation(x, y, z);
        return markDirty("rot");
    }

    public TextBoard setScale(double x, double y) {
        this.scale = new Scale(x, y);
        return markDirty("scale");
    }

    public TextBoard setTextColor(int r, int g, int b, int a) {
        this.textColor = new Color(r, g, b, a);
        return markDirty("textColor");
    }

    public TextBoard setBoardColor(int r, int g, int b, int a) {
        this.boardColor = new Color(r, g, b, a);
        return markDirty("boardColor");
    }

    public TextBoard setFaceCamera(boolean faceCamera) {
        this.faceCamera = faceCamera;
        return markDirty("faceCamera");
    }

    public TextBoard setDepthTest(boolean depthTest) {
        this.depthTest = depthTest;
        return markDirty("depthTest");
    }

    public boolean send(Player player) {
        Objects.requireNonNull(player, "player");
        UUID uuid = player.getUniqueId();
        boolean knownViewer = viewers.contains(uuid);
        Map<String, Object> payload = knownViewer ? buildUpdatePayload() : buildCreatePayload();
        if (payload == null) {
            return true;
        }

        boolean success = plugin().notifyToClient(player, EVENT_NAME, payload);
        if (success) {
            viewers.add(uuid);
            if (knownViewer) {
                dirtyFields.clear();
            }
        }
        return success;
    }

    public boolean send(Collection<? extends Player> players) {
        Objects.requireNonNull(players, "players");
        boolean success = true;
        boolean updatedKnownViewer = false;
        for (Player player : players) {
            if (player == null) {
                continue;
            }
            UUID uuid = player.getUniqueId();
            boolean knownViewer = viewers.contains(uuid);
            Map<String, Object> payload = knownViewer ? buildUpdatePayload() : buildCreatePayload();
            if (payload == null) {
                continue;
            }
            boolean sent = plugin().notifyToClient(player, EVENT_NAME, payload);
            success &= sent;
            if (sent) {
                viewers.add(uuid);
            }
            updatedKnownViewer |= knownViewer && sent;
        }

        if (success && updatedKnownViewer) {
            dirtyFields.clear();
        }
        return success;
    }

    public boolean delete(Player player) {
        Objects.requireNonNull(player, "player");
        HashMapBuilder data = new HashMapBuilder()
                .put("action", "delete")
                .put("boardId", boardId);
        boolean success = plugin().notifyToClient(player, EVENT_NAME, data.build());
        if (success) {
            viewers.remove(player.getUniqueId());
        }
        return success;
    }

    public boolean delete(Collection<? extends Player> players) {
        Objects.requireNonNull(players, "players");
        boolean success = true;
        for (Player player : players) {
            if (player == null) {
                continue;
            }
            success &= delete(player);
        }
        return success;
    }

    private Map<String, Object> buildCreatePayload() {
        HashMapBuilder data = new HashMapBuilder()
                .put("action", "create")
                .put("boardId", boardId)
                .put("text", text)
                .put("bindType", bindType.name())
                .put("textColor", textColor.toMap())
                .put("boardColor", boardColor.toMap())
                .put("faceCamera", faceCamera)
                .put("depthTest", depthTest)
                .put("rot", rot.toMap())
                .put("scale", scale.toMap());

        if (bindType == BindType.WORLD) {
            data.put("pos", pos.toMap());
        } else {
            data.put("bindEntityId", bindEntityId)
                    .put("offset", offset.toMap());
        }
        return data.build();
    }

    private Map<String, Object> buildUpdatePayload() {
        if (dirtyFields.isEmpty()) {
            return null;
        }

        HashMapBuilder data = new HashMapBuilder()
                .put("action", "update")
                .put("boardId", boardId);

        if (dirtyFields.contains("text")) {
            data.put("text", text);
        }
        if (dirtyFields.contains("textColor")) {
            data.put("textColor", textColor.toMap());
        }
        if (dirtyFields.contains("boardColor")) {
            data.put("boardColor", boardColor.toMap());
        }
        if (dirtyFields.contains("faceCamera")) {
            data.put("faceCamera", faceCamera);
        }
        if (dirtyFields.contains("depthTest")) {
            data.put("depthTest", depthTest);
        }
        if (dirtyFields.contains("pos")) {
            data.put("pos", pos.toMap());
        }
        if (dirtyFields.contains("scale")) {
            data.put("scale", scale.toMap());
        }

        if (dirtyFields.contains("rot")) {
            data.put("rot", rot.toMap());
        }

        if (bindType == BindType.ENTITY && (dirtyFields.contains("offset") || dirtyFields.contains("rot"))) {
            data.put("offset", offset.toMap());
            data.put("rot", rot.toMap());
        }

        return data.build();
    }

    private TextBoard markDirty(String field) {
        dirtyFields.add(field);
        return this;
    }

    private void requireWorldBinding(String operation) {
        if (bindType != BindType.WORLD) {
            throw new IllegalStateException(operation + " 仅支持 WORLD 类型文字面板");
        }
    }

    private void requireEntityBinding(String operation) {
        if (bindType != BindType.ENTITY) {
            throw new IllegalStateException(operation + " 仅支持 ENTITY 类型文字面板");
        }
    }

    private static BaseAPI plugin() {
        return JavaPlugin.getPlugin(BaseAPI.class);
    }

    public enum BindType {
        WORLD,
        ENTITY
    }

    public static class Builder {
        private final String boardId = UUID.randomUUID().toString();
        private final String text;
        private BindType bindType = BindType.WORLD;
        private Integer bindEntityId = null;
        private Vec3 pos = new Vec3(0.0D, 0.0D, 0.0D);
        private Vec3 offset = new Vec3(0.0D, 0.0D, 0.0D);
        private Rotation rot = new Rotation(0.0F, 0.0F, 0.0F);
        private Scale scale = new Scale(1.0D, 1.0D);
        private Color textColor = new Color(255, 255, 255, 255);
        private Color boardColor = new Color(0, 0, 0, 100);
        private boolean faceCamera = true;
        private boolean depthTest = true;

        private Builder(String text) {
            this.text = Objects.requireNonNull(text, "text");
        }

        public Builder bindToWorld(double x, double y, double z) {
            this.bindType = BindType.WORLD;
            this.bindEntityId = null;
            this.pos = new Vec3(x, y, z);
            return this;
        }

        public Builder bindToEntity(Entity entity, double offsetX, double offsetY, double offsetZ) {
            Objects.requireNonNull(entity, "entity");
            return bindToEntity(entity.getEntityId(), offsetX, offsetY, offsetZ);
        }

        public Builder bindToEntity(int entityId, double offsetX, double offsetY, double offsetZ) {
            this.bindType = BindType.ENTITY;
            this.bindEntityId = entityId;
            this.offset = new Vec3(offsetX, offsetY, offsetZ);
            return this;
        }

        public Builder textColor(int r, int g, int b, int a) {
            this.textColor = new Color(r, g, b, a);
            return this;
        }

        public Builder boardColor(int r, int g, int b, int a) {
            this.boardColor = new Color(r, g, b, a);
            return this;
        }

        public Builder faceCamera(boolean faceCamera) {
            this.faceCamera = faceCamera;
            return this;
        }

        public Builder depthTest(boolean depthTest) {
            this.depthTest = depthTest;
            return this;
        }

        public Builder rot(float x, float y, float z) {
            this.rot = new Rotation(x, y, z);
            return this;
        }

        public Builder scale(double x, double y) {
            this.scale = new Scale(x, y);
            return this;
        }

        public TextBoard build() {
            if (bindType == BindType.ENTITY && bindEntityId == null) {
                throw new IllegalStateException("ENTITY 类型文字面板必须提供 bindEntityId");
            }
            return new TextBoard(this);
        }

        public TextBoard send(Player player) {
            TextBoard board = build();
            board.send(player);
            return board;
        }

        public TextBoard send(Collection<? extends Player> players) {
            TextBoard board = build();
            board.send(players);
            return board;
        }
    }

    private record Vec3(double x, double y, double z) {
        private Map<String, Object> toMap() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("x", x);
            data.put("y", y);
            data.put("z", z);
            return data;
        }
    }

    private record Rotation(float x, float y, float z) {
        private Map<String, Object> toMap() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("x", x);
            data.put("y", y);
            data.put("z", z);
            return data;
        }
    }

    private record Scale(double x, double y) {
        private Map<String, Object> toMap() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("x", x);
            data.put("y", y);
            return data;
        }
    }

    private record Color(int r, int g, int b, int a) {
        private Color {
            r = clamp(r);
            g = clamp(g);
            b = clamp(b);
            a = clamp(a);
        }

        private Map<String, Object> toMap() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("r", r);
            data.put("g", g);
            data.put("b", b);
            data.put("a", a);
            return data;
        }

        private static int clamp(int value) {
            return Math.max(0, Math.min(255, value));
        }
    }

    private static final class HashMapBuilder {
        private final Map<String, Object> data = new LinkedHashMap<>();

        private HashMapBuilder put(String key, Object value) {
            data.put(key, value);
            return this;
        }

        private Map<String, Object> build() {
            return data;
        }
    }
}
