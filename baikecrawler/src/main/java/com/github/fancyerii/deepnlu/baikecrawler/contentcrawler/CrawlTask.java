package com.github.fancyerii.deepnlu.baikecrawler.contentcrawler;

import java.util.Date;

import lombok.Data;

@Data
public class CrawlTask {
	private boolean forceUpdate;
	private String url;
	private int failCount;
	private String lastFailReason;
	private Date lastCrawlTime;
	
	//临时值
	private BaikeItem item;
}
