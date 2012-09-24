package com.sparc.knappsack.forms;

import com.sparc.knappsack.enums.DomainType;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

public class InvitationForm {
    private List<InviteeForm> inviteeForms = new ArrayList<InviteeForm>();
    private DomainType domainType;
    private Long domainId;
    private MultipartFile contactsGoogle;
    private MultipartFile contactsOutlook;

    public List<InviteeForm> getInviteeForms() {
        return inviteeForms;
    }

    public void setInviteeForms(List<InviteeForm> inviteeForms) {
        this.inviteeForms = inviteeForms;
    }

    public DomainType getDomainType() {
        return domainType;
    }

    public void setDomainType(DomainType domainType) {
        this.domainType = domainType;
    }

    public Long getDomainId() {
        return domainId;
    }

    public void setDomainId(Long domainId) {
        this.domainId = domainId;
    }

    public MultipartFile getContactsGoogle() {
        return contactsGoogle;
    }

    public void setContactsGoogle(MultipartFile contactsGoogle) {
        this.contactsGoogle = contactsGoogle;
    }

    public MultipartFile getContactsOutlook() {
        return contactsOutlook;
    }

    public void setContactsOutlook(MultipartFile contactsOutlook) {
        this.contactsOutlook = contactsOutlook;
    }
}
