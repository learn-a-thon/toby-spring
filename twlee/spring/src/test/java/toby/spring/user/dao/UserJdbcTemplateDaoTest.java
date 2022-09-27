package toby.spring.user.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.test.context.ContextConfiguration;
import toby.spring.user.domain.Level;
import toby.spring.user.domain.User;
import toby.spring.user.exception.DuplicateUserIdException;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static toby.spring.user.UserFixture.*;

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
        user1 = USER1;
        user2 = USER2;
        user3 = USER3;
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
        checkSameUser(user1, findUser1);

        User findUser2 = userJdbcTemplateDao.get(user2.getId());
        checkSameUser(user2, findUser2);
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

    @Test
    void update() {
        userJdbcTemplateDao.deleteAll();

        userJdbcTemplateDao.add(user1);

        user1.setName("나는홍길동");
        user1.setPassword("9999");
        user1.setLevel(Level.GOLD);
        user1.setLogin(1000);
        user1.setRecommend(999);

        userJdbcTemplateDao.update(user1);

        User findUser1 = userJdbcTemplateDao.get(user1.getId());
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
