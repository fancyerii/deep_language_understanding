package com.github.fancyerii.deepnlu.baikecrawler.contentcrawler.datamining;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.github.fancyerii.deepnlu.baikecrawler.contentcrawler.BaikeItem;

public abstract class ContentConsumer extends Thread{
	protected static Logger logger=Logger.getLogger(ContentConsumer.class);
	
	private ArrayBlockingQueue<BaikeItem> taskQueue;
	private ArrayBlockingQueue<ResultData> resQueue;
	
	private volatile boolean bStop;
	void stopMe() {
		bStop=true;
	}
	
	public ContentConsumer(ArrayBlockingQueue<BaikeItem> taskQueue,
			ArrayBlockingQueue<ResultData> resQueue) {
		this.taskQueue=taskQueue;
		this.resQueue=resQueue;
	}
	
	@Override
	public void run() {
		while(!bStop) {
			try {
				BaikeItem item=taskQueue.poll(1, TimeUnit.SECONDS);
				if(item==null) continue;
				try {
					List<Object> res=this.processTask(item);
					for(Object t:res) {
						this.resQueue.put(new ResultData(item.getNewLemmaId(), t));
					}
				}catch(Exception e) {
					logger.error(e.getMessage());
				}
			} catch (InterruptedException e) {				
			}
			
		}
		while(true) {
			try {
				BaikeItem item=taskQueue.poll(1, TimeUnit.SECONDS);
				if(item==null) break;
				List<Object> res=this.processTask(item);
				for(Object t:res) {
					this.resQueue.put(new ResultData(item.getNewLemmaId(), t));
				}
			} catch (InterruptedException e) {				
			}
		}
	}
	
	public abstract List<Object> processTask(BaikeItem item);
}
