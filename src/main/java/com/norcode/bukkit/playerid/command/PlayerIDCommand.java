package com.norcode.bukkit.playerid.command;

import com.norcode.bukkit.playerid.PlayerID;

public class PlayerIDCommand extends BaseCommand {
	public PlayerIDCommand(PlayerID plugin) {
		super(plugin, "playerid", new String[] {}, "playerid.command", null);
		registerSubcommand(new LookupCommand(plugin));
	}

}
