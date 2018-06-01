package com.github.fancyerii.deepnlu.baikecrawler.contentcrawler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter; 
 
import org.apache.log4j.Logger;
 
 

public class ImportTasksFromBaikeWapUrl {
	protected static Logger logger=Logger.getLogger(ImportTasksFromBaikeWapUrl.class);
	public static void main(String[] args)  throws Exception{
		if(args.length!=2){
			System.err.println("need 2 arg: urlFile outFile");
			System.exit(-1);
		} 
  
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(args[0]),"UTF8"));
		BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[1]),"UTF8"));
		String line;
		int lineNumber=0;  
		while((line=br.readLine())!=null){

			if(line.startsWith("http://wapbaike.baidu.com/")){
				lineNumber++;
				if(lineNumber%10000==0){
					logger.info("lineNumber: "+lineNumber);
				}
				String url=line.replace("wapbaike", "baike");
				bw.write(url+"\n");
			}
		}
		bw.close();
		br.close(); 
	}

}
