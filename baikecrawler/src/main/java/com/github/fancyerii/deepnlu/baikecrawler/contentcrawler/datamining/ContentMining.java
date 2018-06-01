package com.github.fancyerii.deepnlu.baikecrawler.contentcrawler.datamining;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;

import com.github.fancyerii.deepnlu.baikecrawler.contentcrawler.BaikeItem;

public class ContentMining {
	protected static Logger logger=Logger.getLogger(ContentMining.class);
	 
	private static final int DEF_CONSUMER_COUNT=8; 
	private static final int DEF_WRITER_COUNT=10; 
	private static final int DEF_QUEUE_SIZE=100;
	
	public static void main(String[] args) throws Exception{
		CommandLineParser parser = new PosixParser();
		Options options = new Options();
		options.addOption("h", "help", false, "print help"); 
		options.addOption("consumerCount", true, "consumerCount, default " + DEF_CONSUMER_COUNT); 
		options.addOption("queueSize", true, "queueSize, default " + DEF_QUEUE_SIZE);
		options.addOption("writerCount", true, "writerCount, default " + DEF_WRITER_COUNT);
		options.addOption("skipIdFile", true, "skipIdFile");
		
		CommandLine line = parser.parse(options, args);
		HelpFormatter formatter = new HelpFormatter();
		String helpStr = "ContentMining rootDir outDir ContentConsumerClass finishIdFile";
		args = line.getArgs();
		
		if (args.length != 4) {
			formatter.printHelp(helpStr, options);
			System.exit(-1);
		}
		if (line.hasOption("help")) {
			formatter.printHelp(helpStr, options);
			System.exit(0);
		}
		String rootDir=args[0];
		String outDir=args[1];
		String clsName=args[2];
		String finishIdFile=args[3];
		String skipIdFile=null;
		
		int consumerCount=DEF_CONSUMER_COUNT;
		int queueSize=DEF_QUEUE_SIZE;
		int writerCount=DEF_WRITER_COUNT;
		if(line.hasOption("consumerCount")) {
			consumerCount=Integer.valueOf(line.getOptionValue("consumerCount"));
		}
		if(line.hasOption("queueSize")) {
			queueSize=Integer.valueOf(line.getOptionValue("queueSize"));
		}
		if(line.hasOption("writerCount")) {
			writerCount=Integer.valueOf(line.getOptionValue("writerCount"));
		}
		if(line.hasOption("skipIdFile")) {
			skipIdFile=line.getOptionValue("skipIdFile");
		}
		logger.info("rootDir: "+rootDir);
		logger.info("outDir: "+outDir);
		logger.info("finishIdFile: "+finishIdFile);
		logger.info("consumerCount: "+consumerCount);
		logger.info("queueSize: "+queueSize);
		logger.info("clsName: "+clsName);
		logger.info("skipIdFile: "+skipIdFile);
		Class cls=Class.forName(clsName);
		Constructor<ContentConsumer> constructor=cls.getConstructor(ArrayBlockingQueue.class, ArrayBlockingQueue.class);
		ArrayBlockingQueue<BaikeItem> taskQueue =new ArrayBlockingQueue<>(queueSize);
		ArrayBlockingQueue<ResultData> resQueue=new ArrayBlockingQueue<>(queueSize);
		File skipFile=(skipIdFile==null?null:new File(skipIdFile));
		ContentProducer producer=new ContentProducer(rootDir, taskQueue, skipFile);
		ContentConsumer[] consumers=new ContentConsumer[consumerCount];
		for(int i=0;i<consumers.length;i++) {
			consumers[i]=constructor.newInstance(taskQueue, resQueue);
			consumers[i].start();
		}
		
		ContentWriter writer=new ContentWriter(outDir, resQueue, writerCount, new File(finishIdFile));
		writer.start();
		
		producer.start();
		logger.info("producer started");
		producer.join();
		logger.info("producer finished");
		
		for(int i=0;i<consumers.length;i++) { 
			consumers[i].stopMe();
		}
		for(int i=0;i<consumers.length;i++) { 
			consumers[i].join();
		}
		logger.info("consumers stopped");
		writer.stopMe();
		writer.join();
		logger.info("all stopped");
		
	}

}
