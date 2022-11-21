package toby.spring.user.sqlservice.updatable;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import toby.spring.user.exception.SqlNotFoundException;
import toby.spring.user.exception.SqlUpdateFailureException;
import toby.spring.user.sqlservice.UpdatableSqlRegistry;

import java.util.Map;
import java.util.Objects;

@Component
public class EmbeddedDbSqlRegistry implements UpdatableSqlRegistry {
    private static final String NOT_FOUND_MESSAGE = " Of Key SQL Not Found";

    private final JdbcTemplate embeddedJdbcTemplate;
    private final TransactionTemplate transactionTemplate;

    public EmbeddedDbSqlRegistry(@Qualifier("embeddedJdbcTemplate") JdbcTemplate embeddedJdbcTemplate) {
        this.embeddedJdbcTemplate = embeddedJdbcTemplate;
        transactionTemplate = new TransactionTemplate(new DataSourceTransactionManager(Objects.requireNonNull(embeddedJdbcTemplate.getDataSource())));
    }

    @Override
    public void updateSql(String key, String sql) throws SqlUpdateFailureException {
        int affected = embeddedJdbcTemplate.update("update sqlmap set sql_ = ? where key_ = ?", sql, key);
        if (affected == 0) {
            throw new SqlUpdateFailureException(key + NOT_FOUND_MESSAGE);
        }
    }

    @Override
    public void updateSql(final Map<String, String> sqlmap) throws SqlUpdateFailureException {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            // 트랜잭션 템플릿이 만드는 트랜잭션 경계 안에서 동작할 코드를 콜백 형태로 만들고 execute 메소드에 전달
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                for (Map.Entry<String, String> entry : sqlmap.entrySet()) {
                    updateSql(entry.getKey(), entry.getValue());
                }
            }
        });
    }

    @Override
    public void registerSql(String key, String sql) {
        embeddedJdbcTemplate.update("insert into sqlmap(key_, sql_) values (?, ?)", key, sql);
    }

    @Override
    public String findSql(String key) throws SqlNotFoundException {
        try {
            return embeddedJdbcTemplate.queryForObject("select sql_ from sqlmap where key_ = ?", String.class, key);
        } catch (EmptyResultDataAccessException e) {
            throw new SqlNotFoundException(key + NOT_FOUND_MESSAGE, e);
        }
    }
}
