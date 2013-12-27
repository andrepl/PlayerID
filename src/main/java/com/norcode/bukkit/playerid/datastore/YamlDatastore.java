package com.norcode.bukkit.playerid.datastore;

import com.norcode.bukkit.playerid.PlayerID;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class YamlDatastore extends Datastore {

	private File playerIdFile;
	private FileConfiguration playerIdCfg;

	private File pluginDataDir;

	private HashMap<String, YamlConfiguration> pluginData = new HashMap<String, YamlConfiguration>();

	public YamlDatastore(PlayerID plugin) {
		super(plugin);
	}

	@Override
	protected void onEnable() throws DatastoreException {
		// create the data folder
		playerIdFile = new File(plugin.getDataFolder(), "data");
		if (!playerIdFile.isDirectory()) {
		 	playerIdFile.mkdirs();
		}
		// create the player-ids file.
		playerIdFile = new File(playerIdFile, "player-ids.yml");
		if (!playerIdFile.exists()) {
			try {
				playerIdFile.createNewFile();
			} catch (IOException e) {
				throw new DatastoreException(e);
			}
		}
		// create the player-data dir
		pluginDataDir = new File(plugin.getDataFolder(), "data");
		pluginDataDir = new File(pluginDataDir, "plugin-data");

	}

	@Override
	protected Map<UUID, String> loadPlayerIds() {
		HashMap<UUID, String> players = new HashMap<UUID, String>();
		playerIdCfg = YamlConfiguration.loadConfiguration(playerIdFile);
		for (String key: playerIdCfg.getKeys(true)) {
			players.put(UUID.fromString(key), playerIdCfg.getString(key));
		}
		return players;
	}

	@Override
	protected void savePlayerId(UUID id, String name) {
		playerIdCfg.set(id.toString(), name);
	}

	@Override
	protected void savePlayerIds(Map<UUID, String> ids) {
		for (Map.Entry<UUID, String> entry: ids.entrySet()) {
			playerIdCfg.set(entry.getKey().toString(), entry.getValue());
		}
	}

	@Override
	protected void deletePlayerId(UUID id) {
		playerIdCfg.set(id.toString(), null);
	}

	@Override
	protected void deletePlayerIds(Set<UUID> ids) {
		for (UUID id: ids) {
			deletePlayerId(id);
		}
	}

	@Override
	protected void onDisable() {
		if (playerIdCfg != null) {
			try {
				playerIdCfg.save(playerIdFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		for (String plugin: pluginData.keySet()) {
			try {
				pluginData.get(plugin).save(new File(pluginDataDir, plugin + ".yml"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	private void initializePluginData(String plugin) {
		File pluginFile = new File(pluginDataDir, plugin + ".yml");
		if (!pluginFile.exists()) {
			try {
				pluginFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		pluginData.put(plugin, YamlConfiguration.loadConfiguration(pluginFile));
	}

	@Override
	public ConfigurationSection getPlayerData(String plugin, UUID playerId) {
		if (!pluginData.containsKey(plugin)) {
			initializePluginData(plugin);
		}
		YamlConfiguration cfg = pluginData.get(plugin);
		ConfigurationSection sec = cfg.getConfigurationSection(playerId.toString());
		if (sec == null) {
			sec = cfg.createSection(playerId.toString());
		}
		return sec;
	}

	@Override
	public void savePlayerData(String plugin, UUID playerId, ConfigurationSection configuration) {
		if (!pluginData.containsKey(plugin)) {
			initializePluginData(plugin);
		}
		YamlConfiguration cfg = pluginData.get(plugin);
		cfg.set(playerId.toString(), configuration);
	}

	@Override
	public boolean pluginHasData(String plugin) {
		File pluginFile = new File(pluginDataDir, plugin+".yml");
		return pluginFile.exists();
	}

	@Override
	public String getType() {
		return "yaml";
	}
}
