package com.sparc.knappsack.comparators;

import com.sparc.knappsack.components.entities.ApplicationVersion;

import java.io.Serializable;
import java.util.Comparator;

public class ApplicationVersionComparator implements Comparator<ApplicationVersion>,Serializable {

    private static final long serialVersionUID = -7171783598134987532L;

    @Override
    public int compare(ApplicationVersion appVersion1, ApplicationVersion appVersion2) {

        String obj1Version = appVersion1.getVersionName();
        String obj2Version = appVersion2.getVersionName();

        return obj1Version.compareToIgnoreCase(obj2Version);
    }
}
