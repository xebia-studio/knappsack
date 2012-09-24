package com.sparc.knappsack;

import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.enums.ApplicationType;
import com.sparc.knappsack.enums.StorageType;

public class EntityUtil {

    public static Category createCategory() {
        Category category = new Category();
        category.setDescription("Test Category");
        category.setName("Test");

        return category;
    }

    public static AppFile createAppFile() {
        AppFile appFile = new AppFile();

        appFile.setName("Test AppFile");
        appFile.setRelativePath("Test/appfile/relative/path");
        appFile.setType("Type");
        double megabyteSize = 2097152 / 1048576;
        appFile.setSize(megabyteSize);
        appFile.setStorageType(StorageType.LOCAL);

        return appFile;
    }

    public static Application createApplication() {
        Application application = new Application();
        application.setName("Application");
        application.setDescription("Description");
        application.setApplicationType(ApplicationType.ANDROID);

        return application;
    }

    public static StorageConfiguration createLocalStorageConfiguration() {
        StorageConfiguration storageConfiguration = new LocalStorageConfiguration();
        storageConfiguration.setBaseLocation("/base/location");
        storageConfiguration.setName("Local storageConfiguration");
        storageConfiguration.setStorageType(StorageType.LOCAL);

        return storageConfiguration;
    }
}
