package toby.spring.user.sqlservice;

import org.springframework.stereotype.Component;
import toby.spring.user.exception.SqlNotFoundException;
import toby.spring.user.exception.SqlRetrievalFailureException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class HashMapSqlRegistry implements SqlRegistry {
    private Map<String, String> sqlMap = new HashMap<>();

    @Override
    public void registerSql(String key, String sql) {
        sqlMap.put(key, sql);
    }

    @Override
    public String findSql(String key) throws SqlNotFoundException {
        String sql = sqlMap.get(key);
        System.out.println("call sql = " + sql);
        if (Objects.isNull(sql)) {
            throw new SqlRetrievalFailureException(key + "에 대한 SQL을 찾을 수 없습니다");
        }
        return sql;
    }
}
