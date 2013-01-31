package com.sparc.knappsack.components.services;

import com.sparc.knappsack.comparators.ApplicationVersionComparator;
import com.sparc.knappsack.components.dao.OrganizationDao;
import com.sparc.knappsack.components.dao.UserDetailsDao;
import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.enums.*;
import com.sparc.knappsack.models.ApplicationModel;
import com.sparc.knappsack.models.Contact;
import com.sparc.knappsack.models.Contacts;
import com.sparc.knappsack.models.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

import static java.util.Collections.reverseOrder;
import static java.util.Collections.sort;

@Transactional( propagation = Propagation.REQUIRED )
@Service("userService")
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Qualifier("userDomainService")
    @Autowired
    private UserDomainService userDomainService;

    @Qualifier("organizationDao")
    @Autowired
    private OrganizationDao organizationDao;

    @Qualifier("roleService")
    @Autowired
    private RoleService roleService;

    @Qualifier("applicationService")
    @Autowired(required = true)
    private ApplicationService applicationService;

    @Autowired(required = true)
    private ApplicationVersionService applicationVersionService;

    @Qualifier("categoryService")
    @Autowired(required = true)
    private CategoryService categoryService;

    @Qualifier("userDetailsDao")
    @Autowired(required = true)
    private UserDetailsDao userDetailsDao;

    @Autowired(required = true)
    private GroupService groupService;

    @Autowired(required = true)
    private DomainService domainService;

    @Qualifier("organizationService")
    @Autowired(required = true)
    private OrganizationService organizationService;

    @Qualifier("passwordEncoder")
    @Autowired(required = true)
    private PasswordEncoder passwordEncoder;

    @Override
    public User get(Long id) {
        User user = null;
        if (id != null && id > 0) {
            user = userDetailsDao.get(id);
        }
        return user;
    }

    @Override
    public void add(User user) {
        userDetailsDao.add(user);
    }

    @Override
    public void delete(Long id) {
        userDetailsDao.delete(get(id));
    }

    @Override
    public void update(User user) {
        userDetailsDao.update(user);
    }

    @Override
    public void save(User user) {
        if (user != null) {
            if (user.getId() != null && user.getId() > 0) {
                userDetailsDao.update(user);
            } else {
                userDetailsDao.add(user);
            }
        }
    }

    @Override
    public List<Group> getGroups(User user) {

//        if(user.isSystemAdmin()) {
//            return groupService.getAll();
//        }

        Set<Group> groups = new HashSet<Group>();
        if (user.isOrganizationAdmin()) {
            List<UserDomain> orgAdminUserDomains = new ArrayList<UserDomain>();

            for (UserDomain userDomain : user.getUserDomains()) {
                if (DomainType.ORGANIZATION.equals(userDomain.getDomain().getDomainType()) && UserRole.ROLE_ORG_ADMIN.equals(userDomain.getRole().getUserRole()) && !orgAdminUserDomains.contains(userDomain)) {
                    orgAdminUserDomains.add(userDomain);
                }
            }

            for (UserDomain userDomain : orgAdminUserDomains) {
                Organization organization = organizationService.get(userDomain.getDomain().getId());
                if (organization != null) {
                    groups.addAll(organization.getGroups());
                }
            }

        }
        List<UserDomain> userDomains = userDomainService.getAll(user, DomainType.GROUP);
        for (UserDomain userDomain : userDomains) {
            Group group = groupService.get(userDomain.getDomain().getId());
            if (group != null && !groups.contains(group)) {
                groups.add(group);
            }
        }
        return new ArrayList<Group>(groups);
    }

    @Override
    public List<Organization> getOrganizations(User user) {
        Set<Organization> organizations = new HashSet<Organization>();

        if(user.isSystemAdmin()) {
            organizations.addAll(organizationDao.getAll());
        } else {

            List<Domain> domains = domainService.getAll(user, DomainType.ORGANIZATION, DomainType.GROUP);
            for (Domain domain : domains) {
                if(DomainType.ORGANIZATION.equals(domain.getDomainType())) {
                    organizations.add((Organization) domain);
                } else if(DomainType.GROUP.equals(domain.getDomainType())) {
                    organizations.add(((Group)domain).getOrganization());
                }
            }
        }

        return new ArrayList<Organization>(organizations);
    }

    @Override
    public List<Organization> getAdministeredOrganizations(User user) {
        List<Organization> organizations = new ArrayList<Organization>();

        if (user != null) {
//            if(user.isSystemAdmin()) {
//                return organizationService.getAll();
//            }

            List<UserDomain> userDomainList = userDomainService.getAll(user, DomainType.ORGANIZATION, UserRole.ROLE_ORG_ADMIN);
            for (UserDomain userDomain : userDomainList) {
                Domain domain = userDomain.getDomain();
                if (domain != null && DomainType.ORGANIZATION.equals(domain.getDomainType())) {
                    organizations.add((Organization) domain);
                }
            }
        }

        return organizations;
    }

    @Override
    public List<Group> getAdministeredGroups(User user) {
        Set<Group> groups = new HashSet<Group>();

        if (user != null) {
//            if (user.isSystemAdmin()) {
//                groups.addAll(groupService.getAll());
//            } else {
                List<Organization> administeredOrganizations = getAdministeredOrganizations(user);
                for (Organization organization : administeredOrganizations) {
                     groups.addAll(organization.getGroups());
                }

                for (UserDomain userDomain : userDomainService.getAll(user, DomainType.GROUP, UserRole.ROLE_GROUP_ADMIN)) {
                    Domain domain = userDomain.getDomain();
                    if (domain instanceof Group) {
                        groups.add((Group) domain);
                    }
                }
//            }
        }

        return new ArrayList<Group>(groups);
    }

    @Override
    public long countAdministeredOrganizations(User user) {
        return userDomainService.countDomains(user, DomainType.ORGANIZATION, UserRole.ROLE_ORG_ADMIN);
    }

    @Override
    public List<Contacts> getContacts(User user) {
        List<Contacts> contactsList = new ArrayList<Contacts>();
        List<Organization> organizations = getOrganizations(user);
        for (Organization organization : organizations) {
            Contacts contacts = new Contacts();
            contacts.setDomainName(organization.getName());
            Set<User> users = organizationService.getAllAdmins(organization, false);
            for (User admin : users) {
                Contact contact = new Contact();
                contact.setName(admin.getFullName());
                contact.setEmail(admin.getEmail());
                contacts.getContacts().add(contact);
            }
            contactsList.add(contacts);
        }
        return contactsList;
    }

    @Override
    public User getByEmail(String email) {
        User user = null;
        if (email != null && !"".equals(email.trim())) {
            user = userDetailsDao.findByEmail(email.trim());
        }
        return user;
    }

    public List<User> get(List<Long> ids) {
        return userDetailsDao.get(ids);
    }

    public List<ApplicationVersion> getApplicationVersions(User user) {
        List<Group> groups = getGroups(user);
        List<Organization> organizations = getOrganizations(user);
        Set<ApplicationVersion> applicationVersions = new HashSet<ApplicationVersion>();
        for (Group group : groups) {
            applicationVersions.addAll(group.getGuestApplicationVersions());
            List<Application> applications = group.getOwnedApplications();
            for (Application application : applications) {
                applicationVersions.addAll(application.getApplicationVersions());
            }
        }

        applicationVersions.addAll(applicationVersionService.getAll(organizations, AppState.ORGANIZATION_PUBLISH));

        return new ArrayList<ApplicationVersion>(applicationVersions);
    }

    @Override
    public List<ApplicationVersion> getApplicationVersions(User user, Long applicationId, SortOrder sortOrder, AppState... appStates) {
        Application application = applicationService.get(applicationId);
        List<ApplicationVersion> versionList = new ArrayList<ApplicationVersion>();
        if (user != null && application != null) {
            for (ApplicationVersion version : getApplicationVersions(user)) {
                if (application.equals(version.getApplication()) && !versionList.contains(version)) {
                    for (AppState appState : appStates) {
                        if (appState.equals(version.getAppState())) {
                            versionList.add(version);
                            break;
                        }
                    }
                }

            }

            if (SortOrder.ASCENDING.equals(sortOrder)) {
                sort(versionList, new ApplicationVersionComparator());
            } else if (SortOrder.DESCENDING.equals(sortOrder)) {
                sort(versionList, reverseOrder(new ApplicationVersionComparator()));
            }
        }
        return versionList;
    }

    @Override
    public List<Application> getApplicationsForUser(User user, ApplicationType deviceType, AppState... appStates) {
        List<Application> applications = new ArrayList<Application>();
        if (user != null && deviceType != null) {
            applications = new ArrayList<Application>();
            List<ApplicationVersion> applicationVersions = getApplicationVersions(user);
            for (ApplicationVersion version : applicationVersions) {
                if (!applications.contains(version.getApplication()) && applicationService.determineApplicationVisibility(version.getApplication(), deviceType)) {
                    for (AppState appState : appStates) {
                        if (appState != null && appState.equals(version.getAppState())) {
                            applications.add(version.getApplication());
                            break;
                        }
                    }
                }
            }
        }
        return applications;
    }

    @Override
    public List<ApplicationModel> getApplicationModelsForUser(User user, ApplicationType deviceType, AppState... appStates) {
        List<Application> applications = getApplicationsForUser(user, deviceType, AppState.ORGANIZATION_PUBLISH, AppState.GROUP_PUBLISH, (user.isOrganizationAdmin() ? AppState.ORG_PUBLISH_REQUEST : null));
        List<ApplicationModel> applicationModels = new ArrayList<ApplicationModel>();
        for (Application application : applications) {
            ApplicationModel applicationModel = applicationService.createApplicationModel(application);
            if (applicationModel != null) {
                applicationModel.setCanUserEdit(canUserEditApplication(user, application));
            }

            applicationModels.add(applicationModel);
        }

        return applicationModels;
    }

    @Override
    public List<Application> getApplicationsForUser(User user, ApplicationType deviceType, Long categoryId, AppState... appStates) {
        List<Application> applications;
        Category category = categoryService.get(categoryId);

        List<Application> appsToReturn = new ArrayList<Application>();

        if (category != null) {
            applications = getApplicationsForUser(user, deviceType, appStates);

            if (applications != null) {
                for (Application application : applications) {
                    if (application.getCategory().getId().equals(category.getId())) {
                        appsToReturn.add(application);
                    }
                }
            }
        }


        return appsToReturn;
    }

    @Override
    public boolean addUserToGroup(User user, Long groupId, UserRole userRole) {
        Group group = groupService.get(groupId);
        return addUserToGroup(user, group, userRole);
    }

    @Override
    public boolean addUserToGroup(User user, Group group, UserRole userRole) {
        if (user != null && group != null && userRole != null) {
            boolean isUserInGroup = isUserInGroup(user, group, userRole);
            if(!isUserInGroup) {
                Role role = roleService.getRoleByAuthority(userRole.toString());
                UserDomain userDomain = new UserDomain();
                userDomain.setDomain(group);
                userDomain.setRole(role);
                userDomain.setUser(userDetailsDao.get(user.getId()));

                userDomainService.add(userDomain);
                user.getUserDomains().add(userDomain);

                return true;
            }
        }

        return false;
    }

    @Override
    public void addUserToOrganization(User user, Long organizationId, UserRole userRole) {
        Organization organization = organizationService.get(organizationId);
        addUserToOrganization(user, organization, userRole);
    }

    @Override
    public void addUserToOrganization(User user, Organization organization, UserRole userRole) {
        if (user != null && organization != null && userRole != null) {
            boolean isUserInOrganization = isUserInOrganization(user, organization, userRole);
            if(!isUserInOrganization) {
                Role role = roleService.getRoleByAuthority(userRole.toString());
                UserDomain userDomain = new UserDomain();
                userDomain.setDomain(organization);
                userDomain.setRole(role);
                userDomain.setUser(userDetailsDao.get(user.getId()));

                userDomainService.add(userDomain);
                user.getUserDomains().add(userDomain);
            }
        }
    }

    @Override
    public void addUserToDomain(User user, Domain domain) {
        if(DomainType.GROUP.equals(domain.getDomainType())) {
            addUserToGroup(user, domain.getId(), UserRole.ROLE_GROUP_USER);
        } else if(DomainType.ORGANIZATION.equals(domain.getDomainType())) {
            addUserToOrganization(user, domain.getId(), UserRole.ROLE_ORG_USER);
        }
    }

    public boolean isUserInDomain(User user, Long domainId, UserRole userRole) {
        UserDomain userDomain = userDomainService.get(user, domainId, userRole);
        return userDomain != null;
    }

    public boolean isUserInDomain(User user, Long domainId) {
        UserDomain userDomain = userDomainService.get(user, domainId);
        return userDomain != null;
    }

    @Override
    public boolean isUserInGroup(User user, Group group, UserRole userRole) {
        UserDomain userDomain = userDomainService.get(user, group.getId(), userRole);
        return userDomain != null;
    }

    @Override
    public boolean isUserInGroup(User user, Group group) {
        UserDomain userDomain = userDomainService.get(user, group.getId());
        return userDomain != null;
    }

    @Override
    public boolean isUserInOrganization(User user, Organization organization, UserRole userRole) {
        UserDomain userDomain = null;
        if (user != null && organization != null && userRole != null) {
            userDomain = userDomainService.get(user, organization.getId(), userRole);
        }
        return userDomain != null;
    }

    @Override
    public boolean activate(Long userId, String code) {
        boolean success = false;
        User user = get(userId);
        if (user != null && StringUtils.hasText(code) && StringUtils.hasText(user.getActivationCode())) {

            if (!user.isActivated() && user.getActivationCode().equals(code.trim())) {
                user.setActivated(true);
                save(user);
                return true;
            }
        }
        return success;
    }

    @Override
    public boolean changePassword(User user, String password, boolean isTempPassword) {
        if (user != null && password != null && !"".equals(password)) {
            user.setPassword(passwordEncoder.encodePassword(password, user.getUsername()));
            user.setPasswordExpired(isTempPassword);

            save(user);

            return true;
        }
        return false;
    }

    @Override
    public List<Category> getCategoriesForUser(User user, ApplicationType deviceType) {
        List<Category> categories = new ArrayList<Category>();

        if (user != null && deviceType != null) {
            List<Application> applications = getApplicationsForUser(user, deviceType, AppState.GROUP_PUBLISH, AppState.ORGANIZATION_PUBLISH, AppState.ORG_PUBLISH_REQUEST);
            for (Application application : applications) {
                Category category = application.getCategory();
                if (!categories.contains(category)) {
                    categories.add(category);
                }
            }
        }

        return categories;
    }

    @Override
    public boolean updateSecurityContext(User user) {

        try {
            User principal = getUserFromSecurityContext();
            if (principal != null && principal.equals(user)) {
                principal.setPassword(user.getPassword());
                principal.setPasswordExpired(user.isPasswordExpired());
                principal.setActivated(user.isActivated());

                return true;
            }
        } catch (Exception e) {
            log.error("Error updating security context:", e);
        }

        return false;
    }

    public User getUserFromSecurityContext() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context.getAuthentication() != null && context.getAuthentication().getPrincipal() != null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Object principal = authentication.getPrincipal();
            if (principal instanceof User) {
                return get(((User) principal).getId());
            }
        }

        return null;
    }

    @Override
    public long countAll() {
        return userDetailsDao.countAll();
    }

    @Override
    public boolean canUserEditApplication(Long userId, Long applicationId) {
        boolean canEdit = false;

        User user = get(userId);
        if (user != null) {
            canEdit = canUserEditApplication(user, applicationService.get(applicationId));
        }

        return canEdit;
    }

    @Override
    public boolean canUserEditApplication(User user, Application application) {
        boolean canEdit = false;

        if (user != null && application != null) {
            Group group = application.getOwnedGroup();//groupService.getOwnedGroup(application);
            if (group != null) {
                Organization organization = group.getOrganization();

                for (UserDomain userDomain : user.getUserDomains()) {
                    if (DomainType.ORGANIZATION.equals(userDomain.getDomain().getDomainType())) {
                        if (organization != null && userDomain.getDomain().equals(organization) && UserRole.ROLE_ORG_ADMIN.equals(userDomain.getRole().getUserRole())) {
                            canEdit = true;
                            break;
                        }
                    } else if (DomainType.GROUP.equals(userDomain.getDomain().getDomainType())) {
                        if (userDomain.getDomain().equals(group) && UserRole.ROLE_GROUP_ADMIN.equals(userDomain.getRole().getUserRole())) {
                            canEdit = true;
                            break;
                        }
                    }
                }
            }
        }

        return canEdit;
    }

    @Override
    public UserModel createUsermodel(User user) {
        UserModel model = null;
        if (user != null) {
            model = new UserModel();
            model.setId(user.getId());
            model.setEmail(user.getEmail());
            model.setFirstName(user.getFirstName());
            model.setLastName(user.getLastName());
            model.setUserName(user.getUsername());
        }

        return model;
    }

    @Override
    public boolean doesEntityExist(Long id) {
        return get(id) != null;
    }
}
