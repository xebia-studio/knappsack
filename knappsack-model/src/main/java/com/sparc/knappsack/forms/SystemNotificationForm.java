package com.sparc.knappsack.forms;

import com.sparc.knappsack.enums.SystemNotificationSeverity;
import com.sparc.knappsack.enums.SystemNotificationType;

import java.util.Date;

public class SystemNotificationForm {

    private Long id;
    private Date startDate;
    private Date endDate;
    private String message;
    private boolean allPages;
    private SystemNotificationType notificationType;
    private SystemNotificationSeverity notificationSeverity;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isAllPages() {
        return allPages;
    }

    public void setAllPages(boolean allPages) {
        this.allPages = allPages;
    }

    public SystemNotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(SystemNotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public SystemNotificationSeverity getNotificationSeverity() {
        return notificationSeverity;
    }

    public void setNotificationSeverity(SystemNotificationSeverity notificationSeverity) {
        this.notificationSeverity = notificationSeverity;
    }
}
