package com.norcode.bukkit.playerid;

import com.norcode.bukkit.playerid.command.PlayerIDCommand;
import com.norcode.bukkit.playerid.datastore.Datastore;
import com.norcode.bukkit.playerid.datastore.DatastoreException;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public class PlayerID extends JavaPlugin implements Listener {

	FileConfiguration data;
	private HashMap<UUID, String> playerMap = new HashMap<UUID, String>();
	private static PlayerID instance;
	private Datastore datastore;
	private PlayerIDCommand command;

	@Override
	public void onEnable() {
		this.command = new PlayerIDCommand(this);
		instance = this;
		saveDefaultConfig();
		try {
			datastore = Datastore.create(this, getConfig().getString("datastore.type", null));
			datastore.enable();
		} catch (DatastoreException e) {
			getLogger().severe("Failed to initialize datastore, shutting down.");
			e.printStackTrace();
			getServer().getPluginManager().disablePlugin(this);
		}
	}

	@Override
	public void onDisable() {
		instance = null;
		if (datastore != null) {
			datastore.disable();
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		UUID uuid = event.getPlayer().getUniqueId();
		playerMap.put(uuid, event.getPlayer().getName());
		getConfig().set(uuid.toString(), event.getPlayer().getName());
	}

	/**
	 * Get a player's name given their UUID
	 *
	 * @param uuid player's UUID
	 * @return Player's name, or null if the player is unknown.
	 */
	public static String getPlayerName(UUID uuid) {
		return instance.playerMap.get(uuid);
	}

	/**
	 * Get an OfflinePlayer for the given UUID
	 * @param uuid player's UUID
	 * @return an OfflinePlayer or null if the player is unknown
	 */
	public static OfflinePlayer getOfflinePlayer(UUID uuid) {
		return instance.getServer().getOfflinePlayer(getPlayerName(uuid));
	}

	/**
	 * Get an ONLINE Player by UUID
	 * @param uuid
	 * @return
	 */
	public static Player getPlayer(UUID uuid) {
		return instance.getServer().getPlayerExact(getPlayerName(uuid));
	}

	public static ConfigurationSection getPlayerData(String plugin, Player player) {
		return instance.datastore.getPlayerData(plugin, player.getUniqueId());
	}

	public static void savePlayerData(String plugin, Player player, ConfigurationSection cfg) {
		instance.datastore.savePlayerData(plugin, player.getUniqueId(), cfg);
	}

	public Datastore getDatastore() {
		return datastore;
	}
}
