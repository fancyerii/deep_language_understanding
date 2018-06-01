package com.github.fancyerii.deepnlu.baikecrawler.utils;

import java.net.MalformedURLException;
import java.net.URL;

public class UrlUtils {
    public static String getAbsoluteUrl(String baseUrl, String url) {
        try {
            URL u = new URL(baseUrl);
            URL uu = new URL(u, url);
            return uu.toString();
        } catch (MalformedURLException e) {
            return null;
        }

    }

}
