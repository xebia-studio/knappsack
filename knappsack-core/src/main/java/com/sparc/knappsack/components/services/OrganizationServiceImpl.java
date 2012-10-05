package com.sparc.knappsack.components.services;

import com.googlecode.flyway.core.util.StringUtils;
import com.sparc.knappsack.components.dao.OrganizationDao;
import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.enums.DomainType;
import com.sparc.knappsack.models.OrganizationModel;
import com.sparc.knappsack.models.UserDomainModel;
import com.sparc.knappsack.models.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Transactional( propagation = Propagation.REQUIRED )
@Service("organizationService")
public class OrganizationServiceImpl implements OrganizationService {

    private static final Logger log = LoggerFactory.getLogger(OrganizationServiceImpl.class);

    @Qualifier("organizationDao")
    @Autowired(required = true)
    private OrganizationDao organizationDao;

    @Qualifier("storageConfigurationService")
    @Autowired
    private StorageConfigurationService storageConfigurationService;

    @Qualifier("userDomainService")
    @Autowired(required = true)
    private UserDomainService userDomainService;

    @Qualifier("groupService")
    @Autowired(required = true)
    private GroupService groupService;

    @Qualifier("invitationService")
    @Autowired(required = true)
    private InvitationService invitationService;

    @Qualifier("userService")
    @Autowired(required = true)
    private UserService userService;

    @Qualifier("applicationVersionService")
    @Autowired(required = true)
    private ApplicationVersionService applicationVersionService;

    @Qualifier("applicationService")
    @Autowired(required = true)
    private ApplicationService applicationService;

    @Qualifier("categoryService")
    @Autowired(required = true)
    private CategoryService categoryService;

    @Override
    public void delete(Long id) {
        Organization organization = get(id);
        if (organization != null) {
            delete(organization);
        }
    }

    @Override
    public Organization get(Long id) {
        Organization organization = null;
        if (id != null && id > 0) {
            organization = organizationDao.get(id);
        }
        return organization;
    }

    @Override
    public void update(Organization organization) {
        organizationDao.update(organization);
    }

    private void save(Organization organization) {
        if (organization != null) {
            if (organization.getId() == null || organization.getId() <= 0) {
                organizationDao.add(organization);
            } else {
                organizationDao.update(organization);
            }
        }
    }

    private void delete(Organization organization) {
        if (organization != null) {
            userDomainService.removeAllFromDomain(organization.getId(), DomainType.ORGANIZATION);
            invitationService.deleteAll(organization.getId(), DomainType.ORGANIZATION);

            //Delete Applications
            Set<Long> applicationIds = new HashSet<Long>();
            for (Application application : getAllOrganizationApplications(organization.getId())) {
                applicationIds.add(application.getId());
            }
            for (Long id : applicationIds) {
                applicationService.delete(id);
            }

            //Delete Categories
            Set<Long> categoryIds = new HashSet<Long>();
            for (Category category : organization.getCategories()) {
                categoryIds.add(category.getId());
            }
            for (Long id : categoryIds) {
                categoryService.delete(id);
            }
            organizationDao.delete(organization);
        }
    }

    @Override
    public void add(Organization organization) {
        organizationDao.add(organization);
    }

    @Override
    public Organization getByName(String name) {
        return organizationDao.get(name);
    }

    public List<Organization> getAll() {
        return organizationDao.getAll();
    }

    private void mapOrgModelToOrg(OrganizationModel organizationModel, Organization organization) {
        if (organizationModel != null && organization != null) {
            organization.setId(organizationModel.getId());
            organization.setName(organizationModel.getName());
            //TODO: map remaining fields
        }
    }

