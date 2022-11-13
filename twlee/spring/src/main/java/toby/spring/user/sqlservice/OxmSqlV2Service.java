package toby.spring.user.sqlservice;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import toby.spring.user.dao.UserDao;
import toby.spring.user.exception.SqlRetrievalFailureException;
import toby.spring.user.sqlservice.jaxb.SqlType;
import toby.spring.user.sqlservice.jaxb.Sqlmap;

import javax.annotation.PostConstruct;
import javax.xml.transform.stream.StreamSource;

@Component
public class OxmSqlV2Service implements SqlService {
    private final BaseSqlService baseSqlService;

    public OxmSqlV2Service(@Qualifier("hashMapSqlRegistry") SqlRegistry sqlRegistry) {
        Jaxb2Marshaller unmarshaller = new Jaxb2Marshaller();
        unmarshaller.setContextPath("toby.spring.user.sqlservice.jaxb");
        baseSqlService = new BaseSqlService(new OxmSqlReader(unmarshaller), sqlRegistry);
    }

    @PostConstruct
    private void init() {
        baseSqlService.init();
    }

    @Override
    public String getSql(String key) throws SqlRetrievalFailureException {
        return baseSqlService.getSql(key);
    }

    private final class OxmSqlReader implements SqlReader {
        private Unmarshaller unmarshaller;
        private String sqlmapFile;

        public OxmSqlReader(Unmarshaller unmarshaller) {
            this.unmarshaller = unmarshaller;
            this.sqlmapFile = "/sqlmap.xml";
        }

        @Override
        public void read(SqlRegistry sqlRegistry) {
            System.out.println("load sqlmapFile = " + sqlmapFile);
            try {
                StreamSource source = new StreamSource(UserDao.class.getResourceAsStream(sqlmapFile));
                Sqlmap sqlmap = (Sqlmap) unmarshaller.unmarshal(source);
                for (SqlType sqlType : sqlmap.getSql()) {
                    System.out.printf("key = %s, value = %s\n", sqlType.getKey(), sqlType.getValue());
                    sqlRegistry.registerSql(sqlType.getKey(), sqlType.getValue());
                }
            } catch (Exception e) {
                throw new IllegalArgumentException(sqlmapFile + "을 가져올 수 없습니다.");
            }
        }

        public void setUnmarshaller(Unmarshaller unmarshaller) {
            this.unmarshaller = unmarshaller;
        }

        public void setSqlmapFile(String sqlmapFile) {
            this.sqlmapFile = sqlmapFile;
        }
    }
}
