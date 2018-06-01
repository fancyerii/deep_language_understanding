package com.github.fancyerii.deepnlu.baikecrawler.contentcrawler.crawler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.github.fancyerii.deepnlu.baikecrawler.contentcrawler.Constants;
import com.github.fancyerii.deepnlu.baikecrawler.contentcrawler.CrawlTask;
import com.google.gson.Gson;

public class TaskStatsWriter extends Thread{
	protected static Logger logger=Logger.getLogger(TaskStatsWriter.class);
	
	private File succFile;
	private File failFile;
	
	private ArrayBlockingQueue<String> succQueue;
	private ArrayBlockingQueue<CrawlTask> failQueue;
	private long lastFlushTime=0;
	private long flushInterval=60_000L;
	private int cacheSize=1000;
	private ArrayList<String> succCache;
	private ArrayList<String> failCache;
	private BufferedWriter succWriter;
	private BufferedWriter failWriter;
	private Gson gson=new Gson();
	
	public TaskStatsWriter(String rootDir, ArrayBlockingQueue<String> succQueue,
			ArrayBlockingQueue<CrawlTask> failQueue) throws IOException {
		File dir=new File(rootDir);
		if(!dir.exists() || !dir.isDirectory()) {
			throw new IllegalArgumentException("not a dir or not exist: "+rootDir);
		}
		dir=new File(rootDir+"/"+Constants.TASK_MANAGER_PATH);
		if(!dir.exists()) {
			dir.mkdirs();
		}
		succFile=new File(rootDir+"/"+Constants.SUCCESS_TASKS_PATH);
		failFile=new File(rootDir+"/"+Constants.FAILED_TASKS_PATH);
		this.succQueue=succQueue;
		this.failQueue=failQueue;
		succCache=new ArrayList<>(cacheSize);
		failCache=new ArrayList<>(cacheSize);
		 
		succWriter=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(succFile, succFile.exists())
				,StandardCharsets.UTF_8));
		
		failWriter=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(failFile, failFile.exists())
				,StandardCharsets.UTF_8));
	}
	
	@Override
	public void run() {
		lastFlushTime=System.currentTimeMillis();
 
		while(true) {
			try {
				String succUrl=this.succQueue.poll(100, TimeUnit.MILLISECONDS);
				if(succUrl!=null) {
					this.succCache.add(succUrl);
				}
			} catch (InterruptedException e) {
			}
			
			{
				CrawlTask ct=this.failQueue.poll();
				if(ct!=null) {
					ct.setFailCount(ct.getFailCount()+1);
					ct.setItem(null);
					this.failCache.add(gson.toJson(ct));
				}
			}
			
			
			this.tryFlush();
		}
		
		
	}
	
	private void flushSucc() {
		for(String s:this.succCache) {
			try {
				this.succWriter.write(s+"\n");
				logger.debug("succ: "+s);
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}
		try {
			this.succWriter.flush();
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		succCache.clear();
	}
	private void flushFail() {
		for(String s:this.failCache) {
			try {
				this.failWriter.write(s+"\n");
				logger.debug("fail: "+s);
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}
		try {
			this.failWriter.flush();
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		failCache.clear();
	}
	
	private void tryFlush() {
		long curr=System.currentTimeMillis();
		if(curr-this.lastFlushTime>=this.flushInterval) {
			logger.debug("succ: "+this.succCache.size());
			logger.debug("fail: "+this.failCache.size());
			this.flushSucc();
			this.flushFail();
			this.lastFlushTime=curr;
		}
		
		if(this.succCache.size()>=cacheSize) {
			this.flushSucc();
			this.lastFlushTime=curr;
		}

		if(this.failCache.size()>=cacheSize) {
			this.flushFail();
			this.lastFlushTime=curr;
		}
	}
}
