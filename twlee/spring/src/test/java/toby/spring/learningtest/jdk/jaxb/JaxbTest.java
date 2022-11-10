package toby.spring.learningtest.jdk.jaxb;

import org.junit.jupiter.api.Test;
import toby.spring.user.sqlservice.jaxb.SqlType;
import toby.spring.user.sqlservice.jaxb.Sqlmap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class JaxbTest {

    //참고: https://homoefficio.github.io/2020/07/21/IDE-%EC%97%90%EC%84%9C%EB%8A%94-%EB%90%98%EB%8A%94%EB%8D%B0-jar-%EC%97%90%EC%84%9C%EB%8A%94-%EC%95%88-%EB%8F%BC%EC%9A%94-Java-Resource/
    @Test
    void readSqlmap() throws JAXBException {
        String contextPath = Sqlmap.class.getPackage().getName();
        JAXBContext context = JAXBContext.newInstance(contextPath);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        InputStream is = JaxbTest.class.getResourceAsStream("/sqlmapTest.xml");
        Sqlmap sqlmap = (Sqlmap) unmarshaller.unmarshal(is);

        List<SqlType> sqlList = sqlmap.getSql();
        for (SqlType sqlType : sqlList) {
            System.out.printf("key = %s, value = %s\n", sqlType.getKey(), sqlType.getValue());
        }
        assertThat(sqlList.size()).isEqualTo(3);
        assertThat(sqlList.get(0).getKey()).isEqualTo("add");
        assertThat(sqlList.get(0).getValue()).isEqualTo("insert");

        assertThat(sqlList.get(1).getKey()).isEqualTo("get");
        assertThat(sqlList.get(1).getValue()).isEqualTo("select");

        assertThat(sqlList.get(2).getKey()).isEqualTo("delete");
        assertThat(sqlList.get(2).getValue()).isEqualTo("delete");
    }
}
