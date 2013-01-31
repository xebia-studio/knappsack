package com.sparc.knappsack.components.services;

import com.sparc.knappsack.models.DomainUserRequestModel;
import com.sparc.knappsack.models.UserModel;
import org.springframework.mail.MailException;

import java.util.List;

public interface EmailService {

    /**
     * @param domainUserRequestId DomainUserRequest ID
     * @throws MailException
     *
     * This method will send an email to the group and organization administrator notifying them that a user has requested access to their domain.
     */
    boolean sendDomainUserAccessRequestEmail(final Long domainUserRequestId);

    /**
     * @param userId User ID
     * @throws MailException
     *
     * This method will send an email to a user with the information necessary to activate their account.
     */
    boolean sendActivationEmail(final Long userId);

    /**
     * @param userId Long User ID
     * @return true if the
     */
    boolean sendActivationSuccessEmail(final Long userId);

    /**
     * @param fromUserId Long - The person who is creating the invitation
     * @param invitationId Invitation ID
     * @throws MailException
     *
     * This method will send an email to a user stating that they were invited to join a domain
     */
    boolean sendInvitationEmail(Long fromUserId, Long invitationId);

    /**
     * @param userId User ID
     * @param password String
     * @throws MailException
     *
     * This method will send an email to a user with their new reset password.
     */
    boolean sendPasswordResetEmail(final Long userId, final String password);

    /**
     * @param applicationVersionId ApplicationVersion ID
     * @param userModel UserModel
     * @throws MailException
     *
     * Sends an email to the organization administrator stating that a group administrator has requested the state of an application be set to one that is more visible.
     */
    boolean sendApplicationPublishRequestEmail(final Long applicationVersionId, UserModel userModel);

    /**
     * @param domainRequestId DomainRequest ID
     * @throws MailException
     *
     * @return Whether or not all emails were successfully sent.  A false indicates that at least one failed.
     */
    boolean sendDomainAccessRequestEmail(final Long domainRequestId);

    /**
     * @param domainUserRequestModel DomainUserRequestModel
     * @throws MailException
     *
     * Sends an email to the User stating whether or not that group access request was accepted or declined.
     */
    boolean sendDomainUserAccessConfirmationEmail(final DomainUserRequestModel domainUserRequestModel);

    /**
     * @param organizationId Organization ID which was registered
     * @param userModel UserModel of the admin of the newly created organization
     * @throws MailException
     */
    boolean sendOrganizationRegistrationEmail(final Long organizationId, final UserModel userModel);

    /**
     * @param applicationVersionId ApplicationVersion ID which is now visible
     * @param userIds List of user IDs which now have access to the given ApplicationVersion
     * @return Whether or not all emails were successfully sent. A false indicates that at least one failed.
     */
    boolean sendApplicationVersionBecameVisibleEmail(final Long applicationVersionId, final List<Long> userIds);

    /**
     * @param organizationId Long - the organization ID that reached their bandwidth limit
     * @param users List<UserModel> - all administrators for the given organization
     * @return boolean - Whether or not all emails were successfully sent. A false indicates that at least one failed.
     */
    boolean sendBandwidthLimitNotification(Long organizationId, List<UserModel> users);

    /**
     * @param applicationVersionId ApplicationVersion ID which was put in the error state.
     * @param userIds List<Long> - all user IDs to be notified.
     * @return boolean - Whether or not all emails were successfully sent.  A false indicates that at least one failed.
     */
    boolean sendApplicationVersionErrorEmail(final Long applicationVersionId, final List<Long> userIds);
}
