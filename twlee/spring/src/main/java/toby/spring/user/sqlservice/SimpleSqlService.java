package toby.spring.user.sqlservice;

import toby.spring.user.exception.SqlRetrievalFailureException;

import java.util.Map;
import java.util.Objects;

public class SimpleSqlService implements SqlService {
    private final Map<String, String> sqlMap;

    public SimpleSqlService() {
        sqlMap = Map.of(
                "userAdd", "insert into users (id, name, password, level, login, recommend, email) values (?, ?, ?, ?, ?, ?, ?)",
                "userGet", "select * from users where id = ?",
                "userGetAll", "select * from users order by id",
                "userDeleteAll", "delete from users",
                "userGetCount", "select count(*) from users",
                "userUpdate", "update users set name = ?, password = ?, level = ?, login = ?, recommend = ?, email = ? where id = ?"
        );
    }

    @Override
    public String getSql(String key) throws SqlRetrievalFailureException {
        String sql = sqlMap.get(key);
        if (Objects.isNull(sql)) {
            throw new SqlRetrievalFailureException(key + "에 대한 SQL을 찾을 수 없습니다");
        }
        return sql;
    }
}
