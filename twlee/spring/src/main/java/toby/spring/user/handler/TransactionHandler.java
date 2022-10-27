package toby.spring.user.handler;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TransactionHandler implements InvocationHandler {
    // 부가기능을 제공할 타깃 객체 타입이 Object이기 때문에 어떤 타입의 객체도 적용 가능
    private Object target;
    // 트랜잭션 기능을 제공하는데 필요한 트랜잭션 매니저
    private PlatformTransactionManager transactionManager;
    // 부가기능을 적용할 메소드 이름 패턴
    private String pattern;

    public TransactionHandler(Object target, PlatformTransactionManager transactionManager) {
        this.target = target;
        this.transactionManager = transactionManager;
    }

    public TransactionHandler(Object target, PlatformTransactionManager transactionManager, String pattern) {
        this.target = target;
        this.transactionManager = transactionManager;
        this.pattern = pattern;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().startsWith(pattern)) {
            return invokeTransaction(method, args);
        }
        return method.invoke(target, args);
    }

    private Object invokeTransaction(Method method, Object[] args) throws Throwable {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            Object obj = method.invoke(target, args);
            transactionManager.commit(status);
            return obj;
        } catch (InvocationTargetException e) {
            transactionManager.rollback(status);
            throw e;
        }
    }
}
