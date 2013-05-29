package com.sparc.knappsack.components.mapper;

import com.sparc.knappsack.components.entities.Organization;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.services.UserService;
import com.sparc.knappsack.models.api.v1.ParentModel;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MapperAspect {

    @Qualifier("userService")
    @Autowired(required = true)
    private UserService userService;

    @AfterReturning(pointcut = "target(com.sparc.knappsack.components.mapper.Mapper)", returning = "returnValue")
    public void afterCreate(Object returnValue) {
        if(returnValue instanceof ParentModel) {
            User user = userService.getUserFromSecurityContext();
            Organization activeOrg = user.getActiveOrganization();
            if(activeOrg != null) {
                ((ParentModel) returnValue).setActiveOrganizationId(activeOrg.getId());
                ((ParentModel) returnValue).setActiveOrganizationName(activeOrg.getName());
            }
        }
    }
}
