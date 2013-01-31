package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.DomainRequest;
import com.sparc.knappsack.components.entities.QDomainRequest;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("domainRequestDao")
public class DomainRequestDaoImpl extends BaseDao implements DomainRequestDao {

    QDomainRequest domainRequest = QDomainRequest.domainRequest;

    @Override
    public void add(DomainRequest domainRequest) {
        getEntityManager().persist(domainRequest);
    }

    @Override
    public DomainRequest get(Long id) {
        return getEntityManager().find(DomainRequest.class, id);
    }

    @Override
    public void delete(DomainRequest domainRequest) {
        getEntityManager().remove(domainRequest);
    }

    @Override
    public void update(DomainRequest domainRequest) {
        getEntityManager().merge(getEntityManager().merge(domainRequest));
    }

    @Override
    public List<DomainRequest> getAllForDomain(long domainId) {
        return query().from(domainRequest).where(domainRequest.domain.id.eq(domainId)).list(domainRequest);
    }

    @Override
    public boolean doesDomainRequestExist(long domainId, String emailAddress) {
        return query().from(domainRequest).where(domainRequest.domain.id.eq(domainId), domainRequest.emailAddress.equalsIgnoreCase(emailAddress)).exists();
    }

    @Override
    public long countAll(Long domainId) {
        return query().from(domainRequest).where(domainRequest.domain.id.eq(domainId)).count();
    }
}
