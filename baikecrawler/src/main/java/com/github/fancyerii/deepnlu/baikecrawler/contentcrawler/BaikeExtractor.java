package com.github.fancyerii.deepnlu.baikecrawler.contentcrawler;
 
import java.util.ArrayList; 
import java.util.List;

import org.apache.log4j.Logger; 

import com.antbrains.httpclientfetcher.HttpClientFetcher;
import com.antbrains.nekohtmlparser.NekoHtmlParser; 
import com.github.fancyerii.deepnlu.baikecrawler.utils.UrlUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class BaikeExtractor{
    protected static Logger logger = Logger.getLogger(BaikeExtractor.class); 
    private JsonParser parser=new JsonParser();
    public static final String FAIL_LOAD = "fail_load";
    public static final String FAIL_NOQUESTION = "fail_no_q";

    public static final String FAIL_REASON_NETWORK = "fail_network";
    public static final String FAIL_REASON_EXTRACT_DELETED = "fail_deleted";
    public static final String FAIL_REASON_EXTRACT_PARSE = "fail_parse";
    public static final String FAIL_REASON_EXTRACT_LOAD = "fail_load";
    public static final String FAIL_REASON_BADURL = "fail_badurl";
    // public static final String FAIL_CONTENT_SIZE="fail_content_size";
    public static final ExtractResult failLoad = new ExtractResult(null, FAIL_REASON_EXTRACT_LOAD);
    private static final ExtractResult failDeleted = new ExtractResult(null, FAIL_REASON_EXTRACT_DELETED);
    private static final ExtractResult failParse = new ExtractResult(null, FAIL_REASON_EXTRACT_PARSE);

    private Gson gson=new Gson();
    public String[] extractNewLemmaIdEnc(String html){
        NekoHtmlParser parser = new NekoHtmlParser();
        try {
            parser.load(html, "UTF8");
        } catch (Exception e) {
            return new String[]{null, FAIL_REASON_EXTRACT_LOAD};
        }
        
        
        int idx=html.indexOf("newLemmaIdEnc:\"");
        if(idx==-1){
            return new String[]{null, "newLemmaIdEnc"};
        }
        int endIdx=html.indexOf("\"", idx+"newLemmaIdEnc:\"".length()+2);
        
        String newLemmaIdEnc=html.substring(idx+"newLemmaIdEnc:\"".length(), endIdx);
        return new String[]{newLemmaIdEnc, null};
    }
    public ExtractResult extract(String url, String html, List<String> synUrls) {
        NekoHtmlParser parser = new NekoHtmlParser();
        try {
            parser.load(html, "UTF8");
        } catch (Exception e) {
            return failLoad;
        }
        String errorH1=parser.getNodeText("//H1[@class='baikeLogo']");
        if(errorH1.contains("百度百科错误")) {
        	return failDeleted;
        }
        BaikeItem item = new BaikeItem(); 
        item.setUrl(url);
        int idx = html.indexOf("nslog().setGlobal(");
        if (idx != -1) {
            boolean res=this.extractFormat1(html, idx+"nslog().setGlobal(".length(), item);
            if(!res) return failParse;
        } else {
            idx = html.indexOf("var list=new AlbumList(");
            if (idx != -1) {
                boolean res= this.extractFormat2(html, idx+"var list=new AlbumList(".length(), item);
                if(!res) return failParse;
            } else {
                List<String> urls=this.extractSyns(parser, url);
                if(urls!=null && !urls.isEmpty()){
                	synUrls.addAll(urls);
                }else{
                    return failParse;
                }
            }
        }
        item.setHtml(html);
        return new ExtractResult(item, null);
    }
    
    private List<String> extractSyns(NekoHtmlParser parser, String baseUrl){
        Node n=parser.selectSingleNode("//DIV[@class='main-content']/DIV[@class='lemmaWgt-subLemmaListTitle']");
        if(n==null) return null;
        NodeList as=parser.selectNodes("//DIV[@class='main-content']/UL/LI//A");
        ArrayList<String> urls=new ArrayList<>(as.getLength());
        for(int i=0;i<as.getLength();i++){
            Node node=as.item(i);
            String href=parser.getNodeText("./@href", node);
            String url=UrlUtils.getAbsoluteUrl(baseUrl, href);
            if(url!=null){
                urls.add(url);
            }
        }
        return urls;
    }

    private boolean extractFormat1(String html, int startIdx, BaikeItem item) {
        int endIdx=html.indexOf("});", startIdx);
        if(endIdx==-1) return false;
        String json=html.substring(startIdx, endIdx+1);
        try{
            JsonObject jo=parser.parse(json).getAsJsonObject();
            item.setNewLemmaId(jo.get("newLemmaId").getAsString());
            item.setLemmaId(jo.get("lemmaId").getAsString());
            item.setSubLemmaId(jo.get("subLemmaId").getAsString());
            item.setItem(jo.get("lemmaTitle").getAsString());
        }catch(Exception e){
            logger.warn("badJson1: "+json);
            return false;
        }
        return true;
    }

    private boolean extractFormat2(String html, int startIdx, BaikeItem item) {
        int endIdx=html.indexOf("});", startIdx);
        if(endIdx==-1) return false;
        String json=html.substring(startIdx, endIdx+1);
        try{
            JsonObject jo=parser.parse(json).getAsJsonObject();
            item.setNewLemmaId(jo.get("newLemmaId").getAsString());
            item.setLemmaId(jo.get("lemmaId").getAsString());
            item.setSubLemmaId(jo.get("subLemmaId").getAsString());
            item.setItem(jo.get("lemmaTitle").getAsString());
        }catch(Exception e){
            logger.warn("badJson2: "+json);
            return false;
        }
        return true;
    }

    public static void main(String[] args) throws Exception {
        HttpClientFetcher fetcher = new HttpClientFetcher("");
        fetcher.init();
        String[] urls=new String[]{
        		"http://baike.baidu.com/subview/33503/33503.htm",
                "http://baike.baidu.com/view/110949.htm",
                "http://baike.baidu.com/subview/1758/18233157.htm",
                "http://baike.baidu.com/view/8696329.htm",
                "https://baike.baidu.com/item/%E5%A8%83%E5%A8%83%E5%B1%8B"
        };
        
        for(String url:urls){
        	System.out.println(url);
            String s = fetcher.httpGet(url, "UTF-8");
            BaikeExtractor ext = new BaikeExtractor();
            ExtractResult res = ext.extract(url, s, new ArrayList<>());
            if(res.getFailReason()!=null) {
            	System.out.println("fail: "+res.getFailReason());
            	continue;
            }
            Gson gson=new Gson();
            res.getItem().setHtml(null);
            System.out.println(gson.toJson(res));
        }

        fetcher.close();
    }

}
