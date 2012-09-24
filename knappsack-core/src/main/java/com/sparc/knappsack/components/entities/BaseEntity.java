package com.sparc.knappsack.components.entities;

import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@MappedSuperclass
public class BaseEntity implements Serializable {

    private static final long serialVersionUID = -8027314860537763026L;

    @Version
    @Column(name = "VERSION")
    private int version = 0;

    @Column(name = "CREATE_DATE")
    @Temporal(TemporalType.DATE)
    private Date createDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "LAST_UPDATE")
    private Date lastUpdate;

    @Column(name = "CHANGED_BY", nullable = true)
    private String changedBy;

    @Column(name = "UUID", nullable = false, unique = true)
    private String uuid;

    @SuppressWarnings("unused")
    public int getVersion() {
        return version;
    }

    @SuppressWarnings("unused")
    public void setVersion(int version) {
        this.version = version;
    }

    @SuppressWarnings("unused")
    public Date getCreateDate() {
        return (Date) createDate.clone();
    }

    @SuppressWarnings("unused")
    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @SuppressWarnings("unused")
    public Date getLastUpdate() {
        return (Date) lastUpdate.clone();
    }

    @SuppressWarnings("unused")
    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @SuppressWarnings("unused")
    public String getChangedBy() {
        return changedBy;
    }

    @SuppressWarnings("unused")
    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public String getUuid() {
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
        }
        return uuid;
    }

    @PrePersist
    protected void onCreate() {
        Object user = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(user instanceof User) {
            changedBy = ((User)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        } else if(user instanceof String) {
            changedBy = (String) user;
        }
        lastUpdate = createDate = new Date();

        // Generate a new UUID for the entity
        getUuid();
    }

    @PreUpdate
    protected void onUpdate() {
        Object user = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(user instanceof User) {
            changedBy = ((User)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();
        } else if(user instanceof String) {
            changedBy = (String) user;
        }
        lastUpdate = new Date();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof BaseEntity)) {
            return false;
        }

        BaseEntity that = (BaseEntity) o;

        return !(uuid != null ? !uuid.equals(that.uuid) : that.uuid != null);
    }

    @Override
    public int hashCode() {
        return uuid != null ? uuid.hashCode() : super.hashCode();
    }
}
