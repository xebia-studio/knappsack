package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.StorageConfiguration;

import java.util.List;

public interface StorageConfigurationDao extends Dao<StorageConfiguration> {

    /**
     * @return List of all StorageConfiguration entities
     */
    List<StorageConfiguration> getAll();

    <T extends StorageConfiguration> T get(Long id, Class<T> storageConfigurationClass);

    /**
     * @param name String
     * @return StorageConfiguration with the given name
     */
    StorageConfiguration get(String name);

    StorageConfiguration getRegistrationDefault();
}
