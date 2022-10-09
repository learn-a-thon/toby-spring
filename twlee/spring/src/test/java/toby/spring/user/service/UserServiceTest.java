package toby.spring.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.MailSender;
import toby.spring.user.dao.UserDao;
import toby.spring.user.domain.Level;
import toby.spring.user.domain.User;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static toby.spring.user.UserFixture.*;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserDao userDao;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private DummyMailSender mailSender;

    private List<User> userList;

    @BeforeEach
    void setUp() {
        userList = Arrays.asList(USER4, USER5, USER6, USER7);
    }

    @Test
    void bean() {
        assertThat(userService).isNotNull();
    }

    @Test
    void upgradeLevels() {
        userDao.deleteAll();
        for (User user : userList) {
            userDao.add(user);
        }
        userService.upgradeLevels();

        checkLevelUpgraded(userList.get(0), false);
        checkLevelUpgraded(userList.get(1), false);
        checkLevelUpgraded(userList.get(2), false);
        checkLevelUpgraded(userList.get(3), true);
    }

    private void checkLevel(User expect, Level level) {
        User user = userDao.get(expect.getId());
        assertEquals(user.getLevel(), level);
    }

    @Test
    void add() {
        userDao.deleteAll();

        User userWithLevel = userList.get(3);
        User userWithoutLevel = userList.get(0);
        userWithoutLevel.setLevel(null);

        userService.add(userWithLevel);
        userService.add(userWithoutLevel);

        User userWithLevelRead = userDao.get(userWithLevel.getId());
        User userWithoutLevelRead = userDao.get(userWithoutLevel.getId());

        assertThat(userWithLevelRead.getLevel()).isEqualTo(userWithLevel.getLevel());
        assertThat(userWithoutLevelRead.getLevel()).isEqualTo(Level.BASIC);
    }

    private void checkLevelUpgraded(User user, boolean upgraded) {
        User userUpgrade = userDao.get(user.getId());
        if (upgraded) {
            assertThat(userUpgrade.getLevel()).isEqualTo(user.getLevel().getNext());
        } else {
            assertThat(userUpgrade.getLevel()).isEqualTo(user.getLevel());
        }
    }

    @Test
    void upgradeAllOrNothing_fail() {
        UserService testUserService = new TestUserService(userDao, dataSource, mailSender, userList.get(3).getId());
        userDao.deleteAll();
        for (User user : userList) {
            userDao.add(user);
        }
        try {
            testUserService.upgradeLevels();
        } catch (TestUserServiceException e) {
            System.out.println("error occur!");
        } catch (Exception e) {
            e.printStackTrace();
        }
        checkLevelUpgraded(userList.get(2), false);
    }

    @Test
    void upgradeAllOrNothingSyncTransaction() {
        UserService testUserService = new TestUserService(userDao, dataSource, mailSender, userList.get(3).getId());
        userDao.deleteAll();
        for (User user : userList) {
            userDao.add(user);
        }
        try {
            testUserService.upgradeLevelsSyncTransaction();
        } catch (TestUserServiceException e) {
            System.out.println("error occur!");
        } catch (Exception e) {
            e.printStackTrace();
        }
        checkLevelUpgraded(userList.get(2), false);
    }

    static class TestUserService extends UserService {
        private String id;

        public TestUserService(UserDao userDao, DataSource dataSource, MailSender mailSender) {
            super(userDao, dataSource, mailSender);
        }

        public TestUserService(UserDao userDao, DataSource dataSource, MailSender mailSender, String id) {
            super(userDao, dataSource, mailSender);
            this.id = id;
        }

        @Override
        protected void upgradeLevel(User user) {
            if (user.getId().equals(this.id)) throw new TestUserServiceException();
            super.upgradeLevel(user);
        }
    }

    static class TestUserServiceException extends RuntimeException {
    }
}
