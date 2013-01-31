package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.enums.StorageType;
import com.sparc.knappsack.forms.CategoryForm;
import com.sparc.knappsack.models.CategoryModel;
import com.sparc.knappsack.util.WebRequest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CategoryServiceIT extends AbstractServiceTests {

    @Autowired(required = true)
    private CategoryService categoryService;

    @Autowired(required = true)
    private OrganizationService organizationService;

    @Autowired(required = true)
    private StorageConfigurationService storageConfigurationService;

    @Autowired(required = true)
    private AppFileService appFileService;

    @Test
    public void addTest() {

        Category category = getCategory();
        List<Category> categories = categoryService.getAll();
        assertTrue(categories.size() == 1);
        assertEquals(categories.get(0), category);
    }
    @Test
    public void deleteTest() {
        Category category = getCategory();

        List<Category> categories = categoryService.getAll();
        assertTrue(categories.size() == 1);
        assertEquals(categories.get(0), category);
        categoryService.delete(category.getId());
        categories = categoryService.getAll();
        assertTrue(categories.size() == 0);
    }

    @Test
    public void updateTest() {
        Category category = getCategory();
        List<Category> categories = categoryService.getAll();
        assertTrue(categories.size() == 1);
        category.setName("Test Category 2");
        categoryService.update(category);
        category = categoryService.get(category.getId());
        assertTrue(category.getName().equals("Test Category 2"));
    }

    @Test
    public void saveCategoryTest() {
        Organization organization = getOrganization();

        List<Category> categories = categoryService.getAll();
        assertTrue(categories.size() == 0);

        CategoryForm categoryForm = new CategoryForm();
        categoryForm.setName("Test Category");
        categoryForm.setDescription("Test Description");
        categoryForm.setEditing(false);
        categoryForm.setOrganizationId(organization.getId());
        categoryForm.setStorageConfigurationId(organization.getStorageConfigurations().get(0).getId());
        Category category = categoryService.saveCategory(categoryForm);
        categories = categoryService.getAll();
        assertTrue(categories.size() == 1);
        assertTrue(category.getName().equals("Test Category"));
    }

    @Test
    public void updateCategoryFromFormTest() {
        Organization organization = getOrganization();

        List<Category> categories = categoryService.getAll();
        assertTrue(categories.size() == 0);

        CategoryForm categoryForm = new CategoryForm();
        categoryForm.setName("Test Category");
        categoryForm.setDescription("Test Description");
        categoryForm.setEditing(false);
        categoryForm.setOrganizationId(organization.getId());
        categoryForm.setStorageConfigurationId(organization.getStorageConfigurations().get(0).getId());
        Category category = categoryService.saveCategory(categoryForm);
        categories = categoryService.getAll();
        assertTrue(categories.size() == 1);
        assertTrue(category.getName().equals("Test Category"));

        categoryForm.setName("Test Category 2");
        categoryForm.setId(category.getId());
        categoryService.updateCategory(categoryForm);
        categories = categoryService.getAll();
        assertTrue(categories.size() == 1);
        assertTrue(category.getName().equals("Test Category 2"));
    }

    @Test
    public void createModelTest() {
        WebRequest.getInstance("http", "serverName", 80, "/knappsack");

        Organization organization = getOrganization();

        AppFile icon = new AppFile();
        icon.setName("icon");
        icon.setRelativePath("/test");
        icon.setStorageType(StorageType.LOCAL);

        List<Category> categories = categoryService.getAll();
        assertTrue(categories.size() == 0);

        Category category = new Category();
        category.setName("Test Category");
        category.setDescription("Test Description");
        category.setOrganization(organization);
        category.setStorageConfiguration(organization.getStorageConfigurations().get(0));
        category.setIcon(icon);
        CategoryModel categoryModel = categoryService.createCategoryModel(category);
        assertTrue(categoryModel.getDescription().equals("Test Description"));
        assertTrue(categoryModel.getName().equals("Test Category"));
        assertTrue(categoryModel.getIcon() != null);
    }

    private Category getCategory() {
        Organization organization = getOrganization();

        Category category = new Category();
        category.setName("Test Category");
        category.setOrganization(organization);
        organization.getCategories().add(category);

        categoryService.add(category);

        return category;
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
}
