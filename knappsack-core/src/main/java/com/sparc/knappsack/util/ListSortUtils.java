package com.sparc.knappsack.util;

import com.sparc.knappsack.enums.SortOrder;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ListSortUtils {

    public static <T> List<T> sortList(List<T> list, SortOrder sortOrder, Comparator comparator) {
        switch (sortOrder) {
            case ASCENDING:
                Collections.sort(list, comparator);
                break;
            case DESCENDING:
                Collections.sort(list, Collections.reverseOrder(comparator));
                break;
        }

        return list;
    }

}
