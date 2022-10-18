package toby.spring.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import toby.spring.user.dao.UserDao;
import toby.spring.user.domain.User;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static toby.spring.user.UserFixture.*;

@SpringBootTest
class UserServiceMockTest {

    @Autowired
    private UserDao userDao;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private MockMailSender mailSender;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private List<User> userList;
    private UserServiceImpl userServiceImpl;
    private UserServiceTx userServiceTx;

    @BeforeEach
    void setUp() {
        userServiceImpl = new UserServiceImpl(userDao, dataSource, mailSender);
        userServiceTx = new UserServiceTx(userServiceImpl, transactionManager);
        userList = Arrays.asList(USER4, USER5, USER6, USER7, USER8, USER9);
    }

    @Test
    void upgradeAllOrNothingSyncTransaction() {
        userDao.deleteAll();
        for (User user : userList) {
            userDao.add(user);
        }
        userServiceImpl.upgradeLevels();

        checkLevelUpgraded(userList.get(0), false);
        checkLevelUpgraded(userList.get(1), false);
        checkLevelUpgraded(userList.get(2), true);
        checkLevelUpgraded(userList.get(3), false);
        checkLevelUpgraded(userList.get(4), true);
        checkLevelUpgraded(userList.get(5), true);

        List<String> targets = mailSender.getTargets();
        assertThat(targets.size()).isEqualTo(3);
        assertThat(targets.get(0)).isEqualTo(userList.get(2).getEmail());
        assertThat(targets.get(1)).isEqualTo(userList.get(4).getEmail());
        assertThat(targets.get(2)).isEqualTo(userList.get(5).getEmail());
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
