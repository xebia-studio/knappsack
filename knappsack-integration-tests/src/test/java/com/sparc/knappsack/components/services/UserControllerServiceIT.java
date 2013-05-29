package com.sparc.knappsack.components.services;

import com.sparc.knappsack.components.entities.User;
import com.sparc.knappsack.util.MailTestUtils;
import com.sparc.knappsack.util.WebRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.subethamail.wiser.Wiser;

import static org.junit.Assert.*;

public class UserControllerServiceIT extends AbstractServiceTests {

    @Autowired(required = true)
    private UserControllerService userControllerService;

    @Value("${dev.mail.server.port}")
    private int mailPort;

    private Wiser wiser;

    @Before
    public void setup() {
        super.setup();
        WebRequest.getInstance("http", "localhost", 8080, "knappsack");

        wiser = new Wiser();
        wiser.setPort(mailPort);
        wiser.start();

        MailTestUtils.reconfigureMailSenders(applicationContext, mailPort);
    }

    @After
    public void after() {
        wiser.stop();
    }

    @Test
    public void resetPassword_Success_Test() {
        User user = getUserWithSecurityContext();
        assertFalse(user.isPasswordExpired());
        boolean success = userControllerService.resetPassword(user);

        assertTrue(success);
        assertTrue(user.isPasswordExpired());
    }

    @Test
    public void resetPassword_Failure_Test() {
        boolean success = userControllerService.resetPassword(null);
        assertFalse(success);
    }

    @Test
    public void changePassword_Success_Test() {
        User user = getUserWithSecurityContext();
        String originalPassword = user.getPassword();
        boolean success = userControllerService.changePassword(user, "newPassword", false);

        assertTrue(success);
        assertNotSame(originalPassword, user.getPassword());

        originalPassword = user.getPassword();
        success = userControllerService.changePassword(user, "anotherNewPassword", true);

        assertTrue(success);
        assertNotSame(originalPassword, user.getPassword());
        assertTrue(user.isPasswordExpired());
    }

}
