package com.norcode.bukkit.playerid;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class PlayerID extends JavaPlugin implements Listener {

	FileConfiguration data;
	private HashMap<UUID, String> playerMap = new HashMap<UUID, String>();
	private static PlayerID instance;
	private Datastore datastore;


	@Override
	public void onEnable() {
		instance = this;
		if (!getDataFolder().exists()) {
			getDataFolder().mkdirs();
		}
		File dataFile = new File(getDataFolder(), "data.yml");
		if (!dataFile.exists()) {
			try {
				dataFile.createNewFile();
			} catch (IOException e) {
				getLogger().severe("Could not write data file: " + dataFile);
				getServer().getPluginManager().disablePlugin(this);
			}
		}
		data = YamlConfiguration.loadConfiguration(dataFile);
		for (String key: data.getKeys(false)) {
			playerMap.put(UUID.fromString(key), data.getString(key));
		}
	}

	@Override
	public void onDisable() {
		instance = null;
		if (data != null) {
			File dataFile = dataFile = new File(getDataFolder(), "data.yml");
			try {
				data.save(dataFile);
			} catch (IOException e) {
				getLogger().severe("Could not write data file: " + dataFile);
			}
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

	public Datastore getDatastore() {
		return datastore;
	}
}
