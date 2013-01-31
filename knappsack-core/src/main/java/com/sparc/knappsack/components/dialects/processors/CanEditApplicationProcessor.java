package com.sparc.knappsack.components.dialects.processors;

import com.sparc.knappsack.components.entities.Application;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.services.ApplicationService;
import com.sparc.knappsack.components.services.GroupService;
import com.sparc.knappsack.components.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;
import org.thymeleaf.processor.attr.AbstractConditionalVisibilityAttrProcessor;
import org.thymeleaf.standard.expression.StandardExpressionProcessor;

@Component("canEditApplicationProcessor")
public class CanEditApplicationProcessor extends AbstractConditionalVisibilityAttrProcessor {

    private static final int ATTR_PRECEDENCE = 1100;
    public static final String ATTR_NAME = "canEditApplication";

    @Qualifier("userService")
    @Autowired(required = true)
    private UserService userService;

    @Qualifier("applicationService")
    @Autowired(required = true)
    private ApplicationService applicationService;

    @Qualifier("groupService")
    @Autowired(required = true)
    private GroupService groupService;

    public CanEditApplicationProcessor() {
        super(ATTR_NAME);
    }

    @Override
    public int getPrecedence() {
        return ATTR_PRECEDENCE;
    }

    @Override
    protected boolean isVisible(Arguments arguments, Element element, String attributeName) {
        final String attributeValue = element.getAttributeValue(attributeName);

        final Object value = StandardExpressionProcessor.processExpression(arguments, attributeValue);

        if (value instanceof Long) {

            User user = userService.getUserFromSecurityContext();
            if(user == null) {
                return false;
            }

            Application application = applicationService.get((Long) value);
            if (application != null) {
                return userService.canUserEditApplication(user, application);
            }
        }

        return false;
    }
}
