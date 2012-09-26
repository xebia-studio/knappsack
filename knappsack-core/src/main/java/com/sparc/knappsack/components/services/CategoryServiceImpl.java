package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.dao.CategoryDao;
import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.enums.AppFileType;
import com.sparc.knappsack.forms.CategoryForm;
import com.sparc.knappsack.models.CategoryModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Transactional( propagation = Propagation.REQUIRED )
@Service("categoryService")
public class CategoryServiceImpl implements CategoryService {

    @Qualifier("categoryDao")
    @Autowired(required = true)
    private CategoryDao categoryDao;

    @Qualifier("storageServiceFactory")
    @Autowired(required = true)
    private StorageServiceFactory storageServiceFactory;

    @Qualifier("storageConfigurationService")
    @Autowired(required = true)
    private StorageConfigurationService storageConfigurationService;

    @Qualifier("organizationService")
    @Autowired(required = true)
    private OrganizationService organizationService;

    @Qualifier("applicationService")
    @Autowired(required = true)
    private ApplicationService applicationService;

    @Qualifier("appFileService")
    @Autowired
    private AppFileService appFileService;

    @Override
    public List<Category> getAll() {
        return categoryDao.getAll();
    }

    @Override
    public Category get(Long id) {
        Category category = null;
        if (id != null && id > 0) {
            category = categoryDao.get(id);
        }

        return category;
    }

    @Override
    public void add(Category category) {
        categoryDao.add(category);
    }

    @Override
    public void delete(Long id) {
        Category category = get(id);

        List<Application> applicationsByCategory = applicationService.getAll(category);
        if(applicationsByCategory != null && !applicationsByCategory.isEmpty()) {
            return;
        }

        if (category != null) {
            deleteIcon(category);
            Organization organization = category.getOrganization();
            organization.getCategories().remove(category);

            categoryDao.delete(category);
        }
    }

    @Override
    public void update(Category category) {
        categoryDao.update(category);
    }

    @Override
    public Category updateCategory(CategoryForm categoryForm) {

        Category savedCategory = get(categoryForm.getId());
        mapCategoryFields(categoryForm, savedCategory);
        update(savedCategory);

        return savedCategory;
    }

    @Override
    public Category saveCategory(CategoryForm categoryForm) {
        Category savedCategory = new Category();
        mapCategoryFields(categoryForm, savedCategory);
        add(savedCategory);

        return savedCategory;
    }

    private void mapCategoryFields(CategoryForm categoryForm, Category category) {
        category.setDescription(categoryForm.getDescription());
        category.setName(categoryForm.getName());

        Long storageConfigurationId = categoryForm.getStorageConfigurationId();
        Long orgStorageConfigId = categoryForm.getOrgStorageConfigId();
        //If null we are editing a category
        if (storageConfigurationId == null) {
            storageConfigurationId = category.getStorageConfiguration().getId();
        }
        Organization organization = organizationService.get(categoryForm.getOrganizationId());
        category.setOrganization(organization);
        organization.getCategories().add(category);
        category.setStorageConfiguration(storageConfigurationService.get(storageConfigurationId));
        AppFile icon = createIcon(categoryForm.getIcon(), orgStorageConfigId, storageConfigurationId, category.getUuid());
        if (icon != null) {
            icon.setStorable(category);
            category.setIcon(icon);
        }
    }

    private AppFile createIcon(MultipartFile icon, Long orgStorageConfigId, Long storageConfigurationId, String uuid) {
        StorageService storageService = getStorageService(storageConfigurationId);
        return storageService.save(icon, AppFileType.ICON.getPathName(), orgStorageConfigId, storageConfigurationId, uuid);
    }

    @Override
    public void deleteIcon(Category category) {
        AppFile appFile = category.getIcon();
        category.setIcon(null);
        appFileService.delete(appFile);
    }

    @Override
    public CategoryModel createCategoryModel(Category category) {
        CategoryModel categoryModel = null;
        if (category != null) {
            categoryModel = new CategoryModel();
            categoryModel.setId(category.getId());
            categoryModel.setName(category.getName());
            categoryModel.setDescription(category.getDescription());
            categoryModel.setIcon(appFileService.createImageModel(category.getIcon()));
        }

        return categoryModel;
    }

    @Override
    public boolean doesEntityExist(Long id) {
        return get(id) != null;
    }

    private StorageService getStorageService(Long storageConfigurationId) {
        return storageServiceFactory.getStorageService(getStorageConfiguration(storageConfigurationId).getStorageType());
    }

    private StorageConfiguration getStorageConfiguration(Long storageConfigurationId) {
        return storageConfigurationService.get(storageConfigurationId);
    }
}
