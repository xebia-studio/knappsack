package com.sparc.knappsack.forms;

import java.beans.PropertyEditorSupport;

@SuppressWarnings("unchecked")
public class EnumEditor extends PropertyEditorSupport {
    private Class clazz;

    public EnumEditor(Class clazz) {
        this.clazz = clazz;
    }

    public final String getAsText() {
        return (getValue() == null ? "" : ((Enum) getValue()).name());
    }

    public final void setAsText(String text) {
        if(text != null && !"".equals(text)) {
            setValue(Enum.valueOf(clazz, text));
        }
    }
}