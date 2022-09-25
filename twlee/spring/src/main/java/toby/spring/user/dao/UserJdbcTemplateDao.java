package toby.spring.user.dao;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import toby.spring.user.domain.User;
import toby.spring.user.exception.DuplicateUserIdException;

import javax.sql.DataSource;
import java.util.List;

public class UserJdbcTemplateDao implements UserDao {
    private JdbcTemplate jdbcTemplate;
    private RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getString("id"));
        user.setName(rs.getString("name"));
        user.setPassword(rs.getString("password"));
        return user;
    };

    public void add(final User user) {
        try {
            jdbcTemplate.update(
                    "insert into users (id, name, password) values (?, ?, ?)",
                    user.getId(),
                    user.getName(),
                    user.getPassword());
        } catch (DuplicateKeyException e) {
            throw new DuplicateUserIdException(e); // 예외 전환
        }
    }

    public void add_exception(final User user) throws DuplicateKeyException {
        jdbcTemplate.update(
                "insert into users (id, name, password) values (?, ?, ?)",
                user.getId(),
                user.getName(),
                user.getPassword());
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

    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
}
