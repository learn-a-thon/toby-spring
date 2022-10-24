package toby.spring.learningtest.jdk;

import org.junit.jupiter.api.Test;
import toby.spring.learningtest.jdk.code.Hello;
import toby.spring.learningtest.jdk.code.HelloTarget;
import toby.spring.learningtest.jdk.code.HelloUppercase;

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
}
