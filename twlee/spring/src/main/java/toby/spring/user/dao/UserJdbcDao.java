package toby.spring.user.dao;

import org.springframework.dao.EmptyResultDataAccessException;
import toby.spring.user.domain.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserJdbcDao {
    private JdbcContext jdbcContext;
    private DataSource dataSource;

    public void add(final User user) throws SQLException {
        StatementStrategy st = conn -> {
            PreparedStatement ps = conn.prepareStatement("insert into users (id, name, password) values (?, ?, ?)");
            ps.setString(1, user.getId());
            ps.setString(2, user.getName());
            ps.setString(3, user.getPassword());
            ps.executeUpdate();
            return ps;
        };
        jdbcContext.workWithStatementStrategy(st);
    }

    public User get(String id) throws SQLException {
        Connection conn = dataSource.getConnection();

        PreparedStatement ps = conn.prepareStatement("select * from users where id = ?");
        ps.setString(1, id);

        ResultSet rs = ps.executeQuery();
        User user = null;
        if (rs.next()) {
            user = new User();
            user.setId(rs.getString("id"));
            user.setName(rs.getString("name"));
            user.setPassword(rs.getString("password"));
        }

        rs.close();
        ps.close();
        conn.close();
        // 조회된 데이터가 없으면 Exception
        if (user == null) {
            throw new EmptyResultDataAccessException(1);
        }
        return user;
    }

    public int getCount() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();

            ps = conn.prepareStatement("select count(*) from users");

            rs = ps.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            return count;
        } catch (SQLException e) {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e1) {
                }
            }
            if (rs != null) {
                try {
                    ps.close();
                } catch (SQLException e1) {
                }
            }
            if (rs != null) {
                try {
                    conn.close();
                } catch (SQLException e1) {
                }
            }
        }
        return 0;
    }

    public void deleteAll() throws SQLException {
        executeSql("delete from users");
    }

    private void executeSql(final String query) throws SQLException {
        jdbcContext.workWithStatementStrategy(conn -> conn.prepareStatement(query));
    }

    public void setDataSource(DataSource dataSource) {
        this.jdbcContext = new JdbcContext();
        jdbcContext.setDataSource(dataSource);
        this.dataSource = dataSource;
    }
}
