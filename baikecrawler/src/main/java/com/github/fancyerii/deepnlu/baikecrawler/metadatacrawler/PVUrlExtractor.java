package com.github.fancyerii.deepnlu.baikecrawler.metadatacrawler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import com.github.fancyerii.deepnlu.baikecrawler.contentcrawler.BaikeExtractor;
import com.github.fancyerii.deepnlu.baikecrawler.contentcrawler.BaikeItem;
import com.github.fancyerii.deepnlu.baikecrawler.contentcrawler.datamining.ContentConsumer;
import com.github.fancyerii.deepnlu.baikecrawler.contentcrawler.datamining.ResultData;

public class PVUrlExtractor extends ContentConsumer{
	private BaikeExtractor ext;
	public PVUrlExtractor(ArrayBlockingQueue<BaikeItem> taskQueue, ArrayBlockingQueue<ResultData> resQueue) {
		super(taskQueue, resQueue);
		ext=new BaikeExtractor();
	}

	@Override
	public List<Object> processTask(BaikeItem item) {
		List<Object> urls=new ArrayList<>(1);
		String[] arr=ext.extractNewLemmaIdEnc(item.getHtml());
		if(arr[0]==null) {
			logger.warn("can't extract newLemmaEnc: "+item.getUrl());
			return urls;
		}
		String url="http://baike.baidu.com/api/lemmapv?id=" + arr[0];
		urls.add(url);
		return urls;
	}

}
