// Test to check JUnit is set up and working
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JUnitTest {
    @Test
    public void testAddition() {
        assertEquals(2 + 2, 4);
        System.out.println("testJUnitWorking: Passed");
    }
}
