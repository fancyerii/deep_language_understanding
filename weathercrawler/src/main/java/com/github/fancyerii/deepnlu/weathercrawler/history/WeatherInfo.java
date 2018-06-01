package com.github.fancyerii.deepnlu.weathercrawler.history;

import lombok.Data;

@Data
public class WeatherInfo {
	private int year;
	private int month;
	private int day;
	private int lowest;
	private int highest;
	private String weatherType;
	private String windDirection;
	private String windPower;
}
