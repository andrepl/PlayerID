package com.norcode.bukkit.playerid.datastore;

import com.norcode.bukkit.playerid.PlayerID;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class RedisDatastore extends Datastore {

	private String host;
	private int port;
	private int db = 0;
	private String pass = null;

	private JedisPool pool;

	public static String HASH_PLAYER_IDS = "player-ids";
	public static String HASH_PLUGIN_DATA = "plugin-data::";

	public RedisDatastore(PlayerID plugin) {
		super(plugin);
	}

	@Override
	protected void onEnable() throws DatastoreException {
		ConfigurationSection cfg = plugin.getConfig().getConfigurationSection("datastore.settings.redis");
		if (cfg == null) {
			cfg = plugin.getConfig().createSection("datastore.settings.redis");
		}
		host = cfg.getString("host", "localhost");
		port = cfg.getInt("port", 6379);
		db = cfg.getInt("database", 0);
		pass = cfg.getString("password", null);
		JedisPoolConfig poolCfg = new JedisPoolConfig();
		pool = new JedisPool(poolCfg, host, port, 0, pass, db);
	}

	@Override
	protected Map<UUID, String> loadPlayerIds() {
		Jedis j = pool.getResource();
		HashMap<UUID, String> map = new HashMap<UUID, String>();
		for (Map.Entry<String, String> entry: j.hgetAll(HASH_PLAYER_IDS).entrySet()) {
			map.put(UUID.fromString(entry.getKey()), entry.getValue());
		}
		pool.returnResource(j);
		return map;
	}

	@Override
	protected void savePlayerId(UUID id, String name) {
		Jedis j = pool.getResource();
		j.hset(HASH_PLAYER_IDS, id.toString(), name);
		pool.returnResource(j);
	}

	@Override
	protected void savePlayerIds(Map<UUID, String> ids) {
		HashMap<String, String> map = new HashMap<String, String>();
		for (Map.Entry<UUID, String> entry: ids.entrySet()) {
			map.put(entry.getKey().toString(), entry.getValue());
		}
		Jedis j = pool.getResource();
		j.hmset(HASH_PLAYER_IDS, map);
		pool.returnResource(j);
	}

	@Override
	protected void deletePlayerId(UUID id) {
		Jedis j = pool.getResource();
		j.hdel(HASH_PLAYER_IDS, id.toString());
		pool.returnResource(j);
	}

	@Override
	protected void deletePlayerIds(Set<UUID> ids) {
		String[] uids = new String[ids.size()];
		int i = 0;
		for (UUID id: ids) {
			uids[i++] = id.toString();
		}
		Jedis j = pool.getResource();
		j.hdel(HASH_PLAYER_IDS, uids);
		pool.returnResource(j);
	}

	@Override
	protected void onDisable() {
		pool.destroy();
	}

	@Override
	public ConfigurationSection getPlayerData(String plugin, UUID playerId) {
		Jedis j = pool.getResource();
		String result = j.hget(HASH_PLUGIN_DATA + plugin, playerId.toString());
		if (result == null) {
			result = "";
		}
		pool.returnResource(j);
		ByteArrayInputStream is = new ByteArrayInputStream(result.getBytes());
		return YamlConfiguration.loadConfiguration(is);
	}

	@Override
	public void savePlayerData(String plugin, UUID playerId, ConfigurationSection configuration) {
		YamlConfiguration cfg = new YamlConfiguration();
		for (Map.Entry<String, Object> entry: configuration.getValues(true).entrySet()) {
			cfg.set(entry.getKey(), entry.getValue());
		}
		Jedis j =  pool.getResource();
		j.hset(HASH_PLUGIN_DATA + plugin, playerId.toString(), cfg.saveToString());
		pool.returnResource(j);
	}

	@Override
	public boolean pluginHasData(String plugin) {
		Jedis j = pool.getResource();
		Set<String> keys = j.keys(HASH_PLUGIN_DATA + "*");
		pool.returnResource(j);
		return keys.contains(HASH_PLUGIN_DATA + plugin);
	}

	@Override
	public String getType() {
		return "redis";
	}
}

