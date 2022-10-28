package toby.spring.user.service;

import org.springframework.mail.MailSender;
import toby.spring.user.dao.UserDao;
import toby.spring.user.domain.User;

import javax.sql.DataSource;

public class TestUserServiceImpl extends UserServiceImpl implements UserService {

    public TestUserServiceImpl(UserDao userDao, DataSource dataSource, MailSender mockMailSender) {
        super(userDao, dataSource, mockMailSender);
    }

    @Override
    protected void upgradeLevel(User user) {
        if (user.getId().equals("up2")) throw new TestUserServiceException();
        super.upgradeLevel(user);
    }

    static class TestUserServiceException extends RuntimeException {
    }
}
