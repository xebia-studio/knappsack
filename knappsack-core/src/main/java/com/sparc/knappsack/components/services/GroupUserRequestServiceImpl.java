package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.dao.GroupUserRequestDao;
import com.sparc.knappsack.components.entities.Group;
import com.sparc.knappsack.components.entities.GroupUserRequest;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.components.events.EventDelivery;
import com.sparc.knappsack.components.events.EventDeliveryFactory;
import com.sparc.knappsack.enums.EventType;
import com.sparc.knappsack.enums.Status;
import com.sparc.knappsack.enums.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(propagation = Propagation.REQUIRED)
@Service("groupUserRequestService")
public class GroupUserRequestServiceImpl implements GroupUserRequestService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Qualifier("groupUserRequestDao")
    @Autowired(required = true)
    private GroupUserRequestDao groupUserRequestDao;

    @Qualifier("userService")
    @Autowired(required = true)
    private UserService userService;

    @Qualifier("groupService")
    @Autowired(required = true)
    private GroupService groupService;

    @Qualifier("eventDeliveryFactory")
    @Autowired(required = true)
    private EventDeliveryFactory eventDeliveryFactory;

    @Override
    public GroupUserRequest get(Long id) {
        return groupUserRequestDao.get(id);
    }

    @Override
    public void delete(Long requestId) {
        if (requestId != null && requestId > 0) {
            GroupUserRequest request = get(requestId);

            if (request != null) {
                groupUserRequestDao.delete(request);
            }
        }
    }

    @Override
    public void update(GroupUserRequest groupUserRequest) {
        groupUserRequestDao.update(groupUserRequest);
    }

    @Override
    public void add(GroupUserRequest groupUserRequest) {
        groupUserRequestDao.add(groupUserRequest);
    }

    @Override
    public GroupUserRequest createGroupUserRequest(User user, String accessCode) {
        GroupUserRequest request = null;
        if (user != null && accessCode != null && !accessCode.isEmpty()) {

            Group group = groupService.getByAccessCode(accessCode);
            if (group != null) {

                //Check if user is already in group or not
                if (!userService.isUserInGroup(user, group)) {

                    //Check if there is already a pendingRequest for this given user and this group
                    if (!groupService.doesRequestExist(user, group, Status.PENDING)) {
                        request = new GroupUserRequest();
                        request.setGroup(group);
                        request.setUser(user);
                        request.setStatus(Status.PENDING);

                        add(request);
                    }

                }
            }
        }

        return request;
    }

    @Override
    public List<GroupUserRequest> getAll(Long groupId) {
        Group group = groupService.get(groupId);
        if (group != null) {
            return groupUserRequestDao.getAllRequests(group);
        }
        return null;
    }

    @Override
    public List<GroupUserRequest> getAll(Long groupId, Status status) {
        Group group = groupService.get(groupId);
        if (group != null && status != null) {
            return groupUserRequestDao.getAllRequests(group, status);
        }
        return null;
    }

    @Override
    public boolean acceptRequest(GroupUserRequest groupUserRequest, UserRole userRole) {
        boolean success = false;
        if (groupUserRequest != null && userRole != null) {
            success = userService.addUserToGroup(groupUserRequest.getUser(), groupUserRequest.getGroup().getId(), userRole);

            if (success) {
                groupUserRequest.setStatus(Status.ACCEPTED);
//                update(groupUserRequest);

                EventDelivery deliveryMechanism = eventDeliveryFactory.getEventDelivery(EventType.GROUP_ACCESS_REQUEST_CONFIRMATION);
                if (deliveryMechanism != null) {
                    if (!deliveryMechanism.sendNotifications(groupUserRequest)) {
                        log.info("Error sending Group Access Request Confirmation Email:", groupUserRequest);
                    }
                }

                delete(groupUserRequest.getId());
            }
        }

        return success;
    }

    @Override
    public boolean declineRequest(GroupUserRequest groupUserRequest) {
        boolean success = false;
        if (groupUserRequest != null) {

            groupUserRequest.setStatus(Status.DECLINED);
//            update(groupUserRequest);

            EventDelivery deliveryMechanism = eventDeliveryFactory.getEventDelivery(EventType.GROUP_ACCESS_REQUEST_CONFIRMATION);
            if (deliveryMechanism != null) {
                success = deliveryMechanism.sendNotifications(groupUserRequest);
                if (!success) {
                    log.info("Error sending Group Access Request Confirmation Email:", groupUserRequest);
                }
            }

            delete(groupUserRequest.getId());

        }

        return success;
    }
}
