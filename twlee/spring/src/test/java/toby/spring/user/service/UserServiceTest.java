package toby.spring.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import toby.spring.user.dao.UserDao;
import toby.spring.user.domain.Level;
import toby.spring.user.domain.User;

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

        checkLevel(userList.get(0), Level.BASIC);
        checkLevel(userList.get(1), Level.SILVER);
        checkLevel(userList.get(2), Level.SILVER);
        checkLevel(userList.get(3), Level.GOLD);
    }

    private void checkLevel(User expect, Level level) {
        User user = userDao.get(expect.getId());
        assertEquals(user.getLevel(), level);
    }
}
