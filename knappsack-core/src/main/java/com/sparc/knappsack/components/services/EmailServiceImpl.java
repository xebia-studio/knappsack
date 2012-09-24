package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.*;
import com.sparc.knappsack.enums.DomainType;
import com.sparc.knappsack.enums.Status;
import com.sparc.knappsack.enums.UserRole;
import com.sparc.knappsack.models.GroupUserRequestModel;
import com.sparc.knappsack.models.UserModel;
import com.sparc.knappsack.util.WebRequest;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service("emailService")
public class EmailServiceImpl implements EmailService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Qualifier("mailSender")
    @Autowired(required = true)
    private JavaMailSender mailSender;

    @Qualifier("templateEngine")
    @Autowired(required = true)
    private TemplateEngine templateEngine;

    @Qualifier("userDomainService")
    @Autowired(required = true)
    private UserDomainService userDomainService;

    @Qualifier("userService")
    @Autowired(required = true)
    private UserService userService;

    @Qualifier("groupUserRequestService")
    @Autowired(required = true)
    private GroupUserRequestService groupUserRequestService;

    @Value("${mail.send}")
    private boolean sendEmail;

    @Value("${NotificationEmailAddress}")
    private String fromAddress;

    @Override
    public boolean sendGroupAccessRequestEmail(final GroupUserRequest groupUserRequest) {
        boolean emailSent = false;
        WebRequest request = WebRequest.getInstance();
        if (request != null && sendEmail) {
            final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
            final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");
            try {
                message.setSubject("Knappsack: Group Access Request");

                Locale locale = LocaleContextHolder.getLocale();

                message.setFrom(fromAddress);

                if (groupUserRequest != null) {
                    List<UserDomain> userDomains = userDomainService.getAll(groupUserRequest.getGroup().getId(), DomainType.GROUP);

                    if (userDomains != null) {
                        List<User> users = new ArrayList<User>();
                        for (UserDomain userDomain : userDomains) {
                            if (userDomain.getUser().isGroupAdmin() && !users.contains(userDomain.getUser())) {
                                users.add(userDomain.getUser());
                            }
                        }

                        if (users.isEmpty()) {
                            List<UserDomain> orgAdminUserDomains = userDomainService.getAll(groupUserRequest.getGroup().getOrganization().getId(), DomainType.ORGANIZATION, UserRole.ROLE_ORG_ADMIN);
                            for (UserDomain userDomain : orgAdminUserDomains) {
                                if (userDomain.getUser().isOrganizationAdmin() && !users.contains(userDomain.getUser())) {
                                    users.add(userDomain.getUser());
                                }
                            }
                        }

                        //TODO: fix so that if there are no group admins the org admins will be notified
                        for (User user : users) {
                            Context ctx = new Context(locale);
                            ctx.setVariable("name", user.getFullName());
                            ctx.setVariable("request", groupUserRequest);

                            ctx.setVariable("url", request.generateURL("/manager"));

                            // Create the HTML body using Thymeleaf
                            final String htmlContent = this.templateEngine.process("email-groupAccessRequestTH", ctx);
                            message.setText(htmlContent, true /* isHtml */);
                            message.setTo(user.getEmail());

                            emailSent = sendMessage(mimeMessage);
                        }
                    }
                }
            } catch (MessagingException e) {
                log.error(String.format("MessagingException sending a group access request for: %s", groupUserRequest.getUser().getEmail()), e);
            }
        }

        return emailSent;
    }

    @Override
    public boolean sendActivationEmail(User user) {
        boolean emailSent = false;
        WebRequest request = WebRequest.getInstance();
        if (request != null && sendEmail) {
            final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
            final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");
            try {
                message.setSubject("Knappsack: Account Activation");

                Locale locale = LocaleContextHolder.getLocale();

                message.setFrom(fromAddress);

                if (user != null && !user.isActivated()) {
                    Context ctx = new Context(locale);
                    ctx.setVariable("name", user.getFullName());
                    ctx.setVariable("activationCode", user.getActivationCode());

                    String servletPath = "/activate/" + user.getActivationCode();
                    ctx.setVariable("url", request.generateURL(servletPath));

                    // Create the HTML body using Thymeleaf
                    final String htmlContent = this.templateEngine.process("email-accountActivationTH", ctx);
                    message.setText(htmlContent, true /* isHtml */);
                    message.setTo(user.getEmail());

                    emailSent = sendMessage(mimeMessage);
                }
            } catch (MessagingException e) {
                log.error(String.format("MessagingException sending an activation for: %s", user.getEmail()), e);
            }
        }

        return emailSent;
    }

    @Override
    public boolean sendInvitationEmail(User user, String toEmail, String invitationDomain, DomainType domainType) {
        boolean emailSent = false;
        WebRequest request = WebRequest.getInstance();
        if (request != null && sendEmail) {
            final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
            final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");
            try {
                message.setSubject("Knappsack: Domain Invitation");

                Locale locale = LocaleContextHolder.getLocale();

                message.setFrom(fromAddress);

                Context ctx = new Context(locale);
                if (user != null) {
                    ctx.setVariable("adminName", user.getFullName());
                }
                ctx.setVariable("invitationDomain", invitationDomain);
                ctx.setVariable("domainType", domainType.name());

                User invitee = userService.getByEmail(toEmail);
                if (invitee == null) {
                    ctx.setVariable("existingUser", false);
                    NameValuePair emailParam = new BasicNameValuePair("email", toEmail);
                    ctx.setVariable("url", request.generateURL("/auth/register", emailParam));
                } else {
                    ctx.setVariable("existingUser", true);
                    ctx.setVariable("url", request.generateURL(""));
                }

                // Create the HTML body using Thymeleaf
                final String htmlContent = this.templateEngine.process("email-invitationTH", ctx);
                message.setText(htmlContent, true /* isHtml */);
                message.setTo(toEmail);

                emailSent = sendMessage(mimeMessage);
            } catch (MessagingException e) {
                log.error(String.format("MessagingException sending an invitation for: %s", toEmail), e);
            }
        }

        return emailSent;
    }

    @Override
    public boolean sendPasswordResetEmail(final User user, final String password) {
        boolean emailSent = false;
        WebRequest request = WebRequest.getInstance();
        if (request != null && sendEmail) {
            final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
            final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");
            try {
                message.setSubject("Knappsack: Password Changed");

                Locale locale = LocaleContextHolder.getLocale();
                message.setFrom(fromAddress);

                if (user != null) {
                    Context ctx = new Context(locale);
                    ctx.setVariable("name", user.getFullName());
                    ctx.setVariable("password", password);

                    // Create the HTML body using Thymeleaf
                    final String htmlContent = this.templateEngine.process("email-passwordChangedTH", ctx);
                    message.setText(htmlContent, true /* isHtml */);
                    message.setTo(user.getEmail());

                    emailSent = sendMessage(mimeMessage);
                }
            } catch (MessagingException e) {
                log.error(String.format("MessagingException sending a password changed for: %s", user.getEmail()), e);
            }
        }

        return emailSent;
    }

    @Override
    public boolean sendApplicationPublishRequestEmail(ApplicationVersion applicationVersion, UserModel userModel) {
        boolean emailSent = false;
        WebRequest request = WebRequest.getInstance();
        if (sendEmail && userModel != null && applicationVersion != null && applicationVersion.getApplication() != null && request != null) {
            final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
            final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");
            try {
                message.setSubject("Knappsack: Application Publish Request");

                Locale locale = LocaleContextHolder.getLocale();
                message.setFrom(fromAddress);

                Organization organization = null;
                try {
                    organization = applicationVersion.getApplication().getCategory().getOrganization();
                } catch (NullPointerException ex) {
                    log.error("NPE caused getting organization from application version", ex);
                }

                if (organization != null) {
                    List<UserDomain> userdomains = userDomainService.getAll(organization.getId(), DomainType.ORGANIZATION, UserRole.ROLE_ORG_ADMIN);

                    for (UserDomain userDomain : userdomains) {
                        Context ctx = new Context(locale);
                        ctx.setVariable("name", userDomain.getUser().getFullName());
                        ctx.setVariable("userName", userModel.getFullName());
                        ctx.setVariable("applicationName", applicationVersion.getApplication().getName());
                        ctx.setVariable("applicationVersion", applicationVersion.getVersionName());
                        ctx.setVariable("organizationName", organization.getName());
                        ctx.setVariable("url", request.generateURL("/manager"));

                        // Create the HTML body using Thymeleaf
                        final String htmlContent = this.templateEngine.process("email-applicationPublishRequestTH", ctx);
                        message.setText(htmlContent, true /* isHtml */);
                        message.setTo(userDomain.getUser().getEmail());

                        emailSent = sendMessage(mimeMessage);
                    }
                }
            } catch (MessagingException e) {
                log.error(String.format("MessagingException sending an application publish request for: %s", userModel.getEmail()), e);
            }
        }

        return emailSent;
    }

    @Override
    public boolean sendGroupAccessConfirmationEmail(GroupUserRequestModel groupUserRequestModel) {
        boolean emailSent = false;
        WebRequest request = WebRequest.getInstance();
        if (sendEmail && groupUserRequestModel != null && groupUserRequestModel.getGroup() != null && groupUserRequestModel.getUser() != null && request != null) {
            final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
            final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");
            try {
                message.setSubject(String.format("Knappsack: Group Access Request %s", (Status.ACCEPTED.equals(groupUserRequestModel.getStatus()) ? "Accepted" : "Declined")));

                Locale locale = LocaleContextHolder.getLocale();
                message.setFrom(fromAddress);

                User user = userService.get(groupUserRequestModel.getUser().getId());
                if (user != null) {
                    Context ctx = new Context(locale);
                    ctx.setVariable("name", user.getFullName());
                    ctx.setVariable("groupName", groupUserRequestModel.getGroup().getName());
                    ctx.setVariable("status", (Status.ACCEPTED.equals(groupUserRequestModel.getStatus()) ? true : false));
                    // Create the HTML body using Thymeleaf
                    final String htmlContent = this.templateEngine.process("email-groupAccessConfirmationTH", ctx);
                    message.setText(htmlContent, true /* isHtml */);
                    message.setTo(user.getEmail());

                    emailSent = sendMessage(mimeMessage);
                }
            } catch (MessagingException e) {
                log.error("MessagingException sending a group access confirmation:", e);
            }
        }

        return emailSent;
    }

    @Override
    public boolean sendApplicationVersionBecameVisibleEmail(ApplicationVersion applicationVersion, List<User> users) {
        boolean emailsSent = false;
        WebRequest request = WebRequest.getInstance();
        if (sendEmail && request != null && applicationVersion != null && applicationVersion.getApplication() != null && users != null) {
            int numSent = 0;
            final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
            final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");
            try {
                message.setSubject("Knappsack: Application Visibility");

                Locale locale = LocaleContextHolder.getLocale();
                message.setFrom(fromAddress);

                Context ctx = new Context(locale);
                ctx.setVariable("applicationName", applicationVersion.getApplication().getName());
                ctx.setVariable("url", request.generateURL(String.format("/detail/%s", applicationVersion.getApplication().getId())));
                ctx.setVariable("applicationVersion", applicationVersion.getVersionName());
                for (User user : users) {
                    ctx.setVariable("userName", user.getFullName());

                    final String htmlContent = this.templateEngine.process("email-applicationVersionBecameVisibleTH", ctx);
                    message.setText(htmlContent, true /* isHtml */);
                    message.setTo(user.getEmail());

                    if (sendMessage(mimeMessage)) {
                        numSent += 1;
                    }
                }

                if (numSent == users.size()) {
                    emailsSent = true;
                }
            } catch (MessagingException e) {
                log.error(String.format("MessagingException sending ApplicationVersionBecameVisible emails for applicationVersionId: %s", applicationVersion.getId()), e);
            }
        }

        return emailsSent;
    }

    private boolean sendMessage(MimeMessage message) {
        boolean success = false;
        if (message != null) {
            try {
                this.mailSender.send(message);
                success = true;
            } catch (MailSendException mailSendException) {
                try {
                    for (Address address : message.getRecipients(Message.RecipientType.TO)) {
                        log.info("Error sending email to " + address + ".", mailSendException);
                    }
                } catch (MessagingException messagingException) {
                    log.info("Error logging message recipients.", messagingException);
                }

                throw mailSendException;
            } catch (MailException mailException) {
                log.info("Error sending email.", mailException);
            }
        }
        return success;
    }
}
