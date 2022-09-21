package toby.spring.learningtest.template;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CalcSumTest {

    private Calculator calculator;
    private String filePath;

    @BeforeEach
    public void setUp() {
        calculator = new Calculator();
        filePath = "./src/test/resources/numbers.txt";
    }

    @Test
    void sumOfNumbers() {
        int result = calculator.fileReadTemplate(filePath,
                br -> {
                    int sum = 0;
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        sum += Integer.parseInt(line);
                    }
                    return sum;
                });
        assertEquals(result, 10);
    }

    @Test
    void multiplyOfNumbers() {
        int result = calculator.fileReadTemplate(filePath,
                br -> {
                    int mul = 1;
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        mul *= Integer.parseInt(line);
                    }
                    return mul;
                });
        assertEquals(result, 24);
    }

    @Test
    void sumOfNumbersLineCallback() {
        int result = calculator.lineReadTemplate(filePath,
                (line, initVal) -> initVal + Integer.parseInt(line),
                0);
        assertEquals(result, 10);
    }

    @Test
    void multiplyOfNumbersLineCallback() {
        int result = calculator.lineReadTemplate(filePath,
                (line, initVal) -> initVal * Integer.parseInt(line),
                1);
        assertEquals(result, 24);
    }

    @Test
    void concatenate() {
        String result = calculator.lineReadTemplate(filePath,
                (line, initVal) -> initVal + line,
                "");
        assertEquals(result, "1234");
    }

    private class Calculator {
        public Calculator() {
        }

        public <T> T lineReadTemplate(String filePath, LineCallback<T> callback, T initVal) {
            BufferedReader br = null;
            T result = initVal;
            try {
                br = new BufferedReader(new FileReader(filePath));
                String line = null;
                while ((line = br.readLine()) != null) {
                    result = callback.doSomethingWithLine(line, result);
                }
                return result;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (br != null) {
                        br.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return result;
        }

        public int fileReadTemplate(String filePath, BufferedReaderCallback callback) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(filePath));
                return callback.doSomethingWithReader(br);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (br != null) {
                        br.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return 0;
        }
    }
}
