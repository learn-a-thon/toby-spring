package toby.spring.user.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.test.context.ContextConfiguration;
import toby.spring.user.domain.User;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static toby.spring.user.UserFixture.*;

@SpringBootTest
@ContextConfiguration(locations = "/test-applicationContext.xml")
class UserJdbcDaoTest {

    @Autowired
    private UserJdbcDao userJdbcDao;

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setUp() {
        DataSource dataSource = new SingleConnectionDataSource("jdbc:h2:tcp://localhost/~/test", "sa", "", true);
        userJdbcDao.setDataSource(dataSource);

        user1 = USER1;
        user2 = USER2;
        user3 = USER3;
    }

    //junit 5 는 메소드에 접근제어자(public)을 생략해도된다.
    @Test
    void addAndGet() throws SQLException, ClassNotFoundException {
        userJdbcDao.deleteAll();
        assertEquals(userJdbcDao.getCount(), 0);

        userJdbcDao.add(user1);
        userJdbcDao.add(user2);
        assertEquals(userJdbcDao.getCount(), 2);

        User findUser1 = userJdbcDao.get(user1.getId());
        assertEquals(user1.getName(), findUser1.getName());
        assertEquals(user1.getPassword(), findUser1.getPassword());

        User findUser2 = userJdbcDao.get(user2.getId());
        assertEquals(user2.getName(), findUser2.getName());
        assertEquals(user2.getPassword(), findUser2.getPassword());
    }

    @Test
    void count() throws SQLException, ClassNotFoundException {
        userJdbcDao.deleteAll();
        assertEquals(userJdbcDao.getCount(), 0);

        userJdbcDao.add(user1);
        assertEquals(userJdbcDao.getCount(), 1);

        userJdbcDao.add(user2);
        assertEquals(userJdbcDao.getCount(), 2);

        userJdbcDao.add(user3);
        assertEquals(userJdbcDao.getCount(), 3);
    }

    @Test
    void get_exception() {
        assertThatThrownBy(() -> userJdbcDao.get("unknown"))
                .isInstanceOf(EmptyResultDataAccessException.class);
    }
}