    @Override
    public void mapOrgToOrgModel(Organization organization, OrganizationModel organizationModel) {
        if (organization != null && organizationModel != null) {
            organizationModel.setId(organization.getId());
            organizationModel.setName(organization.getName());
            if(organization.getOrgStorageConfig() != null) {
                organizationModel.setStorageConfigurationId(organization.getOrgStorageConfig().getStorageConfigurations().get(0).getId());
                organizationModel.setStoragePrefix(organization.getOrgStorageConfig().getPrefix());
            }
        }
    }

    @Override
    public Organization createOrganization(OrganizationModel organizationModel) {
        Organization organization = null;
        if (organizationModel != null) {
            organization = new Organization();
            organization.setAccessCode(UUID.randomUUID().toString());

            StorageConfiguration storageConfiguration = storageConfigurationService.get(organizationModel.getStorageConfigurationId());
            if (storageConfiguration == null) {
                log.error("Attempted to create organization without a StorageConfiguration");
                return null;
            }

            OrgStorageConfig orgStorageConfig = new OrgStorageConfig();
            if (StringUtils.hasText(organizationModel.getStoragePrefix())) {
                orgStorageConfig.setPrefix(organizationModel.getStoragePrefix().trim());
            } else {
                orgStorageConfig.setPrefix(organization.getUuid());
            }
            orgStorageConfig.getStorageConfigurations().add(storageConfiguration);
            orgStorageConfig.setOrganization(organization);
            organization.setOrgStorageConfig(orgStorageConfig);

            organization.setDomainConfiguration(new DomainConfiguration());

            mapOrgModelToOrg(organizationModel, organization);

            save(organization);
        }

        return organization;
    }

    @Override
    public void editOrganization(OrganizationModel organizationModel) {
        if (organizationModel != null) {
            Organization organization = get(organizationModel.getId());
            if (organization != null) {
                mapOrgModelToOrg(organizationModel, organization);

                save(organization);
            }
        }
    }

    @Override
    public void removeUserFromOrganization(Long organizationId, Long userId) {
        if (organizationId != null && organizationId > 0) {
            Organization organization = get(organizationId);
            if (organization != null) {

                //Remove user from all groups for the specified organization
                for (Group group : organization.getGroups()) {
                    groupService.removeUserFromGroup(group.getId(), userId);
                }

                //Remove user from the specified organization
                userDomainService.removeUserDomainFromDomain(organization.getId(), DomainType.ORGANIZATION, userId);
            }
        }
    }

    @Override
    public int getTotalUsers(Organization organization) {
        Set<User> users = new HashSet<User>();
        List<UserDomain> orgUserDomains = userDomainService.getAll(organization.getId(), organization.getDomainType());
        for (UserDomain orgUserDomain : orgUserDomains) {
            users.add(orgUserDomain.getUser());
        }

        for (Group group : organization.getGroups()) {
            List<UserDomain> groupUserDomains = userDomainService.getAll(group.getId(), group.getDomainType());
            for (UserDomain groupUserDomain : groupUserDomains) {
                users.add(groupUserDomain.getUser());
            }
        }

        return users.size();
    }

    @Override
    public int getTotalApplications(Organization organization) {
        int totalApplications = 0;
        for (Group group : organization.getGroups()) {
            totalApplications += groupService.getTotalApplications(group);
        }
        return totalApplications;
    }

    @Override
    public int getTotalApplicationVersions(Organization organization) {
        int totalApplicationVersions = 0;
        for (Group group : organization.getGroups()) {
            totalApplicationVersions += groupService.getTotalApplicationVersions(group);
        }
        return totalApplicationVersions;
    }

    @Override
    public double getTotalMegabyteStorageAmount(Organization organization) {
        double totalMegabyteStorageAmount = 0;
        for (Group group : organization.getGroups()) {
            totalMegabyteStorageAmount += groupService.getTotalMegabyteStorageAmount(group);
        }

        return totalMegabyteStorageAmount;
    }

