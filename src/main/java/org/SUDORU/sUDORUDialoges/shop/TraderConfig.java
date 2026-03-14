package org.SUDORU.sUDORUDialoges.shop;

import java.util.List;

/**
 * Конфигурация одного торговца, загруженная из config.yml.
 */
public class TraderConfig {

    private final String id;
    private final String displayName;
    private final String description;
    private final String iconMaterial;
    private final long refreshSeconds;
    private final int minItems;
    private final int maxItems;
    private final List<ShopItem> items;

    public TraderConfig(String id, String displayName, String description,
                        String iconMaterial, long refreshSeconds,
                        int minItems, int maxItems, List<ShopItem> items) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.iconMaterial = iconMaterial;
        this.refreshSeconds = refreshSeconds;
        this.minItems = Math.max(5, Math.min(8, minItems));
        this.maxItems = Math.max(this.minItems, Math.min(8, maxItems));
        this.items = items;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public String getIconMaterial() { return iconMaterial; }
    public long getRefreshSeconds() { return refreshSeconds; }
    public int getMinItems() { return minItems; }
    public int getMaxItems() { return maxItems; }
    public List<ShopItem> getItems() { return items; }
}

