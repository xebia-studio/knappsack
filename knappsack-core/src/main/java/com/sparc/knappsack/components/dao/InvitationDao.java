package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.Domain;
import com.sparc.knappsack.components.entities.Invitation;
import com.sparc.knappsack.components.entities.Role;

import java.util.List;

public interface InvitationDao extends Dao<Invitation> {
    List<Invitation> getByEmail(String email);

    /**
     * @param domainId Long - the ID of the domain that the user is invited to
     * @return List of Invitations - all invitations currently pending for this specific domain
     */
    List<Invitation> getAllForDomain(Long domainId);

    /**
     * @param domainId Long - the ID of the domain that the user is invited to
     * @return long - a count of all the invitations currently in the system for this domain
     */
    long countAll(Long domainId);

    /**
     * @param email String - the email address of the invited user
     * @param domainId Long - the ID of the domain that the user is invited to
     * @return List of Invitations - the user may have multiple invitations for the same domain as long as the roles are different
     */
    List<Invitation> getAllForEmailAndDomain(String email, Long domainId);

    long deleteAllForDomain(Domain domain);

    Invitation get(String email, Domain domain, Role role);
}
