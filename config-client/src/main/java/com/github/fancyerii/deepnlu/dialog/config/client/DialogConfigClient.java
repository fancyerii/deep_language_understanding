package com.github.fancyerii.deepnlu.dialog.config.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.http.entity.ContentType;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.fancyerii.deepnlu.dialog.config.api.APIPathAndParams;
import com.github.fancyerii.deepnlu.dialog.config.data.RobotConfig;
import com.github.fancyerii.deepnlu.tools.api.ApiResponse;
import com.github.fancyerii.deepnlu.tools.client.HttpTool;
import com.github.fancyerii.deepnlu.tools.client.PathReplacer;
import com.github.fancyerii.deepnlu.tools.json.JsonUtil;

public class DialogConfigClient {

    private String serverUrl = APIPathAndParams.DEFAULT_SERVER;
    private int connectTimeOut = 200;
    private int socketTimeOut = 500;

    public DialogConfigClient() {
    }

    public DialogConfigClient(String serverUrl) {
        this.serverUrl = serverUrl;
    }
    
    public DialogConfigClient(int port) {
    	this.serverUrl=APIPathAndParams.DEFAULT_SERVER+":"+port;
    }

    private String getConfigSrvPath(String robotId) {
        String path = serverUrl + APIPathAndParams.PATH_ROOT;
        return PathReplacer.replaceVariableInPath(path, APIPathAndParams.PARAM_ROBOT_ID, robotId);
    } 
    
    public ApiResponse<RobotConfig> getConfig(String robotId, String type, String key){
        String url = getConfigSrvPath(robotId) + APIPathAndParams.PATH_CONFIG;
        url=HttpTool.buildUrl(url, HttpTool.buildParamMap(APIPathAndParams.PARAM_TYPE, type,
        		APIPathAndParams.PARAM_KEY, key));
        try {
            String s = HttpTool.getGetReq(url, connectTimeOut, socketTimeOut).execute().returnContent()
                    .asString(StandardCharsets.UTF_8);
            return JsonUtil.toBean(s, new TypeReference<ApiResponse<RobotConfig>>() {
            });
        } catch (IOException e) {
        	return ApiResponse.buildClientFailMessage(e.getMessage());
        } 
    }
    
    public ApiResponse<List<RobotConfig>> getConfigs(String robotId, String type){
        String url = getConfigSrvPath(robotId) + APIPathAndParams.PATH_CONFIGS;
        url=HttpTool.buildUrl(url, HttpTool.buildParamMap(APIPathAndParams.PARAM_TYPE, type));
        try {
            String s = HttpTool.getGetReq(url, connectTimeOut, socketTimeOut).execute().returnContent()
                    .asString(StandardCharsets.UTF_8);
            return JsonUtil.toBean(s, new TypeReference<ApiResponse<List<RobotConfig>>>() {
            });
        } catch (IOException e) {
        	return ApiResponse.buildClientFailMessage(e.getMessage());
        } 
    }
    
    public ApiResponse<Void> upsertConfig(RobotConfig cfg){
        String url = getConfigSrvPath(cfg.getRobotId()) + APIPathAndParams.PATH_CONFIG; 
        try {
            String s = HttpTool.getPostReq(url, connectTimeOut, socketTimeOut)
                    .bodyString(JsonUtil.toJsonString(cfg), ContentType.APPLICATION_JSON).execute().returnContent()
                    .asString(StandardCharsets.UTF_8);
            return JsonUtil.toBean(s, new TypeReference<ApiResponse<Void>>() {
            });
        } catch (IOException e) {
        	return ApiResponse.buildClientFailMessage(e.getMessage());
        } 
    }
    
    public ApiResponse<Void> insertConfig(RobotConfig cfg){
        String url = getConfigSrvPath(cfg.getRobotId()) + APIPathAndParams.PATH_CONFIG; 
        try {
            String s = HttpTool.getPutReq(url, connectTimeOut, socketTimeOut)
                    .bodyString(JsonUtil.toJsonString(cfg), ContentType.APPLICATION_JSON).execute().returnContent()
                    .asString(StandardCharsets.UTF_8);
            return JsonUtil.toBean(s, new TypeReference<ApiResponse<Void>>() {
            });
        } catch (IOException e) {
        	return ApiResponse.buildClientFailMessage(e.getMessage());
        } 
    }
 
    public ApiResponse<Void> delConfig(String robotId, String type, String key) {
        String url = getConfigSrvPath(robotId) + APIPathAndParams.PATH_CONFIG;
        url=HttpTool.buildUrl(url, HttpTool.buildParamMap(APIPathAndParams.PARAM_TYPE, type,
        		APIPathAndParams.PARAM_KEY, key));
        try {
            String s = HttpTool.getDelReq(url, connectTimeOut, socketTimeOut).execute().returnContent()
                    .asString(StandardCharsets.UTF_8);
            return JsonUtil.toBean(s, new TypeReference<ApiResponse<Void>>() {
            });
        } catch (IOException e) {
        	return ApiResponse.buildClientFailMessage(e.getMessage());
        } 
    }
  
    public static void main(String[] args) { 
    	DialogConfigClient client=new DialogConfigClient();
    	
    	String robotId="_test_robot_";
    	String type="_test_type_";
    	String key="_test_key_";
    	String key2="_test_key2_";
    	ApiResponse<RobotConfig> resp1=client.getConfig(robotId, type, key);
    	if(!resp1.isSucc()) {
    		System.err.println(resp1.getErrorMsg());
    		return;
    	}else {
    		if(resp1.getData()!=null) {
    			System.err.println("should null");
    			return;
    		}
    	}
    	
    	RobotConfig cfg=new RobotConfig(robotId, type, key, "{'age':30,'gender':'male'}");
    	ApiResponse<Void> resp=client.insertConfig(cfg);
    	if(!resp.isSucc()) {
    		System.err.println(resp.getErrorMsg());
    		return;
    	}
    	resp=client.insertConfig(cfg);
    	System.out.println(resp.getErrorMsg());
    	
    	cfg.setCfgJson("{'age':40}");
    	resp=client.upsertConfig(cfg);
    	if(!resp.isSucc()) {
    		System.err.println(resp.getErrorMsg());
    		return;
    	}
    	
    	RobotConfig cfg2=new RobotConfig(robotId, type, key2, "[1,2,3]");
    	resp=client.insertConfig(cfg2);
    	if(!resp.isSucc()) {
    		System.err.println(resp.getErrorMsg());
    		return;
    	}
    	
    	ApiResponse<List<RobotConfig>> respList=client.getConfigs(robotId, type);
    	if(!respList.isSucc()) {
    		System.err.println(respList.getErrorMsg());
    		return;
    	}
    	System.out.println(respList.getData().size());
    	
    	RobotConfig c=client.getConfig(robotId, type, key).getData();
    	System.out.println(JsonUtil.toJsonStringWithoutException(c));
    	
    	resp=client.delConfig(robotId, type, key);
    	if(!resp.isSucc()) {
    		System.err.println(resp.getErrorMsg());
    		return;
    	}
    	resp=client.delConfig(robotId, type, key2);
    	if(!resp.isSucc()) {
    		System.err.println(resp.getErrorMsg());
    		return;
    	}
    }
}
