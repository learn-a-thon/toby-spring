package toby.spring.user.domain;

public enum Level {
    GOLD(3, null), SILVER(2, GOLD), BASIC(1, SILVER);

    private final int level;
    private final Level next;

    Level(int level, Level next) {
        this.level = level;
        this.next = next;
    }

    public static Level valueOf(int value) {
        switch (value) {
            case 1: return BASIC;
            case 2: return SILVER;
            case 3: return GOLD;
            default: throw new AssertionError("Unknown value: " + value);
        }
    }

    public int intValue() {
        return level;
    }

    public Level getNext() {
        return next;
    }
}
