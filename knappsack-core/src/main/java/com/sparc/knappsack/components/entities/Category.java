package com.sparc.knappsack.components.entities;

import org.codehaus.jackson.annotate.JsonManagedReference;
import org.eclipse.persistence.annotations.CascadeOnDelete;

import javax.persistence.*;

/**
 * A category is a division of applications with a specific purpose.
 */
@Entity
@Table(name = "CATEGORY")
@DiscriminatorValue("CATEGORY")
public class Category extends Storable {

    private static final long serialVersionUID = -6859847730817266053L;

    @Column(name = "NAME")
    private String name;

    @Column(name = "DESCRIPTION")
    private String description;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @JoinColumn(name = "ICON_ID")
    @CascadeOnDelete
    private AppFile icon;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "ORGANIZATION_ID")
    private Organization organization;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AppFile getIcon() {
        return icon;
    }

    public void setIcon(AppFile icon) {
        this.icon = icon;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }
}
