package com.github.fancyerii.deepnlu.weathercrawler.history;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.antbrains.httpclientfetcher.HttpClientFetcher;
import com.antbrains.nekohtmlparser.NekoHtmlParser;
import com.google.gson.Gson;

public class Extractor extends Thread{
	protected static Logger logger=Logger.getLogger(Extractor.class);
	
	private HttpClientFetcher fetcher;
	
	private BlockingQueue<CrawlTask> taskQueue;
	private BlockingQueue<CrawlTask> newTaskQueue;
	private BlockingQueue<ExtractResult> resQueue;
	
	private long interval;
	
	private Pattern cityNamePtn;
	private static Pattern datePtn;
	private Pattern ymdPtn;
	private static final String[] detailHeaders=new String[] {
			"日期",
			"最高气温",
			"最低气温",
			"天气", 
			"风向", 
			"风力"
	};
	private volatile boolean bStop;
	public void stopMe() {
		bStop=true;
	}
	
	public Extractor( HttpClientFetcher fetcher,BlockingQueue<CrawlTask> taskQueue,
			BlockingQueue<CrawlTask> newTaskQueue, BlockingQueue<ExtractResult> resQueue,
			long interval) {
		this.fetcher=fetcher;
		this.taskQueue=taskQueue;
		this.newTaskQueue=newTaskQueue;
		this.resQueue=resQueue;
		this.interval=interval;
		cityNamePtn=this.getPattern("http://lishi.tianqi.com/", "/index.html", "([^/]+)");
		datePtn=this.getPattern("http://lishi.tianqi.com/", ".html", "([^/]+)/(\\d{6})");
		ymdPtn=Pattern.compile("([\\d]+)-([\\d]+)-([\\d]+)");
	}
	
	@Override
	public void run() {
		while(!bStop) {
			try {
				CrawlTask task=taskQueue.poll(100, TimeUnit.MILLISECONDS);
				if(task==null) continue;
				long startTime=System.currentTimeMillis();
				this.processTask(task);
				long timeUsed=System.currentTimeMillis()-startTime;
				if(interval>timeUsed) {
					Thread.sleep(interval-timeUsed);
				}
			} catch (InterruptedException e) {
				
			}
			
		}
		while(true) {
			try {
				CrawlTask task=taskQueue.poll(100, TimeUnit.MILLISECONDS);
				if(task==null) break;
				long startTime=System.currentTimeMillis();
				this.processTask(task);
				long timeUsed=System.currentTimeMillis()-startTime;
				if(interval>timeUsed) {
					Thread.sleep(interval-timeUsed);
				}
			} catch (InterruptedException e) {
				
			}
		}
	}
	
	private void processTask(CrawlTask task) {
		logger.debug("process: "+task.getUrl());
		ExtractResult res=this.extract(task.getUrl());
		if(res==null) {
			logger.warn("fail: "+task.getUrl());
			return;
		}
		
		List<String> links = res.getLinks();
		if(links!=null) {
			for(String link:links) {
				CrawlTask ct=new CrawlTask(link);
				ct.setDepth(task.getDepth()+1);
				try {
					this.newTaskQueue.put(ct);
				} catch (InterruptedException e) {
					logger.error(e.getMessage());
				}
			}
			logger.debug("addLinks: "+links.size());
			if(links.size()>0) {
				logger.debug("first link: "+links.iterator().next());
			}
		}
		WeatherInfo[] infos=res.getInfos();
		if(infos!=null) {
			try {
				this.resQueue.put(res);
			} catch (InterruptedException e) {
				logger.error(e.getMessage());
			}
			logger.debug("addInfo: "+infos.length);
		}
	}
	
