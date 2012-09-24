package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.Domain;
import com.sparc.knappsack.components.entities.Group;
import com.sparc.knappsack.components.entities.Organization;
import com.sparc.knappsack.enums.DomainType;
import org.springframework.stereotype.Repository;

@Repository("domainDao")
public class DomainDaoImpl extends BaseDao implements DomainDao {

    public Domain get(Long id, DomainType domainType) {
        if (DomainType.GROUP.equals(domainType)) {
            return getEntityManager().find(Group.class, id);
        } else if (DomainType.ORGANIZATION.equals(domainType)) {
            return getEntityManager().find(Organization.class, id);
        }

        return null;
    }
}
