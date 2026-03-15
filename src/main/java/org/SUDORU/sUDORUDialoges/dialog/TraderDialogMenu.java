package org.SUDORU.sUDORUDialoges.dialog;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.DialogInstancesProvider;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.body.ItemDialogBody;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.SUDORU.sUDORUDialoges.SUDORUDialoges;
import org.SUDORU.sUDORUDialoges.shop.ShopItem;
import org.SUDORU.sUDORUDialoges.shop.TraderShop;
import org.SUDORU.sUDORUDialoges.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@SuppressWarnings("UnstableApiUsage")
public class TraderDialogMenu {
    private static final int MULTI_QTY = 5;
    private final SUDORUDialoges plugin;
    public TraderDialogMenu(SUDORUDialoges plugin) {
        this.plugin = plugin;
    }
    public void open(Player player, TraderShop shop) {
        var cfg = shop.getConfig();
        var prov = DialogInstancesProvider.instance();
        ClickCallback.Options opts = makeOpts();
        List<DialogBody> bodyList = new ArrayList<>();
        String desc = cfg.getDescription();
        if (desc != null && !desc.isEmpty()) {
            bodyList.add(prov.plainMessageDialogBody(
                    ColorUtil.parse(desc.replace("\\n", "\n")), 280));
        }
        List<ActionButton> buttons = new ArrayList<>();
        Map<Integer, TraderShop.SlotData> slots = shop.getActiveSlots();
        for (int i = 0; i < shop.getProductSlotsCount(); i++) {
            TraderShop.SlotData data = slots.get(i);
            if (data == null) continue;
            final int idx = i;
            if (data.isBought()) {
                String rawName = stripColors(data.getItem().getName());
                buttons.add(ActionButton.builder(
                        Component.empty().decoration(TextDecoration.ITALIC, false)
                                .append(Component.text("#" + (i+1) + " ", NamedTextColor.DARK_GRAY))
                                .append(Component.text(rawName + " -- PRODANO", NamedTextColor.DARK_RED)))
                        .action(prov.register((v, a) -> {}, opts)).width(280).build());
            } else {
                ShopItem si = data.getItem();
                Component label = Component.empty().decoration(TextDecoration.ITALIC, false)
                        .append(Component.text("#" + (i+1) + " ", NamedTextColor.AQUA))
                        .append(ColorUtil.parse(si.getName()))
                        .append(Component.text(" -- ", NamedTextColor.GRAY))
                        .append(Component.text(data.getPrice() + " " + plugin.getCurrencyName(), NamedTextColor.GOLD));
                buttons.add(ActionButton.builder(label)
                        .tooltip(Component.text("Nazhmi dlya otkrytiya kartochki", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false))
                        .action(prov.register((v, a) -> {
                            if (!(a instanceof Player p)) return;
                            plugin.getServer().getScheduler().runTask(plugin, () -> openItemCard(p, shop, idx));
                        }, opts)).width(280).build());
            }
        }
        ActionButton exitBtn = ActionButton.builder(
                Component.text("Vyhod", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false))
                .action(prov.register((v, a) -> {
                    if (!(a instanceof Player p)) return;
                    plugin.getServer().getScheduler().runTask(plugin, () -> p.closeInventory());
                }, opts)).width(150).build();
        DialogBase base = prov.dialogBaseBuilder(ColorUtil.parse(cfg.getDisplayName()))
                .externalTitle(ColorUtil.parse(cfg.getDisplayName()))
                .body(bodyList).canCloseWithEscape(true).pause(false)
                .afterAction(DialogBase.DialogAfterAction.NONE).build();
        player.showDialog(Dialog.create(f -> f.empty().base(base)
                .type(prov.multiAction(buttons).exitAction(exitBtn).columns(1).build())));
    }
    private void openItemCard(Player player, TraderShop shop, int slotIndex) {
        TraderShop.SlotData data = shop.getActiveSlots().get(slotIndex);
        if (data == null) { open(player, shop); return; }
        var prov = DialogInstancesProvider.instance();
        ClickCallback.Options opts = makeOpts();
        ShopItem si = data.getItem();
        int price = data.getPrice();
        ItemDialogBody itemBody = prov.itemDialogBodyBuilder(buildDisplayStack(si))
                .description(prov.plainMessageDialogBody(buildLoreComponent(si, price), 200))
                .showDecorations(true).showTooltip(false).width(310).height(64).build();
        ActionButton buy1Btn;
        ActionButton buyNBtn;
        if (data.isBought()) {
            buy1Btn = buyNBtn = ActionButton.builder(
                    Component.text("Uzhe kupleno", NamedTextColor.DARK_RED).decoration(TextDecoration.ITALIC, false))
                    .action(prov.register((v, a) -> {}, opts)).width(130).build();
        } else {
            buy1Btn = ActionButton.builder(
                    Component.text("> Kupit x1", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false))
                    .tooltip(Component.empty().decoration(TextDecoration.ITALIC, false)
                            .append(Component.text("Kupit " + si.getAmount() + " sht.\n", NamedTextColor.GRAY))
                            .append(Component.text("Tsena: " + price + " " + plugin.getCurrencyName(), NamedTextColor.GOLD)))
                    .action(prov.register((v, a) -> {
                        if (!(a instanceof Player p)) return;
                        plugin.getServer().getScheduler().runTask(plugin, () -> {
                            shop.tryPurchase(p, slotIndex, 1);
                            openItemCard(p, shop, slotIndex);
                        });
                    }, opts)).width(130).build();
            buyNBtn = ActionButton.builder(
                    Component.text("> Kupit x" + MULTI_QTY, NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false))
                    .tooltip(Component.empty().decoration(TextDecoration.ITALIC, false)
                            .append(Component.text("Kupit " + (si.getAmount()*MULTI_QTY) + " sht.\n", NamedTextColor.GRAY))
                            .append(Component.text("Tsena: " + (price*MULTI_QTY) + " " + plugin.getCurrencyName(), NamedTextColor.GOLD)))
                    .action(prov.register((v, a) -> {
                        if (!(a instanceof Player p)) return;
                        plugin.getServer().getScheduler().runTask(plugin, () -> {
                            shop.tryPurchase(p, slotIndex, MULTI_QTY);
                            openItemCard(p, shop, slotIndex);
                        });
                    }, opts)).width(130).build();
        }
        ActionButton backBtn = ActionButton.builder(
                Component.text("<- Nazad", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false))
                .action(prov.register((v, a) -> {
                    if (!(a instanceof Player p)) return;
                    plugin.getServer().getScheduler().runTask(plugin, () -> open(p, shop));
                }, opts)).width(200).build();
        Component cardTitle = ColorUtil.parse(si.getName());
        DialogBase base = prov.dialogBaseBuilder(cardTitle).externalTitle(cardTitle)
                .body(List.of(itemBody)).canCloseWithEscape(true).pause(false)
                .afterAction(DialogBase.DialogAfterAction.NONE).build();
        player.showDialog(Dialog.create(f -> f.empty().base(base)
                .type(prov.multiAction(List.of(buy1Btn, buyNBtn)).exitAction(backBtn).columns(2).build())));
    }
    private ClickCallback.Options makeOpts() {
        return ClickCallback.Options.builder()
                .uses(ClickCallback.UNLIMITED_USES)
                .lifetime(Duration.ofMinutes(10)).build();
    }
    private ItemStack buildDisplayStack(ShopItem si) {
        Material mat = si.getMaterial() != null ? si.getMaterial() : Material.CHEST;
        ItemStack stack = new ItemStack(mat, si.getAmount());
        if (si.getPotionType() != null && !si.getPotionType().isEmpty()) {
            if (stack.getItemMeta() instanceof PotionMeta pm) {
                try { pm.setBasePotionType(PotionType.valueOf(si.getPotionType().toUpperCase())); stack.setItemMeta(pm); }
                catch (IllegalArgumentException ignored) {}
            }
        }
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.displayName(ColorUtil.parse(si.getName()).decoration(TextDecoration.ITALIC, false));
            List<Component> loreComps = new ArrayList<>();
            for (String line : si.getLore()) loreComps.add(ColorUtil.parse(line).decoration(TextDecoration.ITALIC, false));
            meta.lore(loreComps);
            stack.setItemMeta(meta);
        }
        return stack;
    }
    private Component buildLoreComponent(ShopItem si, int price) {
        var sb = Component.text().decoration(TextDecoration.ITALIC, false);
        for (String line : si.getLore()) sb.append(ColorUtil.parse(line)).append(Component.newline());
        if (!si.getLore().isEmpty()) sb.append(Component.newline());
        sb.append(Component.text("Tsena: ", NamedTextColor.GRAY))
                .append(Component.text(price + " " + plugin.getCurrencyName(), NamedTextColor.GOLD));
        if (si.getAmount() > 1) sb.append(Component.newline())
                .append(Component.text("Kolichestvo: " + si.getAmount() + " sht.", NamedTextColor.GRAY));
        return sb.build();
    }
    private String stripColors(String name) {
        if (name == null) return "";
        return name.replaceAll("&#[0-9A-Fa-f]{6}", "")
                .replaceAll("&[0-9a-fk-orA-FK-OR]", "")
                .replaceAll("§[0-9a-fk-orA-FK-OR]", "").trim();
    }
}