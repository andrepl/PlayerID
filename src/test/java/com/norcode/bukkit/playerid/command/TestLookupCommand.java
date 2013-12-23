package com.norcode.bukkit.playerid.command;

import com.norcode.bukkit.playerid.command.LookupCommand;
import org.bukkit.ChatColor;
import org.junit.Assert;
import org.junit.Test;

public class TestLookupCommand {

	@Test
	public void testHighlightStartsWith() {
		String match = "Charlie";
		String search = "char";
		String expected = ChatColor.BOLD + "Char" + ChatColor.RESET + "lie";
		Assert.assertEquals(expected, LookupCommand.highlightSearch(match, search));
	}

	@Test
	public void testHighlightEndsWith() {
		String match = "Charlie";
		String search = "LIE";
		String expected = "Char" + ChatColor.BOLD + "lie" + ChatColor.RESET;
		Assert.assertEquals(expected, LookupCommand.highlightSearch(match, search));
	}

	@Test
	public void testHighlightMiddleMatch() {
		String match = "Charlie";
		String search = "arl";
		String expected = "Ch" + ChatColor.BOLD + "arl" + ChatColor.RESET + "ie";
		Assert.assertEquals(expected, LookupCommand.highlightSearch(match, search));
	}

}