	private ExtractResult extract(String url) {
		String html=null;
		try {
			html=fetcher.httpGet(url, 3);
		} catch (Exception e) {
			logger.warn(e.getMessage());
			return null;
		}
		if(html==null) return null;
		NekoHtmlParser parser=new NekoHtmlParser();
		try {
			parser.load(html, StandardCharsets.UTF_8.name());
		} catch (Exception e) {
			logger.warn(e.getMessage());
			return null;
		}
		
		if(url.equals("http://lishi.tianqi.com/")||url.equals("http://lishi.tianqi.com")) {
			return this.extractCityListPage(parser, url);
		}
		String cityPy=this.getCityPy(url);
		if(cityPy!=null) {
			return this.extractDatePage(parser, url, cityPy);
		}
		String[] cityPyAndDate=getCityAndDate(url);
		if(cityPyAndDate!=null) {
			return this.extractDetailPage(parser, url, cityPyAndDate[0], cityPyAndDate[1]);
		}
		
		return null;
	}
	
	private Pattern getPattern(String left, String right, String matchReg) {
		Pattern ptn=Pattern.compile(Pattern.quote(left)+matchReg+Pattern.quote(right));
		return ptn;
	}
	private String getCityPy(String href) {
		Matcher m=this.cityNamePtn.matcher(href);
		if(m.matches()) {
			return m.group(1);
		}
		return null;
	}
	
	public static String[] getCityAndDate(String href) {
		Matcher m=datePtn.matcher(href);
		if(m.matches()) {
			return new String[] {m.group(1), m.group(2)};
		}
		return null;
	}
	
	private ArrayList<String> getLis(Node ul, NekoHtmlParser parser){
		NodeList lis=parser.selectNodes("./LI", ul);
		ArrayList<String> result=new ArrayList<>(lis.getLength());
		for(int i=0;i<lis.getLength();i++) {
			result.add(lis.item(i).getTextContent().trim());
		}
		
		return result;
	}
	
	private int[] parseYMD(String date) {
		Matcher m=this.ymdPtn.matcher(date);
		if(m.matches()) {
			return new int[] {Integer.valueOf(m.group(1)),
					Integer.valueOf(m.group(2)),
					Integer.valueOf(m.group(3))
			};
		}
		return null;
	}
	
	private ExtractResult extractDetailPage(NekoHtmlParser parser, String url, String cityPy, String yearMonth) {
		int year=Integer.valueOf(yearMonth.substring(0, 4));
		int month=Integer.valueOf(yearMonth.substring(4,6));
		ExtractResult er=new ExtractResult();
		er.setYear(year);
		er.setMonth(month);
		String h1=parser.getNodeText("//H1").trim();
		int idx=h1.indexOf(year+"年");
		if(idx==-1|| !h1.contains("月份天气")) {
			logger.warn("bad h1: "+h1+", url: "+url);
			return null;
		}
		String cityName=h1.substring(0,idx);
		er.setCityCn(cityName);
		er.setCity(cityPy);
		
		NodeList uls=parser.selectNodes("//DIV[@class='tqtongji2']/UL");
		if(uls.getLength()<10) {
			logger.warn("bad ul: "+url);
			return null;
		}
		Node ul0=uls.item(0);
		ArrayList<String> headers=this.getLis(ul0, parser);
		if(headers.size()!=detailHeaders.length) {
			logger.warn("badhead: "+url);
			return null;
		}
		for(int i=0;i<headers.size();i++) {
			if(!headers.get(i).equals(detailHeaders[i])) {
				logger.warn("badhead2: "+url);
				return null;
			}
		}

		ArrayList<WeatherInfo> infos=new ArrayList<>(uls.getLength()-1);
		for(int i=1;i<uls.getLength();i++) {
			Node ul=uls.item(i);
			ArrayList<String> values=this.getLis(ul, parser);
			if(values.size()!=detailHeaders.length) {
				logger.warn("badValues: "+url);
				continue;
			}
			WeatherInfo info=new WeatherInfo();
			info.setYear(year);
			info.setMonth(month);
			int[] ymd=this.parseYMD(values.get(0));
			if(ymd==null || ymd[0]!=year||ymd[1]!=month) {
				logger.warn("badValue: "+values.get(0)+", url: "+url);
				continue;
			}
			info.setDay(ymd[2]);
			try {
				info.setHighest(Integer.valueOf(values.get(1)));
			}catch(NumberFormatException e) {
				logger.warn("badHighest: "+values.get(1)+", url: "+url);
			}
			try {
				info.setLowest(Integer.valueOf(values.get(2)));
			}catch(NumberFormatException e) {
				logger.warn("badLowest: "+values.get(2)+", url: "+url);
			}
			info.setWeatherType(values.get(3));
			info.setWindDirection(values.get(4));
			info.setWindPower(values.get(5));
			infos.add(info);
			
		}
		er.setInfos(infos.toArray(new WeatherInfo[0]));
		return er;
	}
	
