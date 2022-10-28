package toby.spring.user.config;

import lombok.RequiredArgsConstructor;
import org.springframework.aop.Advisor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import toby.spring.user.aop.advice.TransactionAdvice;
import toby.spring.user.aop.poincut.NameMatchClassMethodPointcut;

@Configuration
@RequiredArgsConstructor
public class AppConfig {
//    private final PlatformTransactionManager transactionManager;
//
//    @Bean
//    public Advisor transactionAdvisor() {
//        return new DefaultPointcutAdvisor(transactionPointcut(), transactionAdvice());
//    }
//
//    @Bean
//    public Pointcut transactionPointcut() {
//        NameMatchClassMethodPointcut pointcut = new NameMatchClassMethodPointcut();
//        pointcut.setClassFilter("*Service");
//        pointcut.setMappedName("upgrade*");
//        return pointcut;
//    }
//
//    @Bean
//    public TransactionAdvice transactionAdvice() {
//        return new TransactionAdvice(transactionManager);
//    }
//
//    @Bean
//    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
//        return new DefaultAdvisorAutoProxyCreator();
//    }
}
