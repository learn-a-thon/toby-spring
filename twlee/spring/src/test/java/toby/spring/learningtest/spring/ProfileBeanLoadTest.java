package toby.spring.learningtest.spring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import toby.spring.user.config.AppContext;

@ActiveProfiles("test")
@ContextConfiguration(classes = AppContext.class)
public class ProfileBeanLoadTest {

    @Autowired
    private DefaultListableBeanFactory beanFactory;

    @Test
    void loadBean() {
        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            System.out.println("beanName = " + beanName);
        }
    }
}
