package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.Invitation;
import com.sparc.knappsack.enums.DomainType;
import com.sparc.knappsack.forms.InviteeForm;

public interface InvitationControllerService {

    public boolean inviteUser(InviteeForm inviteeForm, Long domainId, DomainType domainType, boolean deleteInvitationOnSendError);

    public boolean sendInvitation(Invitation invitation, boolean deleteOnError);

}
