package com.sparc.knappsack.components.entities;

import com.sparc.knappsack.enums.DomainType;
import com.sparc.knappsack.enums.NotifiableType;

import javax.persistence.*;

/**
 * An invitation represents a request by an admin of a domain (group, organization, etc...) to a user for access to the admin's domain.
 */
@Entity
@Table(name = "INVITATION", uniqueConstraints = {@UniqueConstraint(columnNames={"EMAIL", "DOMAIN_TYPE", "DOMAIN_ID", "ROLE_ID"})})
public class Invitation extends BaseEntity implements Notifiable {

    private static final long serialVersionUID = -1170543161710859798L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "DOMAIN_TYPE")
    @Enumerated(EnumType.STRING)
    private DomainType domainType;

    @Column(name = "DOMAIN_ID")
    private Long domainId;

    @OneToOne(cascade = CascadeType.REFRESH)
    @JoinColumn(name = "ROLE_ID")
    private Role role;

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Transient
    @Override
    public NotifiableType getNotifiableType() {
        return NotifiableType.INVITATION;
    }
}
