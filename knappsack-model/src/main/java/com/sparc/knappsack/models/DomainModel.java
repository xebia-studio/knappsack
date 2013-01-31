package com.sparc.knappsack.models;

import com.sparc.knappsack.enums.DomainType;

public abstract class DomainModel {

    private Long id;
    private String name;

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

    public abstract DomainType getDomainType();
}
