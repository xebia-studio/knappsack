package com.sparc.knappsack.comparators;

import com.sparc.knappsack.enums.Language;

import java.io.Serializable;
import java.util.Comparator;

public class LanguageComparator implements Comparator<Language>, Serializable {

    private static final long serialVersionUID = 9125068883474431966L;

    @Override
    public int compare(Language language, Language language2) {
        Integer languageSortOrder = language.getSortOrder();
        Integer language2SortOrder = language2.getSortOrder();

        return languageSortOrder.compareTo(language2SortOrder);
    }
}
