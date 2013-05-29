package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.Organization;
import com.sparc.knappsack.components.entities.User;

import java.util.Date;
import java.util.List;

public interface OrganizationDao extends Dao<Organization> {

    /**
     * @param organizationIds List<Long> - a list of organization IDs used to get their corresponding Organization entity
     * @return List<Organization> - all organizations matching the list of given IDs
     */
    List<Organization> get(List<Long> organizationIds);

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

    /**
     * @param orgId Long - count all groups belonging to this organization
     * @return long - the total amount of groups belonging to this organization
     */
    long countOrganizationGroups(Long orgId);

    /**
     * @param orgId Long - count all users belonging to this organization
     * @param includeGroups - Whether group users should be included in the total count
     * @return long - the total amount of users belonging to this organization
     */
    long countOrganizationUsers(Long orgId, boolean includeGroups);

    /**
     * @param orgId Long - count all application belonging to this organization
     * @return long - the total amount of applications belonging to this organization
     */
    long countOrganizationApps(Long orgId);

    /**
     * @param orgId Long - count all application versions belonging to this organization
     * @return long - the total amount of application versions belonging to this organization
     */
    long countOrganizationAppVersions(Long orgId);

    /**
     * @param user
     * @return List of all Organizations which the user is an admin (ROLE_ORG_ADMIN)
     */
    List<Organization> getAdministeredOrganizationsForUser(User user);

    Organization getForGroupId(long groupId);
}
