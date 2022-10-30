package toby.spring.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import toby.spring.user.domain.User;

import java.util.Arrays;
import java.util.List;

import static toby.spring.user.UserFixture.*;

@SpringBootTest
public class TransactionTest {

    @Autowired
    private UserService userServiceImpl;

    private List<User> userList;

    @BeforeEach
    void setUp() {
        userList = Arrays.asList(NON_USER1, UP_USER1, NON_USER2, UP_USER2);
    }

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void transactionSync() {
        userServiceImpl.deleteAll();
        userServiceImpl.add(userList.get(0));
        userServiceImpl.add(userList.get(1));
    }

    @Test
    void transactionSync_declarative() {
        DefaultTransactionDefinition txDefinition = new DefaultTransactionDefinition();
        txDefinition.setReadOnly(true);

        TransactionStatus txStatus = transactionManager.getTransaction(txDefinition);

        userServiceImpl.deleteAll();
        userServiceImpl.add(userList.get(0));
        userServiceImpl.add(userList.get(1));

        transactionManager.commit(txStatus);
    }

    @Test
    void transactionSync_declarative_rollback() {
        DefaultTransactionDefinition txDefinition = new DefaultTransactionDefinition();
        txDefinition.setReadOnly(true);

        TransactionStatus txStatus = transactionManager.getTransaction(txDefinition);
        try {
            userServiceImpl.deleteAll();
            userServiceImpl.add(userList.get(0));
            userServiceImpl.add(userList.get(1));
        } catch (Exception e) {
        } finally {
            transactionManager.rollback(txStatus);
        }
    }

    @Transactional(readOnly = true)
    @Test
    void transactionSync_annotation() {
        userServiceImpl.deleteAll();
        userServiceImpl.add(userList.get(0));
        userServiceImpl.add(userList.get(1));
    }
}
