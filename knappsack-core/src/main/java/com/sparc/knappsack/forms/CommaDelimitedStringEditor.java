package com.sparc.knappsack.forms;

import org.springframework.util.StringUtils;

import java.beans.PropertyEditorSupport;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CommaDelimitedStringEditor extends PropertyEditorSupport {

    private boolean trimWhitespace;

    public CommaDelimitedStringEditor(boolean trimWhitespace) {
        this.trimWhitespace = trimWhitespace;
    }

    @Override
    public String getAsText() {
        return (getValue() == null ? "" : StringUtils.collectionToDelimitedString((Collection)getValue(), ", "));
    }

    @Override
    public void setAsText(String text) {
        Set<String> splitStrings = new HashSet<String>();

        for (String splitString : StringUtils.commaDelimitedListToSet(text)) {
            splitStrings.add((trimWhitespace ? StringUtils.trimAllWhitespace(splitString) : splitString).toLowerCase());
        }
        setValue(splitStrings);
    }
}
