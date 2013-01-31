package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.Domain;
import com.sparc.knappsack.components.entities.Invitation;
import com.sparc.knappsack.components.entities.Role;
import com.sparc.knappsack.enums.UserRole;
import com.sparc.knappsack.forms.InviteeForm;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface InvitationService extends EntityService<Invitation> {

    Invitation get(String email, Domain domain, Role role);

    List<Invitation> getByEmail(String email);

    Invitation createInvitation(InviteeForm inviteeForm, Long domainID);

    Invitation createInvitation(String inviteeEmail, UserRole userRole, Long domainId);

    /**
     * @param domainId Long - the ID of the domain that the user is invited to
     * @return List of all Invitations belonging to this specific domain
     */
    List<Invitation> getAll(Long domainId);

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
    List<Invitation> getAll(String email, Long domainId);

    List<InviteeForm> parseContactsGoogle(MultipartFile contactsFile);

    List<InviteeForm> parseContactsOutlook(MultipartFile contactsFile);

    long deleteAll(Long domainId);

    long deleteAll(Domain domain);

    /**
     * Note: The invitation will only be deleted if the user already exists in Knappsack.  Deleting an invitation
     * for a un-registered user would not allow the user to register since registration is invitation only.
     *
     * @param invitationId Invitation ID to be deleted.
     */
    void deleteInvitation(Long invitationId);

}
