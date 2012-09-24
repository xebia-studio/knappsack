package com.sparc.knappsack.components.dialects.processors;

import org.springframework.stereotype.Component;

@Component("hrefResourceProcessor")
public class HrefResourceProcessor extends ResourceProcessor {

    public HrefResourceProcessor() {
        super("href");
    }
}
