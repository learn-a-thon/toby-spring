package toby.spring.learningtest.jdk;

import org.junit.jupiter.api.Test;
import toby.spring.learningtest.jdk.code.Hello;
import toby.spring.learningtest.jdk.code.HelloTarget;
import toby.spring.learningtest.jdk.code.HelloUppercase;
import toby.spring.learningtest.jdk.code.UppercaseHandler;

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
}
