package toby.spring.user.sqlservice;

import org.springframework.stereotype.Component;
import toby.spring.user.dao.UserDao;
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
public class XmlSqlService implements SqlService {
    private final Map<String, String> sqlMap;
    private final String sqlmapFile;

    public XmlSqlService() {
        sqlMap = new HashMap<>();
        sqlmapFile = "/sqlmap.xml";
    }

    @PostConstruct
    private void init() {
        System.out.println("load sqlmapFile = " + sqlmapFile);
        String contextPath = Sqlmap.class.getPackage().getName();
        try {
            JAXBContext context = JAXBContext.newInstance(contextPath);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            InputStream is = UserDao.class.getResourceAsStream(sqlmapFile);
            Sqlmap sqlmap = (Sqlmap) unmarshaller.unmarshal(is);
            for (SqlType sqlType : sqlmap.getSql()) {
                System.out.printf("key = %s, value = %s\n", sqlType.getKey(), sqlType.getValue());
                sqlMap.put(sqlType.getKey(), sqlType.getValue());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getSql(String key) throws SqlRetrievalFailureException {
        String sql = sqlMap.get(key);
        System.out.println("call sql = " + sql);
        if (Objects.isNull(sql)) {
            throw new SqlRetrievalFailureException(key + "에 대한 SQL을 찾을 수 없습니다");
        }
        return sql;
    }
}
