package toby.spring.user.sqlservice;

public interface SqlReader {
    /*
     * SQL을 외부에서 가져와 SqlRegistry에 등록한다.
     * 다양한 예외가 발생할 수 있지만 대부분 복구 불가한 예외
     */
    void read(SqlRegistry sqlRegistry);
}
