package com.sparc.knappsack.models;

import com.sparc.knappsack.enums.EventType;

import java.util.HashMap;
import java.util.Map;

public class EmailModel {

    private EventType eventType;
    private Map<String, Object> params;

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public Map<String, Object> getParams() {
        if (params == null) {
            params = new HashMap<String, Object>();
        }
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
}
