package toby.spring.user.sqlservice;

import org.springframework.stereotype.Component;
import toby.spring.user.dao.UserDao;
import toby.spring.user.sqlservice.jaxb.SqlType;
import toby.spring.user.sqlservice.jaxb.Sqlmap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;

@Component
public class JaxbXmlSqlReader implements SqlReader {
    private final String sqlmapFile;

    public JaxbXmlSqlReader() {
        this.sqlmapFile = "/sqlmap.xml";
    }

    @Override
    public void read(SqlRegistry sqlRegistry) {
        System.out.println("load sqlmapFile = " + sqlmapFile);
        String contextPath = Sqlmap.class.getPackage().getName();
        try {
            JAXBContext context = JAXBContext.newInstance(contextPath);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            InputStream is = UserDao.class.getResourceAsStream(sqlmapFile);
            Sqlmap sqlmap = (Sqlmap) unmarshaller.unmarshal(is);
            for (SqlType sqlType : sqlmap.getSql()) {
                System.out.printf("key = %s, value = %s\n", sqlType.getKey(), sqlType.getValue());
                sqlRegistry.registerSql(sqlType.getKey(), sqlType.getValue());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
