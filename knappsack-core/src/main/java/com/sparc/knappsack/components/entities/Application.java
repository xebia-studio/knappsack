package com.sparc.knappsack.components.entities;

import com.sparc.knappsack.enums.ApplicationType;
import com.sparc.knappsack.enums.NotifiableType;
import com.sparc.knappsack.enums.StorableType;
import org.codehaus.jackson.annotate.JsonManagedReference;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * An Application is the generic portion of software hosted by Knappsack.
 */
@Entity
@Table(name = "APPLICATION")
public class Application extends Storable implements Notifiable {

    private static final long serialVersionUID = 1265568333226947048L;

    @Column(name = "NAME")
    private String name;

    @Column(name ="DESCRIPTION", length = 10000)
    private String description;

    @Column(name = "APPLICATION_TYPE")
    @Enumerated(EnumType.STRING)
    private ApplicationType applicationType;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "ICON_ID")
    @JsonManagedReference
    @Fetch(FetchMode.JOIN)
    private AppFile icon;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "APPLICATION_SCREENSHOT", joinColumns = @JoinColumn(name = "APPLICATION_ID"), inverseJoinColumns = @JoinColumn(name = "SCREENSHOT_ID"))
    @JsonManagedReference
    @BatchSize(size=30)
    private List<AppFile> screenShots = new ArrayList<AppFile>();

    @ManyToOne(optional = false)
    @JoinColumn(name = "CATEGORY_ID")
    private Category category;

    @ManyToOne(optional = false)
    @JoinColumn(name = "GROUP_ID")
    @Fetch(FetchMode.JOIN)
    private Group ownedGroup;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "application", orphanRemoval = true)
    @JsonManagedReference
    @BatchSize(size=5)
    private List<ApplicationVersion> applicationVersions = new ArrayList<ApplicationVersion>();

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

    public ApplicationType getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(ApplicationType applicationType) {
        this.applicationType = applicationType;
    }

    public AppFile getIcon() {
        return icon;
    }

    public void setIcon(AppFile icon) {
        this.icon = icon;
    }

    public List<AppFile> getScreenShots() {
        return screenShots;
    }

    public void setScreenShots(List<AppFile> screenShots) {
        this.screenShots = screenShots;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Group getOwnedGroup() {
        return ownedGroup;
    }

    public void setOwnedGroup(Group ownedGroup) {
        this.ownedGroup = ownedGroup;
    }

    public List<ApplicationVersion> getApplicationVersions() {
        return applicationVersions;
    }

    public void setApplicationVersions(List<ApplicationVersion> applicationVersions) {
        this.applicationVersions = applicationVersions;
    }

    public StorableType getStorableType() {
        return StorableType.APPLICATION;
    }

    @Transient
    @Override
    public NotifiableType getNotifiableType() {
        return NotifiableType.APPLICATION;
    }
}
