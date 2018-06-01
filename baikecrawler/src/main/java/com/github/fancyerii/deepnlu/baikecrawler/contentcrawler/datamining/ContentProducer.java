package com.github.fancyerii.deepnlu.baikecrawler.contentcrawler.datamining;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.log4j.Logger;

import com.antbrains.httpclientfetcher.FileTools;
import com.github.fancyerii.deepnlu.baikecrawler.contentcrawler.BaikeItem;
import com.github.fancyerii.deepnlu.baikecrawler.contentcrawler.Constants;
import com.google.gson.Gson;

public class ContentProducer extends Thread{
	protected static Logger logger=Logger.getLogger(ContentProducer.class);
	
	private File htmlDir;
	private ArrayBlockingQueue<BaikeItem> taskQueue; 
	private Gson gson=new Gson();
	private int printEvery=10_000;
	private Set<String> skipIds;
	
	public ContentProducer(String rootDir, ArrayBlockingQueue<BaikeItem> taskQueue,
			File skipNewLemmaIdFile) {
		htmlDir=new File(rootDir+"/"+Constants.HTML_PATH);
		if(!htmlDir.exists() || !htmlDir.isDirectory()) {
			throw new IllegalArgumentException("no HTML_PATH in: "+rootDir);
		}
		this.taskQueue=taskQueue;
		if(skipNewLemmaIdFile==null || !skipNewLemmaIdFile.exists()) {
			skipIds=Collections.emptySet();
		}else {
			try {
				List<String> lines=FileTools.readFile2List(skipNewLemmaIdFile.getAbsolutePath(), StandardCharsets.UTF_8.name());
				skipIds=new HashSet<>();
				skipIds.addAll(lines);
			} catch (IOException e) {
				logger.warn("can't read: "+skipNewLemmaIdFile.getAbsolutePath()+", "+e.getMessage());
				skipIds=Collections.emptySet();
			}
		}
		logger.info("skipIds: "+skipIds.size());
	}
	
	@Override
	public void run() {
		int processed=0;
		File[] subDirs=htmlDir.listFiles();
		for(File dir:subDirs) {
			File[] files=dir.listFiles();
			for(File file:files) {
				if(file.getName().endsWith(".gz")) {
					String id=file.getName().substring(0, file.getName().length()-3);
					if(this.skipIds.contains(id)) {
						logger.debug("skip: "+id);
						continue;
					}
					try {
						BaikeItem item=this.readFile(file);
						try {
							this.taskQueue.put(item);
						} catch (InterruptedException e) {
							
						}
					} catch (IOException e) {
						logger.error(file.getName()+" "+e.getMessage());
					}
					processed++;
					if(processed%printEvery==0) {
						logger.info("producer: "+processed);
					}
				}
			}
		}
		logger.info("totalTasks: "+processed);
	}
	
	private BaikeItem readFile(File file) throws IOException{
		try(BufferedReader br = new BufferedReader(new InputStreamReader(
				new GZIPInputStream(new FileInputStream(file)), 
				StandardCharsets.UTF_8))){
			String line=br.readLine();
			return gson.fromJson(line, BaikeItem.class);
		}
	}
}
