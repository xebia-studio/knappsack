package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.Category;
import com.sparc.knappsack.forms.CategoryForm;
import com.sparc.knappsack.models.CategoryModel;

import java.util.List;

public interface CategoryService extends EntityService<Category> {
    /**
     * @return List<Category> - return all categories in the system
     */
    List<Category> getAll();

    /**
     * @param categoryForm CategoryForm
     * @return Category - return a Category populated with data from the CategoryForm
     */
    Category updateCategory(CategoryForm categoryForm);

    /**
     * @param categoryForm CategoryForm
     * @return Category - create and persist a Category populated with data from the CategoryForm
     */
    Category saveCategory(CategoryForm categoryForm);

    /**
     * @param category Category - delete the icon from this Category
     */
    void deleteIcon(Category category);

    /**
     * @param category Category
     * @return CategoryModel - return a CategoryModel with data populated from the Category
     */
    CategoryModel createCategoryModel(Category category);
}
