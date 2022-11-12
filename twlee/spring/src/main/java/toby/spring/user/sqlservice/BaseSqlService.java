package toby.spring.user.sqlservice;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import toby.spring.user.exception.SqlRetrievalFailureException;

import javax.annotation.PostConstruct;
import java.util.Objects;

@Component
public class BaseSqlService implements SqlService {
    protected SqlReader sqlReader;
    protected SqlRegistry sqlRegistry;

    public BaseSqlService(@Qualifier("jaxbXmlSqlReader") SqlReader sqlReader,
                          @Qualifier("hashMapSqlRegistry") SqlRegistry sqlRegistry) {
        this.sqlReader = sqlReader;
        this.sqlRegistry = sqlRegistry;
    }

    @PostConstruct
    private void init() {
        sqlReader.read(sqlRegistry);
    }

    @Override
    public String getSql(String key) throws SqlRetrievalFailureException {
        String sql = sqlRegistry.findSql(key);
        System.out.println("call sql = " + sql);
        if (Objects.isNull(sql)) {
            throw new SqlRetrievalFailureException(key + "에 대한 SQL을 찾을 수 없습니다");
        }
        return sql;
    }
}
