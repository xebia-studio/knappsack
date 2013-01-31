package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.enums.AppState;
import com.sparc.knappsack.enums.ApplicationType;
import com.sparc.knappsack.enums.SortOrder;
import com.sparc.knappsack.enums.UserRole;
import com.sparc.knappsack.models.ApplicationModel;
import com.sparc.knappsack.models.Contacts;
import com.sparc.knappsack.models.UserModel;

import java.util.List;

public interface UserService extends EntityService<User> {

    void save(User user);

    List<Group> getGroups(User user);

    List<Organization> getOrganizations(User user);

    List<Organization> getAdministeredOrganizations(User user);

    List<Group> getAdministeredGroups(User user);

    List<ApplicationVersion> getApplicationVersions(User user);

    List<ApplicationVersion> getApplicationVersions(User user, Long applicationId, SortOrder sortOrder, AppState... appStates);

    List<Application> getApplicationsForUser(User user, ApplicationType deviceType, AppState... appStates);

    List<ApplicationModel> getApplicationModelsForUser(User user, ApplicationType deviceType, AppState... appStates);

    List<Application> getApplicationsForUser(User user, ApplicationType deviceType, Long categoryId, AppState... appStates);

    boolean addUserToGroup(User user, Group group, UserRole userRole);

    boolean addUserToGroup(User user, Long groupId, UserRole userRole);

    void addUserToOrganization(User user, Organization organization, UserRole userRole);

    void addUserToDomain(User user, Domain domain);

    void addUserToOrganization(User user, Long organizationId, UserRole userRole);

    boolean isUserInDomain(User user, Long domainId, UserRole userRole);

    boolean isUserInDomain(User user, Long domainId);

    boolean isUserInGroup(User user, Group group, UserRole userRole);

    boolean isUserInGroup(User user, Group group);

    boolean isUserInOrganization(User user, Organization organization, UserRole userRole);

    boolean activate(Long userId, String code);

    boolean changePassword(User user, String password, boolean isTempPassword);

    User getByEmail(String email);

    List<User> get(List<Long> ids);

    List<Category> getCategoriesForUser(User user, ApplicationType deviceType);

    boolean updateSecurityContext(User user);

    User getUserFromSecurityContext();

    /**
     * @return long - a count of all the users in the system
     */
    long countAll();

    boolean canUserEditApplication(Long userId, Long applicationId);

    boolean canUserEditApplication(User user, Application application);

    UserModel createUsermodel(User user);
    
    /**
     * @param user User
     * @return long - total amount of organizations this user is currently and administrator for
     */
    long countAdministeredOrganizations(User user);

    List<Contacts> getContacts(User user);
}
