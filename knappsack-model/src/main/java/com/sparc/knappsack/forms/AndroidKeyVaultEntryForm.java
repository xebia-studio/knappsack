package com.sparc.knappsack.forms;

import com.sparc.knappsack.enums.ApplicationType;

public class AndroidKeyVaultEntryForm extends AbstractKeyVaultEntryForm {
    @Override
    public ApplicationType getApplicationType() {
        return ApplicationType.ANDROID;
    }
}
