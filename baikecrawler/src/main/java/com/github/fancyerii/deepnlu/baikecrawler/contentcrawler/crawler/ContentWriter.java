package com.github.fancyerii.deepnlu.baikecrawler.contentcrawler.crawler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

import org.apache.log4j.Logger;

import com.github.fancyerii.deepnlu.baikecrawler.contentcrawler.BaikeItem;
import com.github.fancyerii.deepnlu.baikecrawler.contentcrawler.CrawlTask;
import com.google.gson.Gson;

public class ContentWriter extends Thread{
	protected static Logger logger = Logger.getLogger(ContentWriter.class);
	
	private int writerIdx;
	
	private WriterDispatcher dispatcher;
	
	private File htmlDir;
	
	private int[] range;
	
	private Gson gson=new Gson();
	
	private ArrayBlockingQueue<String> succQueue;
	private ArrayBlockingQueue<CrawlTask> failQueue;
	
	public ContentWriter(int writerIdx, WriterDispatcher dispatcher, File htmlDir,
			ArrayBlockingQueue<String> succQueue,
			ArrayBlockingQueue<CrawlTask> failQueue) {
		this.writerIdx=writerIdx;
		this.dispatcher=dispatcher;
		this.htmlDir=htmlDir;
		range=dispatcher.getRange(writerIdx);
		logger.info("writer "+writerIdx+" from "+range[0]+" to "+range[1]);
		this.succQueue=succQueue;
		this.failQueue=failQueue;
	}
	
	@Override
	public void run() {
		ArrayBlockingQueue<CrawlTask> queue=dispatcher.getQueue(this.writerIdx);
		while(true) {
			try {
				CrawlTask task=queue.poll(1, TimeUnit.SECONDS);
				if(task==null) continue;
				try {
					this.saveTask(task);
				}catch(Exception e) {
					logger.error(e.getMessage(),e);
					throw new RuntimeException(e);
				}
			} catch (InterruptedException e) { 
			}
		}
		
	}
	
	private String genFilePath(int dirId, String newLemmaId) {
		return this.htmlDir.getAbsolutePath()+"/"+dirId+"/"+newLemmaId+".gz";
	}
	
	private void saveTask(CrawlTask task) {
		logger.debug("saveTask: "+task.getUrl());
		BaikeItem item=task.getItem();
		int dirId=dispatcher.calcOutDir(item);
		//check
		if(dirId<range[0] || dirId>=range[1]) {
			logger.warn("dispatch wrong: "+task.getUrl()+" dirId: "+dirId+", range[0]="+range[0]+", range[1]="+range[1]);
			return;
		}
		String outPath=genFilePath(dirId, item.getNewLemmaId());
		File tmpFile=new File(outPath+".bak");
		try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
					new GZIPOutputStream(new FileOutputStream(tmpFile)), 
					StandardCharsets.UTF_8))){
			
			bw.write(gson.toJson(item));
			// update successed tasks

		} catch (IOException e) {
			logger.error(e.getMessage());
			task.setLastFailReason(e.getMessage());
			//save failed tasks
			try {
				this.failQueue.put(task);
			} catch (InterruptedException e1) {
			}
			return;
		}
		try {
			Files.move(tmpFile.toPath(), new File(outPath).toPath(),StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e1) {
			logger.error(e1.getMessage());
		}

		logger.debug("succ: "+task.getUrl()+"\t"+outPath);
		try {
			this.succQueue.put(task.getUrl());
		} catch (InterruptedException e) {
		}
	}
}
