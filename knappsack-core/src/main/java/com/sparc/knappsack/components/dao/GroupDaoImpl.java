package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.Application;
import com.sparc.knappsack.components.entities.Group;
import com.sparc.knappsack.components.entities.Organization;
import com.sparc.knappsack.components.entities.QGroup;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("groupDao")
public class GroupDaoImpl extends BaseDao implements GroupDao {

    QGroup group = QGroup.group;

    @Override
    public void add(Group group) {
        getEntityManager().persist(group);
    }

    @Override
    public Group get(Long id) {
        return getEntityManager().find(Group.class, id);
    }

    @Override
    public Group get(String name, Organization organization) {
        return query().from(group).where(group.name.equalsIgnoreCase(name).and(group.organization.eq(organization))).uniqueResult(group);
    }

    @Override
    public void delete(Group group) {
        getEntityManager().remove(group);
    }

    @Override
    public void update(Group group) {
        getEntityManager().merge(group);
    }

    @Override
    public List<Group> getAll() {
        return query().from(group).list(group);
    }

    @Override
    public List<Group> getAllGuestGroups(long applicationVersionId) {
        return query().from(group).where(group.guestApplicationVersions.any().id.eq(applicationVersionId)).list(group);
    }

    @Override
    public Group getOwnedGroup(Application application) {
        return query().from(group).where(group.ownedApplications.contains(application)).uniqueResult(group);
    }

    @Override
    public Group getGroupByAccessCode(String accessCode) {
        return query().from(group).where(group.accessCode.eq(accessCode)).uniqueResult(group);
    }
}
