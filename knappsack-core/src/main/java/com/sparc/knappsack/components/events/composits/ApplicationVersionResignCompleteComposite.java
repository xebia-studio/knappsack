package com.sparc.knappsack.components.events.composits;

import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.enums.ResignErrorType;

public class ApplicationVersionResignCompleteComposite implements EventComposite {

    private boolean success;
    private ResignErrorType resignErrorType;
    private User initiationUser;

    public ApplicationVersionResignCompleteComposite(boolean success, User initiationUser, ResignErrorType resignErrorType) {
        this.success = success;
        this.initiationUser = initiationUser;
        this.resignErrorType = resignErrorType;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public User getInitiationUser() {
        return initiationUser;
    }

    public void setInitiationUser(User initiationUser) {
        this.initiationUser = initiationUser;
    }

    public ResignErrorType getResignErrorType() {
        return resignErrorType;
    }

    public void setResignErrorType(ResignErrorType resignErrorType) {
        this.resignErrorType = resignErrorType;
    }
}
