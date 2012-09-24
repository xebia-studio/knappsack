package com.sparc.knappsack.components.entities;

import com.sparc.knappsack.enums.DomainType;
import com.sparc.knappsack.enums.NotifiableType;
import com.sparc.knappsack.enums.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * A User is a person in the Knappsack system. This entity contains relevant information pertaining to the person.
 * @see UserDetails
 */
@Entity
@Table(name = "USER")
public class User extends BaseEntity implements UserDetails, Notifiable {

    private static final long serialVersionUID = 1690650481252062307L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "USERNAME", unique = true)
    private String username;

    @Column(name = "PASSWORD")
    private String password;

    @Column(name = "EMAIL")
    private String email;

    @ElementCollection
    @CollectionTable(name = "USER_OPENIDIDENTIFIER", joinColumns = @JoinColumn(name = "USER_ID"))
    @Column(name = "OPENIDIDENTIFIER")
    private List<String> openIdIdentifiers = new ArrayList<String>();

    @Column(name = "FIRST_NAME")
    private String firstName;

    @Column(name = "LAST_NAME")
    private String lastName;

    @Column(name = "FULL_NAME")
    private String fullName;

    @ManyToMany(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinTable(name = "USER_ROLE", joinColumns = @JoinColumn(name = "USER_ID"), inverseJoinColumns = @JoinColumn(name = "ROLE_ID"))
    private List<Role> roles = new ArrayList<Role>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user", orphanRemoval = true)
    private List<UserDomain> userDomains = new ArrayList<UserDomain>();

    @Column(name = "ACTIVATED")
    private boolean activated = false;

    @Column(name = "ACTIVATION_CODE")
    private String activationCode;

    @Column(name = "PASSWORD_EXPIRED")
    private boolean passwordExpired = false;

    public User() {
        this.username = "";
        this.password = "";
        this.email = "";
        this.firstName = "";
        this.lastName = "";
        this.fullName = "";
        activationCode = UUID.randomUUID().toString();
    }

    public User(String username, String password, String email, String firstName, String lastName, Collection<? extends Role> authorities) {
        this.username = username.toLowerCase().trim();
        this.password = password;
        this.email = email.toLowerCase().trim();
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = firstName + " " + lastName;

        this.openIdIdentifiers = new ArrayList<String>();

        roles.addAll(authorities);

        activationCode = UUID.randomUUID().toString();
    }

    @Transient
    @Override
    public NotifiableType getNotifiableType() {
        return NotifiableType.USER;
    }

    public Long getId() {
        return id;
    }

    public List<Role> getRoles() {
        return roles;
    }

    @SuppressWarnings("unused")
    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    @Transient
    public Collection<GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();

        for (Role role : getRoles())
        {
            authorities.add(new SimpleGrantedAuthority(role.getAuthority()));
        }

        return authorities;
    }

    public String getEmail() {
        return email;
    }

    @SuppressWarnings("unused")
    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return fullName;
    }

    public List<String> getOpenIdIdentifiers() {
        return openIdIdentifiers;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public boolean isAccountNonExpired() {
        return true;
    }

    public boolean isAccountNonLocked() {
        return true;
    }

    public boolean isCredentialsNonExpired() {
        return true;
    }

    public boolean isEnabled() {
        return true;
    }

    public void setUsername(String username) {
        this.username = username.toLowerCase().trim();
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email.toLowerCase().trim();
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @SuppressWarnings("unused")
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @SuppressWarnings("unused")
    public void setOpenIdIdentifiers(List<String> openIdIdentifiers) {
        this.openIdIdentifiers = openIdIdentifiers;
    }

    public boolean isAnyAdmin() {
        return isSystemAdmin() || isGroupAdmin() || isOrganizationAdmin();
    }
    
    public boolean isSystemAdmin() {
        for (Role role : roles) {
            if(UserRole.ROLE_ADMIN.toString().equals(role.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    public boolean isOrganizationAdmin() {
        for (UserDomain userDomain : userDomains) {
            if (DomainType.ORGANIZATION.equals(userDomain.getDomainType()) && UserRole.ROLE_ORG_ADMIN.toString().equals(userDomain.getRole().getAuthority())) {
                return true;
            }
        }

        return false;
    }

    public boolean isGroupAdmin() {
        for (UserDomain userDomain : userDomains) {
            if (DomainType.GROUP.equals(userDomain.getDomainType()) && UserRole.ROLE_GROUP_ADMIN.toString().equals(userDomain.getRole().getAuthority())) {
                return true;
            }
        }

        return false;
    }

    public boolean isSystemOrOrganizationAdmin() {
        return isSystemAdmin() || isOrganizationAdmin();
    }

    public boolean isOrganizationOrGroupAdmin() {
        return isGroupAdmin() || isOrganizationAdmin();
    }

    public List<UserDomain> getUserDomains() {
        return userDomains;
    }

    @SuppressWarnings("unused")
    public void setUserDomains(List<UserDomain> userDomains) {
        this.userDomains = userDomains;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public String getActivationCode() {
        return activationCode;
    }

    @SuppressWarnings("unused")
    public void setActivationCode(String activationCode) {
        this.activationCode = activationCode;
    }

    public boolean isPasswordExpired() {
        return passwordExpired;
    }

    public void setPasswordExpired(boolean passwordExpired) {
        this.passwordExpired = passwordExpired;
    }
}

