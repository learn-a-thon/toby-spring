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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ContextConfiguration(locations = "/test-applicationContext.xml")
class UserJdbcTemplateDaoTest {

    @Autowired
    private UserJdbcTemplateDao userJdbcTemplateDao;

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setUp() {
        DataSource dataSource = new SingleConnectionDataSource("jdbc:h2:tcp://localhost/~/test", "sa", "", true);
        userJdbcTemplateDao.setDataSource(dataSource);

        user1 = new User("gildong1", "홍길동1", "1001");
        user2 = new User("gildong2", "홍길동2", "1002");
        user3 = new User("gildong3", "홍길동3", "1003");
    }

    //junit 5 는 메소드에 접근제어자(public)을 생략해도된다.
    @Test
    void addAndGet() throws SQLException, ClassNotFoundException {
        userJdbcTemplateDao.deleteAll();
        assertEquals(userJdbcTemplateDao.getCount(), 0);

        userJdbcTemplateDao.add(user1);
        userJdbcTemplateDao.add(user2);
        assertEquals(userJdbcTemplateDao.getCount(), 2);

        User findUser1 = userJdbcTemplateDao.get(user1.getId());
        assertEquals(user1.getName(), findUser1.getName());
        assertEquals(user1.getPassword(), findUser1.getPassword());

        User findUser2 = userJdbcTemplateDao.get(user2.getId());
        assertEquals(user2.getName(), findUser2.getName());
        assertEquals(user2.getPassword(), findUser2.getPassword());
    }

    @Test
    void count() throws SQLException, ClassNotFoundException {
        userJdbcTemplateDao.deleteAll();
        assertEquals(userJdbcTemplateDao.getCount(), 0);

        userJdbcTemplateDao.add(user1);
        assertEquals(userJdbcTemplateDao.getCount(), 1);

        userJdbcTemplateDao.add(user2);
        assertEquals(userJdbcTemplateDao.getCount(), 2);

        userJdbcTemplateDao.add(user3);
        assertEquals(userJdbcTemplateDao.getCount(), 3);
    }

    @Test
    void get_exception() {
        assertThatThrownBy(() -> userJdbcTemplateDao.get("unknown"))
                .isInstanceOf(EmptyResultDataAccessException.class);
    }

    @Test
    void getAll() throws SQLException {
        userJdbcTemplateDao.deleteAll();
        List<User> emptyList = userJdbcTemplateDao.getAll();
        assertEquals(emptyList.size(), 0);

        userJdbcTemplateDao.add(user1);
        userJdbcTemplateDao.add(user2);
        userJdbcTemplateDao.add(user3);
        assertEquals(userJdbcTemplateDao.getCount(), 3);

        List<User> userList = userJdbcTemplateDao.getAll();
        assertEquals(userList.size(), 3);
        checkSameUser(user1, userList.get(0));
        checkSameUser(user2, userList.get(1));
        checkSameUser(user3, userList.get(2));
    }

    private void checkSameUser(User expect, User actual) {
        assertEquals(expect.getId(), actual.getId());
        assertEquals(expect.getName(), actual.getName());
        assertEquals(expect.getPassword(), actual.getPassword());
    }
}