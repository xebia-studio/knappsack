package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.Invitation;
import com.sparc.knappsack.enums.DomainType;
import com.sparc.knappsack.enums.UserRole;
import com.sparc.knappsack.forms.InviteeForm;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface InvitationService extends EntityService<Invitation> {

    List<Invitation> getByEmail(String email);

    Invitation createInvitation(InviteeForm inviteeForm, Long domainID, DomainType domaintType);

    Invitation createInvitation(String inviteeEmail, UserRole userRole, Long domainId, DomainType domainType);

    /**
     * @param domainId Long - the ID of the domain that the user is invited to
     * @param domainType DomainType - the type of domain
     * @return List of all Invitations belonging to this specific domain
     */
    List<Invitation> getAll(Long domainId, DomainType domainType);

    /**
     * @param email String - the email address of the invited user
     * @param domainId Long - the ID of the domain that the user is invited to
     * @param domainType DomainType - the type of domain
     * @return List of Invitations - the user may have multiple invitations for the same domain as long as the roles are different
     */
    List<Invitation> getAll(String email, Long domainId, DomainType domainType);

    List<InviteeForm> parseContactsGoogle(MultipartFile contactsFile);

    List<InviteeForm> parseContactsOutlook(MultipartFile contactsFile);

    long deleteAll(Long domainId, DomainType domainType);

}
