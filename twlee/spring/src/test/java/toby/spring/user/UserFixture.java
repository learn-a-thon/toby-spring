package toby.spring.user;

import toby.spring.user.domain.Level;
import toby.spring.user.domain.User;

import static toby.spring.user.service.UserServiceImpl.MIN_LOGCOUNT_FOR_SILVER;
import static toby.spring.user.service.UserServiceImpl.MIN_RECOMMEND_FOR_GOLD;

public class UserFixture {
    public static final User USER1 = new User("gildong1", "홍길동1", "1001", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER - 1, 0, "test1@email.co.kr");
    public static final User USER2 = new User("gildong2", "홍길동2", "1002", Level.SILVER, MIN_LOGCOUNT_FOR_SILVER, 10, "test2@email.co.kr");
    public static final User USER3 = new User("gildong3", "홍길동3", "1003", Level.GOLD, 100, Integer.MAX_VALUE, "test3@email.co.kr");
    public static final User USER4 = new User("gildong4", "홍길동4", "1004", Level.BASIC, 49, 0, "test4@email.co.kr");
    public static final User USER5 = new User("gildong5", "홍길동5", "1005", Level.SILVER, 50, 0, "test5@email.co.kr");
    public static final User USER6 = new User("gildong6", "홍길동6", "1006", Level.SILVER, 60, MIN_RECOMMEND_FOR_GOLD, "test6@email.co.kr");
    public static final User USER7 = new User("gildong7", "홍길동7", "1007", Level.SILVER, 61, MIN_RECOMMEND_FOR_GOLD - 1, "test7@email.co.kr");
    public static final User USER8 = new User("gildong8", "홍길동8", "1008", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER, 10, "test8@email.co.kr");
    public static final User USER9 = new User("gildong9", "홍길동9", "1009", Level.SILVER, 100, MIN_RECOMMEND_FOR_GOLD + 1, "test9@email.co.kr");

    public static final User NON_USER1 = new User("non1", "넌길동1", "2001", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER - 1, 0, "non1@email.co.kr");
    public static final User NON_USER2 = new User("non2", "넌길동2", "2002", Level.SILVER, 100, MIN_RECOMMEND_FOR_GOLD - 1, "non2@email.co.kr");
    public static final User UP_USER1 = new User("up1", "업길동1", "3001", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER + 1, 0, "up1@email.co.kr");
    public static final User UP_USER2 = new User("up2", "업길동2", "3002", Level.SILVER, 100, MIN_RECOMMEND_FOR_GOLD + 1, "up2@email.co.kr");
}
