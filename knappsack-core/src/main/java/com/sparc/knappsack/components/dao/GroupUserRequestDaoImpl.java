package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.Group;
import com.sparc.knappsack.components.entities.GroupUserRequest;
import com.sparc.knappsack.components.entities.QGroupUserRequest;
import com.sparc.knappsack.enums.Status;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("groupUserRequestDao")
public class GroupUserRequestDaoImpl extends BaseDao implements GroupUserRequestDao {

    QGroupUserRequest groupUserRequest = QGroupUserRequest.groupUserRequest;

    @Override
    public void add(GroupUserRequest groupUserRequest) {
        getEntityManager().persist(groupUserRequest);
    }

    @Override
    public GroupUserRequest get(Long id) {
        return getEntityManager().find(GroupUserRequest.class, id);
    }

    @Override
    public void delete(GroupUserRequest groupUserRequest) {
        getEntityManager().remove(groupUserRequest);
    }

    @Override
    public void update(GroupUserRequest groupUserRequest) {
        getEntityManager().merge(getEntityManager().merge(groupUserRequest));
    }

    @Override
    public List<GroupUserRequest> getAllRequests(Group group) {
        return query().from(groupUserRequest).where(groupUserRequest.group.eq(group)).list(groupUserRequest);
    }

    @Override
    public List<GroupUserRequest> getAllRequests(Group group, Status status) {
        return query().from(groupUserRequest).where(groupUserRequest.group.eq(group).and(groupUserRequest.status.eq(status))).listDistinct(groupUserRequest);
    }
}
