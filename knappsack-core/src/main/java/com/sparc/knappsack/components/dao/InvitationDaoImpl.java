package com.sparc.knappsack.components.dao;

import com.mysema.query.jpa.JPASubQuery;
import com.mysema.query.types.expr.BooleanExpression;
import com.mysema.query.types.query.ListSubQuery;
import com.sparc.knappsack.components.entities.Domain;
import com.sparc.knappsack.components.entities.Invitation;
import com.sparc.knappsack.components.entities.QInvitation;
import com.sparc.knappsack.components.entities.Role;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
        return query().from(invitation).where(invitation.email.equalsIgnoreCase(email)).listDistinct(invitation);
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
    public long countAllForOrganizationIncludingGroups(long organizationId) {
        JPASubQuery invitationIdSubQuery = subQuery().from(invitation)
                .where(invitation.domain.id.in(
                        subQuery().from(group)
                                .where(group.organization.id.eq(organizationId))
                                .list(group.id)
                ).or(invitation.domain.id.eq(organizationId)));
        ListSubQuery<Invitation> invitationEmails = invitationIdSubQuery.list(invitation);

        return query().from(invitation).where(invitation.in(invitationEmails)).count();
    }

    @Override
    public List<Invitation> getAllForEmailAndDomain(String email, Long domainId) {
        return query().from(invitation).where(invitation.email.equalsIgnoreCase(email).and(invitation.domain.id.eq(domainId))).listDistinct(invitation);
    }

    @Override
    public long countEmailsWithoutInvitationsForOrganization(Set<String> emails, long organizationId, boolean includeGroups) {
        List<BooleanExpression> domainExpressions = new ArrayList<BooleanExpression>();

        // Boolean expression for organization
        domainExpressions.add(invitation.domain.id.eq(organizationId));

        if (includeGroups) {
            // SubQuery to get all groups for an Organization
            JPASubQuery groupsForOrganization = subQuery().from(group).where(group.organization.id.eq(organizationId));

            // Boolean expression for groups
            domainExpressions.add(invitation.domain.id.in(groupsForOrganization.list(group.id)));
        }

        ListSubQuery<String> emailListSubQuery = subQuery().from(invitation)
                .where(invitation.email.in(emails)
                        .and(BooleanExpression.anyOf(domainExpressions.toArray(new BooleanExpression[domainExpressions.size()])))
                ).groupBy(invitation.email).list(invitation.email);

        return emails.size() - query().from(invitation).where(invitation.email.in(emailListSubQuery)).countDistinct();
    }

    @Override
    public List<Invitation> getAllForEmailsAndDomains(List<String> emails, List<Long> domainIds) {
        for (String email : emails) {
            email = StringUtils.trimAllWhitespace(email.toLowerCase());
        }

        // Boolean expression for organization
        BooleanExpression domainBooleanExpression = invitation.domain.id.in(domainIds);

        ListSubQuery<String> emailListSubQuery = subQuery().from(invitation)
                .where(invitation.email.toLowerCase().in(emails)
                        .and(domainBooleanExpression)
                ).groupBy(invitation.email).list(invitation.email);

        return query().from(invitation).where(invitation.email.in(emailListSubQuery)).listDistinct(invitation);
    }

    @Override
    public long deleteAllForDomain(Domain domain) {
        return deleteClause(invitation).where(invitation.domain.eq(domain)).execute();
    }

    @Override
    public Invitation get(String email, Domain domain, Role role) {
        return query().from(invitation).where(invitation.email.equalsIgnoreCase(email).and(invitation.domain.eq(domain).and(invitation.role.eq(role)))).uniqueResult(invitation);
    }
}
