package com.github.fancyerii.deepnlu.baikecrawler.contentcrawler.datamining;

import lombok.Data;

@Data
public class ResultData {
	private String newLemmaId;
	private Object data;
	
	public ResultData(String id, Object data) {
		this.newLemmaId=id;
		this.data=data;
	}
}
