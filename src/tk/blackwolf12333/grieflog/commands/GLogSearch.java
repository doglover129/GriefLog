package tk.blackwolf12333.grieflog.commands;

import org.bukkit.ChatColor;

import tk.blackwolf12333.grieflog.utils.searching.SearchTask;
import tk.blackwolf12333.grieflog.GriefLog;
import tk.blackwolf12333.grieflog.PlayerSession;
import tk.blackwolf12333.grieflog.callback.SearchCallback;
import tk.blackwolf12333.grieflog.conversations.SearchConversation;
import tk.blackwolf12333.grieflog.utils.searching.ArgumentParser;
import tk.blackwolf12333.grieflog.utils.GriefLogException;

public class GLogSearch {

	GriefLog plugin;
	
	private String noPermsMsg = ChatColor.DARK_RED + "I am sorry, You do not have permission to run this command.";
	
	public GLogSearch(GriefLog plugin) {
		this.plugin = plugin;
	}
	
	public boolean onCommand(PlayerSession player, String[] args) {
		GriefLog.debug("search parameters:");
		GriefLog.debug(args);
		if(player.hasPermission("grieflog.search")) {
			if(args.length == 1) {
				new SearchConversation(plugin, player, false, false);
				return true;
			} else {
				if(args.length >= 2) {
					try {
						ArgumentParser parser = new ArgumentParser(args);
						new SearchTask(player, new SearchCallback(player, SearchCallback.Type.SEARCH), parser);
					} catch(GriefLogException e) {
						player.print(ChatColor.RED + "[GriefLog] An error occured parsing your command, please check it for any mistakes.");
						return true;
					}
					return true;
				} else {
					try {
						ArgumentParser parser = new ArgumentParser(args);
						new SearchTask(player, new SearchCallback(player, SearchCallback.Type.SEARCH), parser);
					} catch(GriefLogException e) {
						player.print(ChatColor.RED + "[GriefLog] An error occured parsing your command, please check it for any mistakes.");
						return true;
					}
					return true;
				}
			}
		} else {
			player.print(noPermsMsg);
		}
		return true;
	}
}
