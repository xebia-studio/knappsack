package com.sparc.knappsack.web;

import com.sparc.knappsack.util.WebRequest;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.context.request.NativeWebRequest;

public class WebRequestParamArgumentResolver implements WebArgumentResolver {
    @Override
    public Object resolveArgument(MethodParameter parameter, NativeWebRequest request) throws Exception {
        if (WebRequest.class.isAssignableFrom(parameter.getParameterType())) {
            return WebRequest.getInstance();
        } else {
            return WebArgumentResolver.UNRESOLVED;
        }
    }
}
