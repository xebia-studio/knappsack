package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.dao.ApplicationDao;
import com.sparc.knappsack.components.dao.ApplicationVersionDao;
import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.enums.AppFileType;
import com.sparc.knappsack.enums.ApplicationType;
import com.sparc.knappsack.forms.UploadApplication;
import com.sparc.knappsack.models.ApplicationModel;
import com.sparc.knappsack.models.ImageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Transactional(propagation = Propagation.REQUIRED)
@Service("applicationService")
public class ApplicationServiceImpl implements ApplicationService {

    @Qualifier("applicationDao")
    @Autowired(required = true)
    private ApplicationDao applicationDao;

    @Qualifier("applicationVersionDao")
    @Autowired(required = true)
    private ApplicationVersionDao applicationVersionDao;

    @Qualifier("categoryService")
    @Autowired(required = true)
    private CategoryService categoryService;

    @Qualifier("storageConfigurationService")
    @Autowired(required = true)
    protected StorageConfigurationService storageConfigurationService;

    @Qualifier("storageServiceFactory")
    @Autowired
    private StorageServiceFactory storageServiceFactory;

    @Qualifier("groupService")
    @Autowired(required = true)
    private GroupService groupService;

    @Qualifier("appFileService")
    @Autowired(required = true)
    private AppFileService appFileService;

    @Qualifier("eventWatchService")
    @Autowired(required = true)
    private EventWatchService eventWatchService;

    @Qualifier("applicationVersionService")
    @Autowired(required = true)
    private ApplicationVersionService applicationVersionService;

    @Override
    public void add(Application application) {
        applicationDao.add(application);
    }

    @Override
    public void update(Application application) {
        applicationDao.update(application);
    }

    @Override
    public List<Application> getAll() {
        return applicationDao.getAll();
    }

    @Override
    public List<Application> getAll(ApplicationType applicationType) {
        return applicationDao.getAll(applicationType);
    }

    @Transactional(readOnly = true)
    @Override
    public Application get(Long id) {
        return applicationDao.get(id);
    }

    @Override
    public void delete(Long id) {

        Application application = get(id);

        if (application != null) {

            deleteApplicationFilesAndVersions(application);

            Group group = application.getOwnedGroup();
            group.getOwnedApplications().remove(application);
            applicationDao.delete(application);
        }
    }

    @Override
    public void deleteApplicationFilesAndVersions(Application application) {

        if (application != null) {
            eventWatchService.deleteAllEventWatchForNotifiable(application);

            applicationVersionService.deleteAllForApplication(application);
            application.setApplicationVersions(null);

            appFileService.delete(application.getIcon());
            application.setIcon(null);

            for (AppFile screenShot : application.getScreenShots()) {
                appFileService.delete(screenShot);
            }
            application.setScreenShots(null);
        }
    }

    @Override
    public List<Application> getAllByNameAndDescription(String searchCriteria) {
        return applicationDao.getByNameAndDescription(searchCriteria);
    }

    @Override
    public List<Application> getAll(Category category) {
        return applicationDao.getByCategory(category);
    }

    @Override
    public List<ApplicationModel> getAll(Category category, ApplicationType applicationType) {
        List<Application> applications = applicationDao.getByCategoryAndApplicationType(category, applicationType);

        return createApplicationModels(applications);
    }

    @Override
    public List<ApplicationModel> createApplicationModels(List<Application> applications) {
        List<ApplicationModel> items = new ArrayList<ApplicationModel>();
        for (Application application : applications) {
            ApplicationModel item = createApplicationModel(application);
            if (item != null) {
                items.add(item);
            }
        }

        return items;
    }

    @Override
    public ApplicationModel createApplicationModel(Long applicationId) {
        return createApplicationModel(applicationDao.get(applicationId));
    }

    @Override
    public ApplicationModel createApplicationModel(Application application) {
        ApplicationModel model = null;
        if (application != null) {
            model = new ApplicationModel();
            mapApplicationToModel(application, model);
        }
        return model;
    }

    private void mapApplicationToModel(Application application, ApplicationModel model) {
        if (application != null && model != null) {
            model.setId(application.getId());
            model.setName(application.getName());
            model.setDescription(application.getDescription());
            model.setApplicationType(application.getApplicationType());
            model.setIcon(appFileService.createImageModel(application.getIcon()));
            model.setGroupId(application.getOwnedGroup().getId());
            model.setGroupName(application.getOwnedGroup().getName());

            List<ImageModel> screenShotImageModels = new ArrayList<ImageModel>();
            for (AppFile screenShot : application.getScreenShots()) {
                screenShotImageModels.add(appFileService.createImageModel(screenShot));
            }
            model.setScreenShots(screenShotImageModels);
        }
    }

    private void setStorableOnAppFile(AppFile appFile, Storable storable) {
        if (appFile != null) {
            appFile.setStorable(storable);
        }
    }

