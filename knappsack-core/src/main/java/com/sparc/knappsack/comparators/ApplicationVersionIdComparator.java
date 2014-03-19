package com.sparc.knappsack.comparators;

import com.sparc.knappsack.components.entities.ApplicationVersion;

import java.io.Serializable;
import java.util.Comparator;

public class ApplicationVersionIdComparator implements Comparator<ApplicationVersion>,Serializable {

    private static final long serialVersionUID = -7171783598134987532L;

    @Override
    public int compare(ApplicationVersion appVersion1, ApplicationVersion appVersion2) {

        Long obj1Version = appVersion1.getId();
        Long obj2Version = appVersion2.getId();

        return obj1Version.compareTo(obj2Version);
    }
}
