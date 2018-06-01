package com.github.fancyerii.deepnlu.dialog.session.data;

import lombok.Data;

import java.util.Date;

@Data
public class TurnResponse {
	private Object resp;
	private RespType respType;
    private Date rspTime;
    
}
