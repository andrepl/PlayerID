package com.norcode.bukkit.playerid.command;

import com.norcode.bukkit.playerid.PlayerID;

public class PlayerIDCommand extends BaseCommand {
	public PlayerIDCommand(PlayerID plugin) {
		super(plugin, "playerid", new String[] {}, "playerid.command", null);
		plugin.getCommand("playerid").setExecutor(this);
		registerSubcommand(new LookupCommand(plugin));
		registerSubcommand(new ImportCommand(plugin));
		registerSubcommand(new ExportCommand(plugin));
	}

}
