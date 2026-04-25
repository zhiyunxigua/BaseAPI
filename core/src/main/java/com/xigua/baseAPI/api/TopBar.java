package com.xigua.baseAPI.api;

import lombok.Getter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class TopBar {
    private final Map<String, Item> items;
    private final Map<String, Object> pendingUpdates = new LinkedHashMap<>();

    private TopBar(Builder builder) {
        this.items = builder.items;
    }

    public static Builder builder() {
        return new Builder();
    }

    public HashMap<String, Object> toHashMap() {
        HashMap<String, Object> result = new HashMap<>();
        int index = 0;
        for (Map.Entry<String, Item> entry : items.entrySet()) {
            Map<String, Object> itemMap = entry.getValue().toMap();
            itemMap.put("index", index++);
            result.put(entry.getKey(), itemMap);
        }
        return result;
    }

    // 获取所有item的变更并清空
    public HashMap<String, Object> flushAllChanges() {
        HashMap<String, Object> itemsMap = new LinkedHashMap<>();

        for (Map.Entry<String, Item> entry : items.entrySet()) {
            String itemId = entry.getKey();
            Item item = entry.getValue();

            Map<String, Object> changes = null;

            if (item instanceof TextItem) {
                changes = ((TextItem) item).flushChanges();
            } else if (item instanceof ImageItem) {
                changes = ((ImageItem) item).flushChanges();
            }

            if (changes != null && !changes.isEmpty()) {
                itemsMap.put(itemId, changes);
            }
        }

        if (!itemsMap.isEmpty()) {
            return itemsMap;
        }

        return null; // 没有变更
    }

    // 获取指定ID的Item
    public Item getItem(String id) {
        return items.get(id);
    }

    // 获取文本项
    public TextItem getTextItem(String id) {
        Item item = items.get(id);
        if (item instanceof TextItem) {
            return (TextItem) item;
        }
        return null;
    }

    // 获取图片项
    public ImageItem getImageItem(String id) {
        Item item = items.get(id);
        if (item instanceof ImageItem) {
            return (ImageItem) item;
        }
        return null;
    }

    public static class Builder {
        private final Map<String, Item> items = new HashMap<>();

        public Builder label(String id, String text, float[] color, String background) {
            items.put(id, new TextItem(text, color, background));
            return this;
        }

        public Builder label(String id, String text, float[] color) {
            return this.label(id, text, color, null);
        }

        public Builder label(String id, String text) {
            return this.label(id, text, null, null);
        }

        public Builder image(String id, String texture, float[] imageColor, String text, float[] textColor, String background) {
            items.put(id, new ImageItem(texture, imageColor, text, textColor, background));
            return this;
        }

        public Builder image(String id, String texture, float[] imageColor, String text, float[] textColor) {
            return this.image(id, texture, imageColor, text, textColor, null);
        }

        public Builder image(String id, String texture, String text) {
            return this.image(id, texture, null, text, null, null);
        }

        public Builder image(String id, String texture) {
            return this.image(id, texture, null, null, null, null);
        }

        public TopBar build() {
            return new TopBar(this);
        }
    }

    public interface Item {
        Map<String, Object> toMap();
        String getType();
    }

    @Getter
    public static class TextItem implements Item {
        private final String type = "text";
        private String text;
        private float[] color;
        private String background;
        private final Map<String, Object> cachedChanges = new LinkedHashMap<>();

        TextItem(String text, float[] color, String background) {
            this.text = text;
            this.color = color;
            this.background = background;
        }

        public void setText(String text) {
            this.text = text;
            cachedChanges.put("text", text);
        }

        public void setColor(float[] color) {
            this.color = color;
            cachedChanges.put("color", color);
        }

        public void setBackground(String background) {
            this.background = background;
            cachedChanges.put("background", background);
        }

        // 获取并清空缓存
        public Map<String, Object> flushChanges() {
            Map<String, Object> changes = new LinkedHashMap<>(cachedChanges);
            cachedChanges.clear();
            return changes;
        }

        @Override
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("type", type);

            if (background != null) {
                map.put("background", background);
            }

            if (color != null) {
                map.put("color", color);
            }

            map.put("text", text);

            return map;
        }
    }

    @Getter
    public static class ImageItem implements Item {
        private final String type = "image";
        private String texture;
        private float[] imageColor;
        private String text;
        private float[] textColor;
        private String background;
        private final Map<String, Object> cachedChanges = new LinkedHashMap<>();

        ImageItem(String texture, float[] imageColor, String text, float[] textColor, String background) {
            this.texture = texture;
            this.imageColor = imageColor;
            this.text = text;
            this.textColor = textColor;
            this.background = background;
        }

        public void setTexture(String texture) {
            this.texture = texture;
            cachedChanges.put("texture", texture);
        }

        public void setImageColor(float[] imageColor) {
            this.imageColor = imageColor;
            cachedChanges.put("image_color", imageColor);
        }

        public void setText(String text) {
            this.text = text;
            cachedChanges.put("text", text);
        }

        public void setTextColor(float[] textColor) {
            this.textColor = textColor;
            cachedChanges.put("text_color", textColor);
        }

        public void setBackground(String background) {
            this.background = background;
            cachedChanges.put("background", background);
        }

        // 获取并清空缓存
        public Map<String, Object> flushChanges() {
            Map<String, Object> changes = new LinkedHashMap<>(cachedChanges);
            cachedChanges.clear();
            return changes;
        }

        @Override
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("type", type);

            if (background != null) {
                map.put("background", background);
            }

            if (text != null) {
                map.put("text", text);
            }

            if (textColor != null) {
                map.put("text_color", textColor);
            }

            if (imageColor != null) {
                map.put("image_color", imageColor);
            }

            map.put("texture", texture);

            return map;
        }
    }
}