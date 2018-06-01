package com.github.fancyerii.deepnlu.weathercrawler.history;

 
import lombok.Data;

@Data
public class CrawlTask implements Comparable<CrawlTask>{
	private String url;
	private int depth;

	public CrawlTask(String url) {
		this.url=url;
	}

	@Override
	public int compareTo(CrawlTask o) {
		return o.depth-this.depth;
	}
}
