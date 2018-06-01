package com.github.fancyerii.deepnlu.dialog.config.rest;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.github.fancyerii.deepnlu.dialog.config.api.APIPathAndParams;
import com.github.fancyerii.deepnlu.dialog.config.data.RobotConfig;
import com.github.fancyerii.deepnlu.dialog.config.service.ConfigServerException;
import com.github.fancyerii.deepnlu.dialog.config.service.DialogConfigService;
import com.github.fancyerii.deepnlu.tools.api.ApiResponse;
import com.github.fancyerii.deepnlu.tools.common.StringTools;
import com.github.fancyerii.deepnlu.tools.json.JsonUtil;

@Slf4j
@RestController
@RequestMapping(APIPathAndParams.PATH_ROOT)
public class DialogConfigController { 
	@Autowired
	private DialogConfigService service;
	
    @RequestMapping(value = APIPathAndParams.PATH_CONFIG, method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> delConfig(@PathVariable(APIPathAndParams.PARAM_ROBOT_ID) String robotId,
    		@RequestParam(APIPathAndParams.PARAM_TYPE) String type,
    		@RequestParam(APIPathAndParams.PARAM_KEY) String key) {
    	log.debug("delConfig robotId[{}], type[{}], key[{}]", robotId, type, key);
    	if(StringTools.isEmpty(robotId, true)) {
    		return ApiResponse.buildClientFailMessage("robotId is empty");   		
    	}
    	if(StringTools.isEmpty(type, true)) {
    		return ApiResponse.buildClientFailMessage("type is empty");   		
    	}
    	if(StringTools.isEmpty(key, true)) {
    		return ApiResponse.buildClientFailMessage("key is empty");   		
    	}
    	try {
			service.delConfig(robotId, type, key);
		} catch (ConfigServerException e) {
			log.error(e.getMessage(), e);
			return ApiResponse.buildServerFailMessage(e.getMessage());
		}
    	return ApiResponse.buildSuccess(null);
    }
    
    @RequestMapping(value = APIPathAndParams.PATH_CONFIG, method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> insertConfig(@PathVariable(APIPathAndParams.PARAM_ROBOT_ID) String robotId,
    		@RequestBody RobotConfig cfg) {
    	log.debug("insertConfig robotId[{}], cfg[{}]", robotId, cfg);
    	if(StringTools.isEmpty(robotId, true)) {
    		return ApiResponse.buildClientFailMessage("robotId is empty");   		
    	}
    	if(!robotId.equals(cfg.getRobotId())) {
    		return ApiResponse.buildClientFailMessage("robotId mismatch");  
    	}
    	if(StringTools.isEmpty(cfg.getCfgType(), true)) {
    		return ApiResponse.buildClientFailMessage("type is empty");   		
    	}
    	if(StringTools.isEmpty(cfg.getCfgKey(), true)) {
    		return ApiResponse.buildClientFailMessage("key is empty");   		
    	}
    	try {
			service.insertConfig(cfg);
		} catch (ConfigServerException e) { 
			if(e.getCause()!=null && e.getCause() instanceof DuplicateKeyException) {
				return ApiResponse.buildClientFailMessage("duplicatedkey: "+JsonUtil.toJsonStringWithoutException(cfg));
			}else {
				log.error(e.getMessage(),e);
				return ApiResponse.buildServerFailMessage(e.getMessage());
			}
		}
    	return ApiResponse.buildSuccess(null);
    }
    

    @RequestMapping(value = APIPathAndParams.PATH_CONFIG, method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> upsertConfig(@PathVariable(APIPathAndParams.PARAM_ROBOT_ID) String robotId,
    		@RequestBody RobotConfig cfg) {
    	log.debug("upsertConfig robotId[{}], cfg[{}]", robotId, cfg);
    	if(StringTools.isEmpty(robotId, true)) {
    		return ApiResponse.buildClientFailMessage("robotId is empty");   		
    	}
    	if(!robotId.equals(cfg.getRobotId())) {
    		return ApiResponse.buildClientFailMessage("robotId mismatch");  
    	}
    	if(StringTools.isEmpty(cfg.getCfgType(), true)) {
    		return ApiResponse.buildClientFailMessage("type is empty");   		
    	}
    	if(StringTools.isEmpty(cfg.getCfgKey(), true)) {
    		return ApiResponse.buildClientFailMessage("key is empty");   		
    	}
    	try {
			service.upsertConfig(cfg);
		} catch (ConfigServerException e) { 
			log.error(e.getMessage(),e);
			return ApiResponse.buildServerFailMessage(e.getMessage());
		}
    	return ApiResponse.buildSuccess(null);
    }
    
    @RequestMapping(value = APIPathAndParams.PATH_CONFIG, method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<RobotConfig> getConfig(@PathVariable(APIPathAndParams.PARAM_ROBOT_ID) String robotId,
    		@RequestParam(APIPathAndParams.PARAM_TYPE) String type,
    		@RequestParam(APIPathAndParams.PARAM_KEY) String key) {
    	log.debug("delConfig robotId[{}], type[{}], key[{}]", robotId, type, key);
    	if(StringTools.isEmpty(robotId, true)) {
    		return ApiResponse.buildClientFailMessage("robotId is empty");   		
    	}
    	if(StringTools.isEmpty(type, true)) {
    		return ApiResponse.buildClientFailMessage("type is empty");   		
    	}
    	if(StringTools.isEmpty(key, true)) {
    		return ApiResponse.buildClientFailMessage("key is empty");   		
    	}
    	try {
			RobotConfig cfg=service.getConfig(robotId, type, key);
			return ApiResponse.buildSuccess(cfg);
		} catch (ConfigServerException e) {
			log.error(e.getMessage(),e);
			return ApiResponse.buildServerFailMessage(e.getMessage());
		}
    }
    
    @RequestMapping(value = APIPathAndParams.PATH_CONFIGS, method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<RobotConfig>> getConfigs(@PathVariable(APIPathAndParams.PARAM_ROBOT_ID) String robotId,
    		@RequestParam(APIPathAndParams.PARAM_TYPE) String type) {
    	log.debug("delConfig robotId[{}], type[{}]", robotId, type);
    	if(StringTools.isEmpty(robotId, true)) {
    		return ApiResponse.buildClientFailMessage("robotId is empty");   		
    	}
    	if(StringTools.isEmpty(type, true)) {
    		return ApiResponse.buildClientFailMessage("type is empty");   		
    	}
    	
    	try {
			List<RobotConfig> cfgs=service.getConfigs(robotId, type);
			return ApiResponse.buildSuccess(cfgs);
		} catch (ConfigServerException e) {
			log.error(e.getMessage(),e);
			return ApiResponse.buildServerFailMessage(e.getMessage());
		}
    }
}
