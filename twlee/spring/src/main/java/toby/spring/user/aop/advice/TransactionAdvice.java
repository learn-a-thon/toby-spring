package toby.spring.user.aop.advice;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.lang.reflect.InvocationTargetException;

public class TransactionAdvice implements MethodInterceptor {
    private final PlatformTransactionManager transactionManager;

    public TransactionAdvice(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    /**
     * 타깃을 호출하는 기능을 가진 콜백 객체를 프록시로부터 받는다
     * 어드바이스는 특정 타깃에 의존하지 않고 재사용 가능하다.
     */
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            /* 콜백을 호출해서 타깃의 메소드를 실행한다 타깃 메소드 호출 전후로 필요한 부가기능을 넣을 수 있다.
             * 경우에 따라 타깃이 아예 호출되지 않게 하거나 재시도를 위한 반복 호출도 가능하다.
             */
            Object obj = invocation.proceed();
            transactionManager.commit(status);
            return obj;
        } catch (Exception e) {
            transactionManager.rollback(status);
            throw e;
        }
    }
}
