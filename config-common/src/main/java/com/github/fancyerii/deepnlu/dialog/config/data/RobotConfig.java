package com.github.fancyerii.deepnlu.dialog.config.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@NoArgsConstructor
@AllArgsConstructor
public class RobotConfig {
	private String robotId;
	private String cfgType;
	private String cfgKey;
	private String cfgJson;
}
