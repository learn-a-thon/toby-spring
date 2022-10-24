package toby.spring.learningtest.jdk;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

public class ReflectionTest {

    @Test
    void invokeMethod() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String name = "Hello";
        int nameLength = name.length();
        int firstIndex = 0;
        char firstChar = name.charAt(firstIndex);

        // length()
        assertThat(name.length()).isEqualTo(nameLength);
        Method lengthMethod = String.class.getMethod("length");
        assertThat(lengthMethod.invoke(name)).isEqualTo(nameLength);

        // charAt()
        assertThat(name.charAt(firstIndex)).isEqualTo(firstChar);
        Method charAtMethod = String.class.getMethod("charAt", int.class);
        assertThat(charAtMethod.invoke(name, 0)).isEqualTo(firstChar);
    }
}
