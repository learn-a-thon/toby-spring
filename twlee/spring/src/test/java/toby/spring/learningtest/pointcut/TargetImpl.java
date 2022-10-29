package toby.spring.learningtest.pointcut;

public class TargetImpl implements Target {
    @Override
    public void hello() {
    }

    @Override
    public void hello(String name) {
    }

    @Override
    public int minus(int a, int b) throws RuntimeException {
        return 0;
    }

    @Override
    public int plus(int a, int b) {
        return 0;
    }

    @Override
    public void method() {
    }
}
