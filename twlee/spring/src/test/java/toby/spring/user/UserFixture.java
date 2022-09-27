package toby.spring.user;

import toby.spring.user.domain.Level;
import toby.spring.user.domain.User;

public class UserFixture {
    public static final User USER1 = new User("gildong1", "홍길동1", "1001", Level.BASIC, 1, 0);
    public static final User USER2 = new User("gildong2", "홍길동2", "1002", Level.SILVER, 55, 10);
    public static final User USER3 = new User("gildong3", "홍길동3", "1003", Level.GOLD, 100, 40);
    public static final User USER4 = new User("gildong4", "홍길동4", "1004", Level.BASIC, 49, 0);
    public static final User USER5 = new User("gildong5", "홍길동5", "1005", Level.SILVER, 50, 0);
    public static final User USER6 = new User("gildong6", "홍길동6", "1006", Level.SILVER, 60, 29);
    public static final User USER7 = new User("gildong7", "홍길동7", "1007", Level.SILVER, 61, 30);
}