    private Application createApplication(UploadApplication uploadApplication) {
        Application application;
        if (uploadApplication.getId() != null && uploadApplication.getId() > 0) {
            application = get(uploadApplication.getId());
        } else {
            application = new Application();
        }
        application.setName(uploadApplication.getName());
        application.setDescription(uploadApplication.getDescription());
        application.setCategory(categoryService.get(uploadApplication.getCategoryId()));
        application.setOwnedGroup(groupService.get(uploadApplication.getGroupId()));

        if (application != null && (application.getId() == null || application.getId() <= 0)) {
            application.setApplicationType(uploadApplication.getApplicationType());
        }

        Long storageConfigurationId = uploadApplication.getStorageConfigurationId();
        //If null we are editing a category
        if (storageConfigurationId == null) {
            storageConfigurationId = application.getStorageConfiguration().getId();
        }
        uploadApplication.setStorageConfigurationId(storageConfigurationId);
        application.setStorageConfiguration(storageConfigurationService.get(storageConfigurationId));

        add(application);

        return application;
    }

    private AppFile storeIcon(UploadApplication uploadApplication, StorageService storageService, String uuid) {
        return storageService.save(uploadApplication.getIcon(), AppFileType.ICON.getPathName(), uploadApplication.getOrgStorageConfigId(), uploadApplication.getStorageConfigurationId(), uuid);
    }

    private List<AppFile> storeScreenShots(UploadApplication uploadApplication, StorageService storageService, String uuid) {
        List<AppFile> appFiles = new ArrayList<AppFile>();
        for (MultipartFile screenShot : uploadApplication.getScreenShots()) {
            appFiles.add(storageService.save(screenShot, AppFileType.SCREENSHOT.getPathName(), uploadApplication.getOrgStorageConfigId(), uploadApplication.getStorageConfigurationId(), uuid));
        }
        return appFiles;
    }

    private StorageService getStorageService(Long storageConfigurationId) {
        return storageServiceFactory.getStorageService(getStorageConfiguration(storageConfigurationId).getStorageType());
    }

    private StorageConfiguration getStorageConfiguration(Long storageConfigurationId) {
        return storageConfigurationService.get(storageConfigurationId);
    }

    @Override
    public Application saveApplication(UploadApplication uploadApplication) {
        Application application = createApplication(uploadApplication);
        StorageService storageService = getStorageService(application.getStorageConfiguration().getId());

        AppFile icon = storeIcon(uploadApplication, storageService, application.getUuid());
        setStorableOnAppFile(icon, application);
        if (icon != null) {
            application.setIcon(icon);
        }

        List<AppFile> screenShots = storeScreenShots(uploadApplication, storageService, application.getUuid());
        for (AppFile screenShot : screenShots) {
            setStorableOnAppFile(screenShot, application);
        }
        if (screenShots.size() > 0) {
            application.getScreenShots().addAll(screenShots);
        }

        if (application.getId() != null && application.getId() > 0) {
            update(application);
        } else {
            add(application);
        }
        Group group = groupService.get(uploadApplication.getGroupId());
        boolean groupContainsApplication = false;
        for (Application ownedApplication : group.getOwnedApplications()) {
            if (ownedApplication.getId().equals(application.getId())) {
                groupContainsApplication = true;
                break;
            }
        }
        if (!groupContainsApplication) {
            group.getOwnedApplications().add(application);
            groupService.save(group);
        }

        return application;
    }

    private void deleteVersion(Application application, ApplicationVersion applicationVersion, boolean update) {
        List<Group> groups = groupService.getGuestGroups(applicationVersion);
        for (Group group : groups) {
            group.getGuestApplicationVersions().remove(applicationVersion);
        }
        if (application != null && application.getApplicationVersions() != null && applicationVersion != null) {

            StorageConfiguration storageConfiguration = application.getStorageConfiguration();
            StorageService storageService = getStorageService(storageConfiguration.getId());

            storageService.delete(applicationVersion.getInstallationFile());
            storageService.delete(applicationVersion.getProvisioningProfile());

            if (update) {
                application.getApplicationVersions().remove(applicationVersion);
                applicationVersionDao.delete(applicationVersion);
            }
        }
    }

    @Override
    public boolean determineApplicationVisibility(Application application, ApplicationType deviceType) {
        if (application != null && deviceType != null) {
            if(ApplicationType.IPHONE.equals(deviceType) || ApplicationType.IPAD.equals(deviceType)) {
                return (application.getApplicationType().equals(deviceType) || application.getApplicationType().equals(ApplicationType.IOS));
            } else if(deviceType.isMobilePlatform()) {
                return application.getApplicationType().equals(deviceType);
            }
            return true;
        }
        return false;
    }

    @Override
    public void deleteScreenshot(Long applicationId, int index) {
        Application application = get(applicationId);
        if (application != null && application.getScreenShots() != null) {
            AppFile appFile = application.getScreenShots().get(index);
            if (appFile != null) {
                application.getScreenShots().remove(appFile);
                appFileService.delete(appFile);
            }
        }
    }

    @Override
    public void deleteIcon(Long applicationId) {
        Application application = get(applicationId);
        if (application != null) {
            AppFile appFile = application.getIcon();
            if (appFile != null) {
                application.setIcon(null);
                appFileService.delete(appFile);
            }
        }
    }

    @Override
    public boolean isApplicationVersionLimit(Application application, Domain domain) {
        int appVersionCount = application.getApplicationVersions().size();
        DomainConfiguration domainConfiguration = domain.getDomainConfiguration();
        return domainConfiguration.getApplicationVersionLimit() <= appVersionCount;
    }

    @Override
    public boolean doesEntityExist(Long id) {
        return get(id) != null;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Override
    public long countAll() {
        return applicationDao.countAll();
    }
}
