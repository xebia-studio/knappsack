package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.EntityUtil;
import com.sparc.knappsack.components.entities.Category;
import com.sparc.knappsack.components.entities.LocalStorageConfiguration;
import com.sparc.knappsack.components.entities.OrgStorageConfig;
import com.sparc.knappsack.components.entities.Organization;
import com.sparc.knappsack.components.services.AbstractServiceTests;
import com.sparc.knappsack.components.services.OrganizationService;
import com.sparc.knappsack.enums.StorageType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class CategoryDaoIT extends AbstractServiceTests {

    @Autowired(required = true)
    private CategoryDao categoryDao;

    @Autowired(required = true)
    private OrganizationService organizationService;

    /*@Test
    public void testCategoryDao() {
        Category category = EntityUtil.createCategory();
        categoryDao.add(category);
        assertTrue(category.getId() == 1);

        Category returnedCategory = categoryDao.get(category.getId());
        assertNotNull(returnedCategory);
        compareCategories(category, returnedCategory);

        categoryDao.delete(returnedCategory);
        Category deletedCategory = categoryDao.get(returnedCategory.getId());
        assertNull(deletedCategory);
    }*/

    @Test
    public void testCategoryDaoGetAll() {
        List<Category> categories = new ArrayList<Category>();
        categories.add(EntityUtil.createCategory());
        categories.add(EntityUtil.createCategory());
        categories.add(EntityUtil.createCategory());

        Organization organization = getOrganization();

        for (Category category : categories) {
            category.setOrganization(organization);
            categoryDao.add(category);
        }

        List returnedCategories = categoryDao.getAll();
        assertEquals(returnedCategories.size(), categories.size());
    }

    private Organization getOrganization() {
        Organization organization = new Organization();
        organization.setName("Test Organization");

        LocalStorageConfiguration localStorageConfiguration = new LocalStorageConfiguration();
        localStorageConfiguration.setBaseLocation("/path");
        localStorageConfiguration.setName("Local Storage Configuration");
        localStorageConfiguration.setStorageType(StorageType.LOCAL);

        OrgStorageConfig orgStorageConfig = new OrgStorageConfig();
        orgStorageConfig.getStorageConfigurations().add(localStorageConfiguration);
        orgStorageConfig.setPrefix("testPrefix");
        orgStorageConfig.setOrganization(organization);
        organization.setOrgStorageConfig(orgStorageConfig);

        organizationService.add(organization);

        return organization;
    }

    private void compareCategories(Category category1, Category category2) {
        assertEquals(category1.getId(), category2.getId());
        assertEquals(category1.getDescription(), category2.getDescription());
        assertEquals(category1.getName(), category2.getName());
    }

}
