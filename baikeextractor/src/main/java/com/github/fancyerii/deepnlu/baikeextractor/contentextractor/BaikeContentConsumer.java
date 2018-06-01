package com.github.fancyerii.deepnlu.baikeextractor.contentextractor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import com.github.fancyerii.deepnlu.baikecrawler.contentcrawler.BaikeItem;
import com.github.fancyerii.deepnlu.baikecrawler.contentcrawler.datamining.ContentConsumer;
import com.github.fancyerii.deepnlu.baikecrawler.contentcrawler.datamining.ResultData;

public class BaikeContentConsumer extends ContentConsumer{
	private BaikeContentExtractor ext;
	public BaikeContentConsumer(ArrayBlockingQueue<BaikeItem> taskQueue, ArrayBlockingQueue<ResultData> resQueue) {
		super(taskQueue, resQueue);
		ext=new BaikeContentExtractor();
	}

	@Override
	public List<Object> processTask(BaikeItem item) {
		List<Object> result=new ArrayList<>(1);
		BaikeContentExtResult res=ext.extract(item.getHtml());
		res.setNewLemmaId(item.getNewLemmaId());
		res.setLemmaId(item.getLemmaId());
		res.setSubLemmaId(item.getSubLemmaId());
		if(!res.getItem().equals(item.getItem())) {
			logger.warn("item mismatch: "+res.getItem()+" -- "+item.getItem());
		}
		result.add(res);
		return result;
	}

}
