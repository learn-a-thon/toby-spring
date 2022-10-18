package toby.spring.user.service;

import toby.spring.user.domain.User;

public interface UserService {
    void add(User user);
    void upgradeLevels();
}
