package toby.spring.learningtest.junit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(locations = "/junit.xml")
public class JUnitTest {
    @Autowired
    ApplicationContext context;

    static Set<JUnitTest> set = new HashSet<>();
    static ApplicationContext contextObj = null;

    @Test
    void test1() {
        assertThat(set).doesNotContain(this);
        set.add(this);

        assertThat(contextObj == null || contextObj == this.context).isTrue();
        contextObj = this.context;
    }

    @Test
    void test2() {
        assertThat(set).doesNotContain(this);
        set.add(this);

        assertThat(contextObj == null || contextObj == this.context).isTrue();
        contextObj = this.context;
    }

    @Test
    void test3() {
        assertThat(set).doesNotContain(this);
        set.add(this);

        assertThat(contextObj == null || contextObj == this.context).isTrue();
        contextObj = this.context;
    }
}
