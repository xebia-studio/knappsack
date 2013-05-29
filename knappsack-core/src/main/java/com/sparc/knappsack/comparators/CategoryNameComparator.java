package com.sparc.knappsack.comparators;

import com.sparc.knappsack.components.entities.Category;

import java.io.Serializable;
import java.util.Comparator;

public class CategoryNameComparator implements Comparator<Category>, Serializable {
    private static final long serialVersionUID = 8842893238140502423L;

    @Override
    public int compare(Category category, Category category2) {
        String name1 = category.getName();
        String name2 = category2.getName();

        return name1.compareToIgnoreCase(name2);
    }
}
