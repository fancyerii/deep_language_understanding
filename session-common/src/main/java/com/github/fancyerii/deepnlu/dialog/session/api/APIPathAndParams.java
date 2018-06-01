package com.github.fancyerii.deepnlu.dialog.session.api;

public class APIPathAndParams {
	public static final String DEFAULT_SERVER="http://session.deepnlu.fancyerii.github.com";
    public static final String PARAM_ROBOT_ID = "robotId";
    public static final String PARAM_USER_ID = "userId";
    public static final String PATH_SESSION="/session";
    public static final String PATH_SESSION_ID=PATH_SESSION+"/id";
    public static final String PATH_ROOT = "/v1/session/robots/{" + PARAM_ROBOT_ID + "}/userId/{" + PARAM_USER_ID
            + "}";
}
