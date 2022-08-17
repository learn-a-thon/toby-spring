package toby.spring.user.dao;

import java.sql.Connection;
import java.sql.SQLException;

public class CountingConnectionMaker implements ConnectionMaker {
    private int count = 0;
    private final ConnectionMaker realConnectionMaker;

    public CountingConnectionMaker(ConnectionMaker connectionMaker) {
        this.realConnectionMaker = connectionMaker;
    }

    public Connection makeConnection() throws SQLException, ClassNotFoundException {
        this.count++;
        return realConnectionMaker.makeConnection();
    }

    public int getCount() {
        return count;
    }
}
