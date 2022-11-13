package toby.spring.user.sqlservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import toby.spring.user.exception.SqlNotFoundException;
import toby.spring.user.exception.SqlUpdateFailureException;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConcurrentHashMapSqlRegistryTest {
    private UpdatableSqlRegistry sqlRegistry;

    @BeforeEach
    void setUp() {
        sqlRegistry = new ConcurrentHashMapSqlRegistry();
        sqlRegistry.registerSql("key1", "value1");
        sqlRegistry.registerSql("key2", "value2");
        sqlRegistry.registerSql("key3", "value3");
    }

    @Test
    void unknownKey() {
        assertThatThrownBy(() -> sqlRegistry.findSql("unknownKey"))
                .isInstanceOf(SqlNotFoundException.class);
    }

    @Test
    void updateSingle() {
        sqlRegistry.updateSql("key2", "modifyValue2");
    }

    @Test
    void updateMulti() {
        Map<String, String> sqlmap = new HashMap<>();
        sqlmap.put("key1", "modifyValue1");
        sqlmap.put("key2", "modifyValue2");
        sqlRegistry.updateSql(sqlmap);
    }

    @Test
    void updateWithNotExistingKey() {
        assertThatThrownBy(() -> sqlRegistry.updateSql("unknownKey", "hello"))
                .isInstanceOf(SqlUpdateFailureException.class);
    }

    private void checkFindResult(String expected1, String expected2, String expected3) {
        assertThat(sqlRegistry.findSql("key1")).isEqualTo("value1");
        assertThat(sqlRegistry.findSql("key2")).isEqualTo("value2");
        assertThat(sqlRegistry.findSql("key3")).isEqualTo("value3");
    }
}
