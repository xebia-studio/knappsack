package com.sparc.knappsack.forms;

import java.util.ArrayList;
import java.util.List;

public class Result {

    private List<Long> ids = new ArrayList<Long>();

    private boolean result;

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }

    public boolean getResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }
}
