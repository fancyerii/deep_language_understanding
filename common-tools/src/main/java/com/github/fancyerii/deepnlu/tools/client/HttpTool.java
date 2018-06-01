package com.github.fancyerii.deepnlu.tools.client;

import org.apache.http.message.BasicNameValuePair;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.NameValuePair;
import java.util.List;
import org.apache.http.client.utils.URLEncodedUtils;
import java.util.Map;
import org.apache.http.client.fluent.Request;

public class HttpTool {
	private static int TIME_OUT;

	public static Request getPostReq(final String url) {
		return getPostReq(url, HttpTool.TIME_OUT, HttpTool.TIME_OUT);
	}

	public static Request getPostReq(final String url, final int connectTimeOut, final int socketTimeOut) {
		return Request.Post(url).connectTimeout(connectTimeOut).socketTimeout(socketTimeOut);
	}

	public static Request getGetReq(final String url) {
		return getGetReq(url, HttpTool.TIME_OUT, HttpTool.TIME_OUT);
	}

	public static Request getGetReq(final String url, final int connectTimeOut, final int socketTimeOut) {
		return Request.Get(url).connectTimeout(connectTimeOut).socketTimeout(socketTimeOut);
	}

	public static Request getDelReq(final String url) {
		return getDelReq(url, HttpTool.TIME_OUT, HttpTool.TIME_OUT);
	}

	public static Request getDelReq(final String url, final int connectTimeOut, final int socketTimeOut) {
		return Request.Delete(url).connectTimeout(connectTimeOut).socketTimeout(socketTimeOut);
	}

	public static Request getPutReq(final String url) {
		return getPutReq(url, HttpTool.TIME_OUT, HttpTool.TIME_OUT);
	}

	public static Request getPutReq(final String url, final int connectTimeOut, final int socketTimeOut) {
		return Request.Put(url).connectTimeout(connectTimeOut).socketTimeout(socketTimeOut);
	}

	public static String replaceVariableInPath(final String path, final String varName, final String varValue) {
		return path.replace("{" + varName + "}", varValue);
	}
	
	public static Map<String,String> buildParamMap(String key, String value){
		Map<String,String> map=new HashMap<>(1);
		map.put(key, value);
		return map;
	}
	
	public static Map<String,String> buildParamMap(String key1, String value1, String key2, String value2){
		Map<String,String> map=new HashMap<>(2);
		map.put(key1, value1);
		map.put(key2, value2);
		return map;
	}
	
	public static Map<String,String> buildParamMap(String key1, String value1, String key2, String value2,
			String key3, String value3){
		Map<String,String> map=new HashMap<>(3);
		map.put(key1, value1);
		map.put(key2, value2);
		map.put(key3, value3);
		return map;
	}
	
	public static String buildUrl(final String baseUrl, final Map<String, String> params) {
		final List<NameValuePair> nameValuePairs = buildParams(params);
		if (nameValuePairs.isEmpty()) {
			return baseUrl;
		}
		final StringBuilder query = new StringBuilder();
		query.append(baseUrl);
		final String s = URLEncodedUtils.format(nameValuePairs, "UTF-8");
		query.append("?").append(s);
		return query.toString();
	}

	private static List<NameValuePair> buildParams(final Map<String, String> params) {
		if (params==null) {
			return new ArrayList<NameValuePair>(0);
		}
		final List<NameValuePair> paramList = new ArrayList<>();
		for (final Map.Entry<String, String> entry : params.entrySet()) {
			paramList.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}
		return (List<NameValuePair>) paramList;
	}

	static {
		HttpTool.TIME_OUT = 3000;
	}
}
