package toby.spring.user.dao;

import toby.spring.user.domain.User;

import java.util.List;

public class MockUserDao implements UserDao {
    private List<User> users;

    public MockUserDao(List<User> users) {
        this.users = users;
    }

    @Override
    public void add(User user) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add_exception(User user) {
        throw new UnsupportedOperationException();
    }

    @Override
    public User get(String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<User> getAll() {
        return users;
    }

    @Override
    public void deleteAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getCount() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void update(User user) {

    }
}
