package toby.spring.user.factorybean;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import toby.spring.user.handler.TransactionHandler;
import toby.spring.user.service.UserService;

import java.lang.reflect.Proxy;

@Component("userService")
public class TxProxyFactoryBean implements FactoryBean<Object> {
    private Object target;
    private final PlatformTransactionManager transactionManager;
    private final String pattern;
    // 다이내믹 프록시 생성 시 필요
    private final Class<?> serviceInterface;

    public TxProxyFactoryBean(@Qualifier("userServiceImpl") Object target,
                              @Autowired PlatformTransactionManager transactionManager) {
        this.target = target;
        this.transactionManager = transactionManager;
        this.pattern = "upgradeLevels";
        this.serviceInterface = UserService.class;
    }

    @Override
    public Object getObject() throws Exception {
        TransactionHandler handler = new TransactionHandler(target, transactionManager, pattern);
        return Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{serviceInterface},
                handler);
    }

    @Override
    public Class<?> getObjectType() {
        return null;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    public void setTarget(Object target) {
        this.target = target;
    }
}
