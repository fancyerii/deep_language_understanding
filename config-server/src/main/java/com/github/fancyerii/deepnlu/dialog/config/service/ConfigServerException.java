package com.github.fancyerii.deepnlu.dialog.config.service;

public class ConfigServerException extends Exception{
	private static final long serialVersionUID = 1L;
	public ConfigServerException(String msg) {
		this(msg, null);
	}
	public ConfigServerException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
