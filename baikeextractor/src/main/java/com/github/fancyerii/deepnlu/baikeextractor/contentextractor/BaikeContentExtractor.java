package com.github.fancyerii.deepnlu.baikeextractor.contentextractor;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.antbrains.httpclientfetcher.HttpClientFetcher;
import com.antbrains.nekohtmlparser.NekoHtmlParser; 
import com.google.gson.Gson;

public class BaikeContentExtractor {
	protected static Logger logger=Logger.getLogger(BaikeContentExtractor.class);
	
	public BaikeContentExtResult extract(String html) {
		BaikeContentExtResult res=new BaikeContentExtResult();
		NekoHtmlParser parser=new NekoHtmlParser();
		try {
			parser.load(html, StandardCharsets.UTF_8.name());
		} catch (Exception e) {
			res.setSucc(false);
			res.setFailReason(BaikeContentExtResult.FAIL_PARSE);
			return res;
		}
		
		Node ddTitle=parser.selectSingleNode("//DD[@class='lemmaWgt-lemmaTitle-title']");
		if(ddTitle==null) {
			res.setSucc(false);
			res.setFailReason(BaikeContentExtResult.FAIL_NO_ITEM);
			return res;
		}
		
		String h1=parser.getNodeText(".//H1", ddTitle).trim();
		if(h1.isEmpty()) {
			res.setSucc(false);
			res.setFailReason(BaikeContentExtResult.FAIL_NO_ITEM);
			return res;
		}
		res.setItem(h1);
		
		String h2=parser.getNodeText(".//H2", ddTitle).trim();
		if(!h2.isEmpty()) {
			res.setDisambStr(h2);
		}
		
		String py=parser.getNodeText("./SPAN[@class='lemma-pinyin']/SPAN[@class='text']", ddTitle).trim();
		if(!py.isEmpty()) {
			res.setPinyin(py);
		}
		
		Node infoboxNode=parser.selectSingleNode("//DIV[contains(@class,'basic-info')]"); 
		if(infoboxNode!=null) {
			NodeList keyList=parser.selectNodes(".//DT", infoboxNode);
			NodeList valueList=parser.selectNodes(".//DD", infoboxNode);
			if(keyList.getLength()!=valueList.getLength()) {
				res.getWarningMsgs().add("INFO_BOX_KEY<>VALUE");
			}else if(keyList.getLength()==0){
				res.getWarningMsgs().add("INFO_BOX_EMPTY");
			}else {
				for(int i=0;i<keyList.getLength();i++) {
					Node dt=keyList.item(i);
					Node dd=valueList.item(i);
					String key=dt.getTextContent().trim().replaceAll("[\\s ]", "");
					NodeList nodes=dd.getChildNodes();
					List<Object> textOrLinks=new ArrayList<>(nodes.getLength());
					for(int j=0;j<nodes.getLength();j++) {
						Node n=nodes.item(j);
						if(n.getNodeType()==Node.TEXT_NODE) {
							String text=n.getTextContent().trim();
							if(!text.isEmpty()) {
								textOrLinks.add(text);
							}
						}else if(n.getNodeType()==Node.ELEMENT_NODE){
							Element e=(Element) n;
							if(!"a".equalsIgnoreCase(e.getTagName())){
								//res.getWarningMsgs().add("INFO_BOX_BAD_ELEMENT");
							}else {
								String href=e.getAttribute("href");
								if(!href.startsWith("/item/")) {
									//res.getWarningMsgs().add("INFO_BOX_BAD_LINK_HREF");
								}else {
									ItemLink link=new ItemLink(e.getTextContent().trim(), href);
									textOrLinks.add(link);
								}
							}
						}else {
							res.getWarningMsgs().add("INFO_BOX_OTHER_NODE_TYPE");
						}
					}
					StringBuilder sb=new StringBuilder("");
					int linkCount=0;
					int textCount=0;
					for(Object o:textOrLinks) {
						if(o instanceof String) {
							sb.append((String)o);
							textCount++;
						}else {
							ItemLink link=(ItemLink)o;
							sb.append(link.getWord());
							linkCount++;
						}
					}
					if(linkCount==0) {
						res.getInfoboxTextAttrs().put(key, sb.toString());
					}else if(textCount==0 && linkCount==1) {
						res.getInfoboxLinkAttrs().put(key, (ItemLink)textOrLinks.get(0));
					}else{
						res.getInfoboxMixAttrs().put(key, new TextWithLinks(sb.toString(), textOrLinks.toArray(new Object[0])));
					}
				}

			}
		}else {
			res.getWarningMsgs().add("NO_INFO_BOX");
		}
		
		NodeList tags=parser.selectNodes("//DD[@id='open-tag-item']/SPAN");
		if(tags==null||tags.getLength()==0) {
			res.getWarningMsgs().add("NO_TAGS");
		}else {
			String[] tagArr=new String[tags.getLength()];
			res.setTags(tagArr);
			for(int i=0;i<tags.getLength();i++) {
				String tag=tags.item(i).getTextContent().trim();
				tagArr[i]=tag;
			}
		}
		res.setSucc(true);
		return res;
	}
	
	public static void main(String[] args) throws Exception {
		HttpClientFetcher fetcher = new HttpClientFetcher("");
        fetcher.init();
        BaikeContentExtractor ext=new BaikeContentExtractor();
        String[] urls=new String[]{
        		"http://baike.baidu.com/subview/33503/33503.htm",
                "http://baike.baidu.com/view/110949.htm",
                "http://baike.baidu.com/subview/1758/18233157.htm",
                "http://baike.baidu.com/view/8696329.htm",
                "https://baike.baidu.com/item/%E5%A8%83%E5%A8%83%E5%B1%8B"
        };
        Gson gson=new Gson();
        for(String url:urls){
        	System.out.println(url);
            String s = fetcher.httpGet(url, "UTF-8");
            BaikeContentExtResult res=ext.extract(s);
            System.out.println(gson.toJson(res));
        }

        fetcher.close();
	}
}
