package com.sparc.knappsack.components.entities;

import com.sparc.knappsack.enums.DomainType;

import javax.persistence.*;

/**
 * A UserDomain entity is the permission a User has to a specific Domain.  A User may belong to many different domains and have different
 * roles for each domain.
 *
 * @see Domain
 * @see User
 */
@Entity
@Table(name = "USER_DOMAIN",
       uniqueConstraints = {@UniqueConstraint(columnNames={"DOMAIN_TYPE", "DOMAIN_ID", "ROLE_ID", "USER_ID"})}
)
public class UserDomain extends BaseEntity {

    private static final long serialVersionUID = 71727224831146511L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "USER_ID")
    private User user;

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

    public void setId(Long id) {
        this.id = id;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
