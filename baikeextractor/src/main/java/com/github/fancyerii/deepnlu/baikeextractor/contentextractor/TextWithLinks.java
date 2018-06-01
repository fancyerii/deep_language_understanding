package com.github.fancyerii.deepnlu.baikeextractor.contentextractor;

import lombok.Data;

@Data
public class TextWithLinks {
	private String completeStr;
	private Object[] textOrLinks;
	public TextWithLinks(String text, Object[] details) {
		this.completeStr=text;
		this.textOrLinks=details;
	}
}
