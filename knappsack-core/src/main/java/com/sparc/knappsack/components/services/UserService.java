package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.enums.*;

import java.util.List;

public interface UserService extends EntityService<User> {

    void save(User user);

    List<Group> getGroups(User user);

    List<Organization> getOrganizations(User user);

    List<ApplicationVersion> getApplicationVersions(User user);

    List<ApplicationVersion> getApplicationVersions(User user, Long applicationId, SortOrder sortOrder, AppState... appStates);

    List<Application> getApplicationsForUser(User user, ApplicationType deviceType, AppState... appStates);

    List<Application> getApplicationsForUser(User user, ApplicationType deviceType, Long categoryId, AppState... appStates);

    boolean addUserToGroup(User user, Long groupId, UserRole userRole);

    void addUserToOrganization(User user, Long organizationId, UserRole userRole);

    boolean isUserInDomain(User user, Long domainId, DomainType domainType, UserRole userRole);

    boolean isUserInDomain(User user, Long domainId, DomainType domainType);

    boolean isUserInGroup(User user, Group group, UserRole userRole);

    boolean isUserInGroup(User user, Group group);

    boolean isUserInOrganization(User user, Organization organization, UserRole userRole);

    boolean activate(Long userId, String code);

    boolean changePassword(User user, String password, boolean isTempPassword);

    User getByEmail(String email);

    List<Category> getCategoriesForUser(User user, ApplicationType deviceType);

    boolean updateSecurityContext(User user);

    User getUserFromSecurityContext();

    /**
     * @return long - a count of all the users in the system
     */
    long countAll();

    boolean canUserEditApplication(Long userId, Long applicationId);
}
