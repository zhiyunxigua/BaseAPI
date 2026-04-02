package com.xigua.baseAPI.api;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Singular;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.Map;

@Getter
public class TopBar {
    private final Map<String, Item> items;

    private TopBar(Builder builder) {
        this.items = builder.items;
    }

    public static Builder builder() {
        return new Builder();
    }

    public HashMap<String, Object> toHashMap() {
        HashMap<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Item> entry : items.entrySet()) {
            result.put(entry.getKey(), entry.getValue().toMap());
        }
        return result;
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
        @Setter
        private String text;
        @Setter
        private float[] color;
        @Setter
        private String background;

        TextItem(String text, float[] color, String background) {
            this.text = text;
            this.color = color;
            this.background = background;
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
        @Setter
        private String texture;
        @Setter
        private float[] imageColor;
        @Setter
        private String text;
        @Setter
        private float[] textColor;
        @Setter
        private String background;

        ImageItem(String texture, float[] imageColor, String text, float[] textColor, String background) {
            this.texture = texture;
            this.imageColor = imageColor;
            this.text = text;
            this.textColor = textColor;
            this.background = background;
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