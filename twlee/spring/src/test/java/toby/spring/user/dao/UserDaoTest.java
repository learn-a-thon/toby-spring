package toby.spring.user.dao;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import toby.spring.user.domain.User;

import java.sql.SQLException;

class UserDaoTest {
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(DaoFactory.class);
        UserDao userDao = context.getBean("userDao", UserDao.class);

        User user = new User();
        user.setId("gildong1");
        user.setName("홍길동");
        user.setPassword("1004");

        userDao.add(user);
        System.out.println("user = " + user + " 등록 성공!");

        User findUser = userDao.get(user.getId());
        if (!findUser.getName().equals(user.getName())) {
            System.out.println("테스트 실패 (name)");
        } else if (!findUser.getPassword().equals(user.getPassword())) {
            System.out.println("테스트 실패 (password)");
        } else {
            System.out.println("조회 테스트 성공");
        }
    }
}
