package com.github.fancyerii.deepnlu.baikeextractor.contentextractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class BaikeContentExtResult {
	private String newLemmaId;
	private String lemmaId;
	private String subLemmaId;
	private String item;
	private String pinyin;
	private String disambStr;
	private Map<String,ItemLink> infoboxLinkAttrs=new HashMap<>();
	private Map<String,String> infoboxTextAttrs=new HashMap<>();
	private Map<String,TextWithLinks> infoboxMixAttrs=new HashMap<>();
	private String[] tags; 
	
	private boolean succ;
	private String failReason;
	
	private List<String> warningMsgs=new ArrayList<>(2);
	
	public static final String FAIL_PARSE="fail_parse";
	public static final String FAIL_NO_ITEM="fail_no_item";
	
}
