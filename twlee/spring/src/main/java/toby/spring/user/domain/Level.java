package toby.spring.user.domain;

public enum Level {
    BASIC(1), SILVER(2), GOLD(3);

    private final int level;

    Level(int level) {
        this.level = level;
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
}
