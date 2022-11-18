package toby.spring.user.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.mail.MailSender;
import toby.spring.user.dao.UserDao;
import toby.spring.user.service.DummyMailSender;
import toby.spring.user.service.TestUserService;
import toby.spring.user.service.UserService;

import javax.sql.DataSource;

@Profile("test")
@Configuration
public class TestAppContext {
    @Autowired
    private UserDao userDao;

    @Bean
    public DataSource dataSource() {
        SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setDriverClass(org.h2.Driver.class);
        dataSource.setUrl("jdbc:h2:tcp://localhost/~/test");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return dataSource;
    }

    @Bean
    public UserService testUserService() {
        return new TestUserService(userDao, dataSource(), mailSender());
    }

    @Bean
    public MailSender mailSender() {
        return new DummyMailSender();
    }
}
