package com.github.fancyerii.deepnlu.baikecrawler.contentcrawler.datamining;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

import com.google.gson.Gson;

public class ContentWriter extends Thread{
	protected static Logger logger=Logger.getLogger(ContentWriter.class);
	
	private ArrayBlockingQueue<ResultData> resQueue;
	
	private volatile boolean bStop;
	void stopMe() {
		bStop=true;
	}
	private BufferedWriter[] bws;
	
	private BufferedWriter finishBw;
	
	private Gson gson=new Gson();
	
	public ContentWriter(String outPath, ArrayBlockingQueue<ResultData> resQueue,
			int writerCount, File finishIdFile) throws IOException{
		if(finishIdFile.exists()) {
			finishBw=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
					finishIdFile, true), 
					StandardCharsets.UTF_8)); 
		}else {
			finishBw=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
					finishIdFile), 
					StandardCharsets.UTF_8)); 
		}
		
		File outFile=new File(outPath);
		if(outFile.exists()) {
			if(!outFile.isDirectory()) {
				throw new IllegalArgumentException("not a dir: "+outPath);
			}
		}else {
			outFile.mkdirs();
		}
		bws=new BufferedWriter[writerCount];
		try {
			for(int i=0;i<writerCount;i++) {
				bws[i]=new BufferedWriter(new OutputStreamWriter(
						new GZIPOutputStream(new FileOutputStream(new File(outFile,"part_"+i+".gz"))), 
						StandardCharsets.UTF_8));
			}
		}catch(IOException e) {
			for(int i=0;i<writerCount;i++) {
				if(bws[i]!=null) {
					try {
						bws[i].close();
					}catch(IOException ex) {}
				}
			}
		}
		this.resQueue=resQueue;
	}
	
	@Override
	public void run() {
		while(!bStop) {
			try {
				ResultData res=resQueue.poll(1, TimeUnit.SECONDS);
				if(res==null) continue;
				this.saveTask(res);
			} catch (InterruptedException e) {
			}
			
		}
		while(true) {
			try {
				ResultData res=resQueue.poll(1, TimeUnit.SECONDS	);
				if(res==null) break;
				this.saveTask(res);
			} catch (InterruptedException e) {
			}
		}
		
		for(int i=0;i<bws.length;i++) {
			try {
				bws[i].close();
			} catch (IOException e) {
				logger.error(e.getMessage(),e);
			}
		}
		try {
			finishBw.close();
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
		}
	}
	
	private void saveTask(ResultData res) {
		int outIdx=Math.abs(DigestUtils.md5Hex(res.getNewLemmaId()).hashCode())%bws.length;
		try {
			bws[outIdx].write(gson.toJson(res)+"\n");
			finishBw.write(res.getNewLemmaId()+"\n");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
