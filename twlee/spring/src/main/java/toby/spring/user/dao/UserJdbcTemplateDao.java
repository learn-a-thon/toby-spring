package toby.spring.user.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import toby.spring.user.domain.Level;
import toby.spring.user.domain.User;
import toby.spring.user.exception.DuplicateUserIdException;

import javax.sql.DataSource;
import java.util.List;

@Component
public class UserJdbcTemplateDao implements UserDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getString("id"));
        user.setName(rs.getString("name"));
        user.setPassword(rs.getString("password"));
        user.setLevel(Level.valueOf(rs.getInt("level")));
        user.setLogin(rs.getInt("login"));
        user.setRecommend(rs.getInt("recommend"));
        user.setEmail(rs.getString("email"));
        return user;
    };

    public void add(final User user) {
        try {
            jdbcTemplate.update(
                    "insert into users (id, name, password, level, login, recommend, email) values (?, ?, ?, ?, ?, ?, ?)",
                    user.getId(),
                    user.getName(),
                    user.getPassword(),
                    user.getLevel().intValue(),
                    user.getLogin(),
                    user.getRecommend(),
                    user.getEmail());
        } catch (DuplicateKeyException e) {
            throw new DuplicateUserIdException(e); // 예외 전환
        }
    }

    public void add_exception(final User user) throws DuplicateKeyException {
        jdbcTemplate.update(
                "insert into users (id, name, password, level, login, recommend, email) values (?, ?, ?, ?, ?, ?, ?)",
                user.getId(),
                user.getName(),
                user.getPassword(),
                user.getLevel().intValue(),
                user.getLogin(),
                user.getRecommend());
    }

    public User get(String id) {
        return jdbcTemplate.queryForObject("select * from users where id = ?",
                userRowMapper,
                id);
    }

    public int getCount() {
        return jdbcTemplate.query("select count(*) from users", rs -> {
            rs.next();
            return rs.getInt(1);
        });
    }

    public void deleteAll() {
        jdbcTemplate.update("delete from users");
    }

    public List<User> getAll() {
        return jdbcTemplate.query("select * from users order by id", userRowMapper);
    }

    public void update(User user) {
        int result = jdbcTemplate.update(
                "update users set " +
                        "name = ?, " +
                        "password = ?, " +
                        "level = ?, " +
                        "login = ?, " +
                        "recommend = ?, " +
                        "email = ? " +
                        "where id = ?",
                user.getName(),
                user.getPassword(),
                user.getLevel().intValue(),
                user.getLogin(),
                user.getRecommend(),
                user.getEmail(),
                user.getId());
        if (result != 1) {
            throw new RuntimeException("수정 실패");
        }
    }

    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
}
