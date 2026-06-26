package com.example;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

public class TestCalculator {

    @InjectMocks
    private Calculator calculator;

    private AutoCloseable closeable;

    @BeforeEach
    public void beforeAll() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void afterAll() throws Exception {
        closeable.close();
    }

    @Test
    public void testAdd0() throws Exception {
        int result = target.add("TODO: int arg0", "TODO: int arg1");

        Assertions.assertEquals("TODO: expected value", result);
    }

    @Test
    public void testDivide0() throws Exception {
        Assertions.assertThrows(java.lang.IllegalArgumentException.class, () -> {
            target.divide("TODO: int arg0", "TODO: int arg1");
        });
    }

    @Test
    public void testGetName0() throws Exception {
        String result = target.getName();

        Assertions.assertEquals("TODO: expected value", result);
    }

    @Test
    public void testGreet0() throws Exception {
        String result = target.greet("TODO: String arg0");

        Assertions.assertEquals("TODO: expected value", result);
    }

    @Test
    public void testMultiply0() throws Exception {
        int result = multiply("TODO: int arg0", "TODO: int arg1");

        Assertions.assertEquals("TODO: expected value", result);
    }
}
