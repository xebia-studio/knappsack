package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.Organization;

import java.util.List;

public interface OrganizationDao extends Dao<Organization> {

    /**
     * @return List of all Organization entities
     */
    List<Organization> getAll();

    /**
     * @param name String - name of the Organization
     * @return Organization with the given name
     */
    Organization get(String name);
}