	private ExtractResult extractDatePage(NekoHtmlParser parser, String url, String cityPy) {
		NodeList nodes=parser.selectNodes("//DIV[@class='tqtongji1']/UL/LI/A");
		ExtractResult er=new ExtractResult();
		List<String> links=new ArrayList<>(nodes.getLength());
		er.setLinks(links);
		for(int i=0;i<nodes.getLength();i++) {
			Node node=nodes.item(i);
			String href=parser.getNodeText("./@href", node);
			//http://lishi.tianqi.com/beijing/201402.html
			String date=node.getTextContent().trim();
			String[] cityPyAndDate=getCityAndDate(href);
			if(cityPyAndDate==null) {
				logger.warn("bad cityPyAndDate: "+href+" url: "+url);
				continue;
			}
			if(!cityPyAndDate[0].equals(cityPy)) {
				logger.warn("misMatch: "+url);
				continue;
			}
			
			links.add(href);
			
		}
		return er;
	}
	
	private ExtractResult extractCityListPage(NekoHtmlParser parser, String url) {
		NodeList nodes=parser.selectNodes("//DIV[@id='tool_site']//LI/A");
		ExtractResult er=new ExtractResult();
		List<String> links=new ArrayList<>(nodes.getLength());
		er.setLinks(links);
		for(int i=0;i<nodes.getLength();i++) {
			Node node=nodes.item(i);
			String href=parser.getNodeText("./@href", node);
			if("#".equals(href)) continue;
			String cityName=node.getTextContent().trim();
			String cityPy=this.getCityPy(href);
			if(cityPy==null) {
				logger.warn("no cityPy: "+href+", url: "+url);
				continue;
			}
			links.add(href);
			
		}
		return er;
	}
	
	public static void main(String[] args) {
		HttpClientFetcher fetcher=new HttpClientFetcher("");
		fetcher.init();
		Gson gson=new Gson();
		{
			Extractor ext=new Extractor(fetcher,null,null,null,2000);
			ExtractResult er=ext.extract("http://lishi.tianqi.com/anqing/201208.html");		
			System.out.println(gson.toJson(er));	
			
		}
		{
			Extractor ext=new Extractor(fetcher,null,null,null,2000);
			ExtractResult er=ext.extract("http://lishi.tianqi.com/");		
			System.out.println(gson.toJson(er));		
		}
		
		{
			Extractor ext=new Extractor(fetcher,null,null,null,2000);
			ExtractResult er=ext.extract("http://lishi.tianqi.com/zhenxiong/index.html");		
			System.out.println(gson.toJson(er));		
		}
		{
			Extractor ext=new Extractor(fetcher,null,null,null,2000);
			ExtractResult er=ext.extract("http://lishi.tianqi.com/zhuhai/index.html");		
			System.out.println(gson.toJson(er));		
		}
		{
			Extractor ext=new Extractor(fetcher,null,null,null,2000);
			ExtractResult er=ext.extract("http://lishi.tianqi.com/beijing/201502.html");		
			System.out.println(gson.toJson(er));		
			
		}
		fetcher.close();
	}
}
