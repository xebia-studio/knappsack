package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.enums.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

public class EventWatchServiceIT extends AbstractServiceTests {

    @Autowired(required = true)
    private EventWatchService eventWatchService;

    @Autowired(required = true)
    private ApplicationService applicationService;

    @Autowired(required = true)
    private GroupService groupService;

    @Autowired(required = true)
    private OrganizationService organizationService;

    @Autowired(required = true)
    private StorageConfigurationService storageConfigurationService;

    @Autowired(required = true)
    private UserService userService;

    @Test
    public void addTest() {
        Application notifiable = getApplication();
        User user = getUser();
        userService.save(user);
        EventWatch eventWatch = new EventWatch();
        eventWatch.setUser(user);
        eventWatch.setNotifiableId(notifiable.getId());
        eventWatch.setNotifiableType(NotifiableType.APPLICATION);
        eventWatchService.add(eventWatch);
        eventWatch = eventWatchService.get(user, notifiable);
        assertNotNull(eventWatch);
        boolean doesEventWatchExist = eventWatchService.doesEventWatchExist(user, notifiable);
        assertTrue(doesEventWatchExist);
    }

    @Test
    public void updateTest() {
        Application notifiable = getApplication();
        User user = getUser();
        userService.save(user);
        EventWatch eventWatch = new EventWatch();
        eventWatch.setUser(user);
        eventWatch.setNotifiableId(notifiable.getId());
        eventWatch.setNotifiableType(NotifiableType.APPLICATION);
        eventWatchService.add(eventWatch);
        eventWatch = eventWatchService.get(user, notifiable);
        assertNotNull(eventWatch);
        List<EventType> eventTypes = new ArrayList<EventType>();
        eventTypes.add(EventType.APPLICATION_VERSION_BECOMES_AVAILABLE);
        eventWatch.setEventTypes(eventTypes);
        eventWatchService.update(eventWatch);
        eventWatch = eventWatchService.get(user, notifiable);
        assertTrue(eventWatch.getEventTypes().get(0).equals(EventType.APPLICATION_VERSION_BECOMES_AVAILABLE));
    }

    @Test
    public void deleteTest() {
        Application notifiable = getApplication();
        User user = getUser();
        userService.save(user);
        EventWatch eventWatch = new EventWatch();
        eventWatch.setUser(user);
        eventWatch.setNotifiableId(notifiable.getId());
        eventWatch.setNotifiableType(NotifiableType.APPLICATION);
        eventWatchService.add(eventWatch);
        eventWatch = eventWatchService.get(user, notifiable);
        assertNotNull(eventWatch);
        eventWatchService.delete(user, notifiable);
        eventWatch = eventWatchService.get(user, notifiable);
        assertNull(eventWatch);
    }

    @Test
    public void createEventWatchTest() {
        User user = getUser();
        userService.save(user);
        Application application = getApplication();
        boolean isEventCreated = eventWatchService.createEventWatch(user, application, EventType.APPLICATION_VERSION_BECOMES_AVAILABLE);
        assertTrue(isEventCreated);
        EventWatch eventWatch = eventWatchService.get(user, application);
        assertNotNull(eventWatch);
    }

    @Test
    public void deleteAllEventWatchForNotifiableTest() {
        Application notifiable = getApplication();
        User user = getUser();
        userService.save(user);
        EventWatch eventWatch = new EventWatch();
        eventWatch.setUser(user);
        eventWatch.setNotifiableId(notifiable.getId());
        eventWatch.setNotifiableType(NotifiableType.APPLICATION);
        eventWatchService.add(eventWatch);
        eventWatch = eventWatchService.get(user, notifiable);
        assertNotNull(eventWatch);
        eventWatchService.deleteAllEventWatchForNotifiable(notifiable);
        eventWatch = eventWatchService.get(user, notifiable);
        assertNull(eventWatch);
    }

    private Application getApplication() {
        Organization organization = new Organization();
        organization.setAccessCode(UUID.randomUUID().toString());
        organization.setName("Test Organization");
        organizationService.add(organization);

        LocalStorageConfiguration localStorageConfiguration = new LocalStorageConfiguration();
        localStorageConfiguration.setBaseLocation("/path");
        localStorageConfiguration.setName("Local Storage Configuration");
        localStorageConfiguration.setStorageType(StorageType.LOCAL);

        storageConfigurationService.add(localStorageConfiguration);

        Category category = new Category();
        category.setName("Test Category");
        category.setOrganization(organization);
        organization.getCategories().add(category);

        organizationService.getAll();

        Application application = new Application();
        application.setName("Test Application");
        application.setDescription("This is a description.");
        application.setApplicationType(ApplicationType.ANDROID);
        application.setCategory(category);
        application.setStorageConfiguration(localStorageConfiguration);

        ApplicationVersion applicationVersion = new ApplicationVersion();
        applicationVersion.setVersionName("1.0.0");
        applicationVersion.setApplication(application);
        applicationVersion.setAppState(AppState.GROUP_PUBLISH);

        application.getApplicationVersions().add(applicationVersion);
        applicationService.add(application);

        Group group = new Group();
        group.setAccessCode(UUID.randomUUID().toString());
        group.setName("Test Group");
        group.setOrganization(organization);
        group.setOwnedApplications(new ArrayList<Application>());
        group.getOwnedApplications().add(application);
        groupService.save(group);

        Group group2 = new Group();
        group2.setAccessCode(UUID.randomUUID().toString());
        group2.setName("Test Group 2");
        group2.setOrganization(organization);
        group2.setGuestApplicationVersions(new ArrayList<ApplicationVersion>());
        group2.getGuestApplicationVersions().add(applicationVersion);
        groupService.save(group2);

        organization.getGroups().add(group);
        organization.getGroups().add(group2);

        organizationService.getAll();

        return application;
    }
}
