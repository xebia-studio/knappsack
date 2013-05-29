package com.sparc.knappsack.forms;

import com.sparc.knappsack.enums.ApplicationType;
import com.sparc.knappsack.enums.DomainType;

public abstract class AbstractKeyVaultEntryForm {
    private Long id;
    private String name;
    private DomainType domainType;
    private Long domainId;

    public abstract ApplicationType getApplicationType();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DomainType getDomainType() {
        return domainType;
    }

    public void setDomainType(DomainType domainType) {
        this.domainType = domainType;
    }

    public Long getDomainId() {
        return domainId;
    }

    public void setDomainId(Long domainId) {
        this.domainId = domainId;
    }
}
