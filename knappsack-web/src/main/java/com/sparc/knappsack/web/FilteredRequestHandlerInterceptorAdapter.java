package com.sparc.knappsack.web;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;

public abstract class FilteredRequestHandlerInterceptorAdapter extends HandlerInterceptorAdapter {

    protected boolean isBlackLabeledRequest(HttpServletRequest request) {
        String ajaxHeader = request.getHeader("X-Requested-With");
        if("XMLHttpRequest".equals(ajaxHeader)) {
            return true;
        } else if(request.getRequestURI().contains("/static/")) {
            return true;
        }

        return false;
    }

}
