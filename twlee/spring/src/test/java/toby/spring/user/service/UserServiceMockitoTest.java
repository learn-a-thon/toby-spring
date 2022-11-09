package toby.spring.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import toby.spring.user.dao.UserDao;
import toby.spring.user.domain.User;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static toby.spring.user.UserFixture.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class UserServiceMockitoTest {

    @Mock
    private UserDao userDao;

    @Mock
    private DataSource dataSource;

    @Mock
    private MockMailSender mailSender;

    private UserService userServiceImpl;

    private List<User> userList;

    @BeforeEach
    void setUp() {
        userServiceImpl = new UserServiceImpl(userDao, dataSource, mailSender);
        userList = Arrays.asList(USER4, USER5, USER6, USER7, USER8, USER9);
    }

    @Test
    void mockitoUpgradeLevels() {
        when(userDao.getAll()).thenReturn(userList);

        userServiceImpl.upgradeLevels();

        verify(userDao, times(3)).update(any(User.class));
    }
}
