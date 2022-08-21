package toby.spring.user.dao;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;
import toby.spring.user.domain.User;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UserDaoTest {

    //junit 5 는 메소드에 접근제어자(public)을 생략해도된다.
    @Test
    void addAndGet() throws SQLException, ClassNotFoundException {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(DaoFactory.class);
        UserDao userDao = context.getBean("userDao", UserDao.class);

        userDao.deleteAll();
        assertEquals(userDao.getCount(), 0);

        User user1 = new User("gildong1", "홍길동1", "1001");
        User user2 = new User("gildong2", "홍길동2", "1002");

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
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(DaoFactory.class);
        UserDao userDao = context.getBean("userDao", UserDao.class);
        User user1 = new User("user1", "kim", "1001");
        User user2 = new User("user2", "kim", "1002");
        User user3 = new User("user3", "kim", "1003");

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
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(DaoFactory.class);
        UserDao userDao = context.getBean("userDao", UserDao.class);

        assertThatThrownBy(() -> userDao.get("unknown"))
                .isInstanceOf(EmptyResultDataAccessException.class);
    }
}
