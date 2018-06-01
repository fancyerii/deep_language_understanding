package com.github.fancyerii.deepnlu.dialog.session.data;

import java.util.Date;

import lombok.Data;

@Data 
public class TurnRequest {
    private Object req;
    private RequestType reqType; 
    private Location location;
    private Date reqTime;
    
}
