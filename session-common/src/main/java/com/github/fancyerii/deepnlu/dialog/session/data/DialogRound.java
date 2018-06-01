package com.github.fancyerii.deepnlu.dialog.session.data;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DialogRound {
    private int roundSeq;
    private TurnRequest req;
    private TurnResponse rsp; 
    
    
}
