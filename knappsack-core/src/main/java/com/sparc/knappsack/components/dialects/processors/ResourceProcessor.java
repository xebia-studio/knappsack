package com.sparc.knappsack.components.dialects.processors;

import com.googlecode.flyway.core.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.processor.attr.AbstractAttributeModifierAttrProcessor;
import org.thymeleaf.standard.expression.*;
import org.thymeleaf.util.PrefixUtils;

import java.util.HashMap;
import java.util.Map;

public abstract class ResourceProcessor extends AbstractAttributeModifierAttrProcessor {

    private static final int ATTR_PRECEDENCE = 1000;

    @Value("${static.content.url}")
    private String staticContentURL = "";

    public ResourceProcessor(String attributeName) {
        super(attributeName);
    }

    @Override
    protected Map<String, String> getModifiedAttributeValues(Arguments arguments, Element element, String attributeName) {
        final String attributeValue = element.getAttributeValue(attributeName);

        final Map<String, String> values = new HashMap<String, String>();

        String resourceValue = attributeValue;

        Expression expression = StandardExpressionProcessor.parseExpression(arguments, attributeValue);
        if (!(expression instanceof LinkExpression)) {
            throw new TemplateProcessingException("Could not parse as expression: \"" + attributeValue + "\"");
        }

        String linkBase = getExpressionBaseUrl((LinkExpression) expression);

        if (!StringUtils.hasText(linkBase)) {
            throw new TemplateProcessingException("Could not parse as expression: \"" + attributeValue + "\"");
        }

        if(staticContentURL != null && !staticContentURL.isEmpty() && !"${static.content.url}".equals(staticContentURL)) {
            resourceValue = staticContentURL + linkBase;
        } else {

            String processedPath;
            try {
                processedPath = (String) StandardExpressionProcessor.processExpression(arguments, attributeValue);
            } catch (Exception e) {
                throw new TemplateProcessingException("Could not parse as expression: \"" + attributeValue + "\"");
            }

            if (StringUtils.hasText(processedPath)) {
                resourceValue = processedPath;
            }

        }

        String rawAttributeName = PrefixUtils.getUnprefixed(attributeName);
        values.put(rawAttributeName, resourceValue);

        return values;
    }

    @Override
    protected ModificationType getModificationType(Arguments arguments, Element element, String s, String s1) {
        return ModificationType.SUBSTITUTION;
    }

    @Override
    protected boolean removeAttributeIfEmpty(Arguments arguments, Element element, String s, String s1) {
        return false;
    }

    @Override
    protected boolean recomputeProcessorsAfterExecution(Arguments arguments, Element element, String s) {
        return false;
    }

    @Override
    public int getPrecedence() {
        return ATTR_PRECEDENCE;
    }

    private String getExpressionBaseUrl(LinkExpression expression) {
        String linkBase = null;
        Object base = LiteralValue.unwrap(expression.getBase());
        if (base instanceof TextLiteralExpression) {
            linkBase = ((TextLiteralExpression) base).getValue().getValue();
        }

        return linkBase;
    }
}
