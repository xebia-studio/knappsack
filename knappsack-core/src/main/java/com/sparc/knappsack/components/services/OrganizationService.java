package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.Application;
import com.sparc.knappsack.components.entities.Organization;
import com.sparc.knappsack.models.OrganizationModel;
import com.sparc.knappsack.models.UserDomainModel;

import java.util.List;

public interface OrganizationService extends EntityService<Organization> {

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
    * @param organizationId
    * @return
    */
    OrganizationModel createOrganizationModel(Long organizationId);

    List<Application> getAllOrganizationApplications(Long organizationId);

    /**
     * @return Count of all Organizations
     */
    long countAll();
}
