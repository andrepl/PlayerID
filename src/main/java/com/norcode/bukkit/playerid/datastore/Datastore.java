package com.norcode.bukkit.playerid.datastore;

import com.norcode.bukkit.playerid.PlayerID;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

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
			register("redis", RedisDatastore.class);
		} catch (DatastoreException ex) {
			ex.printStackTrace();
		}
	}

	public static HashMap<String, Class<? extends Datastore>> getRegisteredDatastores() {
		return registry;
	}

	protected Datastore(PlayerID plugin) {
		this.plugin = plugin;
	}

	public static Datastore create(PlayerID plugin, String type) throws DatastoreException {
		Class<? extends Datastore> impl = getImplementation(type);
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

	public void enable() throws DatastoreException {
		onEnable();
		namesById.clear();
		idsByName.clear();
		for (Map.Entry<UUID, String> entry: loadPlayerIds().entrySet()) {
			namesById.put(entry.getKey(), entry.getValue());
			idsByName.put(entry.getValue(), entry.getKey());
		}
	}

	public void disable() {
		onDisable();
	}

	public HashMap<UUID, String> getNamesById() {
		return namesById;
	}

	public HashMap<String, UUID> getIdsByName() {
		return idsByName;
	}

	public void save(UUID id, String player) {
		namesById.put(id, player);
		idsByName.put(player.toLowerCase(), id);
		savePlayerId(id, player);
	}

	public void delete(UUID id) {
		String name = namesById.remove(id);
		idsByName.remove(name.toLowerCase());
		deletePlayerId(id);
	}

	public ConfigurationSection getPlayerData(String plugin, String playerName) {
		return getPlayerData(plugin, idsByName.get(playerName.toLowerCase()));
	}

	public ConfigurationSection getPlayerData(String plugin, Player player) {
		return getPlayerData(plugin, player.getUniqueId());
	}


	public void savePlayerData(String plugin, String playerName, ConfigurationSection configuration) {
		savePlayerData(plugin, idsByName.get(playerName), configuration);
	}

	public void savePlayerData(String plugin, Player player, ConfigurationSection configuration) {
		savePlayerData(plugin, player.getUniqueId(), configuration);
	}

	protected abstract void onEnable() throws DatastoreException;
	protected abstract Map<UUID, String> loadPlayerIds();
	protected abstract void savePlayerId(UUID id, String name);
	protected abstract void savePlayerIds(Map<UUID, String> ids);
	protected abstract void deletePlayerId(UUID id);
	protected abstract void deletePlayerIds(Set<UUID> ids);
	protected abstract void onDisable();
	public abstract ConfigurationSection getPlayerData(String plugin, UUID playerId);
	public abstract void savePlayerData(String plugin, UUID playerId, ConfigurationSection configuration);
	public abstract boolean pluginHasData(String plugin);
	public abstract String getType();
}
