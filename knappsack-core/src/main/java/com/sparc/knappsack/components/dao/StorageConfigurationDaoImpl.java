package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.QStorageConfiguration;
import com.sparc.knappsack.components.entities.StorageConfiguration;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StorageConfigurationDaoImpl extends BaseDao implements StorageConfigurationDao {
    QStorageConfiguration storageConfiguration = QStorageConfiguration.storageConfiguration;

    public void add(StorageConfiguration storageConfiguration) {
        getEntityManager().persist(storageConfiguration);
    }

    public List<StorageConfiguration> getAll() {
        return query().from(storageConfiguration).list(storageConfiguration);
    }

    public StorageConfiguration get(Long id) {
        return getEntityManager().find(StorageConfiguration.class, id);
    }

    public StorageConfiguration get(String name) {
        return query().from(storageConfiguration).where(storageConfiguration.name.equalsIgnoreCase(name.trim())).uniqueResult(storageConfiguration);
    }

    public void delete(StorageConfiguration storageConfiguration) {
        getEntityManager().remove(getEntityManager().merge(storageConfiguration));
    }

    public void update(StorageConfiguration storageConfiguration) {
        getEntityManager().merge(storageConfiguration);
    }
}
