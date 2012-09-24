package com.sparc.knappsack.components.dialects.processors;

import org.springframework.stereotype.Component;

@Component("sourceResourceProcessor")
public class SourceResourceProcessor extends ResourceProcessor {

    public SourceResourceProcessor() {
        super("src");
    }
}
