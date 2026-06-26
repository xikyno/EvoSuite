package demo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.openbdfc.measure.service.BaseTestCase;

public class TestSmallTarget extends BaseTestCase {

    @InjectMocks
    private SmallTarget smallTarget;

    @Test
    public void testSign() throws Exception {
        int result = smallTarget.sign(1);

        Assertions.assertEquals(1, result);
    }
}
