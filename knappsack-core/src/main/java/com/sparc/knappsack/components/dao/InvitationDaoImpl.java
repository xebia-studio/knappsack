package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.components.entities.Domain;
import com.sparc.knappsack.components.entities.Invitation;
import com.sparc.knappsack.components.entities.QInvitation;
import com.sparc.knappsack.components.entities.Role;
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
    public List<Invitation> getAllForDomain(Long domainId) {
        return query().from(invitation).where(invitation.domain.id.eq(domainId)).listDistinct(invitation);
    }
    
    @Override
    public long countAll(Long domainId) {
        return query().from(invitation).where(invitation.domain.id.eq(domainId)).countDistinct();
    }

    @Override
    public List<Invitation> getAllForEmailAndDomain(String email, Long domainId) {
        return query().from(invitation).where(invitation.email.eq(email).and(invitation.domain.id.eq(domainId))).listDistinct(invitation);
    }

    @Override
    public long deleteAllForDomain(Domain domain) {
        return deleteClause(invitation).where(invitation.domain.eq(domain)).execute();
    }

    @Override
    public Invitation get(String email, Domain domain, Role role) {
        return query().from(invitation).where(invitation.email.eq(email).and(invitation.domain.eq(domain).and(invitation.role.eq(role)))).uniqueResult(invitation);
    }
}
