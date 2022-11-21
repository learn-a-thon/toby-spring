package toby.spring.user.sqlservice;

import org.springframework.stereotype.Component;

@Component
public class DefaultSqlService extends BaseSqlService {

    public DefaultSqlService() {
        super(new JaxbXmlSqlReader(), new HashMapSqlRegistry());
    }
}
