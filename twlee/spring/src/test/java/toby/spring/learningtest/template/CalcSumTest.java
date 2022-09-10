package toby.spring.learningtest.template;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CalcSumTest {

    @Test
    void sumOfNumbers() {
        Calculator calculator = new Calculator();
        int sum = calculator.sum("./src/test/resources/numbers.txt");
        assertEquals(sum, 10);
    }

    private class Calculator {
        public Calculator() {
        }

        public int sum(String filePath) {
            BufferedReader br = null;
            int sum = 0;
            try {
                br = new BufferedReader(new FileReader(filePath));
                String line = null;
                while ((line = br.readLine()) != null) {
                    sum += Integer.parseInt(line);
                }
                return sum;
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
            return sum;
        }
    }
}
