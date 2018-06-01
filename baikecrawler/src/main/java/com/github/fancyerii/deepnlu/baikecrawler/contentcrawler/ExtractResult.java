package com.github.fancyerii.deepnlu.baikecrawler.contentcrawler;

import lombok.Data;

@Data
public class ExtractResult{
	public ExtractResult(BaikeItem item, String failReason){
		this.item=item;
		this.failReason=failReason; 
	}
	private BaikeItem item;
	private String failReason; 
	
}
