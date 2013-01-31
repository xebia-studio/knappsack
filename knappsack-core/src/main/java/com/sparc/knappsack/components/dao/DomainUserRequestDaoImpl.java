package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.Domain;
import com.sparc.knappsack.components.entities.DomainUserRequest;
import com.sparc.knappsack.components.entities.QDomainUserRequest;
import com.sparc.knappsack.enums.Status;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("domainUserRequestDao")
public class DomainUserRequestDaoImpl extends BaseDao implements DomainUserRequestDao {

    QDomainUserRequest domainUserRequest = QDomainUserRequest.domainUserRequest;

    @Override
    public void add(DomainUserRequest request) {
        getEntityManager().persist(request);
    }

    @Override
    public DomainUserRequest get(Long id) {
        return getEntityManager().find(DomainUserRequest.class, id);
    }

    @Override
    public void delete(DomainUserRequest request) {
        getEntityManager().remove(request);
    }

    @Override
    public void update(DomainUserRequest request) {
        getEntityManager().merge(getEntityManager().merge(request));
    }

    @Override
    public List<DomainUserRequest> getAllRequests(Domain domain) {
        return query().from(domainUserRequest).where(domainUserRequest.domain.eq(domain)).listDistinct(domainUserRequest);
    }

    @Override
    public List<DomainUserRequest> getAllRequests(Domain domain, Status status) {
        return query().from(domainUserRequest).where(domainUserRequest.domain.eq(domain).and(domainUserRequest.status.eq(status))).listDistinct(domainUserRequest);
    }
}
