package toby.spring.learningtest.junit;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class JUnitTest {
    static Set<JUnitTest> set = new HashSet<>();

    @Test
    void test1() {
        assertThat(set).doesNotContain(this);
        set.add(this);
    }

    @Test
    void test2() {
        assertThat(set).doesNotContain(this);
        set.add(this);
    }

    @Test
    void test3() {
        assertThat(set).doesNotContain(this);
        set.add(this);
    }
}
