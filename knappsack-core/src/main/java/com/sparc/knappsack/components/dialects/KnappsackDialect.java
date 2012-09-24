package com.sparc.knappsack.components.dialects;

import com.sparc.knappsack.components.dialects.processors.CanEditApplicationProcessor;
import com.sparc.knappsack.components.dialects.processors.HrefResourceProcessor;
import com.sparc.knappsack.components.dialects.processors.SourceResourceProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.thymeleaf.dialect.AbstractDialect;
import org.thymeleaf.processor.IProcessor;

import java.util.HashSet;
import java.util.Set;

public class KnappsackDialect extends AbstractDialect {

    @Autowired(required = true)
    private HrefResourceProcessor hrefResourceProcessor;

    @Autowired(required = true)
    private SourceResourceProcessor sourceResourceProcessor;

    @Autowired(required = true)
    private CanEditApplicationProcessor canEditApplicationProcessor;

    @Override
    public String getPrefix() {
        return "ks";
    }

    @Override
    public boolean isLenient() {
        return false;
    }

    @Override
    public Set<IProcessor> getProcessors() {
        final Set<IProcessor> processors = new HashSet<IProcessor>();
        processors.add(hrefResourceProcessor);
        processors.add(sourceResourceProcessor);
        processors.add(canEditApplicationProcessor);
        return processors;
    }
}
