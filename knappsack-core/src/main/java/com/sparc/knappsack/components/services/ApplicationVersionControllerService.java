package com.sparc.knappsack.components.services;

import com.sparc.knappsack.enums.AppState;
import com.sparc.knappsack.forms.UploadApplicationVersion;

public interface ApplicationVersionControllerService {

    public boolean saveApplicationVersion(UploadApplicationVersion applicationVersion);

    public boolean updateApplicationVersionState(Long applicationVersionId, AppState appState);

}
