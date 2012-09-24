package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.Application;
import com.sparc.knappsack.components.entities.ApplicationVersion;
import com.sparc.knappsack.components.entities.Group;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.enums.Status;
import com.sparc.knappsack.forms.GroupForm;
import com.sparc.knappsack.models.GroupModel;

import java.util.List;

public interface GroupService extends EntityService<Group> {
    void save(Group group);

    Group get(String name, Long organizationId);

    void mapGroupToGroupForm(Group group, GroupForm groupForm);

    void createGroup(GroupForm groupForm);

    void editGroup(GroupForm groupForm);

    List<Group> getAll();

    Group getByAccessCode(String accessCode);

    boolean doesRequestExist(User user, Group group, Status status);

    List<Group> getGuestGroups(ApplicationVersion applicationVersion);

    Group getOwnedGroup(Application application);

    void removeUserFromGroup(Long groupId, Long userId);

    int getTotalUsers(Group group);

    int getTotalApplications(Group group);

    int getTotalApplicationVersions(Group group);

    double getTotalMegabyteStorageAmount(Group group);

    /**
     * @param group Group - check to see if this group has reached the maximum number of applications allowed.
     * @return boolean true if the group is at the maximum number of applications allowed.
     */
    boolean isApplicationLimit(Group group);

    /**
     * @param group Group - check to see if this group has reached the maximum number of users allowed.
     * @return boolean true if the group is at the maximum number of users allowed.
     */
    boolean isUserLimit(Group group);

    GroupModel createGroupModel(Long groupId);
}
