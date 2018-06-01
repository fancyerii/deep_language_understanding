package com.github.fancyerii.deepnlu.dialog.session.rest;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.github.fancyerii.deepnlu.dialog.session.api.APIPathAndParams;
import com.github.fancyerii.deepnlu.dialog.session.data.DialogSession;
import com.github.fancyerii.deepnlu.dialog.session.service.DialogSessionService;
import com.github.fancyerii.deepnlu.tools.api.ApiResponse;
import com.github.fancyerii.deepnlu.tools.common.StringTools;

@Slf4j
@RestController
@RequestMapping(APIPathAndParams.PATH_ROOT)
public class DialogSessionController {
    @Autowired
    private DialogSessionService dialogSessionService;

    @RequestMapping(value = APIPathAndParams.PATH_SESSION, method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<DialogSession> getSession(@PathVariable(APIPathAndParams.PARAM_ROBOT_ID) String robotId,
    		@PathVariable(APIPathAndParams.PARAM_USER_ID) String userId) {
    	log.debug("getSession robotId[{}], userId[{}]", robotId, userId);
    	@SuppressWarnings("unchecked")
		ApiResponse<DialogSession> checkResult=this.checkRobotIdAndUserId(robotId, userId);
    	if(checkResult!=null) return checkResult;
    	try {
			DialogSession session = dialogSessionService.getOrCreateSession(robotId, userId);
			return ApiResponse.buildSuccess(session);
		} catch (IOException e) { 
			log.error(e.getMessage(), e);
			return ApiResponse.buildServerFailMessage(e.getMessage());
		}
    }
    
    @SuppressWarnings("rawtypes")
	private ApiResponse checkRobotIdAndUserId(String robotId, String userId){
    	if(StringTools.isEmpty(robotId, true)) {
    		return ApiResponse.buildClientFailMessage("robotId is empty");
    	}
    	if(StringTools.isEmpty(userId, true)) {
    		return ApiResponse.buildClientFailMessage("userId is empty");
    	}
    	return null;
    }

    @RequestMapping(value = APIPathAndParams.PATH_SESSION_ID, method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<String> getSessionId(@PathVariable(APIPathAndParams.PARAM_ROBOT_ID) String robotId,
            @PathVariable(APIPathAndParams.PARAM_USER_ID) String userId) {
    	log.debug("getSessionId robotId[{}], userId[{}]", robotId, userId);
    	
    	@SuppressWarnings("unchecked")
		ApiResponse<String> checkResult=this.checkRobotIdAndUserId(robotId, userId);
    	if(checkResult!=null) return checkResult;
    	 
		try {
			String id= dialogSessionService.getSessionId(robotId, userId);
			return ApiResponse.buildSuccess(id);
		} catch (IOException e) {
			log.error(e.getMessage(),e);
			return ApiResponse.buildServerFailMessage(e.getMessage());
		} 
    }

    @RequestMapping(value = APIPathAndParams.PATH_SESSION, method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> saveSession(@PathVariable(APIPathAndParams.PARAM_ROBOT_ID) String robotId, 
    		@PathVariable(APIPathAndParams.PARAM_USER_ID) String userId,
            @RequestBody DialogSession dialogSession) {
    	log.debug("saveSession robotId[{}], userId[{}]", robotId, userId);
    	@SuppressWarnings("unchecked")
		ApiResponse<Void> checkResult=this.checkRobotIdAndUserId(robotId, userId);
    	if(checkResult!=null) return checkResult;
    	
    	if(dialogSession==null) {
    		return ApiResponse.buildClientFailMessage("dialogSession is null");
    	}
    	
        try {
			dialogSessionService.saveSession(robotId, userId, dialogSession);
			return ApiResponse.buildSuccess(null);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			return ApiResponse.buildServerFailMessage(e.getMessage());
		}
        
    }

    @RequestMapping(value = APIPathAndParams.PATH_SESSION, method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> delSession(@PathVariable(APIPathAndParams.PARAM_ROBOT_ID) String robotId,
    		@PathVariable(APIPathAndParams.PARAM_USER_ID) String userId) {
    	log.debug("delSession robotId[{}], userId[{}]", robotId, userId);
    	@SuppressWarnings("unchecked")
		ApiResponse<Void> checkResult=this.checkRobotIdAndUserId(robotId, userId);
    	if(checkResult!=null) return checkResult;
        
    	dialogSessionService.delSession(robotId, userId);
    	return ApiResponse.buildSuccess(null);
    }
    
}
