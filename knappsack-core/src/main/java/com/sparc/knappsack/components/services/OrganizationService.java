package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.Application;
import com.sparc.knappsack.components.entities.Organization;
import com.sparc.knappsack.enums.StorageType;
import com.sparc.knappsack.models.OrganizationModel;
import com.sparc.knappsack.models.UserDomainModel;

import java.util.Date;
import java.util.List;

public interface OrganizationService extends EntityService<Organization>, DomainEntityService<Organization> {
    List<Organization> getOrganizations(List<Long> organizationIds);    
    
    Organization getByName(String name);

    void mapOrgToOrgModel(Organization organization, OrganizationModel organizationModel);

    Organization createOrganization(OrganizationModel organizationModel);

    void editOrganization(OrganizationModel organizationModel);

    List<Organization> getAll();

    void removeUserFromOrganization(Long organizationId, Long UserId);

    int getTotalUsers(Organization organization);

    int getTotalApplications(Organization organization);

    int getTotalApplicationVersions(Organization organization);

    double getTotalMegabyteStorageAmount(Organization organization);

    /**
     * @param organizationId Long - retrieve all administrators for the organization with this ID
     * @return List<UserDomainModel> - a list of all administrators for the organization with this ID
     */
    List<UserDomainModel> getAllOrganizationAdmins(Long organizationId);

    List<UserDomainModel> getAllOrganizationMembers(Long organizationId, boolean includeGuests);

    List<UserDomainModel> getAllOrganizationGuests(Long organizationId);

    /**
     * @param organization Organization - check to see if this organization has reached the maximum number of applications allowed.
     * @return boolean true if the organization is at the maximum number of applications allowed.
     */
    boolean isApplicationLimit(Organization organization);

    /**
     * @param organization Organization - check to see if this organization has reached the maximum number of users allowed.
     * @return boolean true if the organization is at the maximum number of users allowed.
     */
    boolean isUserLimit(Organization organization);

    /**
     * @param organization Organization - check to see if this organization has reached the maximum bandwidth allowed for a given time period
     * @param storageType  StorageType - used to get the correct StorageService to check the bandwidth
     * @param startDate    Date - calculate the bandwidth used from this begin date
     * @param endDate      Date - calculate the bandwidth between the start date and this end date
     * @return boolean true if the bandwidth limit for this organization has been reached.
     */
    boolean isBandwidthLimit(Organization organization, StorageType storageType, Date startDate, Date endDate);

    /**
     * @param organizationId
     * @return
     */
    OrganizationModel createOrganizationModel(Long organizationId, boolean includeExternalData);

    List<OrganizationModel> createOrganizationModels(List<Organization> organizations, boolean includeExternalData);

    List<OrganizationModel> createOrganizationModelsWithoutStorageConfiguration(List<Organization> organizations, boolean includeExternalData);

    OrganizationModel createOrganizationModelWithoutStorageConfiguration(Organization organization, boolean includeExternalData);

    OrganizationModel createOrganizationModel(Organization organization, boolean includeExternalData);

    List<Application> getAllOrganizationApplications(Long organizationId);

    /**
     * @return Count of all Organizations
     */
    long countAll();

    List<OrganizationModel> getAllOrganizationsForCreateDateRange(Date minDate, Date maxDate);

    /**
     * @param organizationId Long - count all users belonging to this organization
     * @return long - the total amount of users belonging to this organization
     */
    long countOrganizationUsers(Long organizationId);

    /**
     * @param organizationId Long - count all application belonging to this organization
     * @return long - the total amount of applications belonging to this organization
     */
    long countOrganizationApps(Long organizationId);

    /**
     * @param organizationId Long - count all application versions belonging to this organization
     * @return long - the total amount of application versions belonging to this organization
     */
    long countOrganizationAppVersions(Long organizationId);

    /**
     * @param organizationId Long - count all groups belonging to this organization
     * @return long - the total amount of groups belonging to this organization
     */
    long countOrganizationGroups(Long organizationId);
}
