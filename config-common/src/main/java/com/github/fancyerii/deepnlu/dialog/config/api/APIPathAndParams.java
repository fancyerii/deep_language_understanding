package com.github.fancyerii.deepnlu.dialog.config.api;

public class APIPathAndParams {
	public static final String DEFAULT_SERVER="http://config.deepnlu.fancyerii.github.com";
    public static final String PARAM_ROBOT_ID = "robotId";
    public static final String PARAM_TYPE = "cfg_type";
    public static final String PARAM_KEY = "cfg_key";
    
    public static final String PATH_CONFIG="/config"; 
    public static final String PATH_CONFIGS="/configs"; 
    
    public static final String PATH_ROOT = "/v1/config/robots/{" + PARAM_ROBOT_ID + "}";
}
