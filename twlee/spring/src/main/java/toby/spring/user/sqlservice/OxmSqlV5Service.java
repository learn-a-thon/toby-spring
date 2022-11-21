package toby.spring.user.sqlservice;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import toby.spring.user.dao.UserDao;
import toby.spring.user.exception.SqlRetrievalFailureException;
import toby.spring.user.sqlservice.jaxb.SqlType;
import toby.spring.user.sqlservice.jaxb.Sqlmap;

import javax.annotation.PostConstruct;
import javax.xml.transform.stream.StreamSource;
import java.util.Objects;

public class OxmSqlV5Service implements SqlService {
    private OxmSqlReader oxmSqlReader;
    private SqlRegistry sqlRegistry;

    public OxmSqlV5Service(SqlRegistry sqlRegistry, Jaxb2Marshaller unmarshaller) {
        this.oxmSqlReader = new OxmSqlReader(unmarshaller);
        this.sqlRegistry = sqlRegistry;
    }

    @PostConstruct
    private void init() {
        oxmSqlReader.read(sqlRegistry);
    }

    @Override
    public String getSql(String key) throws SqlRetrievalFailureException {
        String sql = sqlRegistry.findSql(key);
        System.out.println("call sql = " + sql);
        if (Objects.isNull(sql)) {
            throw new SqlRetrievalFailureException(key + "에 대한 SQL을 찾을 수 없습니다");
        }
        return sql;
    }

    public void setUnmarshaller(Unmarshaller unmarshaller) {
        this.oxmSqlReader.setUnmarshaller(unmarshaller);;
    }

    public void setSqlmapResource(Resource resource) {
        this.oxmSqlReader.setSqlmapResource(resource);
    }

    private final class OxmSqlReader implements SqlReader {
        private Unmarshaller unmarshaller;
        private Resource sqlmapResource = new ClassPathResource("/sqlmap.xml", UserDao.class);

        public OxmSqlReader(Unmarshaller unmarshaller) {
            this.unmarshaller = unmarshaller;
        }

        @Override
        public void read(SqlRegistry sqlRegistry) {
            String filename = sqlmapResource.getFilename();
            System.out.println("load sqlmapFile = " + filename);
            try {
                StreamSource source = new StreamSource(sqlmapResource.getInputStream());
                Sqlmap sqlmap = (Sqlmap) unmarshaller.unmarshal(source);
                for (SqlType sqlType : sqlmap.getSql()) {
                    System.out.printf("key = %s, value = %s\n", sqlType.getKey(), sqlType.getValue());
                    sqlRegistry.registerSql(sqlType.getKey(), sqlType.getValue());
                }
            } catch (Exception e) {
                throw new IllegalArgumentException(filename + "을 가져올 수 없습니다.");
            }
        }

        public void setUnmarshaller(Unmarshaller unmarshaller) {
            this.unmarshaller = unmarshaller;
        }

        public void setSqlmapResource(Resource sqlmapResource) {
            this.sqlmapResource = sqlmapResource;
        }
    }
}
