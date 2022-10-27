package toby.spring.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.PlatformTransactionManager;
import toby.spring.user.dao.UserDao;
import toby.spring.user.domain.User;
import toby.spring.user.factorybean.TxProxyFactoryBean;
import toby.spring.user.handler.TransactionHandler;

import javax.sql.DataSource;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static toby.spring.user.UserFixture.*;

@SpringBootTest
class UserProxyServiceTest {
    @Autowired
    private UserDao userDao;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private MockMailSender mailSender;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private List<User> userList;

    @BeforeEach
    void setUp() {
        userList = Arrays.asList(NON_USER1, UP_USER1, NON_USER2, UP_USER2);
    }

    @Test
    void upgradeAllOrNothing_proxy() {
        userDao.deleteAll();
        for (User user : userList) {
            userDao.add(user);
        }
        TransactionHandler handler = new TransactionHandler(
                new TestUserService(userDao, dataSource, mailSender, userList.get(3).getId()),
                transactionManager,
                "upgradeLevels");
        UserService userService = (UserService) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{UserService.class},
                handler);
        try {
            userService.upgradeLevels();
        } catch (Exception e) {
            checkLevelUpgraded(userList.get(0), false);
            checkLevelUpgraded(userList.get(1), false);
            checkLevelUpgraded(userList.get(2), false);
            checkLevelUpgraded(userList.get(3), false);

            // 메일 발송 이력은 롤백이 안됨...
            List<String> targets = mailSender.getTargets();
            assertThat(targets.size()).isEqualTo(1);
        }
    }

    @Test
    void upgradeAllOrNothing_nonProxy() {
        userDao.deleteAll();
        for (User user : userList) {
            userDao.add(user);
        }
        try {
            UserService userService = new TestUserService(userDao, dataSource, mailSender, userList.get(3).getId());
            userService.upgradeLevels();
        } catch (Exception e) {
            System.out.println("exception!");
            // exception이 발생했는데 upgrade 되면 안됨
            checkLevelUpgraded(userList.get(1), false);
            List<String> targets = mailSender.getTargets();
            assertThat(targets.size()).isEqualTo(1);
        }
    }

    @Autowired
    private ApplicationContext context;

    @DirtiesContext
    @Test
    void upgradeAllOrNothing_factoryBean() throws Exception {
        userDao.deleteAll();
        for (User user : userList) {
            userDao.add(user);
        }
        TxProxyFactoryBean factoryBean = context.getBean("&userService", TxProxyFactoryBean.class);
        assertThat(factoryBean).isInstanceOf(TxProxyFactoryBean.class);
        factoryBean.setTarget(new TestUserService(userDao, dataSource, mailSender, userList.get(3).getId()));
        UserService userService = (UserService) factoryBean.getObject();
        try {
            userService.upgradeLevels();
        } catch (Exception e) {
            System.out.println("exception!");
        }
        // exception이 발생했는데 upgrade 되면 안됨
        List<String> targets = mailSender.getTargets();
        assertThat(targets.size()).isEqualTo(1);
        checkLevelUpgraded(userList.get(1), false);
    }

    private void checkLevelUpgraded(User user, boolean upgraded) {
        User userUpgrade = userDao.get(user.getId());
        if (upgraded) {
            assertThat(userUpgrade.getLevel()).isEqualTo(user.getLevel().getNext());
        } else {
            assertThat(userUpgrade.getLevel()).isEqualTo(user.getLevel());
        }
    }
}
