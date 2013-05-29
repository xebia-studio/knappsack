package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.SystemNotification;
import com.sparc.knappsack.enums.SystemNotificationSeverity;
import com.sparc.knappsack.enums.SystemNotificationType;
import com.sparc.knappsack.models.SystemNotificationModel;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class SystemNotificationServiceIT extends AbstractServiceTests {

    @Autowired(required = true)
    private SystemNotificationService systemNotificationService;

    @Test
    public void delete_Success_Test() {
        Calendar futureDate = Calendar.getInstance();
        futureDate.add(Calendar.DATE, 1);
        SystemNotification systemNotification = createSystemNotification(true, new Date(), futureDate.getTime(), "test message", SystemNotificationType.MAINTENANCE, SystemNotificationSeverity.INFO);

        assertTrue(systemNotificationService.doesEntityExist(systemNotification.getId()));
        systemNotificationService.delete(systemNotification.getId());
        assertFalse(systemNotificationService.doesEntityExist(systemNotification.getId()));
    }

    @Test
    public void update_Success_Test() {
        Calendar futureDate = Calendar.getInstance();
        futureDate.add(Calendar.DATE, 1);
        SystemNotification systemNotification = createSystemNotification(true, new Date(), futureDate.getTime(), "test message", SystemNotificationType.MAINTENANCE, SystemNotificationSeverity.INFO);
        assertTrue(systemNotificationService.doesEntityExist(systemNotification.getId()));

        Date updatedStartDate = systemNotification.getStartDate();
        updatedStartDate.setTime(updatedStartDate.getTime() + 1000);

        Date updatedEndDate = systemNotification.getEndDate();
        updatedEndDate.setTime(updatedEndDate.getTime() + 1000);

        systemNotification.setAllPages(!systemNotification.isAllPages());
        systemNotification.setStartDate(updatedStartDate);
        systemNotification.setEndDate(updatedEndDate);
        systemNotification.setMessage("updated message");
        systemNotification.setNotificationSeverity(SystemNotificationSeverity.ERROR);

        systemNotificationService.update(systemNotification);

        SystemNotification updatedSystemNotification = systemNotificationService.get(systemNotification.getId());

        assertNotNull(updatedSystemNotification);
        assertEquals(updatedSystemNotification.getStartDate(), systemNotification.getStartDate());
        assertEquals(updatedSystemNotification.getEndDate(), systemNotification.getEndDate());
        assertEquals(updatedSystemNotification.isAllPages(), systemNotification.isAllPages());
        assertEquals(updatedSystemNotification.getMessage(), systemNotification.getMessage());
        assertEquals(updatedSystemNotification.getNotificationType(), systemNotification.getNotificationType());
        assertEquals(updatedSystemNotification.getNotificationSeverity(), systemNotification.getNotificationSeverity());
    }

    @Test
    public void addSystemNotification_Success_Test() {
        Calendar futureDate = Calendar.getInstance();
        futureDate.add(Calendar.DATE, 1);
        SystemNotificationModel model = createSystemNotificationModel(null, true, new Date(), futureDate.getTime(), "test message", SystemNotificationType.MAINTENANCE, SystemNotificationSeverity.INFO);

        SystemNotification systemNotification = systemNotificationService.addSystemNotification(model);
        assertNotNull(systemNotification);
        assertNotNull(systemNotification.getId());
    }

    @Test
    public void addSystemNotification_Failure_Test() {
        assertNull(systemNotificationService.addSystemNotification(null));
    }

    @Test
    public void editSystemNotification_Success_Test() {
        Calendar futureDate = Calendar.getInstance();
        futureDate.add(Calendar.DATE, 1);
        SystemNotification systemNotification = createSystemNotification(true, new Date(), futureDate.getTime(), "test message", SystemNotificationType.MAINTENANCE, SystemNotificationSeverity.INFO);
        assertNotNull(systemNotification);
        assertNotNull(systemNotification.getId());

        Date updatedStartDate = systemNotification.getStartDate();
        updatedStartDate.setTime(updatedStartDate.getTime() + 1000);

        Date updatedEndDate = systemNotification.getEndDate();
        updatedEndDate.setTime(updatedEndDate.getTime() + 1000);

        SystemNotificationModel updateModel = createSystemNotificationModel(systemNotification.getId(), !systemNotification.isAllPages(), updatedStartDate, updatedEndDate, "updated message", SystemNotificationType.MAINTENANCE, SystemNotificationSeverity.ERROR);

        SystemNotification updatedSystemNotification = systemNotificationService.editSystemNotification(updateModel);
        assertNotNull(updatedSystemNotification);
        assertEquals(updatedSystemNotification.getStartDate(), updateModel.getStartDate());
        assertEquals(updatedSystemNotification.getEndDate(), updateModel.getEndDate());
        assertEquals(updatedSystemNotification.isAllPages(), updateModel.isAllPages());
        assertEquals(updatedSystemNotification.getMessage(), updateModel.getMessage());
        assertEquals(updatedSystemNotification.getNotificationType(), updateModel.getNotificationType());
        assertEquals(updatedSystemNotification.getNotificationSeverity(), updateModel.getNotificationSeverity());
    }

    @Test
    public void editSystemNotification_Failure_Test() {
        assertNull(systemNotificationService.editSystemNotification(new SystemNotificationModel()));
        assertNull(systemNotificationService.editSystemNotification(null));
    }

    @Test
    public void getAllModels_noFilterDate_Success() {
        Calendar futureDate = Calendar.getInstance();
        futureDate.add(Calendar.DATE, 1);
        SystemNotification systemNotification1 = createSystemNotification(true, new Date(), futureDate.getTime(), "test message", SystemNotificationType.MAINTENANCE, SystemNotificationSeverity.INFO);
        SystemNotification systemNotification2 = createSystemNotification(true, new Date(), futureDate.getTime(), "test message", SystemNotificationType.MAINTENANCE, SystemNotificationSeverity.INFO);

        List<SystemNotificationModel> systemNotifications = systemNotificationService.getAllModels(false);
        assertNotNull(systemNotifications);
        assertEquals(systemNotifications.size(), 2);
    }

    @Test
    public void getAllModels_FilterDate_Success() {
        Calendar futureDate = Calendar.getInstance();
        futureDate.add(Calendar.DATE, 1);

        Calendar anotherFutureDate = Calendar.getInstance();
        anotherFutureDate.add(Calendar.DATE, 4);

        SystemNotification systemNotification1 = createSystemNotification(true, new Date(), futureDate.getTime(), "test message", SystemNotificationType.MAINTENANCE, SystemNotificationSeverity.INFO);
        SystemNotification systemNotification2 = createSystemNotification(true, futureDate.getTime(), anotherFutureDate.getTime(), "test message", SystemNotificationType.MAINTENANCE, SystemNotificationSeverity.INFO);

        List<SystemNotificationModel> models = systemNotificationService.getAllModels(true);
        assertNotNull(models);
        assertEquals(models.size(), 1);
        assertEquals(models.get(0).getId(), systemNotification1.getId());
    }

    @Test
    public void getAllForTypes_NoDateFilter_Success() {
        Calendar futureDate = Calendar.getInstance();
        futureDate.add(Calendar.DATE, 1);

        Calendar anotherFutureDate = Calendar.getInstance();
        anotherFutureDate.add(Calendar.DATE, 4);

        SystemNotification systemNotification1 = createSystemNotification(true, new Date(), futureDate.getTime(), "test message", SystemNotificationType.MAINTENANCE, SystemNotificationSeverity.INFO);
        SystemNotification systemNotification2 = createSystemNotification(true, futureDate.getTime(), anotherFutureDate.getTime(), "test message", SystemNotificationType.MAINTENANCE, SystemNotificationSeverity.INFO);

        List<SystemNotificationModel> models = systemNotificationService.getAllForTypes(false, SystemNotificationType.MAINTENANCE);
        assertNotNull(models);
        assertEquals(models.size(), 2);
        for (SystemNotificationModel model : models) {
            assertEquals(model.getNotificationType(), SystemNotificationType.MAINTENANCE);
        }
    }

    @Test
    public void getAllForTypes_DateFilter_Success() {

        Date now = new Date();

        Calendar tenDaysFuture = Calendar.getInstance();
        tenDaysFuture.setTime(now);
        tenDaysFuture.add(Calendar.DATE, 10);

        Calendar fifteenDaysFuture = Calendar.getInstance();
        fifteenDaysFuture.setTime(now);
        fifteenDaysFuture.add(Calendar.DATE, 15);

        Calendar twentyDaysFuture = Calendar.getInstance();
        twentyDaysFuture.setTime(now);
        twentyDaysFuture.add(Calendar.DATE, 20);

        SystemNotification systemNotification1 = createSystemNotification(true, now, tenDaysFuture.getTime(), "test message", SystemNotificationType.MAINTENANCE, SystemNotificationSeverity.INFO);
        SystemNotification systemNotification2 = createSystemNotification(true, fifteenDaysFuture.getTime(), twentyDaysFuture.getTime(), "test message", SystemNotificationType.MAINTENANCE, SystemNotificationSeverity.INFO);

        List<SystemNotificationModel> models = systemNotificationService.getAllForTypes(true, SystemNotificationType.MAINTENANCE);
        assertNotNull(models);
        assertEquals(models.size(), 1);
        assertEquals(models.get(0).getId(), systemNotification1.getId());
        assertEquals(models.get(0).getNotificationType(), systemNotification1.getNotificationType());
    }

    private SystemNotification createSystemNotification(boolean allPages, Date startDate, Date endDate, String message, SystemNotificationType notificationType, SystemNotificationSeverity notificationSeverity) {
        SystemNotification systemNotification = new SystemNotification();
        systemNotification.setAllPages(allPages);
        systemNotification.setStartDate(startDate);
        systemNotification.setEndDate(endDate);
        systemNotification.setMessage(message);
        systemNotification.setNotificationType(notificationType);
        systemNotification.setNotificationSeverity(notificationSeverity);

        systemNotificationService.add(systemNotification);
        assertNotNull(systemNotification.getId());
        assertTrue(systemNotificationService.doesEntityExist(systemNotification.getId()));

        return systemNotification;
    }

    private SystemNotificationModel createSystemNotificationModel(Long id, boolean allPages, Date startDate, Date endDate, String message, SystemNotificationType notificationType, SystemNotificationSeverity notificationSeverity) {
        SystemNotificationModel model = new SystemNotificationModel();
        if (id != null && id > 0) {
            model.setId(id);
        }
        model.setAllPages(allPages);
        model.setStartDate(startDate);
        model.setEndDate(endDate);
        model.setMessage(message);
        model.setNotificationType(notificationType);
        model.setNotificationSeverity(notificationSeverity);

        return model;
    }
}
