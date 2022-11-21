package toby.spring.user.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import toby.spring.user.sqlservice.OxmSqlV5Service;
import toby.spring.user.sqlservice.SqlMapConfig;
import toby.spring.user.sqlservice.SqlRegistry;
import toby.spring.user.sqlservice.SqlService;
import toby.spring.user.sqlservice.updatable.EmbeddedDbSqlRegistry;

@Configuration
public class SqlServiceContext {
    @Autowired
    private SqlMapConfig sqlMapConfig;

    @Bean
    public SqlService sqlService() {
        OxmSqlV5Service oxmSqlV5Service = new OxmSqlV5Service(sqlRegistry(), unmarshaller());
        oxmSqlV5Service.setSqlmapResource(sqlMapConfig.getSqlMapResource());
        return oxmSqlV5Service;
    }

    @Bean
    public Jaxb2Marshaller unmarshaller() {
        Jaxb2Marshaller unmarshaller = new Jaxb2Marshaller();
        unmarshaller.setContextPath("toby.spring.user.sqlservice.jaxb");
        return unmarshaller;
    }

    @Bean
    public SqlRegistry sqlRegistry() {
        return new EmbeddedDbSqlRegistry(embeddedJdbcTemplate());
    }

    @Bean
    public JdbcTemplate embeddedJdbcTemplate() {
        EmbeddedDatabase db = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .addScript("schema.sql")
                .build();
        return new JdbcTemplate(db);
    }
}
