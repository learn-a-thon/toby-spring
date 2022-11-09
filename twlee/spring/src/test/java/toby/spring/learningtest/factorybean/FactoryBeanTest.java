package toby.spring.learningtest.factorybean;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import toby.spring.learningtest.factorybean.code.Message;
import toby.spring.learningtest.factorybean.code.MessageFactoryBean;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class FactoryBeanTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void getMessageFromFactoryBean() {
        Object message = context.getBean("message");
        assertThat(message).isInstanceOf(Message.class);
        assertThat(((Message) message).getText()).isEqualTo("Factory Bean");
    }

    @Test
    void getMessageFactoryBean() {
        Object message = context.getBean("&message");
        assertThat(message).isInstanceOf(MessageFactoryBean.class);
    }
}
