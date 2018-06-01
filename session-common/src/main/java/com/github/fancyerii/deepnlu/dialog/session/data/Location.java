package com.github.fancyerii.deepnlu.dialog.session.data;

import lombok.Data;

@Data
public class Location {
	private Double lat;
	private Double lng;
	private String country;
	private String province;
	private String city;
	private String county;
	private String town;
	private String ipAddr;
}
