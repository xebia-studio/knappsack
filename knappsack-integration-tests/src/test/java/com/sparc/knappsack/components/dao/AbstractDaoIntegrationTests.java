package com.sparc.knappsack.components.dao;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:**/spring/test-jpa-context.xml", })
@TransactionConfiguration(transactionManager="txManager", defaultRollback=true)
@Transactional
public abstract class AbstractDaoIntegrationTests extends AbstractTransactionalJUnit4SpringContextTests {

    @BeforeClass
    public static void setup() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("test", "test"));

//        User user = new User();
//        SecurityContextHolder.getContext().setAuthentication(
//                new UsernamePasswordAuthenticationToken(user, testUser.getPassword()));
    }

}
