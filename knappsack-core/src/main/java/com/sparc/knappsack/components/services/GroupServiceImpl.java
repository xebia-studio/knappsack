package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.dao.GroupDao;
import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.enums.UserRole;
import com.sparc.knappsack.forms.GroupForm;
import com.sparc.knappsack.models.DomainModel;
import com.sparc.knappsack.models.GroupModel;
import com.sparc.knappsack.models.OrganizationModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Transactional( propagation = Propagation.REQUIRED )
@Service("groupService")
public class GroupServiceImpl implements GroupService {

    @Qualifier("groupDao")
    @Autowired(required = true)
    private GroupDao groupDao;

    @Qualifier("domainUserRequestService")
    @Autowired(required = true)
    private DomainUserRequestService requestService;

    @Qualifier("organizationService")
    @Autowired
    private OrganizationService organizationService;

    @Qualifier("applicationService")
    @Autowired(required = true)
    private ApplicationService applicationService;

    @Qualifier("userDomainService")
    @Autowired(required = true)
    private UserDomainService userDomainService;

    @Qualifier("invitationService")
    @Autowired(required = true)
    private InvitationService invitationService;

    @Override
    public void add(Group group) {
        groupDao.add(group);
    }

    @Override
    public void update(Group group) {
        groupDao.update(group);
    }

    @Override
    public void save(Group group) {
        if (group != null) {
            if (group.getId() == null || group.getId() <= 0) {
                add(group);
            } else {
                update(group);
            }
        }
    }

    @Override
    public Group get(String name, Long organizationId) {
        Organization organization = organizationService.get(organizationId);
        return groupDao.get(name, organization);
    }

    public List<Group> getAll() {
        return groupDao.getAll();
    }

    private void mapGroupFormToGroup(GroupForm groupForm, Group group) {
        if (groupForm != null && group != null) {
            group.setId(groupForm.getId());
            group.setName(groupForm.getName());
        }
    }

    @Override
    public void mapGroupToGroupForm(Group group, GroupForm groupForm) {
        if (group != null && groupForm != null) {
            groupForm.setId(group.getId());
            groupForm.setName(group.getName());
            groupForm.setOrganizationId(group.getOrganization().getId());
        }
    }

    @Override
    @PreAuthorize("isOrganizationAdmin(#groupForm.organizationId) or hasRole('ROLE_ADMIN')")
    public Group createGroup(GroupForm groupForm) {
        Group newGroup = null;
        if (groupForm != null) {
            newGroup = new Group();
            mapGroupFormToGroup(groupForm, newGroup);

            Organization organization = organizationService.get(groupForm.getOrganizationId());

            DomainConfiguration domainConfiguration = new DomainConfiguration();
            if (organization != null && organization.getDomainConfiguration() != null) {
                BeanUtils.copyProperties(organization.getDomainConfiguration(), domainConfiguration, new String[] {"id"});
            }

            domainConfiguration.setDisableLimitValidations(true);
            newGroup.setDomainConfiguration(domainConfiguration);

            newGroup.setOrganization(organization);
            organization.getGroups().add(newGroup);

            save(newGroup);
        }

        return newGroup;
    }

    @Override
    public void editGroup(GroupForm groupForm) {
        if (groupForm != null) {
            Group group = get(groupForm.getId());
            if (group != null) {
                mapGroupFormToGroup(groupForm, group);

//                save(group);
            }
        }
    }

    @Override
    public void delete(Long id) {
        Group group = get(id);
        if (group != null) {
            userDomainService.removeAllFromDomain(group.getId());
            invitationService.deleteAll(group);

            List<Application> applicationsToDelete = new ArrayList<Application>();
            applicationsToDelete.addAll(group.getOwnedApplications());

            for (Application application : applicationsToDelete) {
                applicationService.deleteApplicationFilesAndVersions(application);
                group.getOwnedApplications().remove(application);
            }
            group.getOwnedApplications().clear();

            Organization organization = group.getOrganization();
            organization.getGroups().remove(group);

            groupDao.delete(group);

        }
    }

    @Override
    public Group get(Long id) {
        Group group = null;
        if (id != null && id > 0) {
            group = groupDao.get(id);
        }
        return group;
    }

    @Override
    public Group getByAccessCode(String accessCode) {
        return groupDao.getGroupByUUID(accessCode);
    }

    @Override
    public List<Group> getGuestGroups(ApplicationVersion applicationVersion) {
        List<Group> groups = null;
        if (applicationVersion != null && applicationVersion.getId() != null && applicationVersion.getId() > 0) {
            groups = groupDao.getAllGuestGroups(applicationVersion.getId());
        }
        return groups;
    }

    @Override
    public Group getOwnedGroup(Application application) {
        return application.getOwnedGroup();//groupDao.getOwnedGroup(application);
    }

    @Override
    public void removeUserFromGroup(Long groupId, Long userId) {
        if (groupId != null && groupId > 0 && userId != null && userId > 0) {
            userDomainService.removeUserDomainFromDomain(groupId, userId);
        }
    }

