package toby.spring.user.dao;

import toby.spring.user.domain.User;

import java.util.List;

public interface UserDao {
    void add(User user);

    void add_exception(User user);

    User get(String id);

    List<User> getAll();

    void deleteAll();

    int getCount();
}
