package com.github.fancyerii.deepnlu.weathercrawler.history;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

public class Writer extends Thread{
	protected static Logger logger=Logger.getLogger(Writer.class);
	
	private BlockingQueue<ExtractResult> resQueue;
	
	private File historyDir;
	private Gson gson=new Gson();
	private volatile boolean bStop;
	public void stopMe() {
		bStop=true;
	}
	public Writer(BlockingQueue<ExtractResult> resQueue, 
			String rootDir) {
		this.resQueue=resQueue;
		this.historyDir=Constants.getHistoryDir(rootDir);
		if(!historyDir.exists()) {
			historyDir.mkdirs();
		}
		logger.debug("historyDir: "+historyDir.getAbsolutePath());
	}
	
	@Override
	public void run() {
		while(!bStop) {
			try {
				ExtractResult er=resQueue.poll(1, TimeUnit.SECONDS);
				if(er==null) continue;
				this.saveResult(er);
				
			} catch (InterruptedException e) {
				logger.error(e.getMessage());
			}
		}
		while(true) {
			try {
				ExtractResult er=resQueue.poll(1, TimeUnit.SECONDS);
				if(er==null) break;
				this.saveResult(er);
				
			} catch (InterruptedException e) {
				logger.error(e.getMessage());
			}
		}
	}
	
	public static String removeSuffix(String fn) {
		if(fn.endsWith(".gz")) {
			return fn.substring(0, fn.length()-".gz".length());
		}
		return fn;
	}
	
	private String getFileName(ExtractResult er) {
		if(er.getMonth()<10) {
			return er.getYear()+"0"+er.getMonth()+".gz";
		}else {
			return er.getYear()+""+er.getMonth()+".gz";
		}
	}
	
	private void saveResult(ExtractResult er) {
		File dir=new File(historyDir, er.getCity());
		if(!dir.exists()) {
			dir.mkdirs();
		}
		String fn=this.getFileName(er);
		File tmpFile=new File(dir, fn+".bak");
		try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
					new GZIPOutputStream(new FileOutputStream(tmpFile)), 
					StandardCharsets.UTF_8))){
			
			bw.write(gson.toJson(er));
		} catch (IOException e) {
			logger.error(e.getMessage());
			return;
		}
		try {
			Files.move(tmpFile.toPath(), new File(dir, fn).toPath(),
					StandardCopyOption.REPLACE_EXISTING);
			logger.debug("succ: "+dir.getName()+"/"+fn);
		} catch (IOException e1) {
			logger.error(e1.getMessage());
		}
	}
}
