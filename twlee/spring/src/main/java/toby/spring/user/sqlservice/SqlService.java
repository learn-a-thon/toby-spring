package toby.spring.user.sqlservice;

import toby.spring.user.exception.SqlRetrievalFailureException;

public interface SqlService {
    String getSql(String key) throws SqlRetrievalFailureException;
}
