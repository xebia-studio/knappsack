package com.sparc.knappsack.components.dao;

import com.sparc.knappsack.EntityUtil;
import com.sparc.knappsack.components.entities.Category;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CategoryDaoIT extends AbstractDaoIntegrationTests {

    @Autowired(required = true)
    private CategoryDao categoryDao;

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

        for (Category category : categories) {
            categoryDao.add(category);
        }

        List returnedCategories = categoryDao.getAll();
        assertEquals(returnedCategories.size(), categories.size());
    }

    private void compareCategories(Category category1, Category category2) {
        assertEquals(category1.getId(), category2.getId());
        assertEquals(category1.getDescription(), category2.getDescription());
        assertEquals(category1.getName(), category2.getName());
    }

}
