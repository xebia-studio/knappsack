package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.Invitation;
import com.sparc.knappsack.components.entities.QInvitation;
import com.sparc.knappsack.enums.DomainType;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("invitationDao")
public class InvitationDaoImpl extends BaseDao implements InvitationDao {

    QInvitation invitation = QInvitation.invitation;

    @Override
    public void add(Invitation invitation) {
        getEntityManager().persist(invitation);
    }

    @Override
    public Invitation get(Long id) {
        return getEntityManager().find(Invitation.class, id);
    }

    @Override
    public void delete(Invitation invitation) {
        getEntityManager().remove(getEntityManager().merge(invitation));
    }

    @Override
    public void update(Invitation invitation) {
        getEntityManager().merge(invitation);
    }

    @Override
    public List<Invitation> getByEmail(String email) {
        return query().from(invitation).where(invitation.email.eq(email)).listDistinct(invitation);
    }

    @Override
    public List<Invitation> get(Long domainId, DomainType domainType) {
        return query().from(invitation).where(invitation.domainId.eq(domainId), invitation.domainType.eq(domainType)).listDistinct(invitation);
    }

    @Override
    public List<Invitation> get(String email, Long domainId, DomainType domainType) {
        return query().from(invitation).where(invitation.email.eq(email).and(invitation.domainId.eq(domainId).and(invitation.domainType.eq(domainType)))).listDistinct(invitation);
    }

    @Override
    public long deleteAll(Long domainId, DomainType domainType) {
        return deleteClause(invitation).where(invitation.domainId.eq(domainId), invitation.domainType.eq(domainType)).execute();
    }
}
