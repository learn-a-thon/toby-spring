package toby.spring.learningtest.pointcut;

public interface Target {
    void hello();
    void hello(String name);
    int minus(int a, int b) throws RuntimeException;
    int plus(int a, int b);
    void method();
}
