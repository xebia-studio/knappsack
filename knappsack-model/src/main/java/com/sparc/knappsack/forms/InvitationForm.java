package com.sparc.knappsack.forms;

import com.sparc.knappsack.enums.DomainType;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

public class InvitationForm {
    private List<InviteeForm> inviteeForms = new ArrayList<InviteeForm>();
    private DomainType domainType;
    private Long domainId;
    private MultipartFile contactsGmail;
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

    public MultipartFile getContactsGmail() {
        return contactsGmail;
    }

    public void setContactsGmail(MultipartFile contactsGmail) {
        this.contactsGmail = contactsGmail;
    }

    public MultipartFile getContactsOutlook() {
        return contactsOutlook;
    }

    public void setContactsOutlook(MultipartFile contactsOutlook) {
        this.contactsOutlook = contactsOutlook;
    }
}
