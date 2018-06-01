package com.github.fancyerii.deepnlu.baikecrawler.contentcrawler;

import java.io.File;

public class CreateSubDataDirs {
	public static void main(String[] args) {
		if(args.length!=2) {
			System.out.println("need 2 arg: BAIKE_DIR subDirCount");
			System.exit(-1);
		}
		File dataDir=new File(args[0]);
		int count=Integer.valueOf(args[1]);
		if(!dataDir.exists() || !dataDir.isDirectory()) {
			System.out.println(args[0]+" not exist or not a dir");
			System.exit(-1);
		}
		
		File htmlDir=new File(dataDir.getAbsolutePath()+"/"+Constants.HTML_PATH);
		if(!htmlDir.exists()) {
			htmlDir.mkdir();
		}
		if(!htmlDir.isDirectory()) {
			System.out.println(htmlDir.getAbsolutePath()+" is not a dir");
			System.exit(-1);
		}
		
		for(int i=0;i<count;i++) {
			new File(htmlDir, i+"").mkdir();
		}
		
	}

}
