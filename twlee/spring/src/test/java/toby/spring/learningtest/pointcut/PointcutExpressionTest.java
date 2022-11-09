package toby.spring.learningtest.pointcut;

import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

public class PointcutExpressionTest {

    @Test
    void methodPullSignature() throws NoSuchMethodException {
        Method method = Target.class.getMethod("minus", int.class, int.class);
        System.out.println("method = " + method);
    }

    @Test
    void methodSignaturePointcut() throws NoSuchMethodException {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("execution(public int " +
                "toby.spring.learningtest.pointcut.Target.minus(int,int) " +
                "throws java.lang.RuntimeException)");

        //Target.minus()
        assertThat(pointcut.getClassFilter().matches(Target.class)
                && pointcut.getMethodMatcher().matches(Target.class.getMethod("minus", int.class, int.class), Target.class)).isTrue();

        //Target.plus()
        assertThat(pointcut.getClassFilter().matches(Target.class)
                && pointcut.getMethodMatcher().matches(Target.class.getMethod("plus", int.class, int.class), Target.class)).isFalse();

        //Bean.method()
        assertThat(pointcut.getClassFilter().matches(Bean.class)
                && pointcut.getMethodMatcher().matches(Target.class.getMethod("method"), Bean.class)).isFalse();
    }

    @Test
    void pointcut() throws Exception {
        targetClassPointcutMatches("execution(* *(..))", true, true, true, true, true, true);
    }

    public void targetClassPointcutMatches(String expression, boolean... expected) throws Exception {
        pointcutMatches(expression, expected[0], Target.class, "hello");
        pointcutMatches(expression, expected[1], Target.class, "hello", String.class);
        pointcutMatches(expression, expected[2], Target.class, "plus", int.class, int.class);
        pointcutMatches(expression, expected[3], Target.class, "minus", int.class, int.class);
        pointcutMatches(expression, expected[4], Target.class, "method");
        pointcutMatches(expression, expected[5], Bean.class, "method");
    }

    public void pointcutMatches(String expression, boolean expected, Class<?> clazz, String methodName, Class<?>... args) throws Exception {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression(expression);

        assertThat(pointcut.getClassFilter().matches(clazz)
                && pointcut.getMethodMatcher().matches(clazz.getMethod(methodName, args), clazz)).isEqualTo(expected);
    }
}
