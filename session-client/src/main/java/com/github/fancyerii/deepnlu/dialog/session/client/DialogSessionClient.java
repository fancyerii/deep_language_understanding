package com.github.fancyerii.deepnlu.dialog.session.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.http.entity.ContentType;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.fancyerii.deepnlu.dialog.session.api.APIPathAndParams;
import com.github.fancyerii.deepnlu.dialog.session.data.DialogSession;
import com.github.fancyerii.deepnlu.tools.api.ApiResponse;
import com.github.fancyerii.deepnlu.tools.client.HttpTool;
import com.github.fancyerii.deepnlu.tools.client.PathReplacer;
import com.github.fancyerii.deepnlu.tools.json.JsonUtil;

public class DialogSessionClient {

    private String serverUrl = APIPathAndParams.DEFAULT_SERVER;
    private int connectTimeOut = 200;
    private int socketTimeOut = 500;

    public DialogSessionClient() {
    }

    public DialogSessionClient(String serverUrl) {
        this.serverUrl = serverUrl;
    }
    
    public DialogSessionClient(int port) {
    	this.serverUrl=APIPathAndParams.DEFAULT_SERVER+":"+port;
    }

    private String getSessionSrvPath(String robotId, String userId) {
        String path = serverUrl + APIPathAndParams.PATH_ROOT;
        return PathReplacer.replaceVariableInPath(path, APIPathAndParams.PARAM_ROBOT_ID, robotId,
                APIPathAndParams.PARAM_USER_ID, userId);
    }
 
    public ApiResponse<DialogSession> getOrCreateDialogSession(String robotId, String userId) {
        String url = getSessionSrvPath(robotId, userId) + APIPathAndParams.PATH_SESSION;
        try {
            String result = HttpTool.getGetReq(url, connectTimeOut, socketTimeOut).execute().returnContent()
                    .asString(StandardCharsets.UTF_8);
            TypeReference<ApiResponse<DialogSession>> typeRef 
            = new TypeReference<ApiResponse<DialogSession>>() {};
            return JsonUtil.toBean(result, typeRef);
 
        } catch (IOException e) {
            return ApiResponse.buildClientFailMessage(e.getMessage());
        }
    }
 
    public ApiResponse<String> getDialogSessionId(String robotId, String userId) {
        String url = getSessionSrvPath(robotId, userId) +APIPathAndParams.PATH_SESSION_ID;
        try {
            String result = HttpTool.getGetReq(url, connectTimeOut, socketTimeOut).execute().returnContent()
                    .asString(StandardCharsets.UTF_8);
            return JsonUtil.toBean(result, new TypeReference<ApiResponse<String>>() {
            });
            
        } catch (IOException e) {
        	return ApiResponse.buildClientFailMessage(e.getMessage());
        } 
    }
 
    public ApiResponse<Void> delDialogSession(String robotId, String userId) {
        String url = getSessionSrvPath(robotId, userId) + APIPathAndParams.PATH_SESSION;
        try {
            String s = HttpTool.getDelReq(url, connectTimeOut, socketTimeOut).execute().returnContent()
                    .asString(StandardCharsets.UTF_8);
            return JsonUtil.toBean(s, new TypeReference<ApiResponse<Void>>() {
            });
        } catch (IOException e) {
        	return ApiResponse.buildClientFailMessage(e.getMessage());
        } 
    }
 
    public ApiResponse<Void> saveDialogSession(String robotId, String userId, DialogSession session) {
    	
        String url = getSessionSrvPath(robotId, userId) + APIPathAndParams.PATH_SESSION;
        try {
            String s = HttpTool.getPostReq(url, connectTimeOut, socketTimeOut)
                    .bodyString(JsonUtil.toJsonString(session), ContentType.APPLICATION_JSON).execute().returnContent()
                    .asString(StandardCharsets.UTF_8);
            return JsonUtil.toBean(s, new TypeReference<ApiResponse<Void>>() {
            });
        } catch (IOException e) {
        	return ApiResponse.buildClientFailMessage(e.getMessage());
        } 
    }
    
    public static void main(String[] args) {
    	DialogSessionClient dsClient=new DialogSessionClient();
    	String robotId="_test_robot_";
    	String userId="_test_user_";
    	ApiResponse<DialogSession> resp=dsClient.getOrCreateDialogSession(robotId, userId);
    	if(!resp.isSucc()) {
    		System.err.println(resp.getErrorMsg());
    		return;
    	}
    	 
    	DialogSession sess=resp.getData();
    	sess.getSessionAttrs().put("testKey", "testValue");
    	ApiResponse<Void> resp2=dsClient.saveDialogSession(robotId, userId, sess);
    	if(!resp2.isSucc()) {
    		System.err.println(resp2.getErrorMsg());
    		return;
    	}
    	
    	ApiResponse<Void> resp3=dsClient.delDialogSession(robotId, userId);
    	if(!resp3.isSucc()) {
    		System.err.println(resp3.getErrorMsg());
    		return;
    	}
    	
    	ApiResponse<String> resp4=dsClient.getDialogSessionId(robotId, userId);
    	if(!resp4.isSucc()) {
    		System.err.println(resp4.getErrorMsg());
    		return;
    	}
    	
    	if(resp4.getData()!=null) {
    		System.err.println("id not null: "+resp4.getData());
    	}
    }
}
