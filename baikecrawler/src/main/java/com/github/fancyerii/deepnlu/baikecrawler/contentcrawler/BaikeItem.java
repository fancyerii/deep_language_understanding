package com.github.fancyerii.deepnlu.baikecrawler.contentcrawler;

import lombok.Data;

@Data
public class BaikeItem {
    private String newLemmaId;
    private String html;
    private String item;
    private String lemmaId;
    private String subLemmaId;
    private String url;
}
