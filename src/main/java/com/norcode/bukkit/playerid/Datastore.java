package com.norcode.bukkit.playerid;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public abstract class Datastore {

	protected PlayerID plugin;

	private HashMap<UUID, String> namesById = new HashMap<UUID, String>();
	private HashMap<String, UUID> idsByName = new HashMap<String, UUID>();


	private static HashMap<String, Class<? extends Datastore>> registry = new HashMap<String, Class<? extends Datastore>>();

	protected static Class<? extends Datastore> getImplementation(String type) throws DatastoreException {
		if (!registry.containsKey(type.toLowerCase())) {
			throw new DatastoreException("No implementation found for datastore.type=" + type.toLowerCase());
		}
		return registry.get(type.toLowerCase());
	}

	protected static void register(String string, Class<? extends Datastore> clazz) throws DatastoreException {
		if (registry.containsKey(string.toLowerCase())) {
			throw new DatastoreException("There is already a datastore implementation registered as " + string.toLowerCase());
		}
		registry.put(string, clazz);
	}

	static {
		try {
			register("yaml", YamlDatastore.class);
		} catch (DatastoreException ex) {
			ex.printStackTrace();
		}
	}

	protected Datastore(PlayerID plugin) {
		this.plugin = plugin;
	}

	public static Datastore create(PlayerID plugin) throws DatastoreException {
		ConfigurationSection cfg = plugin.getConfig().getConfigurationSection("datastore");
		Class<? extends Datastore> impl = getImplementation(cfg.getString("type"));
		Datastore store = null;
		try {
			store = impl.getConstructor(PlayerID.class).newInstance(plugin);
		} catch (NoSuchMethodException e) {
			throw new DatastoreException(e);
		} catch (InvocationTargetException e) {
			throw new DatastoreException(e);
		} catch (InstantiationException e) {
			throw new DatastoreException(e);
		} catch (IllegalAccessException e) {
			throw new DatastoreException(e);
		}
		return store;
	}

	void enable() throws DatastoreException {
		onEnable();
		namesById.clear();
		idsByName.clear();
		for (Map.Entry<UUID, String> entry: loadPlayerIds().entrySet()) {
			namesById.put(entry.getKey(), entry.getValue());
			idsByName.put(entry.getValue(), entry.getKey());
		}
	}

	void disable() {
		onDisable();
	}

	public HashMap<UUID, String> getNamesById() {
		return namesById;
	}

	public HashMap<String, UUID> getIdsByName() {
		return idsByName;
	}

	protected void save(UUID id, Player player) {
		namesById.put(id, player.getName());
		idsByName.put(player.getName().toLowerCase(), id);
		savePlayerId(id, player.getName());
	}

	protected void delete(UUID id) {
		String name = namesById.remove(id);
		idsByName.remove(name.toLowerCase());
		deletePlayerId(id);
	}

	public ConfigurationSection getPlayerData(JavaPlugin plugin, UUID playerId) {
		return getPlayerData(plugin.getName(), playerId);
	}

	public ConfigurationSection getPlayerData(JavaPlugin plugin, String playerName) {
		return getPlayerData(plugin, idsByName.get(playerName.toLowerCase()));
	}

	public ConfigurationSection getPlayerData(JavaPlugin plugin, Player player) {
		return getPlayerData(plugin, player.getUniqueId());
	}

	public void savePlayerData(JavaPlugin plugin, UUID playerId, ConfigurationSection configuration) {
		savePlayerData(plugin.getName(), playerId, configuration);
	}

	public void savePlayerData(JavaPlugin plugin, String playerName, ConfigurationSection configuration) {
		savePlayerData(plugin, idsByName.get(playerName), configuration);
	}

	public void savePlayerData(JavaPlugin plugin, Player player, ConfigurationSection configuration) {
		savePlayerData(plugin, player.getUniqueId(), configuration);
	}

	protected abstract void onEnable() throws DatastoreException;
	protected abstract Map<UUID, String> loadPlayerIds();
	protected abstract void savePlayerId(UUID id, String name);
	protected abstract void savePlayerIds(Map<UUID, String> ids);
	protected abstract void deletePlayerId(UUID id);
	protected abstract void deletePlayerIds(Set<UUID> ids);
	protected abstract void onDisable();
	protected abstract ConfigurationSection getPlayerData(String plugin, UUID playerId);
	protected abstract void savePlayerData(String plugin, UUID playerId, ConfigurationSection configuration);
}
