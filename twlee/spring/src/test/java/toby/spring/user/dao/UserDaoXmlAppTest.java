package toby.spring.user.dao;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import toby.spring.user.domain.User;

import java.sql.SQLException;

class UserDaoXmlAppTest {
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        ApplicationContext context = new GenericXmlApplicationContext("applicationContext.xml");
        UserStupidDao userStupidDao = context.getBean("userDao", UserStupidDao.class);

        User user = new User();
        user.setId("gildong5");
        user.setName("홍길동");
        user.setPassword("1004");

        userStupidDao.add(user);

        System.out.println("user = " + user + " 등록 성공!");

        User findUser = userStupidDao.get(user.getId());
        System.out.println(findUser.getName());
    }
}
