package com.norcode.bukkit.playerid.command;

import com.norcode.bukkit.playerid.PlayerID;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LookupCommand extends BaseCommand {
	public LookupCommand(PlayerID plugin) {
		super(plugin, "lookup", new String[] {"find", "search"}, "playerid.lookup", new String[]
				{"Look up a player by martial name or UUID."});
	}

	@Override
	protected void onExecute(CommandSender commandSender, String label, LinkedList<String> args) throws CommandError {
		if (args.size() == 0) {
			showHelp(commandSender, label, args);
			return;
		}
		String search = args.peek().toLowerCase();
		Iterator<Map.Entry<UUID, String>> it = plugin.getDatastore().getNamesById().entrySet().iterator();
		Map.Entry<UUID, String> entry;
		List<String> results = new ArrayList<String>();
		while (it.hasNext()) {
			entry = it.next();
			if (entry.getValue().toLowerCase().contains(search)) {
				results.add(highlightSearch(entry.getValue(), search) + " [" + entry.getKey().toString() + "]");
			} else if (entry.getKey().toString().toLowerCase().contains(search)) {
				results.add(entry.getValue() + " [" + highlightSearch(entry.getKey().toString(), search) + "]");
			}
		}
	}

	public static String highlightSearch(String s, String search) {
		int start = s.toLowerCase().indexOf(search.toLowerCase());
		return s.substring(0,start) + ChatColor.BOLD + s.substring(start, start+search.length()) + ChatColor.RESET + s.substring(start+search.length());
	}
}
