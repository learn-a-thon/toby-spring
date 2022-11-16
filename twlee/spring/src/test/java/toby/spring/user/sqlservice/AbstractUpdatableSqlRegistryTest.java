package toby.spring.user.sqlservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import toby.spring.user.exception.SqlNotFoundException;
import toby.spring.user.exception.SqlUpdateFailureException;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public abstract class AbstractUpdatableSqlRegistryTest {
    protected UpdatableSqlRegistry sqlRegistry;

    @BeforeEach
    public void setUp() {
        sqlRegistry = createUpdatableSqlRegistry();
        sqlRegistry.registerSql("key1", "value1");
        sqlRegistry.registerSql("key2", "value2");
        sqlRegistry.registerSql("key3", "value3");
    }

    abstract protected UpdatableSqlRegistry createUpdatableSqlRegistry();

    @Test
    protected void unknownKey() {
        assertThatThrownBy(() -> sqlRegistry.findSql("unknownKey"))
                .isInstanceOf(SqlNotFoundException.class);
    }

    @Test
    protected void updateSingle() {
        sqlRegistry.updateSql("key2", "modifyValue2");
    }

    @Test
    protected void updateMulti() {
        Map<String, String> sqlmap = new HashMap<>();
        sqlmap.put("key1", "modifyValue1");
        sqlmap.put("key2", "modifyValue2");
        sqlRegistry.updateSql(sqlmap);
    }

    @Test
    protected void updateWithNotExistingKey() {
        assertThatThrownBy(() -> sqlRegistry.updateSql("unknownKey", "hello"))
                .isInstanceOf(SqlUpdateFailureException.class);
    }

    protected void checkFindResult(String expected1, String expected2, String expected3) {
        assertThat(sqlRegistry.findSql("key1")).isEqualTo(expected1);
        assertThat(sqlRegistry.findSql("key2")).isEqualTo(expected2);
        assertThat(sqlRegistry.findSql("key3")).isEqualTo(expected3);
    }
}