    @Override
    public List<UserDomainModel> getAllOrganizationMembers(Long organizationId, boolean includeGuests) {
        Map<Long, UserDomainModel> userDomainModels = new HashMap<Long, UserDomainModel>();
        Organization organization = get(organizationId);

        if (organization != null) {
            for (UserDomain userDomain : userDomainService.getAll(organizationId, DomainType.ORGANIZATION)) {
                UserDomainModel model = createUserDomainModel(userDomain);
                userDomainModels.put(userDomain.getUser().getId(), model);
            }

            if (includeGuests) {
                for (UserDomainModel guest : getAllOrganizationGuests(organizationId)) {
                    if (!userDomainModels.containsKey(guest.getUser().getId())) {
                        userDomainModels.put(guest.getUser().getId(), guest);
                    }
                }
            }
        }

        return new ArrayList<UserDomainModel>(userDomainModels.values());
    }

    @Override
    public List<UserDomainModel> getAllOrganizationGuests(Long organizationId) {
        Map<Long, UserDomainModel> userDomainsModels = new HashMap<Long, UserDomainModel>();
        Organization organization = get(organizationId);

        if (organization != null) {
            for (Group group : organization.getGroups()) {
                for (UserDomain userDomain : userDomainService.getAll(group.getId(), DomainType.GROUP)) {
                    if (!userDomainsModels.containsKey(userDomain.getUser().getId()) && !userService.isUserInDomain(userDomain.getUser(), organization.getId(), DomainType.ORGANIZATION)) {
                        UserDomainModel model = createUserDomainModel(userDomain);
                        userDomainsModels.put(userDomain.getUser().getId(), model);
                    }
                }
            }
        }

        return new ArrayList<UserDomainModel>(userDomainsModels.values());
    }

    private UserDomainModel createUserDomainModel(UserDomain userDomain) {
        UserDomainModel model = null;
        if (userDomain != null) {
            model = new UserDomainModel();
            model.setId(userDomain.getId());
            model.setUserRole(userDomain.getRole().getUserRole());
            model.setDomainId(userDomain.getDomainId());
            model.setDomainType(userDomain.getDomainType());

            UserModel userModel = new UserModel();
            userModel.setEmail(userDomain.getUser().getEmail());
            userModel.setFirstName(userDomain.getUser().getFirstName());
            userModel.setLastName(userDomain.getUser().getLastName());
            userModel.setUserName(userDomain.getUser().getUsername());
            userModel.setId(userDomain.getUser().getId());

            model.setUser(userModel);
        }

        return model;
    }

    @Override
    public boolean isApplicationLimit(Organization organization) {
        return organization.getDomainConfiguration().getApplicationLimit() <= getTotalApplications(organization);
    }

    @Override
    public boolean isUserLimit(Organization organization) {
        return organization.getDomainConfiguration().getUserLimit() <= getTotalUsers(organization);
    }

    @Override
    public OrganizationModel createOrganizationModel(Long organizationId) {
        OrganizationModel model = null;
        Organization organization = get(organizationId);
        if (organization != null) {
            model = new OrganizationModel();
            model.setId(organization.getId());
            model.setName(organization.getName());
            OrgStorageConfig orgStorageConfig = organization.getOrgStorageConfig();
            if (orgStorageConfig != null && orgStorageConfig.getStorageConfigurations() != null) {
                model.setStorageConfigurationId(orgStorageConfig.getStorageConfigurations().get(0).getId());
                model.setStoragePrefix(orgStorageConfig.getPrefix());
            }
        }
        return model;
    }

    @Override
    public List<Application> getAllOrganizationApplications(Long organizationId) {
        List<Application> applications = new ArrayList<Application>();

        Organization organization = get(organizationId);
        if (organization != null) {
            for (Group group : organization.getGroups()) {
                applications.addAll(group.getOwnedApplications());
            }
        }
        return applications;
    }

    @Override
    public boolean doesEntityExist(Long id) {
        return get(id) != null;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Override
    public long countAll() {
        return organizationDao.countAll();
    }
}
