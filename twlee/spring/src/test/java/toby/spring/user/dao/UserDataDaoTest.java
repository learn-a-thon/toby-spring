package toby.spring.user.dao;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import toby.spring.user.domain.User;

import java.sql.SQLException;

class UserDataDaoTest {
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(DataFactory.class);
        UserDataDao userDataDao = context.getBean("userDataDao", UserDataDao.class);

        User user = new User();
        user.setId("gildong7");
        user.setName("홍길동");
        user.setPassword("1004");

        userDataDao.add(user);

        System.out.println("user = " + user + " 등록 성공!");

        User findUser = userDataDao.get(user.getId());
        System.out.println(findUser.getName());
    }
}
