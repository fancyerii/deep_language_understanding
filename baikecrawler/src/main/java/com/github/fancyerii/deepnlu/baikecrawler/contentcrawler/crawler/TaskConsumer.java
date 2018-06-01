package com.github.fancyerii.deepnlu.baikecrawler.contentcrawler.crawler;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.antbrains.httpclientfetcher.HttpClientFetcher;
import com.github.fancyerii.deepnlu.baikecrawler.contentcrawler.BaikeExtractor;
import com.github.fancyerii.deepnlu.baikecrawler.contentcrawler.CrawlTask;
import com.github.fancyerii.deepnlu.baikecrawler.contentcrawler.ExtractResult;

public class TaskConsumer extends Thread{
	protected static Logger logger=Logger.getLogger(TaskConsumer.class);
	
	private ArrayBlockingQueue<CrawlTask> taskQueue;
	
	private long consumerInterval;
	
	private WriterDispatcher dispatcher;
	
	private HttpClientFetcher fetcher;
	
	private BaikeExtractor extractor;
	 
	
	public TaskConsumer(HttpClientFetcher fetcher,ArrayBlockingQueue<CrawlTask> taskQueue, 
			WriterDispatcher dispatcher, long consumerInterval) {
		this.fetcher=fetcher;
		this.taskQueue=taskQueue;

		this.dispatcher=dispatcher;
		extractor=new BaikeExtractor();
		this.consumerInterval=consumerInterval;
	}
	
	
	@Override
	public void run() {
		while(true) {
			try {
				CrawlTask ct=taskQueue.poll(1, TimeUnit.SECONDS);
				if(ct==null) continue;
				long start=System.currentTimeMillis();
				try {
					this.processTask(ct);
				}catch(Exception e) {
					logger.error(e.getMessage(),e);
					throw new RuntimeException(e);
				}
				long sleepMs=this.consumerInterval-(System.currentTimeMillis()-start);
				if(sleepMs>0) {
					Thread.sleep(sleepMs);
				}
			} catch (InterruptedException e) {
			}
			
		}
	}
	
	private void dispatchTask(CrawlTask task) {
		dispatcher.dispatchTask(task);
	}
	
	private void processTask(CrawlTask task) {
		String html=null;
		try {
			html=fetcher.httpGet(task.getUrl(), 3);
		} catch (Exception e) {
			String msg=e.getMessage();
			if(msg.length()>100) {
				msg=msg.substring(0, 100);
			}
			task.setLastFailReason(msg);
			
		}
		ArrayList<String> synUrls=new ArrayList<>();
		if(html!=null) {
			ExtractResult er=this.extractor.extract(task.getUrl(), html, synUrls);
			if(er.getItem()==null) {
				task.setLastFailReason(er.getFailReason());
				if(task.getLastFailReason()==null) {
					task.setLastFailReason("UNKNOWN");
				}
			}else {
				task.setItem(er.getItem());
			}
		}
		this.dispatchTask(task);
		
		for(String synUrl:synUrls) {
			CrawlTask newTask=new CrawlTask();
			newTask.setUrl(synUrl);
			this.processTask(newTask);
		}
	}
}
