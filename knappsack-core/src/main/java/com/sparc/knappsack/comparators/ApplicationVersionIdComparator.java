package com.sparc.knappsack.comparators;

import com.sparc.knappsack.components.entities.ApplicationVersion;
import com.sparc.knappsack.enums.SortOrder;

import java.io.Serializable;
import java.util.Comparator;

public class ApplicationVersionIdComparator implements Comparator<ApplicationVersion>,Serializable {

    private static final long serialVersionUID = -7171783598134987532L;
    private int coef=1;

    public ApplicationVersionIdComparator() {
    }

    public ApplicationVersionIdComparator(SortOrder sortOrder) {

        this.coef = sortOrder.equals(SortOrder.DESCENDING) ? -1 : 1;
    }

    @Override
    public int compare(ApplicationVersion appVersion1, ApplicationVersion appVersion2) {

        return coef * appVersion1.getId().compareTo(appVersion2.getId());
    }
}
