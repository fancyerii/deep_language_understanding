package com.github.fancyerii.deepnlu.baikecrawler.contentcrawler.crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream; 
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue; 

import org.apache.log4j.Logger;

import com.antbrains.httpclientfetcher.FileTools;
import com.github.fancyerii.deepnlu.baikecrawler.contentcrawler.Constants;
import com.github.fancyerii.deepnlu.baikecrawler.contentcrawler.CrawlTask;
import com.google.gson.Gson;

/**
 * 读取未完成的任务到队列里
 * 判断是否完成的方法：1. url抓取过; 2. newLemmaId抓取过
 * @author lili
 *
 */
public class TaskProducer extends Thread{
	protected static Logger logger=Logger.getLogger(TaskProducer.class);
	
	private ArrayBlockingQueue<CrawlTask> taskQueue;
	private File taskDir;
	//不需要抓取的url，包括成功的和失败次数过多的，但是如果forceUpdate是true，那么不过如何都会抓取。
	private Set<String> finishedTasks;
	private int maxFail=3;
	private Gson gson=new Gson();
	private long sleepWhenNoFiles=60_000L;
	private int printEvery=1000;
	private double speed;
	
	private long total;
	private long startTime; 
	private String rootDir;
	
	public TaskProducer(String rootDir, ArrayBlockingQueue<CrawlTask> taskQueue, int maxFail, double speed) throws IOException {
		this.rootDir=rootDir;
		this.taskDir=new File(rootDir+"/"+Constants.TASK_TODO_PATH);
		this.taskQueue=taskQueue;
		this.maxFail=maxFail;
		if(!this.taskDir.isDirectory() || !this.taskDir.exists()) {
			throw new IllegalArgumentException(taskDir+" is not a dir or not exist");
		}
		this.initFinishedTasks(rootDir);
		this.speed=speed;
	}
	
	private void initFinishedTasks(String rootDir) throws IOException {
		finishedTasks=new HashSet<>();
		//TODO 从HTML_PATH读取所有下载过的newLemmaId
		File succFile=new File(rootDir+"/"+Constants.SUCCESS_TASKS_PATH);
		if(succFile.exists()) {
			finishedTasks.addAll(FileTools.readFile2List(succFile.getAbsolutePath(), "UTF8"));
		}
		logger.info("successTasks: "+finishedTasks.size());
		File failFile=new File(rootDir+"/"+Constants.FAILED_TASKS_PATH);
		List<String> failedTasks=null;
		if(failFile.exists()) {
			failedTasks=FileTools.readFile2List(failFile.getAbsolutePath(), "UTF8");
		}else {
			failedTasks=Collections.<String>emptyList();
		}
		
		logger.info("totalFailTasks: "+failedTasks.size());
		for(String ft:failedTasks) {
			CrawlTask ct=gson.fromJson(ft, CrawlTask.class);
			if(ct.getFailCount()<=this.maxFail) {
				finishedTasks.add(ct.getUrl());
			}
		}
		logger.info("totalFailTasks: "+failedTasks.size());
		
	}
	
	private boolean needCrawl(CrawlTask ct) {
		if(this.finishedTasks.contains(ct.getUrl())) {
			return false;
		}
		
		
		return true;
	}
	
	private void speedControl() {
		this.total++;
		long usedMs=(System.currentTimeMillis()-this.startTime);
		long expectedMs=(long)(1000*total/this.speed); //speed 个/秒
		if(expectedMs>usedMs) {
			try {
				Thread.sleep(expectedMs-usedMs);
			} catch (InterruptedException e) {
			}
		}
		if(total%1_000==0) {
			logger.info("total: "+total+", used: "+usedMs+", expected: "+expectedMs);
		}
	}
	
	private void processUrlFile(File file) throws IOException{
		try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"))){
			String line;
			int lineNum=0;
			while((line=br.readLine())!=null) {
				lineNum++;
				if(lineNum%this.printEvery==0) {
					logger.info("processUrl: "+lineNum);
				}
				line=line.trim();
				if(line.isEmpty()) {
					logger.warn("ignore empty line: "+lineNum);
					continue;
				}
				CrawlTask ct=new CrawlTask();
				ct.setUrl(line);
				if(!this.needCrawl(ct)) {
					logger.debug("skip: "+ct.getUrl());
					continue;
				}
				try {
					this.taskQueue.put(ct);
					this.finishedTasks.add(ct.getUrl());
				} catch (InterruptedException e) {
					logger.warn(e.getMessage());
				}
				
				//speed control
				//speedControl();
			}
		}
	}
	
	private String generateNameByTime(long startTime) {
		long endTime=System.currentTimeMillis();
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss");
		return sdf.format(new Date(startTime))+"--"+sdf.format(new Date(endTime));
	}
	
	@Override
	public void run() {
		startTime=System.currentTimeMillis();
		while(true) {
			File[] files=this.taskDir.listFiles();
			if(files.length==0) {
				try {
					Thread.sleep(this.sleepWhenNoFiles);
				} catch (InterruptedException e) {
				}
			}
			for(File file:files) {
				logger.info("producing "+file.getName());
				long startTime=System.currentTimeMillis();
				if(file.getName().endsWith(".ct")) {
					
				}else {
					try {
						this.processUrlFile(file);
					} catch (Exception e) {
						logger.error(e.getMessage(),e);
						throw new RuntimeException(e);
					}
				}
				logger.info("finishing "+file.getName());
				String newName=this.rootDir+"/"+Constants.TASK_MANAGER_PATH
						+"/"+file.getName()+"_"+this.generateNameByTime(startTime);
				logger.info("remove to: "+newName);
				try {
					Files.move(file.toPath(), new File(newName).toPath(),StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					logger.error(e.getMessage());
				} 
			}
			
			
		}
	}
}
