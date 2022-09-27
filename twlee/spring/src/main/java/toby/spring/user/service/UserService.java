package toby.spring.user.service;

import org.springframework.stereotype.Component;
import toby.spring.user.dao.UserDao;
import toby.spring.user.domain.Level;
import toby.spring.user.domain.User;

import java.util.List;

@Component
public class UserService {
    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public void upgradeLevels() {
        List<User> users = userDao.getAll();
        for (User user : users) {
            Boolean changed = null;
            if (user.getLevel() == Level.BASIC && user.getLogin() >= 50) {
                user.setLevel(Level.SILVER);
                changed = true;
            } else if (user.getLevel() == Level.SILVER && user.getRecommend() >= 30) {
                user.setLevel(Level.GOLD);
                changed = true;
            } else if (user.getLevel() == Level.SILVER) {
                changed = false;
            } else {
                changed = false;
            }
            if (changed) {
                userDao.update(user);
            }
        }
    }
}
