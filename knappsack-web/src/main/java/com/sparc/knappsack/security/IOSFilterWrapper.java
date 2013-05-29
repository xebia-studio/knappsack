package com.sparc.knappsack.security;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class IOSFilterWrapper extends GenericFilterBean implements ApplicationEventPublisherAware, MessageSourceAware {

    private List<AbstractAuthenticationProcessingFilter> filterList = new ArrayList<AbstractAuthenticationProcessingFilter>();

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        for (ApplicationEventPublisherAware applicationEventPublisherAware : filterList) {
            applicationEventPublisherAware.setApplicationEventPublisher(applicationEventPublisher);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        FilterChain filterChain = new FilterChain() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
                chain.doFilter(request, response);
            }
        };

        Vector<FilterChain> filterChains = new Vector<FilterChain>();
        filterChains.add(filterChain);
        for (final GenericFilterBean filter : filterList) {
            final FilterChain lastChain = filterChains.lastElement();
            FilterChain loopChain = new FilterChain() {
                @Override
                public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
                    filter.doFilter(request, response, lastChain);
                }
            };
            filterChains.add(loopChain);
        }
        filterChains.lastElement().doFilter(request, response);
    }

    @Override
    public void setMessageSource(MessageSource messageSource) {
        for (MessageSourceAware filter : filterList) {
            filter.setMessageSource(messageSource);
        }
    }

    public void setFilterList(List filterList) {
        this.filterList = filterList;
    }
}
