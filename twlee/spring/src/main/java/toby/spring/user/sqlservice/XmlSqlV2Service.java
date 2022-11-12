package toby.spring.user.sqlservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import toby.spring.user.dao.UserDao;
import toby.spring.user.exception.SqlNotFoundException;
import toby.spring.user.exception.SqlRetrievalFailureException;
import toby.spring.user.sqlservice.jaxb.SqlType;
import toby.spring.user.sqlservice.jaxb.Sqlmap;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class XmlSqlV2Service implements SqlService, SqlReader, SqlRegistry {
    // 생성자 주입 방식을 사용하면 빈 생성시 순환 참조 오류가 발생
    @Qualifier("xmlSqlV2Service")
    @Autowired
    private SqlReader sqlReader;
    @Qualifier("xmlSqlV2Service")
    @Autowired
    private SqlRegistry sqlRegistry;
    private final Map<String, String> sqlMap;
    private final String sqlmapFile;

    public XmlSqlV2Service() {
        this.sqlMap = new HashMap<>();
        this.sqlmapFile = "/sqlmap.xml";
    }

    @PostConstruct
    private void init() {
        sqlReader.read(sqlRegistry);
    }

    @Override
    public String getSql(String key) throws SqlRetrievalFailureException {
        try {
            return findSql(key);
        } catch (SqlNotFoundException e) {
            throw new SqlRetrievalFailureException(e);
        }
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

    @Override
    public void registerSql(String key, String sql) {
        sqlMap.put(key, sql);
    }

    @Override
    public String findSql(String key) throws SqlNotFoundException {
        String sql = sqlMap.get(key);
        System.out.println("call sql = " + sql);
        if (Objects.isNull(sql)) {
            throw new SqlRetrievalFailureException(key + "에 대한 SQL을 찾을 수 없습니다");
        }
        return sql;
    }
}
