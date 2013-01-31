package com.sparc.knappsack.security;

import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.components.services.*;
import com.sparc.knappsack.enums.ApplicationType;
import com.sparc.knappsack.enums.DomainType;
import com.sparc.knappsack.enums.UserRole;
import com.sparc.knappsack.exceptions.EntityNotFoundException;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;

import java.util.Arrays;
import java.util.List;

public class CustomMethodSecurityExpressionRoot extends SecurityExpressionRoot {

    private GroupService groupService;

    private UserService userService;

    private ApplicationVersionService applicationVersionService;

    private CategoryService categoryService;

    private DomainService domainService;

    private DomainUserRequestService domainUserRequestService;

    private DomainRequestService domainRequestService;

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
    public boolean isOrganizationAdmin() {
        return getUser() != null && getUser().isOrganizationAdmin();
    }

    @SuppressWarnings("unused")
    public boolean isOrganizationAdmin(Long id) {
        return isUserInDomain(id, UserRole.ROLE_ORG_ADMIN);
    }

    private boolean isOrganizationAdminForGroup(Group group) {
        if (group == null) {
            return false;
        }
        return isUserInDomain(group.getOrganization().getId(), UserRole.ROLE_ORG_ADMIN);
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
    public boolean isDomainAdmin() {
        User user = getUser();
        return user != null && (user.isGroupAdmin() || user.isOrganizationAdmin());
    }

    @SuppressWarnings("unused")
    public boolean isDomainAdmin(Long domainId) {
        return isUserInDomain(domainId, UserRole.ROLE_GROUP_ADMIN, UserRole.ROLE_ORG_ADMIN);
    }

    @SuppressWarnings("unused")
    public boolean isDomainAdmin(List<Long> domainIds) {
        if(domainIds == null) {
            return false;
        }

        for (Long domainId : domainIds) {
            if(!isDomainAdmin(domainId)) {
                return false;
            }
        }

        return true;
    }

    @SuppressWarnings("unused")
    private boolean isDomainAdmin(Domain domain) {
        return isUserInDomain(domain, UserRole.ROLE_ORG_ADMIN, UserRole.ROLE_GROUP_ADMIN);
    }

    @SuppressWarnings("unused")
    public boolean hasDomainConfigurationAccess(Long domainId) {
        User user = getUser();
        if(user == null) {
            return false;
        }

        Domain domain = domainService.get(domainId);

        if (domain != null) {
            switch (domain.getDomainType()) {
                case GROUP:
                    return isUserInDomain(domain, UserRole.ROLE_ORG_ADMIN) || user.isSystemAdmin();
                case ORGANIZATION:
                    return user.isSystemAdmin();
            }
        }

        return false;
    }



    public boolean canEditApplication(Long applicationId) {
        User user = getUser();
        if(user == null) {
            return false;
        }

        return userService.canUserEditApplication(user.getId(), applicationId);
    }

    @SuppressWarnings("unused")
    public boolean canEditApplicationVersion(Long applicationVersionId) {
        User user = getUser();
        if (user == null) {
            return false;
        }
        ApplicationVersion applicationVersion = applicationVersionService.get(applicationVersionId);
        if (applicationVersion == null || applicationVersion.getApplication() == null) {
            throw new EntityNotFoundException();
        }

        return canEditApplication(applicationVersion.getApplication().getId());
    }

    @SuppressWarnings("unused")
    public boolean hasAccessToCategory(Long categoryId, ApplicationType applicationType) {
        User user = getUser();
        if (user == null) {
            return false;
        }

        for (Category category : userService.getCategoriesForUser(user, applicationType)) {
            if (category.getId().equals(categoryId)) {
                return true;
            }
        }

        return false;
    }

    @SuppressWarnings("unused")
    public boolean canEditDomainUserRequest(Long domainUserRequestId) {
        DomainUserRequest domainUserRequest = domainUserRequestService.get(domainUserRequestId);

        if (domainUserRequest != null) {
            return isDomainAdmin(domainUserRequest.getDomain());
        }

        return false;
    }

    @SuppressWarnings("unused")
    public boolean canEditDomainRequest(Long domainRequestId) {
        DomainRequest domainRequest = domainRequestService.get(domainRequestId);

        if (domainRequest != null) {
            return isDomainAdmin(domainRequest.getDomain());
        }

        return false;
    }

    @SuppressWarnings("unused")
    public boolean canEditDomainRegion(Long regionId) {
        Domain domain = domainService.getDomainForRegion(regionId);

        return isDomainAdmin(domain);
    }

    private boolean isUserInDomain(Long id, UserRole... userRoles) {
        return isUserInDomain(domainService.get(id), userRoles);
    }

    private boolean isUserInDomain(Domain domain, UserRole... userRoles) {
        if (domain != null) {
            User user = getUser();

            if (user != null) {
                for (UserDomain userDomain : user.getUserDomains()) {
                    if (domain.equals(userDomain.getDomain())
                            && Arrays.asList(userRoles).contains(UserRole.valueOf(userDomain.getRole().getAuthority()))) {
                        return true;
                    } else if(DomainType.GROUP.equals(domain.getDomainType()) && isOrganizationAdminForGroup((Group) domain)) {
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

    public void setApplicationVersionService(ApplicationVersionService applicationVersionService) {
        this.applicationVersionService = applicationVersionService;
    }

    public void setCategoryService(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    public void setDomainService(DomainService domainService) {
        this.domainService = domainService;
    }

    public void setDomainUserRequestService(DomainUserRequestService domainUserRequestService) {
        this.domainUserRequestService = domainUserRequestService;
    }

    public void setDomainRequestService(DomainRequestService domainRequestService) {
        this.domainRequestService = domainRequestService;
    }

    private User getUser() {
        return userService.getUserFromSecurityContext();
    }
}