    @Override
    public List<User> getAllUsersForRole(Group group, UserRole userRole) {
        List<User> users = new ArrayList<User>();
        if (group != null && group.getId() != null && group.getId() > 0 && userRole != null) {
            List<UserDomain> userDomains = userDomainService.getAll(group.getId(), userRole);
            users.addAll(getUsersFromUserDomains(userDomains));
        }
        return users;
    }


    private List<User> getUsersFromUserDomains(List<UserDomain> userDomains) {
        List<User> users = new ArrayList<User>();
        if (userDomains != null) {
            for (UserDomain userDomain : userDomains) {
                if (!users.contains(userDomain.getUser())) {
                    users.add(userDomain.getUser());
                }
            }
        }
        return users;
    }

    @Override
    public int getTotalUsers(Group group) {
        return userDomainService.getAll(group.getId()).size();
    }

    @Override
    public int getTotalApplications(Group group) {
        return group.getOwnedApplications().size();
    }

    @Override
    public int getTotalApplicationVersions(Group group) {
        int applicationVersions = 0;
        for(Application application : group.getOwnedApplications()) {
            applicationVersions += application.getApplicationVersions().size();
        }
        return applicationVersions;
    }

    @Override
    public double getTotalMegabyteStorageAmount(Group group) {
        double totalMegabyteStorageAmount = 0;
        for(Application application : group.getOwnedApplications()) {
            for(ApplicationVersion applicationVersion : application.getApplicationVersions()) {
                totalMegabyteStorageAmount += applicationVersion.getInstallationFile().getSize();
            }
        }
        return totalMegabyteStorageAmount;
    }

    @Override
    public boolean isApplicationLimit(Group group) {
        return group.getDomainConfiguration().getApplicationLimit() <= getTotalApplications(group);
    }

    @Override
    public boolean isUserLimit(Group group) {
        return group.getDomainConfiguration().getUserLimit() <= getTotalUsers(group);
    }

    @Override
    public GroupModel createGroupModel(Long groupId) {
        return createGroupModel(get(groupId));
    }

    @Override
    public GroupModel createGroupModel(Group group) {
        GroupModel model = null;
        if (group != null) {
            model = new GroupModel();
            model.setId(group.getId());
            model.setName(group.getName());
        }
        return model;
    }

    @Override
    public GroupModel createGroupModelWithOrganization(Group group, boolean includeOrgStorageConfig, boolean includeExternalData) {
        GroupModel model = createGroupModel(group);

        if (model != null) {
            OrganizationModel organizationModel = null;

            if (includeOrgStorageConfig) {
                organizationModel = organizationService.createOrganizationModel(group.getOrganization(), includeExternalData);
            } else {
                organizationModel = organizationService.createOrganizationModelWithoutStorageConfiguration(group.getOrganization(), includeExternalData);
            }

            model.setOrganization(organizationModel);
        }

        return model;
    }

    @Override
    public DomainModel createDomainModel(Group group) {
        DomainModel model = null;
        if (group != null) {
            model = createGroupModel(group);
        }
        return model;
    }

    @Override
    public List<DomainModel> getAssignableDomainModelsForDomainRequest(Group group) {
        List<DomainModel> domainModels = new ArrayList<DomainModel>();
        domainModels.add(createDomainModel(group));

        return domainModels;
    }

    @Override
    public boolean isDomainAdmin(Group group, User user) {
        for(UserDomain userDomain : user.getUserDomains()) {
            if(userDomain.getDomain().equals(group) && UserRole.ROLE_GROUP_ADMIN.name().equals(userDomain.getRole().getAuthority())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<User> getDomainRequestUsersForNotification(Group group) {
        Set<User> users = new HashSet<User>();
        for(UserDomain userDomain : group.getUserDomains()) {
            if(isDomainAdmin(group, userDomain.getUser())) {
                users.add(userDomain.getUser());
            }
        }

        if(users.isEmpty()) {
            for(UserDomain userDomain : group.getOrganization().getUserDomains()) {
                if(organizationService.isDomainAdmin(group.getOrganization(), userDomain.getUser())) {
                    users.add(userDomain.getUser());
                }
            }
        }

        return users;
    }

    @Override
    public Set<User> getAllAdmins(Group group, boolean includeParentDomainAdminsIfEmpty) {
        Set<User> users = new HashSet<User>();
        if (group != null) {
            List<UserDomain> groupAdmins = userDomainService.getAll(group.getId(), UserRole.ROLE_GROUP_ADMIN);

            if (groupAdmins != null) {
                for (UserDomain groupAdmin : groupAdmins) {
                    User user = groupAdmin.getUser();
                    if (user != null) {
                        users.add(user);
                    }
                }
            }

            if (users.size() <= 0) {
                Organization parentDomain = group.getOrganization();
                if (parentDomain != null) {
                    Set<User> parentDomainAdmins = organizationService.getAllAdmins(parentDomain, true);
                    if (parentDomainAdmins != null) {
                        users.addAll(parentDomainAdmins);
                    }
                }
            }
        }

        return users;
    }

    @Override
    public boolean doesEntityExist(Long id) {
        return get(id) != null;
    }
}
