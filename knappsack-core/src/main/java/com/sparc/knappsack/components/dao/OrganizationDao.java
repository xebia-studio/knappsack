package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.Organization;

import java.util.Date;
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

    /**
     * @return Total number of Organizations
     */
    long countAll();

    /**
     * Get a list of all Organizations for a given Date range
     * @param minDate
     * @param maxDate
     * @return
     */
    List<Organization> getAllForCreateDateRange(Date minDate, Date maxDate);
}
