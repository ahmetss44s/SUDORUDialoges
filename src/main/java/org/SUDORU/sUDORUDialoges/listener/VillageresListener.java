package org.SUDORU.sUDORUDialoges.listener;

import org.SUDORU.sUDORUDialoges.SUDORUDialoges;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.persistence.PersistentDataType;

public class VillageresListener implements Listener {
    private final SUDORUDialoges plugin;

    public VillageresListener(SUDORUDialoges plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Villager villager)) return;

        String traderId = villager.getPersistentDataContainer()
                .get(plugin.getTraderNpcKey(), PersistentDataType.STRING);
        if (traderId == null || traderId.isBlank()) return;

        event.setCancelled(true);
        Player player = event.getPlayer();
        player.performCommand("shopbridge open " + traderId.toLowerCase());
    }
}
