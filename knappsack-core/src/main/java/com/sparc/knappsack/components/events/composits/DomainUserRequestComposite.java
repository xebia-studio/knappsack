package com.sparc.knappsack.components.events.composits;

public class DomainUserRequestComposite {

    private boolean regionSpecific;

    public DomainUserRequestComposite(boolean regionSpecific) {
        this.regionSpecific = regionSpecific;
    }

    public boolean isRegionSpecific() {
        return regionSpecific;
    }
}
