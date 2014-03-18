package fr.xebia.spring.security.web.access.channel;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.web.FilterInvocation;
import org.springframework.util.Assert;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Collection;

public class SecureChannelProcessor extends org.springframework.security.web.access.channel.SecureChannelProcessor {

    @Override
    public void decide(FilterInvocation invocation, Collection<ConfigAttribute> config) throws IOException, ServletException {
        Assert.isTrue((invocation != null) && (config != null),
                "Nulls cannot be provided");

        for (ConfigAttribute attribute : config) {
            if (supports(attribute)) {
                if (!invocation.getHttpRequest().isSecure() && ! "https".equals(invocation.getHttpRequest().getHeader("X-Forwarded-Proto"))) {
                    getEntryPoint().commence(invocation.getRequest(), invocation.getResponse());
                }
            }
        }
    }

}
