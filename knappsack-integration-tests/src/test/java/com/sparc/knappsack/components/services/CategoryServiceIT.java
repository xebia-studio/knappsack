package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.AppFile;
import com.sparc.knappsack.components.entities.Category;
import com.sparc.knappsack.components.entities.LocalStorageConfiguration;
import com.sparc.knappsack.components.entities.Organization;
import com.sparc.knappsack.enums.StorageType;
import com.sparc.knappsack.forms.CategoryForm;
import com.sparc.knappsack.models.CategoryModel;
import com.sparc.knappsack.util.WebRequest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

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
        Category category = new Category();
        category.setName("Test Category");
        category.setDescription("Test Description");
        categoryService.add(category);
        List<Category> categories = categoryService.getAll();
        assertTrue(categories.size() == 1);
        assertTrue(categories.get(0).getName().equals("Test Category"));
    }
    @Test
    public void deleteTest() {
        Organization organization = new Organization();
        organization.setName("Test Organization");
        organizationService.add(organization);

        Category category = new Category();
        category.setName("Test Category");
        category.setDescription("Test Description");
        category.setOrganization(organization);
        categoryService.add(category);

        List<Category> categories = categoryService.getAll();
        assertTrue(categories.size() == 1);
        assertTrue(categories.get(0).getName().equals("Test Category"));
        categoryService.delete(category.getId());
        categories = categoryService.getAll();
        assertTrue(categories.size() == 0);
    }

    @Test
    public void updateTest() {
        Category category = new Category();
        category.setName("Test Category");
        category.setDescription("Test Description");
        categoryService.add(category);
        List<Category> categories = categoryService.getAll();
        assertTrue(categories.size() == 1);
        category.setName("Test Category 2");
        categoryService.update(category);
        category = categoryService.get(category.getId());
        assertTrue(category.getName().equals("Test Category 2"));
    }

    @Test
    public void saveCategoryTest() {
        Organization organization = new Organization();
        organization.setName("Test Organization");
        organizationService.add(organization);

        LocalStorageConfiguration localStorageConfiguration = new LocalStorageConfiguration();
        localStorageConfiguration.setName("Local Storage Configuration");
        localStorageConfiguration.setStorageType(StorageType.LOCAL);
        storageConfigurationService.add(localStorageConfiguration);

        List<Category> categories = categoryService.getAll();
        assertTrue(categories.size() == 0);

        CategoryForm categoryForm = new CategoryForm();
        categoryForm.setName("Test Category");
        categoryForm.setDescription("Test Description");
        categoryForm.setEditing(false);
        categoryForm.setOrganizationId(organization.getId());
        categoryForm.setStorageConfigurationId(localStorageConfiguration.getId());
        Category category = categoryService.saveCategory(categoryForm);
        categories = categoryService.getAll();
        assertTrue(categories.size() == 1);
        assertTrue(category.getName().equals("Test Category"));
    }

    @Test
    public void updateCategoryFromFormTest() {
        Organization organization = new Organization();
        organization.setName("Test Organization");
        organizationService.add(organization);

        LocalStorageConfiguration localStorageConfiguration = new LocalStorageConfiguration();
        localStorageConfiguration.setName("Local Storage Configuration");
        localStorageConfiguration.setStorageType(StorageType.LOCAL);
        storageConfigurationService.add(localStorageConfiguration);

        List<Category> categories = categoryService.getAll();
        assertTrue(categories.size() == 0);

        CategoryForm categoryForm = new CategoryForm();
        categoryForm.setName("Test Category");
        categoryForm.setDescription("Test Description");
        categoryForm.setEditing(false);
        categoryForm.setOrganizationId(organization.getId());
        categoryForm.setStorageConfigurationId(localStorageConfiguration.getId());
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

        Organization organization = new Organization();
        organization.setName("Test Organization");
        organizationService.add(organization);

        LocalStorageConfiguration localStorageConfiguration = new LocalStorageConfiguration();
        localStorageConfiguration.setName("Local Storage Configuration");
        localStorageConfiguration.setStorageType(StorageType.LOCAL);
        storageConfigurationService.add(localStorageConfiguration);

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
        category.setStorageConfiguration(localStorageConfiguration);
        category.setIcon(icon);
        CategoryModel categoryModel = categoryService.createCategoryModel(category);
        assertTrue(categoryModel.getDescription().equals("Test Description"));
        assertTrue(categoryModel.getName().equals("Test Category"));
        assertTrue(categoryModel.getIcon() != null);
    }
}
