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

@SpringBootTest
@ContextConfiguration(locations = "/test-applicationContext.xml")
class UserDaoDataTest {

    @Autowired
    private UserDataDao userDao;

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setUp() {
        DataSource dataSource = new SingleConnectionDataSource("jdbc:h2:tcp://localhost/~/test", "sa", "", true);
        userDao.setDataSource(dataSource);

        user1 = new User("gildong1", "홍길동1", "1001");
        user2 = new User("gildong2", "홍길동2", "1002");
        user3 = new User("gildong3", "홍길동3", "1003");
    }

    //junit 5 는 메소드에 접근제어자(public)을 생략해도된다.
    @Test
    void addAndGet() throws SQLException, ClassNotFoundException {
        userDao.deleteAll();
        assertEquals(userDao.getCount(), 0);

        userDao.add(user1);
        userDao.add(user2);
        assertEquals(userDao.getCount(), 2);

        User findUser1 = userDao.get(user1.getId());
        assertEquals(user1.getName(), findUser1.getName());
        assertEquals(user1.getPassword(), findUser1.getPassword());

        User findUser2 = userDao.get(user2.getId());
        assertEquals(user2.getName(), findUser2.getName());
        assertEquals(user2.getPassword(), findUser2.getPassword());
    }

    @Test
    void count() throws SQLException, ClassNotFoundException {
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
}
