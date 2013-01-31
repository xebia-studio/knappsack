package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.Domain;
import com.sparc.knappsack.components.entities.DomainUserRequest;
import com.sparc.knappsack.enums.Status;

import java.util.List;

public interface DomainUserRequestDao extends Dao<DomainUserRequest> {
    /**
     * @param domain Domain - Get all DomainUserRequest entries for this Group
     * @return List<DomainUserRequest> - all DomainUserRequest entries for this Group regardless of Status
     */
    List<DomainUserRequest> getAllRequests(Domain domain);

    /**
     * @param domain Domain - Get all DomainUserRequest entries for this Group
     * @param status Status - Filter the DomainUserRequest by this specific Status
     * @return List<DomainUserRequest> - all DomainUserRequest entries for this Group of this specific status
     */
    List<DomainUserRequest> getAllRequests(Domain domain, Status status);
}
