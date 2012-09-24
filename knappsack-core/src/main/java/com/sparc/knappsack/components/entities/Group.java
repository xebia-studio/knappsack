package com.sparc.knappsack.components.entities;

import com.sparc.knappsack.enums.DomainType;
import org.eclipse.persistence.annotations.CascadeOnDelete;

import javax.persistence.*;
import java.util.List;

/**
 * A group is a specific type of domain.  This domain is a subdomain of an organization and owns a group of applications as well as has guest rights
 * to other applications that it does not own.
 * @see Domain
 */
@Entity
@Table(name = "ORG_GROUP", uniqueConstraints={@UniqueConstraint(columnNames={"NAME", "ORGANIZATION_ID"})})
public class Group extends BaseEntity implements Domain {

    private static final long serialVersionUID = -7673360751084836586L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "ACCESS_CODE")
    private String accessCode;

    @Column(name = "NAME")
    private String name;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "ORGANIZATION_ID")
    private Organization organization;

    @Column(name = "DOMAIN_TYPE")
    @Enumerated(EnumType.STRING)
    private DomainType domainType = DomainType.GROUP;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "ORG_GROUP_APPLICATION", joinColumns = @JoinColumn(name = "ORG_GROUP_ID"), inverseJoinColumns = @JoinColumn(name = "APPLICATION_ID"))
    @CascadeOnDelete
    private List<Application> ownedApplications;

    @ManyToMany(cascade = {CascadeType.REFRESH, CascadeType.REMOVE})
    @JoinTable(name = "ORG_GROUP_GUEST_APPLICATION_VERSION", joinColumns = @JoinColumn(name = "ORG_GROUP_ID"), inverseJoinColumns = @JoinColumn(name = "APPLICATION_VERSION_ID"))
    private List<ApplicationVersion> guestApplicationVersions;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, optional = false)
    private DomainConfiguration domainConfiguration;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccessCode() {
        return accessCode;
    }

    public void setAccessCode(String accessCode) {
        this.accessCode = accessCode;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    @SuppressWarnings("unused")
    public DomainType getDomainType() {
        return domainType;
    }

    public List<Application> getOwnedApplications() {
        return ownedApplications;
    }

    @SuppressWarnings("unused")
    public void setOwnedApplications(List<Application> ownedApplications) {
        this.ownedApplications = ownedApplications;
    }

    public List<ApplicationVersion> getGuestApplicationVersions() {
        return guestApplicationVersions;
    }

    public void setGuestApplicationVersions(List<ApplicationVersion> guestApplicationVersions) {
        this.guestApplicationVersions = guestApplicationVersions;
    }

    public DomainConfiguration getDomainConfiguration() {
        return domainConfiguration;
    }

    public void setDomainConfiguration(DomainConfiguration domainConfiguration) {
        this.domainConfiguration = domainConfiguration;
    }

    @PreRemove
    public void preRemove() {
        setGuestApplicationVersions(null);
        setOrganization(null);
    }

    @SuppressWarnings("all")
    @PrePersist
    private void prePersist() {
        if(domainConfiguration == null) {
            domainConfiguration = new DomainConfiguration();
        }
    }
}
