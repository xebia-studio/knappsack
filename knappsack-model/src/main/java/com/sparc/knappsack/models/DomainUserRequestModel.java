package com.sparc.knappsack.models;

import com.sparc.knappsack.enums.Status;

public class DomainUserRequestModel {

    private Long id;
    private UserModel user;
    private DomainModel domain;
    private Status status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserModel getUser() {
        return user;
    }

    public void setUser(UserModel user) {
        this.user = user;
    }

    public DomainModel getDomain() {
        return domain;
    }

    public void setDomain(DomainModel domain) {
        this.domain = domain;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
