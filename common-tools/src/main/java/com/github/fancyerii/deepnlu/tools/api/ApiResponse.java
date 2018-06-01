package com.github.fancyerii.deepnlu.tools.api;

import lombok.Data;

@Data
public class ApiResponse <T>{
	public static final int CODE_OK=200;
	public static final int CODE_CLIENT_ERR=400;
	public static final int CODE_SERVER_ERR=500;
	
	private int code;
	private String errorMsg;
	private T data;
	
	public static <S> ApiResponse<S> buildSuccess(final S entity) {
		final ApiResponse<S> response = new ApiResponse<S>();
		response.setCode(CODE_OK);
		response.setData(entity);
		return response;
	}
	
	public static <S> ApiResponse<S> buildClientFailMessage(final String message) {
		final ApiResponse<S> response = new ApiResponse<S>();
		response.setCode(CODE_CLIENT_ERR);
		response.setErrorMsg(message);
		return response;
	}
 

	public static <S> ApiResponse<S> buildServerFailMessage(final String message) {
		final ApiResponse<S> response = new ApiResponse<S>();
		response.setCode(CODE_SERVER_ERR);
		response.setErrorMsg(message);
		return response;
	}
	
	public boolean isSucc() {
		return code==CODE_OK;
	}
	
	
}
