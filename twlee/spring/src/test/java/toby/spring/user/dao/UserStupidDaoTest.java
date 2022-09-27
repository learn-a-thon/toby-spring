package toby.spring.user.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.ContextConfiguration;
import toby.spring.user.domain.User;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static toby.spring.user.UserFixture.*;

@SpringBootTest
@ContextConfiguration(locations = "/applicationContext.xml")
class UserStupidDaoTest {

    @Autowired
    private UserStupidDao userStupidDao;

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setUp() {
        user1 = USER1;
        user2 = USER2;
        user3 = USER3;
    }

    //junit 5 는 메소드에 접근제어자(public)을 생략해도된다.
    @Test
    void addAndGet() throws SQLException, ClassNotFoundException {
        userStupidDao.deleteAll();
        assertEquals(userStupidDao.getCount(), 0);

        userStupidDao.add(user1);
        userStupidDao.add(user2);
        assertEquals(userStupidDao.getCount(), 2);

        User findUser1 = userStupidDao.get(user1.getId());
        assertEquals(user1.getName(), findUser1.getName());
        assertEquals(user1.getPassword(), findUser1.getPassword());

        User findUser2 = userStupidDao.get(user2.getId());
        assertEquals(user2.getName(), findUser2.getName());
        assertEquals(user2.getPassword(), findUser2.getPassword());
    }

    @Test
    void count() throws SQLException, ClassNotFoundException {
        userStupidDao.deleteAll();
        assertEquals(userStupidDao.getCount(), 0);

        userStupidDao.add(user1);
        assertEquals(userStupidDao.getCount(), 1);

        userStupidDao.add(user2);
        assertEquals(userStupidDao.getCount(), 2);

        userStupidDao.add(user3);
        assertEquals(userStupidDao.getCount(), 3);
    }

    @Test
    void get_exception() {
        assertThatThrownBy(() -> userStupidDao.get("unknown"))
                .isInstanceOf(EmptyResultDataAccessException.class);
    }
}
