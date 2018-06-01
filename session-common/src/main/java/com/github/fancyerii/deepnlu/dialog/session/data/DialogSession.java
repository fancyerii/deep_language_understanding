package com.github.fancyerii.deepnlu.dialog.session.data;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class DialogSession {
    private String sessionId;
    private Date startTime;
    private String robotId;
    private String userId;
    private ArrayList<DialogRound> rounds;
    private Map<String, Object> sessionAttrs = new HashMap<>();
    
    
    public DialogSession(String sessionId, String robotId, String userId) {
    	this.sessionId=sessionId;
    	this.robotId=robotId;
    	this.userId=userId;
    }
}
