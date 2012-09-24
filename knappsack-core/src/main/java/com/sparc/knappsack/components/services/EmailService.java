package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.ApplicationVersion;
import com.sparc.knappsack.components.entities.GroupUserRequest;
import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.enums.DomainType;
import com.sparc.knappsack.models.GroupUserRequestModel;
import com.sparc.knappsack.models.UserModel;
import org.springframework.mail.MailException;

import java.util.List;

public interface EmailService {

    /**
     * @param groupUserRequest GroupUserRequest
     * @throws MailException
     *
     * This method will send an email to the group and organization administrator notifying them that a user has requested access to their domain.
     */
    boolean sendGroupAccessRequestEmail(final GroupUserRequest groupUserRequest);

    /**
     * @param user User
     * @throws MailException
     *
     * This method will send an email to a user with the information necessary to activate their account.
     */
    boolean sendActivationEmail(final User user);

    /**
     * @param user User
     * @param toEmail String - the address to send the email "to"
     * @param invitationDomain String - the name of the specific Domain (group name, organization name, etc...)
     * @param domainType DomainType
     * @throws MailException
     *
     * This method will send an email to a user stating that they were invited to join a domain
     */
    boolean sendInvitationEmail(User user, String toEmail,  String invitationDomain, DomainType domainType);

    /**
     * @param user User
     * @param password String
     * @throws MailException
     *
     * This method will send an email to a user with their new reset password.
     */
    boolean sendPasswordResetEmail(final User user, final String password);

    /**
     * @param applicationVersion ApplicationVersion
     * @param userModel UserModel
     * @throws MailException
     *
     * Sends an email to the organization administrator stating that a group administrator has requested the state of an application be set to one that is more visible.
     */
    boolean sendApplicationPublishRequestEmail(final ApplicationVersion applicationVersion, UserModel userModel);

    /**
     * @param groupUserRequestModel GroupUserRequestModel
     * @throws MailException
     *
     * Sends an email to the User stating whether or not that group access request was accepted or declined.
     */
    boolean sendGroupAccessConfirmationEmail(final GroupUserRequestModel groupUserRequestModel);

    /**
     * @param applicationVersion ApplicationVersion which is now visible
     * @param users List of users which now have access to the given ApplicationVersion
     * @return Whether or not all emails were successfully sent. A false indicates that at least one failed.
     */
    boolean sendApplicationVersionBecameVisibleEmail(final ApplicationVersion applicationVersion, final List<User> users);
}
