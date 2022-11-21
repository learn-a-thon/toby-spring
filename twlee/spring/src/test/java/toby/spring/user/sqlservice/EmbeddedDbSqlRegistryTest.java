package toby.spring.user.sqlservice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import toby.spring.user.exception.SqlUpdateFailureException;
import toby.spring.user.sqlservice.updatable.EmbeddedDbSqlRegistry;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;

public class EmbeddedDbSqlRegistryTest extends AbstractUpdatableSqlRegistryTest {
    private EmbeddedDatabase db;

    @AfterEach
    void tearDown() {
        db.shutdown();
    }

    @Override
    protected UpdatableSqlRegistry createUpdatableSqlRegistry() {
        db = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .addScript("schema.sql")
                .build();
        return new EmbeddedDbSqlRegistry(new JdbcTemplate(db));
    }

    @Test
    void transactionUpdate() {
        checkFindResult("value1", "value2", "value3");

        Map<String, String> sqlmap = new HashMap<>();
        sqlmap.put("key1", "modifiedValue1");
        sqlmap.put("key@#!SAD@#", "modifiedValue2");

        try {
            sqlRegistry.updateSql(sqlmap);
            fail();
        } catch (SqlUpdateFailureException e) {
            System.out.println(e.getMessage());
        }
        checkFindResult("value1", "value2", "value3");
    }
}
