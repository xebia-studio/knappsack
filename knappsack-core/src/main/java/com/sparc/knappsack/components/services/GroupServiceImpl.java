package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.dao.GroupDao;
import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.enums.DomainType;
import com.sparc.knappsack.enums.Status;
import com.sparc.knappsack.forms.GroupForm;
import com.sparc.knappsack.models.GroupModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Transactional( propagation = Propagation.REQUIRED )
@Service("groupService")
public class GroupServiceImpl implements GroupService {

    @Qualifier("groupDao")
    @Autowired(required = true)
    private GroupDao groupDao;

    @Qualifier("groupUserRequestService")
    @Autowired(required = true)
    private GroupUserRequestService requestService;

    @Qualifier("organizationService")
    @Autowired
    private OrganizationService organizationService;

    @Qualifier("applicationService")
    @Autowired(required = true)
    private ApplicationService applicationService;

    @Qualifier("userDomainService")
    @Autowired(required = true)
    private UserDomainService userDomainService;

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
    public void createGroup(GroupForm groupForm) {
        if (groupForm != null) {
            Group newGroup = new Group();
            mapGroupFormToGroup(groupForm, newGroup);
            newGroup.setAccessCode(UUID.randomUUID().toString());
            DomainConfiguration domainConfiguration = new DomainConfiguration();
            domainConfiguration.setDisableLimitValidations(true);
            newGroup.setDomainConfiguration(domainConfiguration);

            Organization organization = organizationService.get(groupForm.getOrganizationId());
            newGroup.setOrganization(organization);
            organization.getGroups().add(newGroup);

            save(newGroup);
        }
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
            List<Application> ownedApplications = group.getOwnedApplications();
            for (Application application : ownedApplications) {
                applicationService.deleteApplicationFilesAndVersions(application.getId());
            }
            group.getOwnedApplications().clear();
            Organization organization = group.getOrganization();
            organization.getGroups().remove(group);

            List<UserDomain> userDomains = userDomainService.getAll(group.getId(), DomainType.GROUP);
            for (UserDomain userDomain : userDomains) {
                userDomainService.delete(userDomain.getId());
            }

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
        return groupDao.getGroupByAccessCode(accessCode);
    }

    @Override
    public boolean doesRequestExist(User user, Group group, Status status) {
        if (user != null && group != null && status != null) {
            List<GroupUserRequest> requests = requestService.getAll(group.getId());
            if (requests != null && requests.size() > 0) {
                for (GroupUserRequest request : requests) {
                    if (user.getId().equals(request.getUser().getId()) && status.equals(request.getStatus())) {
                        return true;
                    }
                }
            }
        }
        return false;
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
        return groupDao.getOwnedGroup(application);
    }

    @Override
    public void removeUserFromGroup(Long groupId, Long userId) {
        if (groupId != null && groupId > 0 && userId != null && userId > 0) {
            userDomainService.removeUserDomainFromDomain(groupId, DomainType.GROUP, userId);
        }
    }

    @Override
    public int getTotalUsers(Group group) {
        return userDomainService.getAll(group.getId(), group.getDomainType()).size();
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
    public GroupModel createGroupModel(Long groupId, boolean includeOrganizationModel, boolean includeExternalData) {
        GroupModel model = null;
        Group group = get(groupId);
        if (group != null) {
            model = new GroupModel();
            model.setId(group.getId());
            model.setName(group.getName());
            if (includeOrganizationModel) {
                Organization organization = group.getOrganization();
                if (organization != null) {
                    model.setOrganization(organizationService.createOrganizationModel(organization.getId(), includeExternalData));
                }
            }
        }
        return model;
    }

    @Override
    public boolean doesEntityExist(Long id) {
        return get(id) != null;
    }
}
