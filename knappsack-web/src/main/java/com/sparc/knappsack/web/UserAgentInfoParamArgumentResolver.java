package com.sparc.knappsack.web;

import com.sparc.knappsack.util.UAgentInfo;
import com.sparc.knappsack.util.UserAgentInfo;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.context.request.NativeWebRequest;

public class UserAgentInfoParamArgumentResolver implements WebArgumentResolver {

    @Override
    public Object resolveArgument(MethodParameter param,
                                  NativeWebRequest request) throws Exception {

        UserAgentInfo uAgentInfo = new UserAgentInfo(request.getHeader("User-Agent"), request.getHeader("Accept"));

        if (UAgentInfo.class.isAssignableFrom(param.getParameterType())) {
			return uAgentInfo;
		} else {
			return WebArgumentResolver.UNRESOLVED;
		}

    }

}

