package toby.spring.learningtest.spring.embeddeddb;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EmbeddedDbTest {
    private EmbeddedDatabase db;
    private JdbcTemplate template;

    @BeforeEach
    public void setUp() {
        db = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .addScript("schema.sql")
                .addScript("import.sql")
                .build();
        template = new JdbcTemplate(db);
    }

    @AfterEach
    public void tearDown() {
        db.shutdown();
    }

    @Test
    void findFile() throws IOException {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource("schema.sql");
        System.out.println("exists = " + resource.exists());
        System.out.println("getFilename = " + resource.getFilename());
        System.out.println("getPath = " + resource.getFile().getPath());
        System.out.println("getAbsolutePath = " + resource.getFile().getAbsolutePath());
        System.out.println("getCanonicalPath = " + resource.getFile().getCanonicalPath());
        System.out.println("getParent = " + resource.getFile().getParent());
    }

    @Test
    void initData() {
        assertEquals(2, getSqlmapCount());

        List<Map<String, Object>> list = template.queryForList("select * from sqlmap order by key_");
        assertEquals("key1", list.get(0).get("key_"));
        assertEquals("sql1", list.get(0).get("sql_"));
        assertEquals("key2", list.get(1).get("key_"));
        assertEquals("sql2", list.get(1).get("sql_"));
    }

    @Test
    void insert() {
        template.update("insert into sqlmap(key_, sql_) values(?, ?)", "key3", "sql3");

        assertEquals(3, getSqlmapCount());
    }

    private int getSqlmapCount() {
        return template.query("select count(*) from sqlmap", rs -> {
            rs.next();
            int anInt = rs.getInt(1);
            System.out.println("rs = " + anInt);
            return anInt;
        });
    }
}
