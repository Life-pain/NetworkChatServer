import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestThreadServer {
    ThreadServer threadServer = new ThreadServer();
    @Test
    public void test_GetPort(){
        int result = 11111;
        Assertions.assertEquals(threadServer.getPort(), result);
    }
}
