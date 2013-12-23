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

public class ExportCommand extends BaseCommand {

	public ExportCommand(PlayerID plugin) {
		super(plugin, "import", new String[]{}, "playerid.import", new String[] {"Export data to another datastore."});
	}

	@Override
	protected void onExecute(CommandSender commandSender, String label, LinkedList<String> args) throws CommandError {
		String to = args.peek().toLowerCase();
		Class<? extends Datastore> clazz = Datastore.getRegisteredDatastores().get(to);
		if (clazz == null) {
			throw new CommandError("Unknown Datastore type: " + to);
		} else if (to.equalsIgnoreCase(plugin.getDatastore().getType())) {
			throw new CommandError("You cannot export to the current datastore type.");
		}
		Datastore newStore = null;
		try {
			newStore = Datastore.create(plugin, to);
			newStore.enable();
		} catch (DatastoreException e) {
			e.printStackTrace();
			throw new CommandError(e.getMessage());
		}
		int count = 0;
		for (Map.Entry<UUID, String> e: plugin.getDatastore().getNamesById().entrySet()) {
			newStore.save(e.getKey(), e.getValue());
			count ++;
		}
		commandSender.sendMessage("Exported " + count + " Player ID Mappings.");
		for (Plugin pl: plugin.getServer().getPluginManager().getPlugins()) {
			if (plugin.getDatastore().pluginHasData(pl.getName())) {
				count = 0;
				for (UUID uuid: plugin.getDatastore().getNamesById().keySet()) {
					ConfigurationSection cfg = plugin.getDatastore().getPlayerData(pl, uuid);
					if (cfg.getKeys(false).size() > 0) {
						count ++;
						newStore.savePlayerData(pl, uuid, cfg);
					}
				}
				commandSender.sendMessage("Exported " + pl.getName() + " data for " + count + " players.");
			}
		}
		newStore.disable();
		commandSender.sendMessage("Export complete, shut down and modify your config file to use the new datastore.");
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
