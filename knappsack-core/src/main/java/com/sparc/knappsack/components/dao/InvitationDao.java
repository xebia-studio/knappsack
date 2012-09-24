package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.Invitation;
import com.sparc.knappsack.enums.DomainType;

import java.util.List;

public interface InvitationDao extends Dao<Invitation> {
    List<Invitation> getByEmail(String email);

    /**
     * @param domainId Long - the ID of the domain that the user is invited to
     * @param domainType DomainType - the type of domain
     * @return List of Invitations - all invitations currently pending for this specific domain
     */
    List<Invitation> get(Long domainId, DomainType domainType);

    /**
     * @param email String - the email address of the invited user
     * @param domainId Long - the ID of the domain that the user is invited to
     * @param domainType DomainType - the type of domain
     * @return List of Invitations - the user may have multiple invitations for the same domain as long as the roles are different
     */
    List<Invitation> get(String email, Long domainId, DomainType domainType);

    long deleteAll(Long domainId, DomainType domainType);
}
