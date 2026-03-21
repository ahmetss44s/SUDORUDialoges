package org.SUDORU.sUDORUDialoges.command;

import org.SUDORU.sUDORUDialoges.SUDORUDialoges;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class VillageresCommand implements CommandExecutor {
    private final SUDORUDialoges plugin;

    public VillageresCommand(SUDORUDialoges plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cКоманду может использовать только игрок.");
            return true;
        }
        if (!player.hasPermission("sudoru.villageres.admin")) {
            player.sendMessage("§c✗ Нет прав для создания NPC.");
            return true;
        }
        if (args.length < 2 || !"create".equalsIgnoreCase(args[0])) {
            player.sendMessage("§7Использование: §f/villageres create <traderId> [name...]");
            return true;
        }

        String traderId = args[1].toLowerCase();
        if (plugin.getTraderManager().getShop(traderId) == null) {
            player.sendMessage("§c✗ traderId '" + traderId + "' не найден в конфиге плагина.");
            return true;
        }

        Villager villager = (Villager) player.getWorld().spawnEntity(player.getLocation(), EntityType.VILLAGER);
        villager.setAI(false);
        villager.setInvulnerable(true);
        villager.setCustomNameVisible(true);
        villager.addScoreboardTag("Villager.Buy");
        villager.addScoreboardTag("SUDORU.Villageres");
        villager.getPersistentDataContainer().set(plugin.getTraderNpcKey(), PersistentDataType.STRING, traderId);

        if (args.length > 2) {
            String name = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
            villager.setCustomName(name.replace("&", "§"));
        } else {
            villager.setCustomName("§6Villageres §7[" + traderId + "]");
        }

        player.sendMessage("§a✔ Villageres создан и привязан к traderId: §f" + traderId);
        return true;
    }
}
