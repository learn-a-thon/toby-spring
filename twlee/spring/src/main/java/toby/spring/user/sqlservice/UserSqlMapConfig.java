package toby.spring.user.sqlservice;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import toby.spring.user.dao.UserDao;

@Component
public class UserSqlMapConfig implements SqlMapConfig {
    @Override
    public Resource getSqlMapResource() {
        return new ClassPathResource("/sqlmap.xml", UserDao.class);
    }
}
