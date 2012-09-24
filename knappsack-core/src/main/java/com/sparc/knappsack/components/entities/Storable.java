package com.sparc.knappsack.components.entities;

import org.codehaus.jackson.annotate.JsonIgnore;

import javax.persistence.*;
import java.util.UUID;

/**
 * A Storable entity is one that has one or many AppFile entities associated with it.  The purpose of this is to keep track of the files for
 * this Storable entity.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name="DISCRIMINATOR", discriminatorType=DiscriminatorType.STRING)
@Table(name = "STORABLE")
public abstract class Storable extends BaseEntity {

    private static final long serialVersionUID = 131232227317295130L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @OneToOne
    @JsonIgnore
    @JoinColumn(name = "STORAGE_CONFIGURATION_ID")
    private StorageConfiguration storageConfiguration;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StorageConfiguration getStorageConfiguration() {
        return storageConfiguration;
    }

    public void setStorageConfiguration(StorageConfiguration storageConfiguration) {
        this.storageConfiguration = storageConfiguration;
    }
}
