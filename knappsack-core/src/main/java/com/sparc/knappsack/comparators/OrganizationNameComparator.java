package com.sparc.knappsack.comparators;

import com.sparc.knappsack.components.entities.Organization;

import java.io.Serializable;
import java.util.Comparator;

public class OrganizationNameComparator implements Comparator<Organization>, Serializable {

    private static final long serialVersionUID = -7393740034995072091L;

    @Override
    public int compare(Organization organization, Organization organization2) {
        String name1 = organization.getName();
        String name2 = organization2.getName();

        return name1.compareToIgnoreCase(name2);
    }
}
