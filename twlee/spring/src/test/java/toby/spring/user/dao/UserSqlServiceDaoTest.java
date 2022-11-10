package toby.spring.user.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import toby.spring.user.domain.Level;
import toby.spring.user.domain.User;
import toby.spring.user.exception.DuplicateUserIdException;
import toby.spring.user.sqlservice.XmlSqlService;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static toby.spring.user.UserFixture.*;

@SpringBootTest
class UserSqlServiceDaoTest {

    @Autowired
    DataSource dataSource;

    private UserDao userDao;

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setUp() {
        userDao = new UserSqlServiceDao(dataSource, new XmlSqlService());

        user1 = USER1;
        user2 = USER2;
        user3 = USER3;
    }

    @Test
    void addAndGet() {
        userDao.deleteAll();
        assertEquals(userDao.getCount(), 0);

        userDao.add(user1);
        userDao.add(user2);
        assertEquals(userDao.getCount(), 2);

        User findUser1 = userDao.get(user1.getId());
        checkSameUser(user1, findUser1);

        User findUser2 = userDao.get(user2.getId());
        checkSameUser(user2, findUser2);
    }

    @Test
    void count() {
        userDao.deleteAll();
        assertEquals(userDao.getCount(), 0);

        userDao.add(user1);
        assertEquals(userDao.getCount(), 1);

        userDao.add(user2);
        assertEquals(userDao.getCount(), 2);

        userDao.add(user3);
        assertEquals(userDao.getCount(), 3);
    }

    @Test
    void get_exception() {
        assertThatThrownBy(() -> userDao.get("unknown"))
                .isInstanceOf(EmptyResultDataAccessException.class);
    }

    @Test
    void getAll() {
        userDao.deleteAll();
        List<User> emptyList = userDao.getAll();
        assertEquals(emptyList.size(), 0);

        userDao.add(user1);
        userDao.add(user2);
        userDao.add(user3);
        assertEquals(userDao.getCount(), 3);

        List<User> userList = userDao.getAll();
        assertEquals(userList.size(), 3);
        checkSameUser(user1, userList.get(0));
        checkSameUser(user2, userList.get(1));
        checkSameUser(user3, userList.get(2));
    }

    @Test
    void add_exception() {
        userDao.deleteAll();

        userDao.add(user1);
        assertThatThrownBy(() -> userDao.add(user1))
                .isInstanceOf(DuplicateUserIdException.class);
    }

    @Test
    void sqlExceptionTranslate() {
        userDao.deleteAll();
        try {
            userDao.add_exception(user1);
            userDao.add_exception(user1);
        } catch (DuplicateKeyException e) {
            SQLException sqlEx = (SQLException) e.getRootCause();
            SQLErrorCodeSQLExceptionTranslator set = new SQLErrorCodeSQLExceptionTranslator(this.dataSource);
            assertThat(set.translate(null, null, sqlEx)).isInstanceOf(DuplicateKeyException.class);
        }
    }

    @Test
    void update() {
        userDao.deleteAll();

        userDao.add(user1);

        user1.setName("나는홍길동");
        user1.setPassword("9999");
        user1.setLevel(Level.GOLD);
        user1.setLogin(1000);
        user1.setRecommend(999);

        userDao.update(user1);

        User findUser1 = userDao.get(user1.getId());
        checkSameUser(user1, findUser1);
    }

    private void checkSameUser(User expect, User actual) {
        assertEquals(expect.getId(), actual.getId());
        assertEquals(expect.getName(), actual.getName());
        assertEquals(expect.getPassword(), actual.getPassword());
        assertEquals(expect.getLevel(), actual.getLevel());
        assertEquals(expect.getLogin(), actual.getLogin());
        assertEquals(expect.getRecommend(), actual.getRecommend());
    }
}
