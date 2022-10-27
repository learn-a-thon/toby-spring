package toby.spring.user.service;

import org.springframework.mail.MailSender;
import toby.spring.user.dao.UserDao;
import toby.spring.user.domain.User;

import javax.sql.DataSource;

public class TestUserService extends UserServiceImpl {
    private String id;

    public TestUserService(UserDao userDao, DataSource dataSource, MailSender mailSender) {
        super(userDao, dataSource, mailSender);
    }

    public TestUserService(UserDao userDao, DataSource dataSource, MailSender mailSender, String id) {
        super(userDao, dataSource, mailSender);
        this.id = id;
    }

    @Override
    protected void upgradeLevel(User user) {
        if (user.getId().equals(this.id)) throw new TestUserServiceException();
        super.upgradeLevel(user);
    }

    static class TestUserServiceException extends RuntimeException {
    }
}
