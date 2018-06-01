package com.github.fancyerii.deepnlu.weathercrawler.history;

import java.util.List;

import lombok.Data;

@Data
public class ExtractResult {
	private List<String> links;
	private String city;
	private String cityCn;
	private int year;
	private int month;
	private WeatherInfo[] infos;
}
