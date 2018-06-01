package com.github.fancyerii.deepnlu.weathercrawler.history;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

public class TaskDedup extends Thread{
	protected static Logger logger=Logger.getLogger(TaskDedup.class);
	
	private BlockingQueue<CrawlTask> newTaskQueue;
	private BlockingQueue<CrawlTask> taskQueue;
	public static final String CITY_DATE_SEP="-";
	
	private Set<String> finishedTasks;
	private volatile boolean bStop;
	public void stopMe() {
		bStop=true;
	}
	
	public TaskDedup(BlockingQueue<CrawlTask> newTaskQueue,
			BlockingQueue<CrawlTask> taskQueue,
			String rootDir) throws IOException {
		this.newTaskQueue=newTaskQueue;
		this.taskQueue=taskQueue;
		File dir=Constants.getHistoryDir(rootDir);
		this.init(dir);
	}
	
	private void init(File dir) throws IOException {
		finishedTasks=new HashSet<>();
		this.recurProcessFile(dir);
		if(!finishedTasks.isEmpty()) {
			logger.debug("finished: "+finishedTasks.iterator().next());
		}
	}
	
	private void recurProcessFile(File f) throws IOException {
		if(!f.exists()) return;
		if(f.isFile()) {
			this.processFile(f);
		}else {
			for(File child:f.listFiles()) {
				this.recurProcessFile(child);
			}
		}
	}
	
	private void processFile(File f) throws IOException {
		this.finishedTasks.add(f.getParent()+CITY_DATE_SEP+Writer.removeSuffix(f.getName()));
	}
	
	
	
	@Override
	public void run() {
		while(!bStop) {
			try {
				CrawlTask newTask=this.newTaskQueue.poll(1, TimeUnit.SECONDS);
				if(newTask==null) continue;
				this.doWork(newTask);
			} catch (InterruptedException e) {
				
			}
			
		}
		while(true) {
			try {
				CrawlTask newTask=this.newTaskQueue.poll(1, TimeUnit.SECONDS);
				if(newTask==null) break;
				this.doWork(newTask);
			} catch (InterruptedException e) {
				
			}
		}
	}
	
	private void doWork(CrawlTask newTask) throws InterruptedException {
		String url=newTask.getUrl();
		String[] arr=Extractor.getCityAndDate(url);
		if(arr==null) {
			this.taskQueue.put(newTask);
		}else {
			String city=arr[0];
			String date=arr[1];
			if(this.finishedTasks.contains(city+CITY_DATE_SEP+date)) {
				logger.info("duplicated: "+url);
			}else {
				this.taskQueue.put(newTask);
			}
		}
	}
}
