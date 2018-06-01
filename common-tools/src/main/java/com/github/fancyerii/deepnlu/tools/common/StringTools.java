package com.github.fancyerii.deepnlu.tools.common;

public class StringTools {
	public static boolean isEmpty(String s) {
		return isEmpty(s, false);
	}
	public static boolean isEmpty(String s, boolean trim) {
		if(!trim) {
			return s == null || s.isEmpty();
		}else {
			return s == null || s.trim().isEmpty();
		}
	}
 
}
