package com.sparc.knappsack.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class AntiSamyRequestWrapper extends HttpServletRequestWrapper {

    private final Map<String, String[]> parameterMap;
    private final Map<String, String[]> cleanParameterMap;

    /**
     * Constructs a request object wrapping the given request.
     *
     * @throws IllegalArgumentException if the request is null
     */
    @SuppressWarnings("unchecked")
    public AntiSamyRequestWrapper(HttpServletRequest request) {
        super(request);
        this.parameterMap = super.getParameterMap();
        this.cleanParameterMap = cleanParameterMap();
    }

    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        if (value == null) {
            return value;
        }

        return AntiSamyScanner.cleanHTML(value);
    }

    @Override
    public String getParameter(String name) {

        String value = super.getParameter(name);
        if (value == null) {
            return null;
        }

        return AntiSamyScanner.cleanHTML(value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, String[]> getParameterMap() {
        return cleanParameterMap;
    }

    @Override
    public String[] getParameterValues(String name) {
        return cleanParameterMap.get(name);
    }

    @Override
    public Enumeration getParameterNames() {
        return Collections.enumeration(cleanParameterMap.keySet());
    }

    private Map<String, String[]> cleanParameterMap() {
        if (parameterMap == null) {
            return parameterMap;
        }

        Map<String, String[]> cleanMap = new HashMap<String, String[]>();
        for (String key : parameterMap.keySet()) {
            String[] values = parameterMap.get(key);
            String[] cleanValues = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                String value = values[i];
                cleanValues[i] = AntiSamyScanner.cleanHTML(value);
            }
            cleanMap.put(key, cleanValues);
        }

        return cleanMap;
    }
}
