package demo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class TestSmallTarget {

    @InjectMocks
    private SmallTarget smallTarget;

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
    public void testSign() throws Exception {
        int result = smallTarget.sign(1);

        Assertions.assertEquals(1, result);
    }
}
