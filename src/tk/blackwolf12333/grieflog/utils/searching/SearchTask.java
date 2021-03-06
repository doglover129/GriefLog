package tk.blackwolf12333.grieflog.utils.searching;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;

import tk.blackwolf12333.grieflog.GriefLog;
import tk.blackwolf12333.grieflog.PlayerSession;
import tk.blackwolf12333.grieflog.callback.SearchCallback;
import tk.blackwolf12333.grieflog.data.BaseData;
import tk.blackwolf12333.grieflog.utils.config.ConfigHandler;
import tk.blackwolf12333.grieflog.utils.filters.BlockFilter;
import tk.blackwolf12333.grieflog.utils.filters.EventFilter;
import tk.blackwolf12333.grieflog.utils.filters.Filter;
import tk.blackwolf12333.grieflog.utils.filters.PlayerFilter;
import tk.blackwolf12333.grieflog.utils.filters.TimeFilter;
import tk.blackwolf12333.grieflog.utils.filters.WorldEditFilter;
import tk.blackwolf12333.grieflog.utils.filters.WorldFilter;
import tk.blackwolf12333.grieflog.utils.logging.Time;

public class SearchTask implements Runnable {

	public String world;
	ArrayList<File> filesToSearch = new ArrayList<File>();
	ArrayList<BaseData> foundData = new ArrayList<BaseData>();
	List<Filter> filters = new ArrayList<Filter>();
	PlayerSession player;
	SearchCallback action;
	
	public SearchTask(PlayerSession p, SearchCallback action, ArgumentParser parser) {
		this.player = p;
		this.action = action;
		this.world = parser.world;
		this.filters = getFilters(p, parser);
		
		addFilesToList();
		new Thread(this).start();
		p.print(ChatColor.YELLOW + "[GriefLog] Searching for matching results...");
	}
	
	public SearchTask(PlayerSession p, SearchCallback action, Filter... filters) {
		this.player = p;
		this.action = action;
		this.filters = Arrays.asList(filters);
		
		addFilesToList();
		new Thread(this).start();
		p.print(ChatColor.YELLOW + "[GriefLog] Searching for matching results...");
	}
	
	private ArrayList<Filter> getFilters(PlayerSession player, ArgumentParser parser) {
		ArrayList<Filter> filters = new ArrayList<Filter>();
		if(parser.worldedit == true) {
			GriefLog.debug("Searching in worldedit region");
			filters.add(new WorldEditFilter(player));
		}
		if(parser.player != null) {
			GriefLog.debug("Searching for: " + parser.player.toString());
			filters.add(new PlayerFilter(parser.player));
		}
		if(parser.event != null) {
			GriefLog.debug("Searching for: " + parser.event);
			filters.add(new EventFilter(parser.event));
		}
		if(parser.world != null) {
			GriefLog.debug("Searching in: " + parser.world);
			filters.add(new WorldFilter(parser.world));
		}
		if(parser.blockFilter != null) {
			GriefLog.debug("Searching for: " + parser.blockFilter);
			filters.add(new BlockFilter(player, parser.blockFilter));
		}
		if(parser.time != null) {
			GriefLog.debug("Searching for: " + parser.time);
			filters.add(new TimeFilter(Time.getTimeFrom(parser.time)));
		}
		GriefLog.debug(filters);
		return filters;
	}
	
	public void addFilesToList() {
		if(GriefLog.logsDir.exists()) {
			if((world != null) && (!world.equalsIgnoreCase("null"))) {
				File f = new File(GriefLog.logsDir, world);
				addFilesInsideToFilesToSearch(f);
			} else {
				File[] list = GriefLog.logsDir.listFiles();
				for (File f : list) {
					if(f.isFile() && f.getName().startsWith("GriefLog")) {
						filesToSearch.add(f);
					} else if(f.isDirectory()) {
						addFilesInsideToFilesToSearch(f);
					}
				}
			}
		}
	}
	
	private void addFilesInsideToFilesToSearch(File f) {
		f.mkdir();
		File[] dircontents = f.listFiles();
		for(File file : dircontents) {
			if(file.isFile() && file.getName().startsWith("GriefLog")) {
				filesToSearch.add(file);
			}
		}
	}
	
	protected void searchFile(File file) {
		try {
			if(ConfigHandler.values.getLoggingMethod().equalsIgnoreCase("csv")) {
				List<BaseData> lines = GriefLog.csvIO.read(file);
				for(BaseData data : lines) {
					addToFoundDataIfComesThroughFilters(data);
				}
			} else {
				String query = GriefLog.fileIO.read2String(file);
				String[] lines = query.split(System.getProperty("line.separator"));
				for(String line : lines) {
					BaseData data = BaseData.loadFromString(line);
					if(data != null) {
						addToFoundDataIfComesThroughFilters(data);
					} else {
						GriefLog.debug("Encountered an unidentified data type:");
						GriefLog.debug(line);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void addToFoundDataIfComesThroughFilters(BaseData line) {
		if(doesComeThroughFilter(line)) {
			foundData.add(line);
		}
	}
	
	private boolean doesComeThroughFilter(BaseData data) {
		if(this.filters != null && !this.filters.isEmpty()) {
			for(Filter filter : filters) {
				if(filter.doFilter(data)) {
					continue;
				} else {
					return false;
				}
			}
		}
		return data != null;
	}
	
	@Override
	public void run() {
		for(File searchFile : filesToSearch) {
			GriefLog.debug("Searching file: " + searchFile.getName() + " Size: " + GriefLog.fileIO.getFileSize(searchFile));
			long currentTime = System.currentTimeMillis();
			searchFile(searchFile);
			long nextTime = System.currentTimeMillis();
			GriefLog.debug("Took: " + (nextTime - currentTime) + "ms");
		}
		
		Collections.sort(foundData);
		player.setSearchResult(foundData);
		action.start(player);
	}
}
