package com.sparc.knappsack.util;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URL;
import java.util.Arrays;

public class WebRequest {

    private static final Logger log = LoggerFactory.getLogger(WebRequest.class);

    private String scheme;
    private String serverName;
    private int serverPort;
    private String contextPath;

    private static WebRequest webRequest;

    private WebRequest() {};

    private WebRequest(String scheme, String serverName, int serverPort, String contextPath) {
        this.scheme = scheme;
        this.serverName = serverName;
        this.serverPort = serverPort;
        this.contextPath = contextPath;

        if (StringUtils.endsWithIgnoreCase(contextPath, "/")) {
            this.contextPath = this.contextPath.substring(0, this.contextPath.length() - 1);
        }
    }

    //Get WebRequest and init if needed.
    public static WebRequest getInstance(String scheme, String serverName, int serverPort, String contextPath) {
        if (webRequest == null) {
            if (StringUtils.hasText(scheme) && StringUtils.hasText(serverName)) {
                webRequest = new WebRequest(scheme, serverName, serverPort, contextPath);
            }
        }

        return webRequest;
    }

    public static WebRequest getInstance() {
        return webRequest;
    }

    public String generateURL(String requestURI) {
        return generateURL(requestURI, null);
    }

    public String generateURL(String absolutePath, NameValuePair... params) {
        if (!StringUtils.hasText(absolutePath)) {
            absolutePath = "/";
        }

        if (!absolutePath.startsWith("/")) {
            absolutePath = contextPath + "/" + absolutePath;
        } else {
            absolutePath = contextPath + absolutePath;
        }

        String builtUrl = null;
        try {
            URIBuilder builder = new URIBuilder();
            builder.setScheme(scheme);
            builder.setHost(serverName);
            builder.setPort(displayPort(scheme, serverPort) ? serverPort : -1);
            builder.setPath(absolutePath);
            builder.setQuery(createQueryString(params));

            URI uri = builder.build();

            if (uri != null) {
                URL url = uri.toURL();

                if (url != null) {
                    builtUrl = url.toString();
                }
            }
        } catch (Exception e) {
            log.error("Error creating url.", e);
        }

        return builtUrl;
    }

    public static String createQueryString(NameValuePair... params) {
        String queryString = null;

        if (params != null && params.length > 0) {
            queryString = URLEncodedUtils.format(Arrays.asList(params), "UTF-8");
        }

        return queryString;
    }

    private static boolean displayPort(String schema, int serverPort) {
        boolean display = false;

        if ("http".equalsIgnoreCase(schema)) {
            if (serverPort != 80 && serverPort > 0) {
                display = true;
            }
        } else if ("https".equalsIgnoreCase(schema)) {
            if (serverPort != 443 && serverPort > 0) {
                display = true;
            }
        }

        return display;
    }

}
