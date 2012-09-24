package com.sparc.knappsack.comparators;

import com.sparc.knappsack.components.entities.Application;

import java.io.Serializable;
import java.util.Comparator;

public class ApplicationNameComparator implements Comparator<Application>,Serializable {

    private static final long serialVersionUID = 2603595822829659863L;

    @Override
    public int compare(Application app1, Application app2) {

        String app1Name = app1.getName();
        String app2Name = app2.getName();

        return app1Name.compareToIgnoreCase(app2Name);
    }

}
