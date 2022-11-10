package toby.spring.user.sqlservice;

import toby.spring.user.dao.UserDao;
import toby.spring.user.exception.SqlRetrievalFailureException;
import toby.spring.user.sqlservice.jaxb.SqlType;
import toby.spring.user.sqlservice.jaxb.Sqlmap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class XmlSqlService implements SqlService {
    private final Map<String, String> sqlMap = new HashMap<>();

    public XmlSqlService() {
        String contextPath = Sqlmap.class.getPackage().getName();
        try {
            JAXBContext context = JAXBContext.newInstance(contextPath);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            InputStream is = UserDao.class.getResourceAsStream("/sqlmap.xml");
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
        if (Objects.isNull(sql)) {
            throw new SqlRetrievalFailureException(key + "에 대한 SQL을 찾을 수 없습니다");
        }
        return sql;
    }
}
