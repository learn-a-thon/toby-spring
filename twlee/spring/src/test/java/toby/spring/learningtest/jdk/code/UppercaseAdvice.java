package toby.spring.learningtest.jdk.code;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class UppercaseAdvice implements MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Object result = invocation.proceed();
        if (result instanceof String) {
            return ((String) result).toUpperCase();
        }
        return result;
    }
}
