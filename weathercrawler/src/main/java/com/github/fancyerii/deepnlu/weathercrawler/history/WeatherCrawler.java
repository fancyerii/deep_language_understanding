package com.github.fancyerii.deepnlu.weathercrawler.history;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import org.apache.log4j.Logger;

import com.antbrains.httpclientfetcher.HttpClientFetcher;

public class WeatherCrawler {
	protected static Logger logger=Logger.getLogger(WeatherCrawler.class);
	
	private static void doWork(String rootDir, int extCount, long crawlInterval) throws Exception{
		HttpClientFetcher fetcher=new HttpClientFetcher(WeatherCrawler.class.getName());
		fetcher.init();

		BlockingQueue<CrawlTask> taskQueue=new PriorityBlockingQueue<>();
		BlockingQueue<CrawlTask> newTaskQueue=new LinkedBlockingQueue<>();
		BlockingQueue<ExtractResult> resQueue=new LinkedBlockingQueue<>();
		Extractor[] exts=new Extractor[extCount];

		for(int i=0;i<exts.length;i++) {
			exts[i]=new Extractor(fetcher, taskQueue, newTaskQueue, resQueue, crawlInterval);
			exts[i].start();
		}
		
		TaskDedup dedup=new TaskDedup(newTaskQueue, taskQueue, rootDir);
		dedup.start();
		
		Writer writer=new Writer(resQueue, rootDir);
		writer.start();
		
		CrawlTask initTask=new CrawlTask("http://lishi.tianqi.com/");
		taskQueue.put(initTask);
		int noTaskMs=0;
		while(true) {
			Thread.sleep(60000);
			logger.info("taskQueue: "+taskQueue.size()+", newTaskQueue: "+newTaskQueue.size()+", resQueue: "+resQueue.size());
			if(taskQueue.isEmpty() && newTaskQueue.isEmpty() && resQueue.isEmpty()) {
				noTaskMs++;
			}else {
				noTaskMs=0;
			}
			if(noTaskMs>30) {
				logger.info("finish task");
				break;
			}
		}		
		for(int i=0;i<exts.length;i++) { 
			exts[i].stopMe();
		}
		for(int i=0;i<exts.length;i++) { 
			exts[i].join();
		}
		dedup.stopMe();
		dedup.join();
		writer.stopMe();
		writer.join();
		
		fetcher.close();
	}
	public static void main(String[] args) throws Exception {
		if(args.length!=3) {
			System.out.println("need 3 args: rootDir extThreads crawlInterval");
		}
		String rootDir=args[0]; 
		int extCount=Integer.valueOf(args[1]);
		long crawlInterval=Long.valueOf(args[2]);
		logger.info("rootDir: "+rootDir);
		logger.info("extCount: "+extCount);
		logger.info("crawlInterval: "+crawlInterval);
		long taskInterval=12*3600*1000L;
		int taskId=0;
		while(true) {
			taskId++;
			logger.info("start task "+taskId);
			long start=System.currentTimeMillis();
			doWork(rootDir, extCount, crawlInterval);
			
			long used=System.currentTimeMillis()-start;
			logger.debug("used: "+used+" ms");
			if(taskInterval>used) {
				Thread.sleep(taskInterval-used);
			}
		}
	}

}
