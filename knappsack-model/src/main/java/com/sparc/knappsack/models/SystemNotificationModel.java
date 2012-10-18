package com.sparc.knappsack.models;

import com.sparc.knappsack.enums.SystemNotificationSeverity;
import com.sparc.knappsack.enums.SystemNotificationType;

import java.util.Date;

public class SystemNotificationModel {

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SystemNotificationModel)) return false;

        SystemNotificationModel model = (SystemNotificationModel) o;

        if (allPages != model.allPages) return false;
        if (endDate != null ? !endDate.equals(model.endDate) : model.endDate != null) return false;
        if (id != null ? !id.equals(model.id) : model.id != null) return false;
        if (message != null ? !message.equals(model.message) : model.message != null) return false;
        if (notificationSeverity != model.notificationSeverity) return false;
        if (notificationType != model.notificationType) return false;
        if (startDate != null ? !startDate.equals(model.startDate) : model.startDate != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (startDate != null ? startDate.hashCode() : 0);
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (allPages ? 1 : 0);
        result = 31 * result + (notificationType != null ? notificationType.hashCode() : 0);
        result = 31 * result + (notificationSeverity != null ? notificationSeverity.hashCode() : 0);
        return result;
    }
}
