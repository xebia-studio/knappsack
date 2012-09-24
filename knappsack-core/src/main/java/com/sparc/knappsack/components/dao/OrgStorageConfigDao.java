package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.OrgStorageConfig;

public interface OrgStorageConfigDao extends Dao<OrgStorageConfig> {

    /**
     * @param prefix String - this is essentially the name of the OrgStorageConfig.  It is used to create the path to files stored for an
     *               Organization and StorageConfiguration.
     * @return OrgStorageConfig with the given prefix
     */
    OrgStorageConfig get(String prefix);
}
