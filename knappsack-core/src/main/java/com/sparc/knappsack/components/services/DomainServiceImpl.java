package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.dao.DomainDao;
import com.sparc.knappsack.components.entities.Domain;
import com.sparc.knappsack.enums.DomainType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("domainService")
public class DomainServiceImpl implements DomainService {

    @Qualifier("domainDao")
    @Autowired(required = true)
    private DomainDao domainDao;

    @Override
    public Domain get(Long domainId, DomainType domainType) {
        return domainDao.get(domainId, domainType);
    }
}
