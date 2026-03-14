package org.SUDORU.sUDORUDialoges.shop;

import org.bukkit.Material;

import java.util.List;

/**
 * Описывает один возможный товар в ассортименте торговца.
 */
public class ShopItem {

    private final Material material;
    private final String name;
    private final List<String> lore;
    private final int basePrice;
    private final int priceRange;
    private final double chance;
    private final int amount;
    private final String potionType; // null если не зелье
    private final List<String> enchantments;

    public ShopItem(Material material, String name, List<String> lore,
                    int basePrice, int priceRange, double chance,
                    int amount, String potionType, List<String> enchantments) {
        this.material = material;
        this.name = name;
        this.lore = lore;
        this.basePrice = basePrice;
        this.priceRange = priceRange;
        this.chance = chance;
        this.amount = amount;
        this.potionType = potionType;
        this.enchantments = enchantments;
    }

    /** Рассчитывает итоговую цену с учётом диапазона */
    public int calculatePrice() {
        if (priceRange <= 0) return basePrice;
        int offset = (int) (Math.random() * (priceRange * 2 + 1)) - priceRange;
        return Math.max(1, basePrice + offset);
    }

    public Material getMaterial() { return material; }
    public String getName() { return name; }
    public List<String> getLore() { return lore; }
    public int getBasePrice() { return basePrice; }
    public int getPriceRange() { return priceRange; }
    public double getChance() { return chance; }
    public int getAmount() { return amount; }
    public String getPotionType() { return potionType; }
    public List<String> getEnchantments() { return enchantments; }
}

