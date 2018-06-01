package com.github.fancyerii.deepnlu.weathercrawler.history;

import java.io.File;

public class Constants {
	public static final String HISTORY="history";
	
	public static File getHistoryDir(String rootDir) {
		return new File(rootDir, HISTORY);
	}
}
