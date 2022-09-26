package toby.spring.user.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.test.context.ContextConfiguration;
import toby.spring.user.domain.User;
import toby.spring.user.exception.DuplicateUserIdException;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ContextConfiguration(locations = "/test-applicationContext.xml")
class UserJdbcTemplateDaoTest {

    @Autowired
    private UserDao userJdbcTemplateDao;

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setUp() {
        user1 = new User("gildong1", "홍길동1", "1001");
        user2 = new User("gildong2", "홍길동2", "1002");
        user3 = new User("gildong3", "홍길동3", "1003");
    }

    //junit 5 는 메소드에 접근제어자(public)을 생략해도된다.
    @Test
    void addAndGet() {
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
    void count() {
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
    void getAll() {
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

    @Test
    void add_exception() {
        userJdbcTemplateDao.deleteAll();

        userJdbcTemplateDao.add(user1);
        assertThatThrownBy(() -> userJdbcTemplateDao.add(user1))
                .isInstanceOf(DuplicateUserIdException.class);
    }

    @Autowired
    DataSource dataSource;

    @Test
    void sqlExceptionTranslate() {
        userJdbcTemplateDao.deleteAll();
        try {
            userJdbcTemplateDao.add_exception(user1);
            userJdbcTemplateDao.add_exception(user1);
        } catch (DuplicateKeyException e) {
            SQLException sqlEx = (SQLException) e.getRootCause();
            SQLErrorCodeSQLExceptionTranslator set = new SQLErrorCodeSQLExceptionTranslator(this.dataSource);
            assertThat(set.translate(null, null, sqlEx)).isInstanceOf(DuplicateKeyException.class);
        }
    }

    private void checkSameUser(User expect, User actual) {
        assertEquals(expect.getId(), actual.getId());
        assertEquals(expect.getName(), actual.getName());
        assertEquals(expect.getPassword(), actual.getPassword());
    }
}
