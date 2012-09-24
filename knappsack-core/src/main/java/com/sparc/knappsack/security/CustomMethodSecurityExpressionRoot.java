package com.sparc.knappsack.security;

import com.sparc.knappsack.components.entities.ApplicationVersion;
import com.sparc.knappsack.components.entities.Group;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.entities.UserDomain;
import com.sparc.knappsack.components.services.GroupService;
import com.sparc.knappsack.components.services.UserService;
import com.sparc.knappsack.enums.DomainType;
import com.sparc.knappsack.enums.UserRole;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;

import java.util.Arrays;
import java.util.List;

public class CustomMethodSecurityExpressionRoot extends SecurityExpressionRoot {

    private GroupService groupService;

    private UserService userService;

    /**
     * @param authentication Authentication
     */
    public CustomMethodSecurityExpressionRoot(Authentication authentication) {
        super(authentication);
    }

    public CustomMethodSecurityExpressionRoot(Authentication authentication, FilterInvocation fi) {
        super(authentication);
    }

    @SuppressWarnings("unused")
    public boolean hasGroupAdminAccess(Long id) {
        //Check for Admin Access for the Organization that this group belongs to
        return false;
    }

    @SuppressWarnings("unused")
    public boolean isGroupAdmin(Long id) {
        return id != null && isUserInDomain(id, DomainType.GROUP, UserRole.ROLE_GROUP_ADMIN);
    }

    @SuppressWarnings("unused")
    public boolean isOrganizationAdmin() {
        return getUser() != null && getUser().isOrganizationAdmin();
    }

    @SuppressWarnings("unused")
    public boolean isOrganizationAdmin(Long id) {
        return isUserInDomain(id, DomainType.ORGANIZATION, UserRole.ROLE_ORG_ADMIN);
    }

    @SuppressWarnings("unused")
    public boolean isOrganizationAdminForGroup(Long groupId) {
        Group group = groupService.get(groupId);
        Long organizationId = group.getOrganization().getId();
        return isUserInDomain(organizationId, DomainType.ORGANIZATION, UserRole.ROLE_ORG_ADMIN);
    }

    @SuppressWarnings("unused")
    public boolean hasAccessToApplicationVersion(Long applicationVersionId) {
        User user = getUser();
        if(user == null) {
            return false;
        }

        List<ApplicationVersion> applicationVersions = userService.getApplicationVersions(user);
        for (ApplicationVersion applicationVersion : applicationVersions) {
            if(applicationVersion.getId().equals(applicationVersionId)) {
                return true;
            }
        }

        return false;
    }

    @SuppressWarnings("unused")
    public boolean hasAccessToApplication(Long applicationId) {
        User user = getUser();
        if(user == null) {
            return false;
        }

        List<ApplicationVersion> applicationVersions = userService.getApplicationVersions(user);
        for (ApplicationVersion applicationVersion : applicationVersions) {
            if(applicationVersion.getApplication().getId().equals(applicationId)) {
                return true;
            }
        }

        return false;
    }

    @SuppressWarnings("unused")
    public boolean hasUserDomainAccess(UserDomain userDomain) {
        if (userDomain != null) {
            switch (userDomain.getDomainType()) {
                case GROUP:
                    return isUserInDomain(userDomain.getDomainId(), DomainType.GROUP, UserRole.ROLE_GROUP_ADMIN, UserRole.ROLE_ORG_ADMIN);
                case ORGANIZATION:
                    return isUserInDomain(userDomain.getDomainId(), DomainType.ORGANIZATION, UserRole.ROLE_ORG_ADMIN);
            }
        }

        return false;
    }

    @SuppressWarnings("unused")
    public boolean isDomainAdmin(Long domainId, DomainType domainType) {
        return isUserInDomain(domainId, domainType, UserRole.ROLE_GROUP_ADMIN, UserRole.ROLE_ORG_ADMIN);
    }

    @SuppressWarnings("unused")
    public boolean hasDomainConfigurationAccess(Long domainId, DomainType domainType) {
        User user = getUser();
        if(user == null) {
            return false;
        }

        switch (domainType) {
            case GROUP:
                return isUserInDomain(domainId, DomainType.GROUP, UserRole.ROLE_ORG_ADMIN) || user.isSystemAdmin();
            case ORGANIZATION:
                return user.isSystemAdmin();
        }

        return false;
    }

    @SuppressWarnings("unused")
    public boolean canEditApplication(Long applicationId) {
        User user = getUser();
        if(user == null) {
            return false;
        }

        return userService.canUserEditApplication(user.getId(), applicationId);
    }

    private boolean isUserInDomain(Long id, DomainType domainType, UserRole... userRoles) {
        if (id != null && id > 0) {
            User user = getUser();

            if (user != null) {
                for (UserDomain userDomain : user.getUserDomains()) {
                    if (id.equals(userDomain.getDomainId())
                            && domainType.equals(userDomain.getDomainType())
                            && Arrays.asList(userRoles).contains(UserRole.valueOf(userDomain.getRole().getAuthority()))) {
                        return true;
                    } else if(DomainType.GROUP.equals(domainType) && isOrganizationAdminForGroup(id)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public void setGroupService(GroupService groupService) {
        this.groupService = groupService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    private User getUser() {
        return userService.getUserFromSecurityContext();
    }
}
