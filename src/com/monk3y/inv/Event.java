package com.monk3y.inv;

import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;

public class Event implements org.bukkit.event.Listener {
	private Core plugin;

	public Event(Core plugin) {
		this.plugin = plugin;
	}

	@org.bukkit.event.EventHandler(priority = EventPriority.HIGH)
	public void onPlayerDeath(PlayerDeathEvent e) {
		Player p = e.getEntity().getPlayer();
		if (this.plugin.getConfig().getBoolean("DeathMessage")) {
			for (Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
				if ((player.hasPermission("ir.msg"))
						&& (this.plugin.getPlayers().getBoolean(player.getUniqueId().toString() + ".Alerts"))) {
					player.sendMessage("Do /invrestore");
				}
			}
		}

		this.plugin.getPlayers().set(p.getUniqueId().toString() + ".Inv", p.getInventory().getContents());
		this.plugin.getPlayers().set(p.getUniqueId().toString() + ".Armor", p.getInventory().getArmorContents());
		this.plugin.savePlayers();
	}
}