package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.Domain;
import com.sparc.knappsack.components.entities.Group;
import com.sparc.knappsack.components.entities.Organization;
import com.sparc.knappsack.enums.DomainType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static junit.framework.Assert.*;

public class DomainServiceIT extends AbstractServiceTests {

    @Autowired(required = true)
    private DomainService domainService;

    @Autowired(required = true)
    private OrganizationService organizationService;

    @Autowired(required = true)
    private GroupService groupService;

    @Test
    public void getTest() {
        Organization organization = new Organization();
        organization.setName("Test Organization");

        organizationService.add(organization);
        organizationService.getAll();

        Group group = new Group();
        group.setName("Test Group");
        organization.getGroups().add(group);

        groupService.save(group);
        List<Group> groups = groupService.getAll();
        assertTrue(groups.size() == 1);

        Domain orgDomain = domainService.get(organization.getId(), DomainType.ORGANIZATION);
        assertNotNull(orgDomain);
        assertTrue(orgDomain.getName().equals("Test Organization"));

        Domain groupDomain = domainService.get(group.getId(), DomainType.GROUP);
        assertNotNull(groupDomain);
        assertTrue(groupDomain.getName().equals("Test Group"));

        Domain domain = domainService.get(4l, DomainType.ORGANIZATION);
        assertNull(domain);
    }
}
