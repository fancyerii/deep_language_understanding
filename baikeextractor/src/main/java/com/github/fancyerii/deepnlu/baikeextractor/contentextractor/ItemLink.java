package com.github.fancyerii.deepnlu.baikeextractor.contentextractor;

import lombok.Data;

@Data
public class ItemLink {
	private String word;
	private String url;
	
	public ItemLink(String w, String u) {
		word=w;
		url=u;
	}
}
