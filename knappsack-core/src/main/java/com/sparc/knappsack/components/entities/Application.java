package com.sparc.knappsack.components.entities;

import com.sparc.knappsack.enums.ApplicationType;
import com.sparc.knappsack.enums.NotifiableType;
import org.codehaus.jackson.annotate.JsonManagedReference;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * An Application is the generic portion of software hosted by Knappsack.
 */
@Entity
@Table(name = "APPLICATION")
@DiscriminatorValue("APPLICATION")
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
    private AppFile icon;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "APPLICATION_SCREENSHOT", joinColumns = @JoinColumn(name = "APPLICATION_ID"), inverseJoinColumns = @JoinColumn(name = "SCREENSHOT_ID"))
    @JsonManagedReference
    private List<AppFile> screenShots = new ArrayList<AppFile>();

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "CATEGORY_ID")
    private Category category;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "application", orphanRemoval = true)
    @JsonManagedReference
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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public List<ApplicationVersion> getApplicationVersions() {
        return applicationVersions;
    }

    @Transient
    @Override
    public NotifiableType getNotifiableType() {
        return NotifiableType.APPLICATION;
    }
}
