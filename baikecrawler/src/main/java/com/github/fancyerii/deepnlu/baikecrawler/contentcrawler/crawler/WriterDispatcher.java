package com.github.fancyerii.deepnlu.baikecrawler.contentcrawler.crawler;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

import com.github.fancyerii.deepnlu.baikecrawler.contentcrawler.BaikeExtractor;
import com.github.fancyerii.deepnlu.baikecrawler.contentcrawler.BaikeItem;
import com.github.fancyerii.deepnlu.baikecrawler.contentcrawler.CrawlTask;

public class WriterDispatcher {
	protected static Logger logger=Logger.getLogger(WriterDispatcher.class);
	
	private final ArrayList<ArrayBlockingQueue<CrawlTask>> queues;
	
	private ArrayBlockingQueue<CrawlTask> failQueue;
	
	
	private static final int DEF_QUEUE_SIZE=100;
	private int writerNum;
	private int dirCount;
	
	public ArrayBlockingQueue<CrawlTask> getQueue(int id){
		return queues.get(id);
	}
	
	public WriterDispatcher(int writerNum, int dirCount,
			ArrayBlockingQueue<CrawlTask> failQueue) {
		this(writerNum, dirCount, failQueue, DEF_QUEUE_SIZE);
	}
	
	public WriterDispatcher(int writerNum, int dirCount,
			ArrayBlockingQueue<CrawlTask> failQueue,
			int queueSize) {
		this.failQueue=failQueue;
		queues=new ArrayList<>(writerNum);
		for(int i=0;i<writerNum;i++) {
			queues.add(new ArrayBlockingQueue<>(queueSize));
		}
		this.writerNum=writerNum;
		this.dirCount=dirCount;
	}
	
	public int calcOutDir(BaikeItem item) {
		return Math.abs(DigestUtils.md5Hex(item.getNewLemmaId()).hashCode())%dirCount;
	}
	
	public int[] getRange(int idx) {
		int countPerWriter=(int)Math.floor(1.0*dirCount/writerNum);
		if(idx<writerNum	) {
			return new int[] {idx*countPerWriter,(idx+1)*countPerWriter};
		}else {
			return new int[] {idx*countPerWriter, dirCount};
		}
	}
	
	public  int getWriterIdx(int outDir) {
		int countPerWriter=(int)Math.floor(1.0*dirCount/writerNum);
		int idx = outDir/countPerWriter;
		if(idx>=writerNum) throw new IllegalArgumentException("out of range: "+idx+", "+writerNum+", "+dirCount);
		return idx;
	}
	
	private void addFailTask2Stats(CrawlTask task) {
		try {
			this.failQueue.put(task);
		} catch (InterruptedException e) {		
		}
	}
	
	//可能blocking
	public void dispatchTask(CrawlTask task) { 
		if(task.getLastFailReason()!=null) {
			if(task.getLastFailReason().equals(BaikeExtractor.FAIL_REASON_EXTRACT_DELETED)) {
				task.setFailCount(99);
			}
			this.addFailTask2Stats(task);
			return;
		}
		
		BaikeItem item=task.getItem();
		if(item==null) {
			logger.warn("item is null: "+task.getUrl());
			this.addFailTask2Stats(task);
			return;
		}
		if(item.getNewLemmaId()==null) {
			logger.warn("newLemmaId is null: "+task.getUrl());
			task.setFailCount(99);
			task.setLastFailReason("newLemmaId is null");
			this.addFailTask2Stats(task);
			return;
		}
		int outDir=calcOutDir(item);
		int writerIdx=getWriterIdx(outDir);
		logger.debug("put task: "+task.getUrl()+" to "+writerIdx+" outDir "+outDir);
		try {
			ArrayBlockingQueue<CrawlTask> queue=this.queues.get(writerIdx);
			queue.put(task);
		} catch (InterruptedException e) {
			logger.warn(e.getMessage(), e);
		}
	}
}
