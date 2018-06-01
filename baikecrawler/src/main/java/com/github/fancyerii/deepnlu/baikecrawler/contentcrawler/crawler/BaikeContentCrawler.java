package com.github.fancyerii.deepnlu.baikecrawler.contentcrawler.crawler;
 
import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;

import com.antbrains.httpclientfetcher.HttpClientFetcher;
import com.github.fancyerii.deepnlu.baikecrawler.contentcrawler.Constants;
import com.github.fancyerii.deepnlu.baikecrawler.contentcrawler.CrawlTask;
import com.github.fancyerii.deepnlu.baikecrawler.utils.CrawlerLock;

public class BaikeContentCrawler {
	protected static Logger logger=Logger.getLogger(BaikeContentCrawler.class);
	
	private static final int DEF_MAX_FAIL=3;
	private static final double DEF_SPEED=2.0;
	private static final int DEF_CONSUMER_COUNT=3;
	private static final int DEF_WRITER_COUNT=10;
	private static final int DEF_QUEUE_SIZE=100;
	private static final long DEF_CONSUMER_INTERVAL=2000;
	
	
	public static void main(String[] args) throws Exception {
		CommandLineParser parser = new PosixParser();
		Options options = new Options();
		options.addOption("h", "help", false, "print help");
		options.addOption("maxFail", true, "max crawl fail count, default " + DEF_MAX_FAIL);
		options.addOption("speed", true, "craw speed, default " + DEF_SPEED);
		options.addOption("consumerCount", true, "consumerCount, default " + DEF_CONSUMER_COUNT);
		options.addOption("writerCount", true, "writerCount, default " + DEF_WRITER_COUNT);
		options.addOption("queueSize", true, "allQueueSize, default " + DEF_QUEUE_SIZE);
		options.addOption("consumerInterval", true, "consumerInterval, default " + DEF_CONSUMER_INTERVAL);
		
		CommandLine line = parser.parse(options, args);
		HelpFormatter formatter = new HelpFormatter();
		String helpStr = "BaikeContentCrawler rootDir";
		args = line.getArgs();
		
		if (args.length != 1) {
			formatter.printHelp(helpStr, options);
			System.exit(-1);
		}
		if (line.hasOption("help")) {
			formatter.printHelp(helpStr, options);
			System.exit(0);
		}
		
		String rootDir=args[0];
		
		int maxFail=DEF_MAX_FAIL;
		double speed=DEF_SPEED;
		int consumerCount=DEF_CONSUMER_COUNT;
		int writerCount=DEF_WRITER_COUNT;
		int queueSize=DEF_QUEUE_SIZE;
		long consumerInterval=DEF_CONSUMER_INTERVAL;
		
		if (line.hasOption("maxFail")) {
			maxFail = Integer.valueOf(line.getOptionValue("maxFail"));
		}
		
		if (line.hasOption("speed")) {
			speed = Double.valueOf(line.getOptionValue("speed"));
		}
		
		if (line.hasOption("consumerCount")) {
			consumerCount = Integer.valueOf(line.getOptionValue("consumerCount"));
		}
		
		if (line.hasOption("writerCount")) {
			writerCount = Integer.valueOf(line.getOptionValue("writerCount"));
		}
		
		if (line.hasOption("queueSize")) {
			queueSize = Integer.valueOf(line.getOptionValue("queueSize"));
		}
		
		if(line.hasOption("consumerInterval")) {
			consumerInterval=Long.valueOf(line.getOptionValue("consumerInterval"));
		}
		
		int dirCount=0;
		
		//check dirCount
		File htmlDir=new File(rootDir+"/"+Constants.HTML_PATH);
		int realCount=0;
		for(File f:htmlDir.listFiles()) {
			String fn=f.getName();
			try {
				Integer fnInt=Integer.valueOf(fn);
				if(fnInt<0) throw new RuntimeException("bad fn: "+fn);
				dirCount=Math.max(dirCount, fnInt);
				realCount++;
			}catch(Exception e) {
				throw new IllegalArgumentException("bad fn: "+fn);
			}
		}
		dirCount++;
		if(realCount!=dirCount || realCount<0) {
			throw new RuntimeException("bad dir, realCount: "+realCount+", dirCount: "+dirCount);
		}
		
		logger.info("rootDir: "+rootDir);
		logger.info("maxFail: "+maxFail);
		logger.info("speed[deprecated]: "+speed);
		logger.info("consumerCount: "+consumerCount);
		logger.info("writerCount: "+writerCount);
		logger.info("dirCount: "+dirCount);
		logger.info("queueSize: "+queueSize);
		logger.info("consumerInterval: "+consumerInterval);
		
		new CrawlerLock(rootDir+"/lock");
		
		ArrayBlockingQueue<CrawlTask> taskQueue=new ArrayBlockingQueue<>(queueSize);
		ArrayBlockingQueue<CrawlTask> failQueue=new ArrayBlockingQueue<>(10_000);
		ArrayBlockingQueue<String> succQueue=new ArrayBlockingQueue<>(10_000);

		TaskProducer producer=new TaskProducer(rootDir, taskQueue, maxFail, speed);
		TaskConsumer[] consumers=new TaskConsumer[consumerCount];
		HttpClientFetcher fetcher=new HttpClientFetcher(BaikeContentCrawler.class.getSimpleName());
		fetcher.setMaxConnectionPerRoute(consumerCount);
		fetcher.init();
		final WriterDispatcher dispatcher=new WriterDispatcher(writerCount, dirCount, failQueue, queueSize);
		for(int i=0;i<consumers.length;i++) {
			consumers[i]=new TaskConsumer(fetcher, taskQueue, dispatcher, consumerInterval);
			consumers[i].start();
		}
		
		
		
		ContentWriter[] writers=new ContentWriter[writerCount];
		for(int i=0;i<writers.length;i++) {
			writers[i]=new ContentWriter(i, dispatcher, new File(rootDir+"/"+Constants.HTML_PATH),
					succQueue, failQueue);
			writers[i].start();
		}
		
		TaskStatsWriter statsWriter=new TaskStatsWriter(rootDir, succQueue, failQueue);
		statsWriter.start();
		
		//最后启动producer
		producer.start();
		
		while(true) {
			Thread.sleep(60_000);
			logger.debug("tasksQueue: "+taskQueue.size()+", succ: "+succQueue.size()+", fail: "+failQueue.size());
			for(int i=0;i<writerCount;i++) {
				int qSize=dispatcher.getQueue(i	).size();
				logger.debug("queue"+i+": "+qSize);
			}
			
		}
	}

}
