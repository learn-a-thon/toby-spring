package toby.spring.user.config;

import lombok.RequiredArgsConstructor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.MailSender;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import toby.spring.user.aop.poincut.NameMatchClassMethodPointcut;
import toby.spring.user.dao.UserDao;
import toby.spring.user.service.TestUserServiceImpl;
import toby.spring.user.service.UserService;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@TestConfiguration
@RequiredArgsConstructor
public class TestAppConfig {
    private final UserDao userDao;
    private final DataSource dataSource;
    private final MailSender mockMailSender;
    private final TransactionManager transactionManager;

    @Bean
    public UserService testUserServiceImpl() {
        return new TestUserServiceImpl(userDao, dataSource, mockMailSender);
    }

    @Bean
    public DefaultPointcutAdvisor transactionAdvisor() {
        return new DefaultPointcutAdvisor(aspectJExpressionPointcut(), transactionAdvice());
    }

    @Bean
    public NameMatchClassMethodPointcut transactionPointcut(){
        NameMatchClassMethodPointcut pointcut = new NameMatchClassMethodPointcut();
        pointcut.setMappedClassName("*ServiceImpl");
        pointcut.setMappedNames("*");
        return pointcut;
    }

    private AspectJExpressionPointcut aspectJExpressionPointcut() {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("bean(*ServiceImpl)");
        return pointcut;
    }

    private NameMatchTransactionAttributeSource transactionAttributeSource() {
        NameMatchTransactionAttributeSource attributeSource = new NameMatchTransactionAttributeSource();

        Map<String, TransactionAttribute> matches = new HashMap<>();
        RuleBasedTransactionAttribute rule1 = new RuleBasedTransactionAttribute();
        rule1.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        rule1.setReadOnly(true);
        matches.put("get*", rule1);

        RuleBasedTransactionAttribute rule2 = new RuleBasedTransactionAttribute();
        rule2.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        matches.put("*", rule2);

        attributeSource.setNameMap(matches);
        return attributeSource;
    }

    @Bean
    public TransactionInterceptor transactionAdvice() {
//        return new TransactionAdvice(transactionManager);
        return new TransactionInterceptor(transactionManager, transactionAttributeSource());
    }

    @Bean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        return new DefaultAdvisorAutoProxyCreator();
    }
}
