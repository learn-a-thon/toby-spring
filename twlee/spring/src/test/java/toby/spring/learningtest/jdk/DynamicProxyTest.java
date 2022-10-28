package toby.spring.learningtest.jdk;

import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.NameMatchMethodPointcut;
import toby.spring.learningtest.jdk.code.*;

import java.lang.reflect.Proxy;

import static org.assertj.core.api.Assertions.assertThat;

public class DynamicProxyTest {

    @Test
    void simpleProxy() {
        Hello hello = new HelloTarget();
        String name = "Spring";
        assertThat(hello.sayHello(name)).isEqualTo("Hello " + name);
        assertThat(hello.sayHi(name)).isEqualTo("Hi " + name);
        assertThat(hello.sayThankYou(name)).isEqualTo("Thank You " + name);
    }

    @Test
    void decorator() {
        Hello hello = new HelloUppercase(new HelloTarget());
        String name = "Spring";
        assertThat(hello.sayHello(name)).isEqualTo("HELLO " + name.toUpperCase());
        assertThat(hello.sayHi(name)).isEqualTo("HI " + name.toUpperCase());
        assertThat(hello.sayThankYou(name)).isEqualTo("THANK YOU " + name.toUpperCase());
    }

    @Test
    void createDynamicProxy() {
        // 다이내믹 프록시 객체가 타깃 인터페이스를 구현하고 있어서 캐스팅에 안전
        Hello helloProxy = (Hello) Proxy.newProxyInstance(
                //동적으로 생성되는 다이내믹 프록시 클래스의 로딩에 사용할 클래스 로더
                getClass().getClassLoader(),
                //구현할 인터페이스
                new Class[]{Hello.class},
                //부가기능과 위임 코드를 담은 invocationHandler
                new UppercaseHandler(new HelloTarget())
        );
        System.out.println(helloProxy.getClass());
    }

    @Test
    void proxyFactoryBean() {
        ProxyFactoryBean factoryBean = new ProxyFactoryBean();
        // 타깃 설정
        factoryBean.setTarget(new HelloTarget());
        // 부가기능을 담은 어드바이스 추가, 여러 개를 추가할 수 있다.
        factoryBean.addAdvice(new UppercaseAdvice());

        String name = "Spring";
        Hello target = (Hello) factoryBean.getObject();
        System.out.println(target.getClass());

        assertThat(target.sayHello(name)).isEqualTo("HELLO " + name.toUpperCase());
        assertThat(target.sayHi(name)).isEqualTo("HI " + name.toUpperCase());
        assertThat(target.sayThankYou(name)).isEqualTo("THANK YOU " + name.toUpperCase());
    }

    /**
     * 어드바이스 + 포인트컷 학습 테스트
     */
    @Test
    void pointcutAdvisor() {
        ProxyFactoryBean factoryBean = new ProxyFactoryBean();
        factoryBean.setTarget(new HelloTarget());

        NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut();
        pointcut.setMappedName("sayH*");

        factoryBean.addAdvisor(new DefaultPointcutAdvisor(pointcut, new UppercaseAdvice()));

        String name = "Spring";
        Hello target = (Hello) factoryBean.getObject();
        System.out.println(target.getClass());

        assertThat(target.sayHello(name)).isEqualTo("HELLO " + name.toUpperCase());
        assertThat(target.sayHi(name)).isEqualTo("HI " + name.toUpperCase());
        assertThat(target.sayThankYou(name)).isEqualTo("Thank You " + name);
    }
}
