package toby.spring.user.config;

import lombok.RequiredArgsConstructor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.MailSender;
import org.springframework.transaction.PlatformTransactionManager;
import toby.spring.user.aop.advice.TransactionAdvice;
import toby.spring.user.aop.poincut.NameMatchClassMethodPointcut;
import toby.spring.user.dao.UserDao;
import toby.spring.user.service.TestUserServiceImpl;
import toby.spring.user.service.UserService;

import javax.sql.DataSource;

@TestConfiguration
@RequiredArgsConstructor
public class TestAppConfig {
    private final UserDao userDao;
    private final DataSource dataSource;
    private final MailSender mockMailSender;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public UserService testUserServiceImpl() {
        return new TestUserServiceImpl(userDao, dataSource,mockMailSender);
    }

    @Bean
    public DefaultPointcutAdvisor transactionAdvisor() {
        return new DefaultPointcutAdvisor(transactionPointcut(), transactionAdvice());
    }

//    @Bean
    public NameMatchClassMethodPointcut transactionPointcut(){
        NameMatchClassMethodPointcut pointcut = new NameMatchClassMethodPointcut();
        pointcut.setMappedClassName("*ServiceImpl");
        pointcut.setMappedNames("upgrade*");
        return pointcut;
    }

    @Bean
    public AspectJExpressionPointcut aspectJExpressionPointcut() {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("executioin(* *..*ServiceImpl.upgrade*(..))");
        return pointcut;
    }

    @Bean
    public TransactionAdvice transactionAdvice() {
        return new TransactionAdvice(transactionManager);
    }

    @Bean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        return new DefaultAdvisorAutoProxyCreator();
    }
}
