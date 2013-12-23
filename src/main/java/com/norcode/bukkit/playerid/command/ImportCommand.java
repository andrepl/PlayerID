package com.norcode.bukkit.playerid.command;

import com.norcode.bukkit.playerid.PlayerID;
import com.norcode.bukkit.playerid.datastore.Datastore;
import com.norcode.bukkit.playerid.datastore.DatastoreException;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ImportCommand extends BaseCommand {

	public ImportCommand(PlayerID plugin) {
		super(plugin, "import", new String[]{}, "playerid.import", new String[] {"Import data from another datastore."});
	}

	@Override
	protected void onExecute(CommandSender commandSender, String label, LinkedList<String> args) throws CommandError {
		String from = args.peek().toLowerCase();
		Class<? extends Datastore> clazz = Datastore.getRegisteredDatastores().get(from);
		if (clazz == null) {
			throw new CommandError("Unknown Datastore type: " + from);
		} else if (from.equalsIgnoreCase(plugin.getDatastore().getType())) {
			throw new CommandError("You cannot import from the current datastore type.");
		}
		Datastore oldStore = null;
		try {
			oldStore = Datastore.create(plugin, from);
			oldStore.enable();
		} catch (DatastoreException e) {
			e.printStackTrace();
			throw new CommandError(e.getMessage());
		}
		int count = 0;
		for (Map.Entry<UUID, String> e: oldStore.getNamesById().entrySet()) {
			plugin.getDatastore().save(e.getKey(), e.getValue());
			count ++;
		}
		commandSender.sendMessage("Imported " + count + " Player ID Mappings.");
		for (Plugin pl: plugin.getServer().getPluginManager().getPlugins()) {
			if (oldStore.pluginHasData(pl.getName())) {
				count = 0;
				for (UUID uuid: oldStore.getNamesById().keySet()) {
					ConfigurationSection cfg = oldStore.getPlayerData(pl, uuid);
					if (cfg.getKeys(false).size() > 0) {
						count ++;
						plugin.getDatastore().savePlayerData(pl, uuid, cfg);
					}
				}
				commandSender.sendMessage("Imported " + pl.getName() + " data for " + count + " players.");
			}
		}
		oldStore.disable();
		commandSender.sendMessage("Import complete.");
	}

	@Override
	protected List<String> onTab(CommandSender sender, LinkedList<String> args) {
		List<String> results = new ArrayList<String>();
		if (args.size() == 1) {
			for (String s: Datastore.getRegisteredDatastores().keySet()) {
				if (s.toLowerCase().startsWith(args.peek().toLowerCase()) && !s.equals(plugin.getDatastore().getType())) {
					results.add(s);
				}
			}
		}
		return results;
	}
}
