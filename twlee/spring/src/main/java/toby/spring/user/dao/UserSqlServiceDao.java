package toby.spring.user.dao;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import toby.spring.user.domain.Level;
import toby.spring.user.domain.User;
import toby.spring.user.exception.DuplicateUserIdException;
import toby.spring.user.sqlservice.SqlService;

import javax.sql.DataSource;
import java.util.List;


public class UserSqlServiceDao implements UserDao {
    private final JdbcTemplate jdbcTemplate;
    private final SqlService sqlService;

    public UserSqlServiceDao(DataSource dataSource, SqlService sqlService) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.sqlService = sqlService;
    }

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
                    sqlService.getSql("userAdd"),
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
                sqlService.getSql("userAddEx"),
                user.getId(),
                user.getName(),
                user.getPassword(),
                user.getLevel().intValue(),
                user.getLogin(),
                user.getRecommend(),
                user.getEmail());
    }

    public User get(String id) {
        return jdbcTemplate.queryForObject(
                sqlService.getSql("userGet"),
                userRowMapper,
                id);
    }

    public int getCount() {
        return jdbcTemplate.query(sqlService.getSql("userGetCount"), rs -> {
            rs.next();
            return rs.getInt(1);
        });
    }

    public void deleteAll() {
        jdbcTemplate.update(sqlService.getSql("userDeleteAll"));
    }

    public List<User> getAll() {
        return jdbcTemplate.query(sqlService.getSql("userGetAll"), userRowMapper);
    }

    public void update(User user) {
        int result = jdbcTemplate.update(
                sqlService.getSql("userUpdate"),
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
}
