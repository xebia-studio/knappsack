package com.sparc.knappsack.comparators;

import com.sparc.knappsack.components.entities.Region;

import java.io.Serializable;
import java.util.Comparator;

public class RegionNameComparator implements Comparator<Region>, Serializable {

    private static final long serialVersionUID = -2477142336326718205L;

    @Override
    public int compare(Region region, Region region2) {
        String regionName = region.getName();
        String region2Name = region2.getName();

        return regionName.compareToIgnoreCase(region2Name);
    }
}
