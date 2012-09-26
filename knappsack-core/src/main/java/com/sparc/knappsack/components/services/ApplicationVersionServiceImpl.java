package com.sparc.knappsack.components.services;

import com.dd.plist.NSDictionary;
import com.dd.plist.NSString;
import com.dd.plist.PropertyListParser;
import com.sparc.knappsack.components.dao.ApplicationVersionDao;
import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.enums.AppFileType;
import com.sparc.knappsack.enums.AppState;
import com.sparc.knappsack.forms.UploadApplicationVersion;
import com.sparc.knappsack.models.ApplicationVersionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Transactional(propagation = Propagation.REQUIRED)
@org.springframework.stereotype.Service("applicationVersionService")
public class ApplicationVersionServiceImpl implements ApplicationVersionService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationVersionServiceImpl.class);

    @Qualifier("applicationVersionDao")
    @Autowired(required = true)
    private ApplicationVersionDao applicationVersionDao;

    @Qualifier("applicationService")
    @Autowired(required = true)
    private ApplicationService applicationService;

    @Qualifier("groupService")
    @Autowired(required = true)
    private GroupService groupService;

    @Qualifier("organizationService")
    @Autowired(required = true)
    private OrganizationService organizationService;

    @Qualifier("storageConfigurationService")
    @Autowired(required = true)
    protected StorageConfigurationService storageConfigurationService;

    @Qualifier("storageServiceFactory")
    @Autowired(required = true)
    private StorageServiceFactory storageServiceFactory;

    @Qualifier("appFileService")
    @Autowired(required = true)
    private AppFileService appFileService;

    @Override
    public void add(ApplicationVersion applicationVersion) {
        applicationVersionDao.add(applicationVersion);
    }

    @Transactional(readOnly = true)
    @Override
    public ApplicationVersion get(Long id) {
        ApplicationVersion applicationVersion = null;
        if (id != null && id > 0) {
            applicationVersion = applicationVersionDao.get(id);
        }
        return applicationVersion;
    }

    @Override
    public void delete(Long id) {
        delete(get(id));
    }

    @Override
    public void update(ApplicationVersion applicationVersion) {
        applicationVersionDao.update(applicationVersion);
    }

    private void save(ApplicationVersion applicationVersion) {
        if (applicationVersion != null) {
            if (applicationVersion.getId() != null && applicationVersion.getId() > 0) {
                update(applicationVersion);
            } else {
                add(applicationVersion);
            }
        }
    }

    private void delete(ApplicationVersion applicationVersion) {
        Application application = applicationVersion.getApplication();
        List<Group> groups = groupService.getGuestGroups(applicationVersion);
        for (Group group : groups) {
            group.getGuestApplicationVersions().remove(applicationVersion);
        }
        if (application != null && application.getApplicationVersions() != null) {

            StorageConfiguration storageConfiguration = application.getStorageConfiguration();
            StorageService storageService = getStorageService(storageConfiguration.getId());

            storageService.delete(applicationVersion.getInstallationFile());
            storageService.delete(applicationVersion.getProvisioningProfile());

            application.getApplicationVersions().remove(applicationVersion);
            applicationVersionDao.delete(applicationVersion);
        }
    }

    @Override
    public void updateAppState(Long appVersionId, AppState appState) {
        ApplicationVersion appVersion = applicationVersionDao.get(appVersionId);
        appVersion.setAppState(appState);
        applicationVersionDao.update(appVersion);
    }

    @Override
    public ApplicationVersion saveApplicationVersion(UploadApplicationVersion version) {
        ApplicationVersion versionToSave = null;
        if (version != null) {
            Application application = applicationService.get(version.getParentId());
            if (application != null && version != null) {

                versionToSave = createApplicationVersion(version);

                StorageService storageService = getStorageService(application.getStorageConfiguration().getId());
                Group group = groupService.get(version.getGroupId());
                Long orgStorageConfigId = group.getOrganization().getOrgStorageConfig().getId();

                if (version.getAppFile() != null) {
                    AppFile installationAppFile = storeInstallationFile(version, orgStorageConfigId, application.getStorageConfiguration().getId(), storageService, versionToSave.getUuid());
                    setStorableOnAppFile(installationAppFile, versionToSave);
                    versionToSave.setInstallationFile(installationAppFile);
                }

                if (version.getProvisioningProfile() != null) {
                    AppFile provisioningAppFile = storeProvisioningFile(version, orgStorageConfigId, application.getStorageConfiguration().getId(), storageService, versionToSave.getUuid());
                    setStorableOnAppFile(provisioningAppFile, versionToSave);
                    versionToSave.setProvisioningProfile(provisioningAppFile);
                }

                setIOSPlistInfoOnApplication(version, versionToSave);

                versionToSave.setApplication(application);

                List<Group> existingGuestGroups = groupService.getGuestGroups(versionToSave);
                if (existingGuestGroups != null) {
                    for (Group guestGroup : existingGuestGroups) {
                        guestGroup.getGuestApplicationVersions().remove(versionToSave);
                    }
                }

                List<Long> guestGroupIds = version.getGuestGroupIds();
                for (Long guestGroupId : guestGroupIds) {
                    Group guestGroup = groupService.get(guestGroupId);

                    if (!guestGroup.getGuestApplicationVersions().contains(versionToSave)) {
                        guestGroup.getGuestApplicationVersions().add(versionToSave);
                    }
                }

                if (!application.getApplicationVersions().contains(versionToSave)) {
                    application.getApplicationVersions().add(versionToSave);
                }

                return versionToSave;
            }
        }

        return versionToSave;
    }

    @Override
    public List<ApplicationVersion> getAll(Long organizationId) {
        return getAll(organizationId, AppState.values());
    }

    @Override
    public List<ApplicationVersion> getAll(Long organizationId, AppState... appStates) {
        List<ApplicationVersion> versions = new ArrayList<ApplicationVersion>();
        Organization organization = organizationService.get(organizationId);
        if (organization != null && appStates != null && appStates.length > 0) {
            List<Group> groups = organization.getGroups();
            for (Group group : groups) {
                versions.addAll(getAll(group, appStates));
            }
        }
        return versions;
    }

    @Override
    public List<ApplicationVersion> getAll(Group group, AppState... appStates) {
        List<ApplicationVersion> versions = new ArrayList<ApplicationVersion>();
        if (group != null && appStates != null && appStates.length > 0) {
            List<Application> applications = group.getOwnedApplications();
            for (Application application : applications) {
                versions.addAll(getAll(application, appStates));
            }
        }
        return versions;
    }

    @Override
    public List<ApplicationVersion> getAll(Application application, AppState... appStates) {
        List<ApplicationVersion> versions = new ArrayList<ApplicationVersion>();
        if (application != null && appStates != null && appStates.length > 0) {
            for (ApplicationVersion version : application.getApplicationVersions()) {
                if (Arrays.asList(appStates).contains(version.getAppState())) {
                    versions.add(version);
                }
            }
        }
        return versions;
    }

    @Override
    public ApplicationVersionModel createApplicationVersionModel(Long applicationVersionId) {
        ApplicationVersionModel model = null;
        ApplicationVersion applicationVersion = get(applicationVersionId);
        if (applicationVersion != null) {
            model = new ApplicationVersionModel();
            model.setId(applicationVersion.getId());
            model.setVersionName(applicationVersion.getVersionName());
            model.setRecentChanges(applicationVersion.getRecentChanges());
            model.setCfBundleIdentifier(applicationVersion.getCfBundleIdentifier());
            model.setCfBundleName(applicationVersion.getCfBundleName());
            model.setCfBundleVersion(applicationVersion.getCfBundleVersion());
            model.setAppState(applicationVersion.getAppState());
            AppFile installationFile = applicationVersion.getInstallationFile();
            if (installationFile != null) {
                model.setInstallationFile(appFileService.createAppFileModel(installationFile.getId()));
            }
        }
        return model;
    }

    @Override
    public boolean doesEntityExist(Long id) {
        return get(id) != null;
    }

    private ApplicationVersion createApplicationVersion(UploadApplicationVersion uploadApplicationVersion) {
        ApplicationVersion applicationVersion;
        if (uploadApplicationVersion.getId() != null && uploadApplicationVersion.getId() > 0) {
            applicationVersion = get(uploadApplicationVersion.getId());
        } else {
            applicationVersion = new ApplicationVersion();
        }

        if (uploadApplicationVersion.getVersionName() != null && !"".equals(uploadApplicationVersion.getVersionName().trim())) {
            applicationVersion.setVersionName(uploadApplicationVersion.getVersionName());
        }

        applicationVersion.setRecentChanges(uploadApplicationVersion.getRecentChanges());
        applicationVersion.setAppState(uploadApplicationVersion.getAppState());
        applicationVersion.setApplication(applicationService.get(uploadApplicationVersion.getParentId()));

        Long storageConfigurationId = uploadApplicationVersion.getStorageConfigurationId();
        //If null we are editing a category
        if (storageConfigurationId == null) {
            storageConfigurationId = applicationVersion.getStorageConfiguration().getId();
        }
        uploadApplicationVersion.setStorageConfigurationId(storageConfigurationId);
        applicationVersion.setStorageConfiguration(storageConfigurationService.get(storageConfigurationId));

        return applicationVersion;
    }

    private AppFile storeInstallationFile(UploadApplicationVersion uploadApplicationVersion, Long orgStorageConfigId, Long storageConfigurationId, StorageService storageService, String uuid) {
        return storageService.save(uploadApplicationVersion.getAppFile(), AppFileType.INSTALL.getPathName() + storageService.getPathSeparator() + uploadApplicationVersion.getVersionName(), orgStorageConfigId, storageConfigurationId, uuid);
    }

    private AppFile storeProvisioningFile(UploadApplicationVersion uploadApplicationVersion, Long orgStorageConfigId, Long storageConfigurationId, StorageService storageService, String uuid) {
        return storageService.save(uploadApplicationVersion.getProvisioningProfile(), AppFileType.INSTALL.getPathName() + storageService.getPathSeparator() + uploadApplicationVersion.getVersionName(), orgStorageConfigId, storageConfigurationId, uuid);
    }

    private StorageService getStorageService(Long storageConfigurationId) {
        return storageServiceFactory.getStorageService(getStorageConfiguration(storageConfigurationId).getStorageType());
    }

    private void setStorableOnAppFile(AppFile appFile, Storable storable) {
        if (appFile != null) {
            appFile.setStorable(storable);
        }
    }

    private StorageConfiguration getStorageConfiguration(Long storageConfigurationId) {
        return storageConfigurationService.get(storageConfigurationId);
    }

    private void setIOSPlistInfoOnApplication(UploadApplicationVersion uploadApplicationVersion, ApplicationVersion applicationVersion) {
        if (uploadApplicationVersion.getAppFile() != null && uploadApplicationVersion.getAppFile().getOriginalFilename().toLowerCase().endsWith(".ipa")) {
            //TODO: Refactor cracking of iOS IPA if possible
            ZipInputStream inputStream = null;
            try {
                inputStream = new ZipInputStream(uploadApplicationVersion.getAppFile().getInputStream());
                ZipEntry zipEntry = inputStream.getNextEntry();
                boolean payloadLocated = false;
                String infoPlistName = "";
                while (zipEntry != null) {
                    if (payloadLocated) {
                        payloadLocated = false;
                        infoPlistName = zipEntry.getName() + "Info.plist";
                    }
                    if (zipEntry.getName().equals("Payload/")) {
                        payloadLocated = true;
                    } else if (zipEntry.getName().equalsIgnoreCase(infoPlistName)) {
                        break;
                    }
                    zipEntry = inputStream.getNextEntry();
                }

                if (zipEntry != null) {
                    NSDictionary object = (NSDictionary) PropertyListParser.parse(inputStream);
                    if (object != null) {
                        NSString cfBundleIdentifier = (NSString) object.objectForKey("CFBundleIdentifier");
                        NSString cfBundleVersion = (NSString) object.objectForKey("CFBundleVersion");
                        NSString cfBundleName = (NSString) object.objectForKey("CFBundleName");

                        applicationVersion.setCfBundleIdentifier(cfBundleIdentifier.toString());
                        applicationVersion.setCfBundleVersion(cfBundleVersion.toString());
                        applicationVersion.setCfBundleName(cfBundleName.toString());
                    }
                }
            } catch (IOException e) {
                log.error("IOException caught while extracting IOS data", e);
            } catch (Exception e) {
                log.error("Exception caught while extracting IOS data", e);
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        log.error("IOException caught closing inputStream while extracting IOS data", e);
                    }
                }
            }
        }
    }
}
