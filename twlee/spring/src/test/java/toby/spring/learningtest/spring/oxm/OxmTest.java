package toby.spring.learningtest.spring.oxm;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import toby.spring.user.sqlservice.jaxb.SqlType;
import toby.spring.user.sqlservice.jaxb.Sqlmap;

import javax.xml.transform.stream.StreamSource;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

//test config: https://reflectoring.io/spring-boot-testconfiguration/
@SpringBootTest
public class OxmTest {

    @Autowired
    private Unmarshaller unmarshaller;

    @Test
    void unmarshallSqlMap() throws Exception {
        StreamSource xmlSource = new StreamSource(OxmTest.class.getResourceAsStream("/sqlmapTest.xml"));
        Sqlmap sqlmap = (Sqlmap) unmarshaller.unmarshal(xmlSource);

        List<SqlType> sqlList = sqlmap.getSql();
        assertThat(sqlList.size()).isEqualTo(3);
        assertThat(sqlList.get(0).getKey()).isEqualTo("add");
        assertThat(sqlList.get(0).getValue()).isEqualTo("insert");

        assertThat(sqlList.get(1).getKey()).isEqualTo("get");
        assertThat(sqlList.get(1).getValue()).isEqualTo("select");

        assertThat(sqlList.get(2).getKey()).isEqualTo("delete");
        assertThat(sqlList.get(2).getValue()).isEqualTo("delete");
    }

    @TestConfiguration
    static class OxmTestConfig {
        @Bean
        public Unmarshaller unmarshaller() {
            Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
            jaxb2Marshaller.setContextPath("toby.spring.user.sqlservice.jaxb");
            return jaxb2Marshaller;
        }
    }
}
