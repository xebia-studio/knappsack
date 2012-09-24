package com.sparc.knappsack.components.entities;

import com.sparc.knappsack.enums.DomainType;
import com.sparc.knappsack.enums.NotifiableType;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * An Organization is a specific type of Domain.  This domain is the root of other subdomains.
 * @see Domain
 */
@Entity
@Table(name = "ORGANIZATION")
public class Organization extends BaseEntity implements Domain, Notifiable {

    private static final long serialVersionUID = 396567098678320561L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NAME", unique = true, nullable = false)
    private String name;

    @Column(name = "ACCESS_CODE")
    private String accessCode;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "organization", fetch = FetchType.EAGER, orphanRemoval = true)
    private List<Category> categories = new ArrayList<Category>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "organization", fetch = FetchType.EAGER, orphanRemoval = true)
    private List<Group> groups = new ArrayList<Group>();

    @Column(name = "DOMAIN_TYPE")
    @Enumerated(EnumType.STRING)
    private DomainType domainType = DomainType.ORGANIZATION;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "organization", orphanRemoval = true)
    private OrgStorageConfig orgStorageConfig;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, optional = false)
    private DomainConfiguration domainConfiguration;

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

    public String getAccessCode() {
        return accessCode;
    }

    public void setAccessCode(String accessCode) {
        this.accessCode = accessCode;
    }

    public List<Category> getCategories() {
        return categories;
    }

    @SuppressWarnings("unused")
    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public DomainType getDomainType() {
        return domainType;
    }

    public OrgStorageConfig getOrgStorageConfig() {
        return orgStorageConfig;
    }

    public void setOrgStorageConfig(OrgStorageConfig orgStorageConfig) {
        this.orgStorageConfig = orgStorageConfig;
    }

    public DomainConfiguration getDomainConfiguration() {
        return domainConfiguration;
    }

    public void setDomainConfiguration(DomainConfiguration domainConfiguration) {
        this.domainConfiguration = domainConfiguration;
    }

    @SuppressWarnings("all")
    @PrePersist
    private void prePersist() {
        if(domainConfiguration == null) {
            domainConfiguration = new DomainConfiguration();
        }
    }

    @Override
    public NotifiableType getNotifiableType() {
        return NotifiableType.ORGANIZATION;
    }
}
